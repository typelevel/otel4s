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

import com.google.protobuf.ByteString
import io.opentelemetry.proto.collector.trace.v1.trace_service.ExportTraceServiceRequest
import io.opentelemetry.proto.common.v1.common.{
  InstrumentationScope => ScopeProto
}
import io.opentelemetry.proto.common.v1.common.AnyValue
import io.opentelemetry.proto.common.v1.common.ArrayValue
import io.opentelemetry.proto.common.v1.common.KeyValue
import io.opentelemetry.proto.resource.v1.resource.{Resource => ResourceProto}
import io.opentelemetry.proto.trace.v1.trace.{Span => SpanProto}
import io.opentelemetry.proto.trace.v1.trace.{Status => StatusProto}
import io.opentelemetry.proto.trace.v1.trace.ResourceSpans
import io.opentelemetry.proto.trace.v1.trace.ScopeSpans
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

private object ProtoCodecs {
  trait ToProto[A, P] {
    def encode(a: A): P
  }

  def toProto[A, P](a: A)(implicit ev: ToProto[A, P]): P =
    ev.encode(a)

  implicit val attributeToProto: ToProto[Attribute[_], KeyValue] = { att =>
    import AnyValue.Value

    def primitive[A](lift: A => Value): Value =
      lift(att.value.asInstanceOf[A])

    def list[A](lift: A => Value): Value.ArrayValue = {
      val list = att.value.asInstanceOf[List[A]]
      Value.ArrayValue(ArrayValue(list.map(value => AnyValue(lift(value)))))
    }

    val value = att.key.`type` match {
      case AttributeType.Boolean     => primitive[Boolean](Value.BoolValue)
      case AttributeType.Double      => primitive[Double](Value.DoubleValue)
      case AttributeType.String      => primitive[String](Value.StringValue)
      case AttributeType.Long        => primitive[Long](Value.IntValue)
      case AttributeType.BooleanList => list[Boolean](Value.BoolValue)
      case AttributeType.DoubleList  => list[Double](Value.DoubleValue)
      case AttributeType.StringList  => list[String](Value.StringValue)
      case AttributeType.LongList    => list[Long](Value.IntValue)
    }

    KeyValue(att.key.name, Some(AnyValue(value)))
  }

  implicit val attributesToProto: ToProto[Attributes, Seq[KeyValue]] = { attr =>
    attr.toList.map(attribute => toProto[Attribute[_], KeyValue](attribute))
  }

  implicit val resourceToProto: ToProto[Resource, ResourceProto] = { resource =>
    ResourceProto(
      attributes = toProto(resource.attributes),
      droppedAttributesCount = 0 // todo: add droppedAttributes
    )
  }

  implicit val scopeInfoToProto: ToProto[InstrumentationScope, ScopeProto] = {
    scope =>
      ScopeProto(
        name = scope.name,
        version = scope.version.getOrElse(""),
        attributes = toProto(scope.attributes),
        droppedAttributesCount = 0 // todo: add droppedAttributes
      )
  }

  implicit val statusToProto: ToProto[Status, StatusProto.StatusCode] = {
    case Status.Unset => StatusProto.StatusCode.STATUS_CODE_UNSET
    case Status.Ok    => StatusProto.StatusCode.STATUS_CODE_OK
    case Status.Error => StatusProto.StatusCode.STATUS_CODE_ERROR
  }

  implicit val spanKindToProto: ToProto[SpanKind, SpanProto.SpanKind] = {
    case SpanKind.Internal => SpanProto.SpanKind.SPAN_KIND_INTERNAL
    case SpanKind.Server   => SpanProto.SpanKind.SPAN_KIND_SERVER
    case SpanKind.Client   => SpanProto.SpanKind.SPAN_KIND_CLIENT
    case SpanKind.Producer => SpanProto.SpanKind.SPAN_KIND_PRODUCER
    case SpanKind.Consumer => SpanProto.SpanKind.SPAN_KIND_CONSUMER
  }

  implicit val statusDataToProto: ToProto[StatusData, StatusProto] = { data =>
    StatusProto(
      message = data.description.getOrElse(""),
      code = toProto(data.status)
    )
  }

  implicit val eventDataToProto: ToProto[EventData, SpanProto.Event] = { data =>
    SpanProto.Event(
      timeUnixNano = data.timestamp.toNanos,
      name = data.name,
      attributes = toProto(data.attributes),
      droppedAttributesCount = 0 // data.droppedAttributesCount
    )
  }

  implicit val linkDataToProto: ToProto[LinkData, SpanProto.Link] = { data =>
    val traceState = data.spanContext.traceState.asMap
      .map { case (key, value) => s"$key=$value" }
      .mkString(",")

    SpanProto.Link(
      traceId = ByteString.copyFrom(data.spanContext.traceId.toArray),
      spanId = ByteString.copyFrom(data.spanContext.spanId.toArray),
      traceState = traceState,
      attributes = toProto(data.attributes),
      droppedAttributesCount = 0 // data.droppedAttributesCount
    )
  }

  implicit val spanDataToProto: ToProto[SpanData, SpanProto] = { span =>
    val traceState = span.spanContext.traceState.asMap
      .map { case (key, value) => s"$key=$value" }
      .mkString(",")

    SpanProto(
      ByteString.copyFrom(span.spanContext.traceId.toArray),
      ByteString.copyFrom(span.spanContext.spanId.toArray),
      traceState = traceState,
      parentSpanId = span.parentSpanContext
        .map(s => ByteString.copyFrom(s.spanId.toArray))
        .getOrElse(ByteString.EMPTY),
      name = span.name,
      kind = toProto(span.kind),
      startTimeUnixNano = span.startTimestamp.toNanos,
      endTimeUnixNano = span.endTimestamp.map(_.toNanos).getOrElse(0L),
      attributes = toProto(span.attributes),
      droppedAttributesCount = 0, // span.droppedAttributesCount,
      events = span.events.map(event => toProto(event)),
      droppedEventsCount = 0, // span.droppedEventsCount,
      links = span.links.map(link => toProto(link)),
      droppedLinksCount = 0, // span.droppedLinksCount,
      status = Some(toProto(span.status))
    )
  }

  implicit val spanDataToRequest
      : ToProto[List[SpanData], ExportTraceServiceRequest] = { spans =>
    val resourceSpans =
      spans
        .groupBy(_.resource)
        .map { case (resource, resourceSpans) =>
          val scopeSpans: List[ScopeSpans] =
            resourceSpans
              .groupBy(_.instrumentationScope)
              .map { case (scope, spans) =>
                ScopeSpans(
                  scope = Some(toProto(scope)),
                  spans = spans.map(span => toProto(span)),
                  schemaUrl = scope.schemaUrl.getOrElse("")
                )
              }
              .toList

          ResourceSpans(
            Some(toProto(resource)),
            scopeSpans,
            resource.schemaUrl.getOrElse("")
          )
        }
        .toList

    ExportTraceServiceRequest(resourceSpans)
  }

}
