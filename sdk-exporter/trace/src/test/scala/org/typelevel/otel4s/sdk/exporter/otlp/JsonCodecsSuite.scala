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
import org.typelevel.otel4s.sdk.{Resource => InstrumentationResource}
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

class JsonCodecsSuite extends ScalaCheckSuite {
  import JsonCodecs._
  import arbitrary._

  property("encode Resource") {
    Prop.forAll(Arbitrary.arbitrary[InstrumentationResource]) { resource =>
      val expected = Json.obj(
        "attributes" := Encoder[Attributes].apply(resource.attributes)
      )

      assertEquals(Encoder[InstrumentationResource].apply(resource), expected)
    }
  }

  property("encode Attribute[_]") {
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

  property("encode InstrumentationScopeInfo") {
    Prop.forAll(Arbitrary.arbitrary[InstrumentationScopeInfo]) { info =>
      val expected = Json.obj(
        "name" := info.name,
        "version" := info.version.getOrElse(""),
        "schemaUrl" := info.schemaUrl.getOrElse(""),
        "attributes" := info.attributes
      )

      assertEquals(Encoder[InstrumentationScopeInfo].apply(info), expected)
    }
  }

  property("encode Status") {
    Prop.forAll(Arbitrary.arbitrary[Status]) { status =>
      val expected = status match {
        case Status.Unset => 0.asJson
        case Status.Ok    => 1.asJson
        case Status.Error => 2.asJson
      }

      assertEquals(Encoder[Status].apply(status), expected)
    }
  }

  property("encode StatusData") {
    Prop.forAll(Arbitrary.arbitrary[StatusData]) { statusData =>
      val expected = Json.obj(
        "code" := statusData.status,
        "message" := statusData.description
      )

      assertEquals(Encoder[StatusData].apply(statusData), expected)
    }
  }

  property("encode SpanKind") {
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

  property("encode EventData") {
    Prop.forAll(Arbitrary.arbitrary[EventData]) { eventData =>
      val expected = Json.obj(
        "name" := eventData.name,
        "timeUnixNano" := eventData.epochNanos,
        "attributes" := eventData.attributes,
        "droppedAttributesCount" := eventData.droppedAttributesCount
      )

      assertEquals(Encoder[EventData].apply(eventData), expected)
    }
  }

  property("encode LinkData") {
    Prop.forAll(Arbitrary.arbitrary[LinkData]) { link =>
      val expected = Json.obj(
        "traceId" := link.spanContext.traceIdHex,
        "spanId" := link.spanContext.spanIdHex,
        "traceState" := "", // todo: don't forget to export traceState
        "attributes" := link.attributes,
        "droppedAttributesCount" := link.droppedAttributesCount
      )

      assertEquals(Encoder[LinkData].apply(link), expected)
    }
  }

  property("encode SpanData") {
    Prop.forAll(Arbitrary.arbitrary[SpanData]) { span =>
      val expected = Json.obj(
        "traceId" := span.spanContext.traceIdHex,
        "spanId" := span.spanContext.spanIdHex,
        "traceState" := "", // todo: don't forget to export traceState
        "parentSpanId" := span.parentSpanContext.map(_.spanIdHex),
        "name" := span.name,
        "kind" := span.kind,
        "startTimeUnixNano" := span.startEpochNanos.toString,
        "endTimeUnixNano" := span.endEpochNanos.toString,
        "attributes" := span.attributes,
        "droppedAttributesCount" := span.droppedAttributesCount,
        "events" := span.events,
        "droppedEventsCount" := span.droppedEventsCount,
        "links" := span.links,
        "droppedLinksCount" := span.droppedLinksCount,
        "status" := span.status
      )

      assertEquals(Encoder[SpanData].apply(span), expected)
    }
  }

  /*property("encode List[SpanData]") { // todo: the compilation hangs for some reason
    Prop.forAll(Gen.listOfN(5, Arbitrary.arbitrary[SpanData])) { spans =>
      val resourceSpans =
        spans.groupBy(_.resource).map { case (resource, resourceSpans) =>
          val scopeSpans: Iterable[Json] =
            resourceSpans
              .groupBy(_.instrumentationScopeInfo)
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
  }*/

}
