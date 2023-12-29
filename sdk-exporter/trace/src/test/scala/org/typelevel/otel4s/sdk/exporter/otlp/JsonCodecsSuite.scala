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

package org.typelevel.otel4s
package sdk
package exporter.otlp

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.{Resource => InstrumentationResource}
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.Gens
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

import scala.concurrent.duration._

class JsonCodecsSuite extends ScalaCheckSuite {
  import JsonCodecs._

  private implicit val resourceArbitrary: Arbitrary[InstrumentationResource] =
    Arbitrary(Gens.resource)

  private implicit val attributeArbitrary: Arbitrary[Attribute[_]] =
    Arbitrary(Gens.attribute)

  private implicit val scopeArbitrary: Arbitrary[InstrumentationScope] =
    Arbitrary(Gens.instrumentationScope)

  private implicit val statusArbitrary: Arbitrary[Status] =
    Arbitrary(Gens.status)

  private implicit val statusDataArbitrary: Arbitrary[StatusData] =
    Arbitrary(Gens.statusData)

  private implicit val spanKindArbitrary: Arbitrary[SpanKind] =
    Arbitrary(Gens.spanKind)

  private implicit val eventDataArbitrary: Arbitrary[EventData] =
    Arbitrary(Gens.eventData)

  private implicit val linkDataArbitrary: Arbitrary[LinkData] =
    Arbitrary(Gens.linkData)

  private implicit val spanDataArbitrary: Arbitrary[SpanData] =
    Arbitrary(Gens.spanData)

  test("encode Resource") {
    Prop.forAll(Arbitrary.arbitrary[InstrumentationResource]) { resource =>
      val expected = Json.obj(
        "attributes" := Encoder[Attributes].apply(resource.attributes)
      )

      assertEquals(Encoder[InstrumentationResource].apply(resource), expected)
    }
  }

  test("encode Attribute[_]") {
    Prop.forAll(Arbitrary.arbitrary[Attribute[_]]) { attribute =>
      val value = attribute.value

      def list[A: Encoder](typeName: String): Json = {
        val list = value.asInstanceOf[List[A]]
        Json.obj("values" := list.map(value => Json.obj(typeName := value)))
      }

      val expected = {
        val v = attribute.key.`type` match {
          case AttributeType.Boolean =>
            Json.obj("boolValue" := value.asInstanceOf[Boolean])
          case AttributeType.Double =>
            Json.obj("doubleValue" := value.asInstanceOf[Double])
          case AttributeType.String =>
            Json.obj("stringValue" := value.asInstanceOf[String])
          case AttributeType.Long =>
            Json.obj("intValue" := value.asInstanceOf[Long])
          case AttributeType.BooleanList =>
            Json.obj("arrayValue" := list[Boolean]("boolValue"))
          case AttributeType.DoubleList =>
            Json.obj("arrayValue" := list[Double]("doubleValue"))
          case AttributeType.StringList =>
            Json.obj("arrayValue" := list[String]("stringValue"))
          case AttributeType.LongList =>
            Json.obj("arrayValue" := list[Long]("intValue"))
        }

        Json.obj("key" := attribute.key.name, "value" := v)
      }

      assertEquals(Encoder[Attribute[_]].apply(attribute), expected)
    }
  }

  test("encode InstrumentationScope") {
    Prop.forAll(Arbitrary.arbitrary[InstrumentationScope]) { scope =>
      val expected = Json
        .obj(
          "name" := scope.name,
          "version" := scope.version,
          "attributes" := scope.attributes
        )
        .dropNullValues

      assertEquals(Encoder[InstrumentationScope].apply(scope), expected)
    }
  }

  test("encode InstrumentationScope (noSpaces)") {
    val attrs = Attributes(Attribute("key", "value"))

    assertEquals(
      InstrumentationScope(
        "name",
        None,
        None,
        Attributes.empty
      ).asJson.noSpaces,
      """{"name":"name","attributes":[]}"""
    )

    assertEquals(
      InstrumentationScope(
        "name",
        Some("version"),
        Some("schema"),
        attrs
      ).asJson.noSpaces,
      """{"name":"name","version":"version","attributes":[{"key":"key","value":{"stringValue":"value"}}]}"""
    )
  }

  test("encode Status") {
    Prop.forAll(Arbitrary.arbitrary[Status]) { status =>
      val expected = status match {
        case Status.Unset => 0.asJson
        case Status.Ok    => 1.asJson
        case Status.Error => 2.asJson
      }

      assertEquals(Encoder[Status].apply(status), expected)
    }
  }

  test("encode StatusData") {
    Prop.forAll(Arbitrary.arbitrary[StatusData]) { statusData =>
      val expected = Json
        .obj(
          "message" := statusData.description,
          "code" := statusData.status
        )
        .dropNullValues

      assertEquals(Encoder[StatusData].apply(statusData), expected)
    }
  }

  test("encode StatusData (noSpaces)") {
    assertEquals(
      StatusData(Status.Error, "").asJson.noSpaces,
      """{"code":2}"""
    )

    assertEquals(
      StatusData(Status.Error, "description").asJson.noSpaces,
      """{"message":"description","code":2}"""
    )
  }

  test("encode SpanKind") {
    Prop.forAll(Arbitrary.arbitrary[SpanKind]) { spanKind =>
      val expected = spanKind match {
        case SpanKind.Internal => 1.asJson
        case SpanKind.Server   => 2.asJson
        case SpanKind.Client   => 3.asJson
        case SpanKind.Producer => 4.asJson
        case SpanKind.Consumer => 5.asJson
      }

      assertEquals(Encoder[SpanKind].apply(spanKind), expected)
    }
  }

  test("encode EventData") {
    Prop.forAll(Arbitrary.arbitrary[EventData]) { eventData =>
      val expected = Json
        .obj(
          "timeUnixNano" := eventData.timestamp.toNanos.toString,
          "name" := eventData.name,
          "attributes" := eventData.attributes,
          // "droppedAttributesCount" := 0 eventData.droppedAttributesCount
        )
        .dropNullValues

      assertEquals(Encoder[EventData].apply(eventData), expected)
    }
  }

  test("encode EventData (noSpaces)") {
    val attrs = Attributes(Attribute("key", "value"))

    assertEquals(
      EventData("name", 1.nanos, Attributes.empty).asJson.noSpaces,
      """{"timeUnixNano":"1","name":"name","attributes":[]}"""
    )

    assertEquals(
      EventData("name", 1.nanos, attrs).asJson.noSpaces,
      """{"timeUnixNano":"1","name":"name","attributes":[{"key":"key","value":{"stringValue":"value"}}]}"""
    )
  }

  test("encode LinkData") {
    Prop.forAll(Arbitrary.arbitrary[LinkData]) { link =>
      val expected = Json
        .obj(
          "traceId" := link.spanContext.traceIdHex,
          "spanId" := link.spanContext.spanIdHex,
          "traceState" := link.spanContext.traceState,
          "attributes" := link.attributes,
        )
        .dropNullValues

      assertEquals(Encoder[LinkData].apply(link), expected)
    }
  }

  test("encode LinkData (noSpaces)") {
    val attrs = Attributes(Attribute("key", "value"))

    val ctx = SpanContext(
      ByteVector.fromValidHex("aae6750d58ff8148fa33894599afaaf2"),
      ByteVector.fromValidHex("f676d76b0b3d4324"),
      TraceFlags.fromByte(1),
      TraceState.empty.updated("k", "v").updated("k2", "v2"),
      false
    )

    val ctx2 = SpanContext(
      ByteVector.fromValidHex("aae6750d58ff8148fa33894599afaaf2"),
      ByteVector.fromValidHex("f676d76b0b3d4324"),
      TraceFlags.Default,
      TraceState.empty,
      false
    )

    assertEquals(
      LinkData(ctx).asJson.noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324","traceState":"k2=v2,k=v","attributes":[]}"""
    )

    assertEquals(
      LinkData(ctx, attrs).asJson.noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324","traceState":"k2=v2,k=v","attributes":[{"key":"key","value":{"stringValue":"value"}}]}"""
    )

    assertEquals(
      LinkData(ctx2).asJson.noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324","attributes":[]}"""
    )
  }

  test("encode SpanData") {
    Prop.forAll(Arbitrary.arbitrary[SpanData]) { span =>
      val expected = Json
        .obj(
          "traceId" := span.spanContext.traceIdHex,
          "spanId" := span.spanContext.spanIdHex,
          "traceState" := span.spanContext.traceState,
          "parentSpanId" := span.parentSpanContext.map(_.spanIdHex),
          "name" := span.name,
          "kind" := span.kind,
          "startTimeUnixNano" := span.startTimestamp.toNanos.toString,
          "endTimeUnixNano" := span.endTimestamp.map(_.toNanos.toString),
          "attributes" := span.attributes,
          // "droppedAttributesCount" := 0, // span.droppedAttributesCount,
          "events" := span.events,
          // "droppedEventsCount" := 0, // span.droppedEventsCount,
          "links" := span.links,
          // "droppedLinksCount" := 0, // span.droppedLinksCount,
          "status" := span.status
        )
        .dropNullValues

      assertEquals(Encoder[SpanData].apply(span), expected)
    }
  }

  test("encode List[SpanData]") {
    Prop.forAll(Arbitrary.arbitrary[List[SpanData]]) { spans =>
      val resourceSpans =
        spans.groupBy(_.resource).map { case (resource, resourceSpans) =>
          val scopeSpans: Iterable[Json] =
            resourceSpans
              .groupBy(_.instrumentationScope)
              .map { case (scope, spans) =>
                Json.obj(
                  "scope" := scope,
                  "spans" := spans.map(Encoder[SpanData].apply)
                )
              }

          Json.obj(
            "resource" := resource,
            "scopeSpans" := scopeSpans
          )
        }

      val expected = Json.obj("resourceSpans" := resourceSpans)

      assertEquals(Encoder[List[SpanData]].apply(spans), expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)

}
