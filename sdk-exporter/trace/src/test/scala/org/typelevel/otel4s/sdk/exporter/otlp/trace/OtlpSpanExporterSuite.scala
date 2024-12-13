/*
 * Copyright 2023 Typelevel
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
package trace

import cats.effect.IO
import com.comcast.ip4s.IpAddress
import io.circe.Encoder
import io.circe.Json
import munit._
import org.http4s.Headers
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers._
import org.http4s.syntax.literals._
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.StatusCode

import java.util.Locale
import scala.concurrent.duration._

class OtlpSpanExporterSuite extends CatsEffectSuite with ScalaCheckEffectSuite with SuiteRuntimePlatform {

  import OtlpSpanExporterSuite._

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
        s"OtlpSpanExporter{client=OtlpClient{protocol=$protocol, " +
          "endpoint=https://localhost:4318/api/v1/traces, " +
          "timeout=5 seconds, " +
          s"compression=$compression, " +
          "headers={X-Forwarded-For: 127.0.0.1}}}"

      OtlpSpanExporter
        .builder[IO]
        .addHeaders(
          Headers(`X-Forwarded-For`(IpAddress.fromString("127.0.0.1")))
        )
        .withEndpoint(uri"https://localhost:4318/api/v1/traces")
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
          "http://localhost:4318/v1/traces"

        case OtlpProtocol.Grpc =>
          "http://localhost:4317/opentelemetry.proto.collector.trace.v1.TraceService/Export"
      }

      val expected =
        s"OtlpSpanExporter{client=OtlpClient{protocol=$protocol, " +
          s"endpoint=$endpoint, " +
          "timeout=10 seconds, " +
          "compression=none, " +
          "headers={}}}"

      OtlpSpanExporter
        .builder[IO]
        .withProtocol(protocol)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("export spans") {
    PropF.forAllF { (sd: SpanData, protocol: OtlpProtocol, compression: PayloadCompression) =>
      IO.realTime.flatMap { now =>
        // we need to tweak end timestamps and attributes, so we recreate the span data
        val span = SpanData(
          name = sd.name,
          spanContext = sd.spanContext,
          parentSpanContext = sd.parentSpanContext,
          kind = sd.kind,
          startTimestamp = now,
          endTimestamp = Some(now.plus(5.seconds)),
          status = sd.status,
          attributes = sd.attributes.map(adaptAttributes),
          events = sd.events.map {
            _.map { event =>
              EventData(
                event.name,
                now.plus(2.seconds),
                event.attributes.map(adaptAttributes)
              )
            }
          },
          links = sd.links,
          instrumentationScope = sd.instrumentationScope,
          resource = TelemetryResource.default
        )

        val expected = {
          val references = {
            val childOf = span.parentSpanContext.map { parent =>
              JaegerRef(
                "CHILD_OF",
                span.spanContext.traceIdHex,
                parent.spanIdHex
              )
            }

            val links = span.links.elements.map { d =>
              JaegerRef(
                "FOLLOWS_FROM",
                d.spanContext.traceIdHex,
                d.spanContext.spanIdHex
              )
            }

            childOf.toList ::: links.toList
          }

          val duration =
            span.endTimestamp.getOrElse(Duration.Zero) - span.startTimestamp

          val tags = {
            val extra = List(
              List(
                Attribute(
                  "span.kind",
                  span.kind.toString.toLowerCase(Locale.ROOT)
                )
              ),
              Option.when(span.status.status != StatusCode.Unset)(
                Attribute(
                  "otel.status_code",
                  span.status.status.toString.toUpperCase(Locale.ROOT)
                )
              ),
              Option.when(span.status.status == StatusCode.Error)(
                Attribute("error", true)
              ),
              span.status.description.filter(_.nonEmpty).map { description =>
                Attribute("otel.status_description", description)
              },
              List(Attribute("internal.span.format", "otlp"))
            ).flatten

            span.attributes.elements.map(a => toJaegerTag(a)).toList ++
              extra.map(a => toJaegerTag(a))
          }

          val events =
            span.events.elements
              .map(d => JaegerLog(d.timestamp.toMicros))
              .toList

          val jaegerSpan = JaegerSpan(
            span.spanContext.traceIdHex,
            span.spanContext.spanIdHex,
            span.name,
            references,
            span.startTimestamp.toMicros,
            duration.toMicros,
            tags,
            events,
            "p1"
          )

          val jaegerTrace = JaegerTrace(
            span.spanContext.traceIdHex,
            List(jaegerSpan)
          )

          JaegerResponse(List(jaegerTrace))
        }

        OtlpSpanExporter
          .builder[IO]
          .withProtocol(protocol)
          .withCompression(compression)
          .withTimeout(20.seconds)
          .withRetryPolicy(
            RetryPolicy.builder
              .withInitialBackoff(2.second)
              .withMaxBackoff(20.seconds)
              .build
          )
          .build
          .use(exporter => exporter.exportSpans(List(span)))
          .flatMap { _ =>
            assertIO(findTrace(span.spanContext.traceIdHex), expected)
          }
      }
    }
  }

  // it's hard to deal with big numeric values due to various encoding pitfalls
  // so we simplify the numbers
  private def adaptAttributes(attributes: Attributes): Attributes = {
    val adapted = attributes.map { attribute =>
      val name = attribute.key.name
      attribute.key.`type` match {
        case AttributeType.Double    => Attribute(name, 1.1)
        case AttributeType.DoubleSeq => Attribute(name, Seq(1.1))
        case AttributeType.Long      => Attribute(name, 1L)
        case AttributeType.LongSeq   => Attribute(name, Seq(1L))
        case _                       => attribute
      }
    }

    adapted.to(Attributes)
  }

  private def toJaegerTag(a: Attribute[_]): JaegerTag = {
    import io.circe.syntax._

    def primitive[A: Encoder](tpe: String): JaegerTag =
      JaegerTag(a.key.name, tpe, a.value.asInstanceOf[A].asJson)

    def seq[A: Encoder]: JaegerTag = {
      val json = a.value.asInstanceOf[Seq[A]].map(_.asJson).asJson
      JaegerTag(a.key.name, "string", json.noSpaces.asJson)
    }

    a.key.`type` match {
      case AttributeType.Boolean    => primitive[Boolean]("bool")
      case AttributeType.String     => primitive[String]("string")
      case AttributeType.Double     => primitive[Double]("float64")
      case AttributeType.Long       => primitive[Long]("int64")
      case AttributeType.BooleanSeq => seq[Boolean]
      case AttributeType.StringSeq  => seq[String]
      case AttributeType.DoubleSeq  => seq[Double]
      case AttributeType.LongSeq    => seq[Long]
    }
  }

  private def findTrace(traceIdHex: String): IO[JaegerResponse] =
    EmberClientBuilder.default[IO].build.use { client =>
      import org.http4s.syntax.literals._
      import org.http4s.circe.CirceEntityCodec._
      import io.circe.generic.auto._

      val url = uri"http://localhost:16686" / "api" / "traces" / traceIdHex
      client.expect[JaegerResponse](url)
    }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

}

object OtlpSpanExporterSuite {
  case class JaegerRef(refType: String, traceID: String, spanID: String)
  case class JaegerTag(key: String, `type`: String, value: Json)
  case class JaegerLog(timestamp: Long)

  case class JaegerSpan(
      traceID: String,
      spanID: String,
      operationName: String,
      references: List[JaegerRef],
      startTime: Long,
      duration: Long,
      tags: List[JaegerTag],
      logs: List[JaegerLog],
      processID: String
  )

  case class JaegerTrace(traceID: String, spans: List[JaegerSpan])
  case class JaegerResponse(data: List[JaegerTrace])

}
