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
package trace

import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.StatusCode
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

import scala.concurrent.duration._

class SpansProtoEncoderSuite extends ScalaCheckSuite {
  import SpansJsonCodecs._
  import SpansProtoEncoder._

  test("encode StatusData") {
    Prop.forAll(Arbitrary.arbitrary[StatusData]) { statusData =>
      val message =
        statusData.description.filter(_.trim.nonEmpty).fold(Json.Null)(_.asJson)

      val code = statusData.status match {
        case StatusCode.Unset => Json.Null
        case StatusCode.Ok    => 1.asJson
        case StatusCode.Error => 2.asJson
      }

      val expected = Json
        .obj(
          "message" := message,
          "code" := code
        )
        .dropNullValues

      assertEquals(ProtoEncoder.toJson(statusData), expected)
    }
  }

  test("encode StatusData (noSpaces)") {
    assertEquals(
      ProtoEncoder.toJson(StatusData(StatusCode.Unset)).noSpaces,
      """{}"""
    )

    assertEquals(
      ProtoEncoder.toJson(StatusData(StatusCode.Ok)).noSpaces,
      """{"code":1}"""
    )

    assertEquals(
      ProtoEncoder.toJson(StatusData(StatusCode.Error)).noSpaces,
      """{"code":2}"""
    )

    assertEquals(
      ProtoEncoder.toJson(StatusData(StatusCode.Error, "description")).noSpaces,
      """{"message":"description","code":2}"""
    )
  }

  test("encode EventData") {
    Prop.forAll(Arbitrary.arbitrary[EventData]) { eventData =>
      val expected = Json
        .obj(
          "timeUnixNano" := eventData.timestamp.toNanos.toString,
          "name" := eventData.name,
          "attributes" := eventData.attributes
        )
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(eventData), expected)
    }
  }

  test("encode EventData (noSpaces)") {
    val attrs = Attributes(Attribute("key", "value"))

    assertEquals(
      ProtoEncoder
        .toJson(EventData("name", 1.nanos, Attributes.empty))
        .noSpaces,
      """{"timeUnixNano":"1","name":"name"}"""
    )

    assertEquals(
      ProtoEncoder.toJson(EventData("name", 1.nanos, attrs)).noSpaces,
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
          "flags" := encodeFlags(link.spanContext.traceFlags)
        )
        .dropNullValues
        .dropEmptyValues

      assertEquals(ProtoEncoder.toJson(link), expected)
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
      ProtoEncoder.toJson(LinkData(ctx)).noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324","traceState":"k2=v2,k=v","flags":1}"""
    )

    assertEquals(
      ProtoEncoder.toJson(LinkData(ctx, attrs)).noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324","traceState":"k2=v2,k=v","attributes":[{"key":"key","value":{"stringValue":"value"}}],"flags":1}"""
    )

    assertEquals(
      ProtoEncoder.toJson(LinkData(ctx2)).noSpaces,
      """{"traceId":"aae6750d58ff8148fa33894599afaaf2","spanId":"f676d76b0b3d4324"}"""
    )
  }

  test("encode SpanData") {
    Prop.forAll(Arbitrary.arbitrary[SpanData]) { span =>
      val name =
        if (span.name.trim.nonEmpty) span.name.asJson else Json.Null

      val expected = Json
        .obj(
          "traceId" := span.spanContext.traceIdHex,
          "spanId" := span.spanContext.spanIdHex,
          "traceState" := span.spanContext.traceState,
          "parentSpanId" := span.parentSpanContext.map(_.spanIdHex),
          "flags" := encodeFlags(span.spanContext.traceFlags),
          "name" := name,
          "kind" := span.kind,
          "startTimeUnixNano" := span.startTimestamp.toNanos.toString,
          "endTimeUnixNano" := span.endTimestamp.map(_.toNanos.toString),
          "attributes" := span.attributes,
          "events" := span.events,
          "links" := span.links
        )
        .dropNullValues
        .dropEmptyValues
        .deepMerge(Json.obj("status" := span.status))

      assertEquals(ProtoEncoder.toJson(span), expected)
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
                Json
                  .obj(
                    "scope" := scope,
                    "spans" := spans.map(Encoder[SpanData].apply),
                    "schemaUrl" := scope.schemaUrl
                  )
                  .dropNullValues
              }

          Json
            .obj(
              "resource" := resource,
              "scopeSpans" := scopeSpans,
              "schemaUrl" := resource.schemaUrl
            )
            .dropNullValues
        }

      val expected = Json.obj("resourceSpans" := resourceSpans).dropEmptyValues

      assertEquals(
        ProtoEncoder.toJson(spans).noSpacesSortKeys,
        expected.noSpacesSortKeys
      )
    }
  }

  private def encodeFlags(traceFlags: TraceFlags): Json = {
    val int = traceFlags.toByte.toInt
    if (int == 0) Json.Null else int.asJson
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(5)
      .withMaxSize(5)

}
