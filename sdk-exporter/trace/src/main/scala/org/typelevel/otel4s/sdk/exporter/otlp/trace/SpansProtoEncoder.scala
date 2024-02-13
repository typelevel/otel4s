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

import com.google.protobuf.ByteString
import io.circe.Json
import io.opentelemetry.proto.collector.trace.v1.trace_service.ExportTraceServiceRequest
import io.opentelemetry.proto.trace.v1.trace.{Span => SpanProto}
import io.opentelemetry.proto.trace.v1.trace.{Status => StatusProto}
import io.opentelemetry.proto.trace.v1.trace.ResourceSpans
import io.opentelemetry.proto.trace.v1.trace.ScopeSpans
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import scalapb.descriptors.FieldDescriptor
import scalapb.descriptors.PByteString
import scalapb.descriptors.PValue
import scalapb_circe.Printer
import scodec.bits.ByteVector

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/trace/v1/trace.proto]]
  */
private object SpansProtoEncoder {

  implicit val jsonPrinter: Printer = new ProtoEncoder.JsonPrinter {
    private val EncodeAsHex = Set("trace_id", "span_id", "parent_span_id")

    /** The `traceId` and `spanId` byte arrays are represented as
      * case-insensitive hex-encoded strings; they are not base64-encoded as is
      * defined in the standard Protobuf JSON Mapping. Hex encoding is used for
      * traceId and spanId fields in all OTLP Protobuf messages, e.g., the Span,
      * Link, LogRecord, etc. messages.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/docs/specification.md#json-protobuf-encoding]]
      */
    override def serializeSingleValue(
        fd: FieldDescriptor,
        value: PValue,
        formattingLongAsNumber: Boolean
    ): Json = {
      value match {
        case PByteString(bs) if EncodeAsHex.contains(fd.name) =>
          Json.fromString(ByteVector(bs.toByteArray()).toHex)
        case _ =>
          super.serializeSingleValue(fd, value, formattingLongAsNumber)
      }
    }
  }

  implicit val statusEncoder: ProtoEncoder[Status, StatusProto.StatusCode] = {
    case Status.Unset => StatusProto.StatusCode.STATUS_CODE_UNSET
    case Status.Ok    => StatusProto.StatusCode.STATUS_CODE_OK
    case Status.Error => StatusProto.StatusCode.STATUS_CODE_ERROR
  }

  implicit val spanKindEncoder: ProtoEncoder[SpanKind, SpanProto.SpanKind] = {
    case SpanKind.Internal => SpanProto.SpanKind.SPAN_KIND_INTERNAL
    case SpanKind.Server   => SpanProto.SpanKind.SPAN_KIND_SERVER
    case SpanKind.Client   => SpanProto.SpanKind.SPAN_KIND_CLIENT
    case SpanKind.Producer => SpanProto.SpanKind.SPAN_KIND_PRODUCER
    case SpanKind.Consumer => SpanProto.SpanKind.SPAN_KIND_CONSUMER
  }

  implicit val statusDataEncoder: ProtoEncoder[StatusData, StatusProto] = {
    data =>
      StatusProto(
        message = data.description.getOrElse(""),
        code = ProtoEncoder.encode(data.status)
      )
  }

  implicit val eventDataEncoder: ProtoEncoder[EventData, SpanProto.Event] = {
    data =>
      SpanProto.Event(
        timeUnixNano = data.timestamp.toNanos,
        name = data.name,
        attributes = ProtoEncoder.encode(data.attributes)
      )
  }

  implicit val linkDataEncoder: ProtoEncoder[LinkData, SpanProto.Link] = {
    data =>
      val traceState = data.spanContext.traceState.asMap
        .map { case (key, value) => s"$key=$value" }
        .mkString(",")

      SpanProto.Link(
        traceId = ByteString.copyFrom(data.spanContext.traceId.toArray),
        spanId = ByteString.copyFrom(data.spanContext.spanId.toArray),
        traceState = traceState,
        attributes = ProtoEncoder.encode(data.attributes),
        flags = data.spanContext.traceFlags.toByte.toInt
      )
  }

  implicit val spanDataEncoder: ProtoEncoder[SpanData, SpanProto] = { span =>
    val traceState = span.spanContext.traceState.asMap
      .map { case (key, value) => s"$key=$value" }
      .mkString(",")

    SpanProto(
      traceId = ByteString.copyFrom(span.spanContext.traceId.toArray),
      spanId = ByteString.copyFrom(span.spanContext.spanId.toArray),
      traceState = traceState,
      parentSpanId = span.parentSpanContext
        .map(s => ByteString.copyFrom(s.spanId.toArray))
        .getOrElse(ByteString.EMPTY),
      flags = span.spanContext.traceFlags.toByte.toInt,
      name = span.name,
      kind = ProtoEncoder.encode(span.kind),
      startTimeUnixNano = span.startTimestamp.toNanos,
      endTimeUnixNano = span.endTimestamp.map(_.toNanos).getOrElse(0L),
      attributes = ProtoEncoder.encode(span.attributes),
      events = span.events.map(event => ProtoEncoder.encode(event)),
      links = span.links.map(link => ProtoEncoder.encode(link)),
      status = Some(ProtoEncoder.encode(span.status))
    )
  }

  implicit val spanDataToRequest
      : ProtoEncoder[List[SpanData], ExportTraceServiceRequest] = { spans =>
    val resourceSpans =
      spans
        .groupBy(_.resource)
        .map { case (resource, resourceSpans) =>
          val scopeSpans: List[ScopeSpans] =
            resourceSpans
              .groupBy(_.instrumentationScope)
              .map { case (scope, spans) =>
                ScopeSpans(
                  scope = Some(ProtoEncoder.encode(scope)),
                  spans = spans.map(span => ProtoEncoder.encode(span)),
                  schemaUrl = scope.schemaUrl.getOrElse("")
                )
              }
              .toList

          ResourceSpans(
            Some(ProtoEncoder.encode(resource)),
            scopeSpans,
            resource.schemaUrl.getOrElse("")
          )
        }
        .toList

    ExportTraceServiceRequest(resourceSpans)
  }

}
