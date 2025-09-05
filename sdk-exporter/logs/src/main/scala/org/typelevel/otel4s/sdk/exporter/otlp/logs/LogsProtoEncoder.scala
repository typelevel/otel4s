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

package org.typelevel.otel4s
package sdk
package exporter.otlp
package logs

import com.google.protobuf.ByteString
import io.circe.Json
import org.typelevel.otel4s.sdk.exporter.proto.common.{AnyValue => AnyValueProto}
import org.typelevel.otel4s.sdk.exporter.proto.common.ArrayValue
import org.typelevel.otel4s.sdk.exporter.proto.common.KeyValue
import org.typelevel.otel4s.sdk.exporter.proto.common.KeyValueList
import org.typelevel.otel4s.sdk.exporter.proto.logs.{LogRecord => LogProto}
import org.typelevel.otel4s.sdk.exporter.proto.logs.ResourceLogs
import org.typelevel.otel4s.sdk.exporter.proto.logs.ScopeLogs
import org.typelevel.otel4s.sdk.exporter.proto.logs.SeverityNumber
import org.typelevel.otel4s.sdk.exporter.proto.logs_service.ExportLogsServiceRequest
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import scalapb.descriptors.FieldDescriptor
import scalapb.descriptors.PByteString
import scalapb.descriptors.PValue
import scalapb_circe.Printer
import scodec.bits.ByteVector

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/opentelemetry/proto/logs/v1/logs.proto]]
  */
private object LogsProtoEncoder {

  implicit val jsonPrinter: Printer = new ProtoEncoder.JsonPrinter {
    private val EncodeAsHex = Set("trace_id", "span_id")

    /** The `traceId` and `spanId` byte arrays are represented as case-insensitive hex-encoded strings; they are not
      * base64-encoded as is defined in the standard Protobuf JSON Mapping. Hex encoding is used for traceId and spanId
      * fields in all OTLP Protobuf messages, e.g., the Span, Link, LogRecord, etc. messages.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/docs/specification.md#json-protobuf-encoding]]
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

  implicit val logRecordDataEncoder: ProtoEncoder[LogRecordData, LogProto] = { log =>
    val traceId =
      log.traceContext
        .map(v => ByteString.copyFrom(v.traceId.toArray))
        .getOrElse(ByteString.EMPTY)

    val spanId =
      log.traceContext
        .map(v => ByteString.copyFrom(v.spanId.toArray))
        .getOrElse(ByteString.EMPTY)

    def toAnyValueProto(value: AnyValue): AnyValueProto =
      value match {
        case string: AnyValue.StringValue =>
          AnyValueProto(AnyValueProto.Value.StringValue(string.value))

        case boolean: AnyValue.BooleanValue =>
          AnyValueProto(AnyValueProto.Value.BoolValue(boolean.value))

        case long: AnyValue.LongValue =>
          AnyValueProto(AnyValueProto.Value.IntValue(long.value))

        case double: AnyValue.DoubleValue =>
          AnyValueProto(AnyValueProto.Value.DoubleValue(double.value))

        case AnyValue.ByteArrayValueImpl(byteArray) =>
          AnyValueProto(AnyValueProto.Value.BytesValue(ByteString.copyFrom(byteArray)))

        case seq: AnyValue.SeqValue =>
          AnyValueProto(AnyValueProto.Value.ArrayValue(ArrayValue(seq.value.map(toAnyValueProto))))

        case map: AnyValue.MapValue =>
          AnyValueProto(AnyValueProto.Value.KvlistValue(KeyValueList(map.value.map { case (k, v) =>
            KeyValue(k, Some(toAnyValueProto(v)))
          }.toSeq)))

        case AnyValue.EmptyValueImpl =>
          AnyValueProto(AnyValueProto.Value.Empty)
      }

    LogProto(
      timeUnixNano = log.timestamp.map(_.toNanos).getOrElse(0L),
      observedTimeUnixNano = log.observedTimestamp.toNanos,
      severityNumber = SeverityNumber.fromValue(log.severity.map(_.value).getOrElse(0)),
      severityText = log.severityText.getOrElse(""),
      body = log.body.map(toAnyValueProto),
      attributes = ProtoEncoder.encode(log.attributes.elements),
      droppedAttributesCount = log.attributes.dropped,
      flags = 0,
      traceId = traceId,
      spanId = spanId,
    )
  }

  implicit val logRecordDataToRequest: ProtoEncoder[List[LogRecordData], ExportLogsServiceRequest] = { logs =>
    val resourceLogs =
      logs
        .groupBy(_.resource)
        .map { case (resource, resourceSpans) =>
          val scopeLogs: List[ScopeLogs] =
            resourceSpans
              .groupBy(_.instrumentationScope)
              .map { case (scope, logs) =>
                ScopeLogs(
                  scope = Some(ProtoEncoder.encode(scope)),
                  logRecords = logs.map(log => ProtoEncoder.encode(log)),
                  schemaUrl = scope.schemaUrl.getOrElse("")
                )
              }
              .toList

          ResourceLogs(
            Some(ProtoEncoder.encode(resource)),
            scopeLogs,
            resource.schemaUrl.getOrElse("")
          )
        }
        .toList

    ExportLogsServiceRequest(resourceLogs)
  }

}
