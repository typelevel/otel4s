package org.typelevel.otel4s.sdk.exporter.otlp.metrics

import com.google.protobuf.ByteString
import io.circe.Json
import io.opentelemetry.proto.collector.trace.v1.trace_service.ExportTraceServiceRequest
import io.opentelemetry.proto.collector.metrics.v1.metrics_service.ExportMetricsServiceRequest
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
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status
import scalapb.GeneratedMessage
import scalapb.descriptors.FieldDescriptor
import scalapb.descriptors.PByteString
import scalapb.descriptors.PValue
import scalapb_circe.Printer
import scodec.bits.ByteVector

private object ProtoEncoder {

}
