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
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState

// the instances mimic Protobuf encoding
private object SpansJsonCodecs extends JsonCodecs {

  implicit val spanKindJsonEncoder: Encoder[SpanKind] =
    Encoder[Int].contramap {
      case SpanKind.Internal => 1
      case SpanKind.Server   => 2
      case SpanKind.Client   => 3
      case SpanKind.Producer => 4
      case SpanKind.Consumer => 5
    }

  implicit val traceStateJsonEncoder: Encoder[TraceState] =
    Encoder.instance { state =>
      if (state.isEmpty)
        Json.Null
      else
        state.asMap
          .map { case (key, value) => s"$key=$value" }
          .mkString(",")
          .asJson
    }

  implicit val statusDataJsonEncoder: Encoder[StatusData] =
    Encoder.instance { statusData =>
      val message =
        statusData.description.filter(_.trim.nonEmpty).fold(Json.Null)(_.asJson)

      val code = statusData.status match {
        case Status.Unset => Json.Null
        case Status.Ok    => 1.asJson
        case Status.Error => 2.asJson
      }

      Json
        .obj(
          "message" := message,
          "code" := code
        )
        .dropNullValues
    }

  implicit val eventDataJsonEncoder: Encoder[EventData] =
    Encoder.instance { eventData =>
      Json
        .obj(
          "timeUnixNano" := eventData.timestamp.toNanos.toString,
          "name" := eventData.name,
          "attributes" := eventData.attributes
        )
        .dropEmptyValues
    }

  implicit val linkDataJsonEncoder: Encoder[LinkData] =
    Encoder.instance { link =>
      Json
        .obj(
          "traceId" := link.spanContext.traceIdHex,
          "spanId" := link.spanContext.spanIdHex,
          "traceState" := link.spanContext.traceState,
          "attributes" := link.attributes,
          "flags" := encodeFlags(link.spanContext.traceFlags)
        )
        .dropNullValues
        .dropEmptyValues
    }

  implicit val spanDataJsonEncoder: Encoder[SpanData] =
    Encoder.instance { span =>
      Json
        .obj(
          "traceId" := span.spanContext.traceIdHex,
          "spanId" := span.spanContext.spanIdHex,
          "traceState" := span.spanContext.traceState,
          "parentSpanId" := span.parentSpanContext.map(_.spanIdHex),
          "flags" := encodeFlags(span.spanContext.traceFlags),
          "name" := span.name,
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
    }

  implicit val spanDataListJsonEncoder: Encoder[List[SpanData]] =
    Encoder.instance { spans =>
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

      Json.obj("resourceSpans" := resourceSpans).deepDropNullValues
    }

  private def encodeFlags(traceFlags: TraceFlags): Json = {
    val int = traceFlags.toByte.toInt
    if (int == 0) Json.Null else int.asJson
  }
}
