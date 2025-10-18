/*
 * Copyright 2025 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.sdk
package exporter
package otlp
package logs

import cats.effect.IO
import com.comcast.ip4s._
import fs2.io.compression._
import munit._
import org.http4s.Headers
import org.http4s.Method
import org.http4s.Request
import org.http4s.Uri
import org.http4s.UrlForm
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers._
import org.http4s.syntax.literals._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.scalacheck.Arbitraries._

import java.util.Base64
import scala.concurrent.duration._

class OtlpLogRecordExporterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val protocolArbitrary: Arbitrary[OtlpProtocol] =
    Arbitrary(
      Gen.oneOf(
        OtlpProtocol.httpJson,
        OtlpProtocol.httpProtobuf,
        OtlpProtocol.grpc
      )
    )

  private implicit val compressionArbitrary: Arbitrary[PayloadCompression] =
    Arbitrary(
      Gen.oneOf(
        PayloadCompression.gzip,
        PayloadCompression.none
      )
    )

  test("represent builder parameters in the name") {
    PropF.forAllF { (protocol: OtlpProtocol, compression: PayloadCompression) =>
      val expected =
        s"OtlpLogRecordExporter{client=OtlpClient{protocol=$protocol, " +
          "endpoint=https://localhost:4318/api/v1/logs, " +
          "timeout=5 seconds, " +
          s"compression=$compression, " +
          "headers={X-Forwarded-For: 127.0.0.1}}}"

      OtlpLogRecordExporter
        .builder[IO]
        .addHeaders(
          Headers(`X-Forwarded-For`(IpAddress.fromString("127.0.0.1")))
        )
        .withEndpoint(uri"https://localhost:4318/api/v1/logs")
        .withTimeout(5.seconds)
        .withProtocol(protocol)
        .withCompression(compression)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("change endpoint according to the protocol") {
    PropF.forAllF { (protocol: OtlpProtocol) =>
      val endpoint = protocol match {
        case _: OtlpProtocol.Http =>
          "http://localhost:4318/v1/logs"

        case OtlpProtocol.Grpc =>
          "http://localhost:4317/opentelemetry.proto.collector.logs.v1.LogsService/Export"
      }

      val expected =
        s"OtlpLogRecordExporter{client=OtlpClient{protocol=$protocol, " +
          s"endpoint=$endpoint, " +
          "timeout=10 seconds, " +
          "compression=none, " +
          "headers={}}}"

      OtlpLogRecordExporter
        .builder[IO]
        .withProtocol(protocol)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("export logs") {
    PropF.forAllF { (lr: LogRecordData, protocol: OtlpProtocol, compression: PayloadCompression) =>
      def logRecord(now: FiniteDuration, serviceName: String) = LogRecordData(
        timestamp = None,
        observedTimestamp = now,
        traceContext = lr.traceContext,
        severity = lr.severity,
        severityText = lr.severityText,
        body = lr.body,
        eventName = lr.eventName,
        attributes = lr.attributes.map(_.map(adaptAttribute).to(Attributes)),
        instrumentationScope = InstrumentationScope(
          lr.instrumentationScope.name,
          lr.instrumentationScope.version,
          lr.instrumentationScope.schemaUrl,
          lr.instrumentationScope.attributes.map(adaptAttribute).to(Attributes),
        ),
        resource = TelemetryResource(
          lr.resource.attributes.map(adaptAttribute).to(Attributes) + Attribute("service.name", serviceName),
          lr.resource.schemaUrl
        )
      )

      def endpoint(collector: DockerUtils.CollectorPortMappings) = protocol match {
        case _: OtlpProtocol.Http =>
          Uri.unsafeFromString(
            s"http://localhost:${collector.otlpHttp}/v1/logs"
          )

        case OtlpProtocol.Grpc =>
          Uri.unsafeFromString(
            s"http://localhost:${collector.otlpGrpc}/opentelemetry.proto.collector.logs.v1.LogsService/Export"
          )
      }

      def exporter(collector: DockerUtils.CollectorPortMappings) =
        OtlpLogRecordExporter
          .builder[IO]
          .withProtocol(protocol)
          .withCompression(compression)
          .withEndpoint(endpoint(collector))
          .withTimeout(20.seconds)
          .withRetryPolicy(
            RetryPolicy.builder
              .withInitialBackoff(2.second)
              .withMaxBackoff(20.seconds)
              .build
          )
          .build

      for {
        collector <- DockerUtils.getCollectorPortMappings[IO]("otel4s-it--sdk-exporter-logs--otel-collector")
        lokiPort <- DockerUtils.getPortMapping[IO]("otel4s-it--sdk-exporter-logs--loki", port"3100")

        now <- IO.realTime
        serviceName <- IO.randomUUID.map(_.toString)
        record = logRecord(now, serviceName)
        _ <- exporter(collector).use(exporter => exporter.exportLogRecords(List(record)))
        logs <- findLogs(serviceName, lokiPort).delayBy(1.second)
      } yield {
        assertEquals(logs.size, 1)
        val StreamResult(attributes, values) = logs.head

        val expectedAttributes = {
          val common = List(
            "service_name" -> Some(serviceName),
            "scope_name" -> Some(record.instrumentationScope.name),
            "scope_version" -> record.instrumentationScope.version,
            "severity_number" -> record.severity.map(_.value.toString),
            "severity_text" -> record.severityText,
            "detected_level" -> record.severityText
              .orElse(
                record.severity.map(v => if (v.name.last.isDigit) v.name.dropRight(1) else v.name).map(_.toLowerCase)
              )
              .orElse(Some("unknown")),
            "dropped_attributes_count" -> Some(record.attributes.dropped.toString),
            "trace_id" -> record.traceContext.map(_.traceId.toHex),
            "span_id" -> record.traceContext.map(_.spanId.toHex),
          ).collect { case (key, Some(value)) => (key, value) }.toMap

          val attributes =
            (record.attributes.elements ++
              record.instrumentationScope.attributes ++
              record.resource.attributes.filter(_.key.name != "service.name"))
              .map(prettyAttribute)
              .toMap

          common ++ attributes
        }

        val expectedValues = List(
          record.observedTimestamp.toNanos.toString -> record.body.map(renderBody(_)).getOrElse("")
        )

        assertEquals(attributes, expectedAttributes)
        assertEquals(values, expectedValues)
      }
    }
  }

  private def adaptAttribute(attribute: Attribute[_]): Attribute[_] =
    attribute.key.`type` match {
      case AttributeType.Boolean    => attribute
      case AttributeType.Double     => Attribute(attribute.key.name, -100.1)
      case AttributeType.Long       => attribute
      case AttributeType.String     => attribute
      case AttributeType.BooleanSeq => attribute
      case AttributeType.DoubleSeq  => Attribute(attribute.key.name, Seq(-100.2, 100.3))
      case AttributeType.LongSeq    => attribute
      case AttributeType.StringSeq  => attribute
    }

  private def prettyAttribute(attribute: Attribute[_]): (String, String) = {
    val key = attribute.key.name

    def primitive: String = attribute.value.toString

    def seq[A](f: A => String): String =
      attribute.value.asInstanceOf[Seq[A]].map(f).mkString("[", ",", "]")

    val value = attribute.key.`type` match {
      case AttributeType.Boolean    => primitive
      case AttributeType.Double     => primitive
      case AttributeType.Long       => primitive
      case AttributeType.String     => primitive
      case AttributeType.BooleanSeq => seq[Boolean](_.toString)
      case AttributeType.DoubleSeq  => seq[Double](_.toString)
      case AttributeType.LongSeq    => seq[Long](_.toString)
      case AttributeType.StringSeq  => seq[String](s => "\"" + s + "\"")
    }

    (key, value)
  }

  private def renderBody(body: AnyValue, nested: Boolean = false): String = {
    body match {
      case boolean: AnyValue.BooleanValue => boolean.value.toString
      case long: AnyValue.LongValue       => long.value.toString
      case double: AnyValue.DoubleValue   => double.value.toString

      case string: AnyValue.StringValue =>
        if (nested) "\"" + string.value + "\"" else string.value

      case AnyValue.ByteArrayValueImpl(bytes) =>
        val base64 = Base64.getEncoder.encodeToString(bytes)
        if (nested) {
          if (base64.isEmpty) "null" else "\"" + base64 + "\""
        } else {
          base64
        }

      case seq: AnyValue.SeqValue =>
        seq.value.map(other => renderBody(other, nested = true)).mkString("[", ",", "]")

      case map: AnyValue.MapValue =>
        map.value.toList
          .sortBy(_._1)
          .map { case (key, value) =>
            s""""$key":${renderBody(value, nested = true)}"""
          }
          .mkString("{", ",", "}")

      case _: AnyValue.EmptyValue =>
        if (nested) "null" else ""
    }
  }

  private def findLogs(serviceName: String, port: Port): IO[List[StreamResult]] =
    EmberClientBuilder.default[IO].build.use { client =>
      import org.http4s.circe.CirceEntityCodec._
      import io.circe.generic.auto._

      val request = Request[IO](
        method = Method.POST,
        uri = Uri.unsafeFromString(s"http://localhost:$port") / "loki" / "api" / "v1" / "query_range",
      ).withEntity(UrlForm("query" -> s"""{service_name="$serviceName"}"""))

      def loop(attempts: Int): IO[List[StreamResult]] =
        client
          .expectOr[LokiQueryResponse](request) { response =>
            for {
              body <- response.as[String]
              _ <- IO.println(
                s"Cannot retrieve logs. Status ${response.status} body $body"
              )
            } yield new RuntimeException(
              s"Cannot retrieve logs. Status ${response.status} body $body"
            )
          }
          .handleErrorWith { error =>
            IO.println(s"Cannot retrieve logs due to ${error.getMessage}")
              .as(LokiQueryResponse("", QueryData("", Nil)))
          }
          .flatMap { response =>
            if (response.data.result.isEmpty && attempts > 0)
              loop(attempts - 1).delayBy(300.millis)
            else if (response.data.result.isEmpty)
              IO.println(s"Couldn't find logs for service_name=$serviceName after 10 attempts").as(response.data.result)
            else
              IO.pure(response.data.result)
          }

      loop(10)
    }

  case class LokiQueryResponse(status: String, data: QueryData)
  case class QueryData(resultType: String, result: List[StreamResult])
  case class StreamResult(stream: Map[String, String], values: List[(String, String)])

  override def munitIOTimeout: Duration =
    2.minute

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)
}
