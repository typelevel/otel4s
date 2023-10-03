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
import org.typelevel.otel4s.sdk.{Resource => InstrumentationResource}
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

// Spec: https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/trace/v1/trace.proto
private[otlp] object JsonCodecs {

  implicit val attributeEncoder: Encoder[Attribute[_]] =
    Encoder.instance { attribute =>
      Json.obj(
        "key" := attribute.key.name,
        "value" := Json.obj(
          attributeTypeName(attribute.key.`type`) := attributeValue(
            attribute.key.`type`,
            attribute.value
          )
        )
      )
    }

  implicit val attributesEncoder: Encoder[Attributes] =
    Encoder[List[Attribute[_]]].contramap(_.toList)

  implicit val resourceEncoder: Encoder[InstrumentationResource] =
    Encoder.instance { resource =>
      Json.obj(
        "attributes" := resource.attributes
      )
    }

  implicit val scopeInfoEncoder: Encoder[InstrumentationScopeInfo] =
    Encoder.instance { scope =>
      Json.obj(
        "name" := scope.name,
        "version" := scope.version.getOrElse(""),
        // "schemaUrl" := scope.schemaUrl.getOrElse(""), todo: Jaeger does not accept the request when 'schemaUrl' is present
        // "attributes" := scope.attributes todo: Jaeger does not accept the request when 'attributes' is present
      )
    }

  implicit val statusEncoder: Encoder[Status] =
    Encoder[Int].contramap {
      case Status.Unset => 0
      case Status.Ok    => 1
      case Status.Error => 2
    }

  implicit val spanKindEncoder: Encoder[SpanKind] =
    Encoder[Int].contramap {
      case SpanKind.Internal => 1
      case SpanKind.Server   => 2
      case SpanKind.Client   => 3
      case SpanKind.Producer => 4
      case SpanKind.Consumer => 5
    }

  implicit val statusDataEncoder: Encoder[StatusData] =
    Encoder.instance { statusData =>
      Json.obj(
        "code" := statusData.status,
        "message" := statusData.description
      )
    }

  implicit val eventDataEncoder: Encoder[EventData] =
    Encoder.instance { eventData =>
      Json.obj(
        "name" := eventData.name,
        "timeUnixNano" := eventData.epochNanos,
        "attributes" := eventData.attributes,
        "droppedAttributesCount" := eventData.droppedAttributesCount
      )
    }

  implicit val linkDataEncoder: Encoder[LinkData] =
    Encoder.instance { link =>
      Json.obj(
        "traceId" := link.spanContext.traceIdHex,
        "spanId" := link.spanContext.spanIdHex,
        "traceState" := "", // todo: don't forget to export traceState
        "attributes" := link.attributes,
        "droppedAttributesCount" := link.droppedAttributesCount
      )
    }

  implicit val spanDataEncoder: Encoder[SpanData] =
    Encoder.instance { span =>
      Json.obj(
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
    }

  implicit val spanDataListEncoder: Encoder[List[SpanData]] =
    Encoder.instance { spans =>
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

      Json.obj("resourceSpans" := resourceSpans).deepDropNullValues
    }

  private def attributeValue(
      attributeType: AttributeType[_],
      value: Any
  ): Json = {
    def primitive[A: Encoder]: Json =
      Encoder[A].apply(value.asInstanceOf[A])

    def list[A: Encoder](attributeType: AttributeType[A]): Json = {
      val typeName = attributeTypeName(attributeType)
      val list = value.asInstanceOf[List[A]]
      Json.obj("values" := list.map(value => Json.obj(typeName := value)))
    }

    attributeType match {
      case AttributeType.Boolean     => primitive[Boolean]
      case AttributeType.Double      => primitive[Double]
      case AttributeType.String      => primitive[String]
      case AttributeType.Long        => primitive[Long]
      case AttributeType.BooleanList => list[Boolean](AttributeType.Boolean)
      case AttributeType.DoubleList  => list[Double](AttributeType.Double)
      case AttributeType.StringList  => list[String](AttributeType.String)
      case AttributeType.LongList    => list[Long](AttributeType.Long)
    }
  }

  private def attributeTypeName(attributeType: AttributeType[_]): String =
    attributeType match {
      case AttributeType.Boolean     => "boolValue"
      case AttributeType.Double      => "doubleValue"
      case AttributeType.String      => "stringValue"
      case AttributeType.Long        => "intValue"
      case AttributeType.BooleanList => "arrayValue"
      case AttributeType.DoubleList  => "arrayValue"
      case AttributeType.StringList  => "arrayValue"
      case AttributeType.LongList    => "arrayValue"
    }

}
