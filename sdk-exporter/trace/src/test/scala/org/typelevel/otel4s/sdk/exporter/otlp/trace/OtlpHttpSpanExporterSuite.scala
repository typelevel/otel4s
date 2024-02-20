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
import org.typelevel.otel4s.trace.Status

import java.util.Locale
import scala.concurrent.duration._

class OtlpHttpSpanExporterSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite
    with SuiteRuntimePlatform {

  import OtlpHttpSpanExporterSuite._

  private implicit val encodingArbitrary: Arbitrary[HttpPayloadEncoding] =
    Arbitrary(Gen.oneOf(HttpPayloadEncoding.Protobuf, HttpPayloadEncoding.Json))

  test("represent builder parameters in the name") {
    PropF.forAllF { (encoding: HttpPayloadEncoding) =>
      val enc = encoding match {
        case HttpPayloadEncoding.Json     => "Json"
        case HttpPayloadEncoding.Protobuf => "Protobuf"
      }

      val expected =
        s"OtlpHttpSpanExporter{client=OtlpHttpClient{encoding=$enc, " +
          "endpoint=https://localhost:4318/api/v1/traces, " +
          "timeout=5 seconds, " +
          "gzipCompression=true, " +
          "headers={X-Forwarded-For: 127.0.0.1}}}"

      OtlpHttpSpanExporter
        .builder[IO]
        .addHeaders(
          Headers(`X-Forwarded-For`(IpAddress.fromString("127.0.0.1")))
        )
        .withEndpoint(uri"https://localhost:4318/api/v1/traces")
        .withTimeout(5.seconds)
        .withGzip
        .withEncoding(encoding)
        .build
        .use { exporter =>
          IO(assertEquals(exporter.name, expected))
        }
    }
  }

  test("export spans") {
    PropF.forAllF { (sd: SpanData, encoding: HttpPayloadEncoding) =>
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
          attributes = adaptAttributes(sd.attributes),
          events = sd.events.map { event =>
            EventData(
              event.name,
              now.plus(2.seconds),
              adaptAttributes(event.attributes)
            )
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

            val links = span.links.map { d =>
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
              Option.when(span.status.status != Status.Unset)(
                Attribute(
                  "otel.status_code",
                  span.status.status.toString.toUpperCase(Locale.ROOT)
                )
              ),
              Option.when(span.status.status == Status.Error)(
                Attribute("error", true)
              ),
              span.status.description.filter(_.nonEmpty).map { description =>
                Attribute("otel.status_description", description)
              },
              List(Attribute("internal.span.format", "otlp"))
            ).flatten

            span.attributes.map(a => toJaegerTag(a)).toList ++
              extra.map(a => toJaegerTag(a))
          }

          val events =
            span.events.map(d => JaegerLog(d.timestamp.toMicros)).toList

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

        OtlpHttpSpanExporter
          .builder[IO]
          .withEncoding(encoding)
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
        case AttributeType.Double     => Attribute(name, 1.1)
        case AttributeType.DoubleList => Attribute(name, List(1.1))
        case AttributeType.Long       => Attribute(name, 1L)
        case AttributeType.LongList   => Attribute(name, List(1L))
        case _                        => attribute
      }
    }

    adapted.to(Attributes)
  }

  private def toJaegerTag(a: Attribute[_]): JaegerTag = {
    import io.circe.syntax._

    def primitive[A: Encoder](tpe: String): JaegerTag =
      JaegerTag(a.key.name, tpe, a.value.asInstanceOf[A].asJson)

    def list[A: Encoder]: JaegerTag = {
      val json = a.value.asInstanceOf[List[A]].map(_.asJson).asJson
      JaegerTag(a.key.name, "string", json.noSpaces.asJson)
    }

    a.key.`type` match {
      case AttributeType.Boolean     => primitive[Boolean]("bool")
      case AttributeType.String      => primitive[String]("string")
      case AttributeType.Double      => primitive[Double]("float64")
      case AttributeType.Long        => primitive[Long]("int64")
      case AttributeType.BooleanList => list[Boolean]
      case AttributeType.StringList  => list[String]
      case AttributeType.DoubleList  => list[Double]
      case AttributeType.LongList    => list[Long]
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

object OtlpHttpSpanExporterSuite {
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
