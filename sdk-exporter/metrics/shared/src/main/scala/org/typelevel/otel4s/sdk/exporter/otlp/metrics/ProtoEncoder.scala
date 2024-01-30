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
package exporter.otlp.metrics

import io.circe.Json
import io.opentelemetry.proto.collector.metrics.v1.metrics_service.ExportMetricsServiceRequest
import io.opentelemetry.proto.common.v1.common.{
  InstrumentationScope => ScopeProto
}
import io.opentelemetry.proto.common.v1.common.AnyValue
import io.opentelemetry.proto.common.v1.common.ArrayValue
import io.opentelemetry.proto.common.v1.common.KeyValue
import io.opentelemetry.proto.resource.v1.resource.{Resource => ResourceProto}
import io.opentelemetry.proto.metrics.v1.metrics.{Metric => MetricProto}
import io.opentelemetry.proto.metrics.v1.metrics.ScopeMetrics
import io.opentelemetry.proto.metrics.v1.metrics.ResourceMetrics
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.data.{
  AggregationTemporality,
  Data,
  ExemplarData,
  MetricData
}
import scalapb.GeneratedMessage
import scalapb.descriptors.FieldDescriptor
import scalapb.descriptors.PByteString
import scalapb.descriptors.PValue
import scalapb_circe.Printer
import scodec.bits.ByteVector

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/common/v1/common.proto]]
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/trace/v1/trace.proto]]
  */
private object ProtoEncoder {
  trait ToProto[-A, P] {
    def encode(a: A): P
  }

  private val printer = new Printer(
    includingDefaultValueFields = false,
    formattingLongAsNumber = false,
    formattingEnumsAsNumber = true,
  ) {
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

  def toProto[A, P](a: A)(implicit ev: ToProto[A, P]): P =
    ev.encode(a)

  def toByteArray[A, P <: GeneratedMessage](a: A)(implicit
      ev: ToProto[A, P]
  ): Array[Byte] =
    ev.encode(a).toByteArray

  def toJson[A, P <: GeneratedMessage](a: A)(implicit ev: ToProto[A, P]): Json =
    printer.toJson(ev.encode(a))

  implicit val attributeToProto: ToProto[Attribute[_], KeyValue] = { att =>
    import AnyValue.Value

    def primitive[A](lift: A => Value): Value =
      lift(att.value.asInstanceOf[A])

    def list[A](lift: A => Value): Value.ArrayValue = {
      val list = att.value.asInstanceOf[List[A]]
      Value.ArrayValue(ArrayValue(list.map(value => AnyValue(lift(value)))))
    }

    val value = att.key.`type` match {
      case AttributeType.Boolean     => primitive[Boolean](Value.BoolValue(_))
      case AttributeType.Double      => primitive[Double](Value.DoubleValue(_))
      case AttributeType.String      => primitive[String](Value.StringValue(_))
      case AttributeType.Long        => primitive[Long](Value.IntValue(_))
      case AttributeType.BooleanList => list[Boolean](Value.BoolValue(_))
      case AttributeType.DoubleList  => list[Double](Value.DoubleValue(_))
      case AttributeType.StringList  => list[String](Value.StringValue(_))
      case AttributeType.LongList    => list[Long](Value.IntValue(_))
    }

    KeyValue(att.key.name, Some(AnyValue(value)))
  }

  implicit val attributesToProto: ToProto[Attributes, Seq[KeyValue]] = { attr =>
    attr.toList.map(attribute => toProto[Attribute[_], KeyValue](attribute))
  }

  implicit val resourceToProto: ToProto[TelemetryResource, ResourceProto] = {
    resource =>
      ResourceProto(
        attributes = toProto(resource.attributes)
      )
  }

  implicit val scopeInfoToProto: ToProto[InstrumentationScope, ScopeProto] = {
    scope =>
      ScopeProto(
        name = scope.name,
        version = scope.version.getOrElse(""),
        attributes = toProto(scope.attributes)
      )
  }

  def temp(
      aggregationTemporality: AggregationTemporality
  ): io.opentelemetry.proto.metrics.v1.metrics.AggregationTemporality =
    aggregationTemporality match {
      case AggregationTemporality.Delta =>
        io.opentelemetry.proto.metrics.v1.metrics.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA
      case AggregationTemporality.Cumulative =>
        io.opentelemetry.proto.metrics.v1.metrics.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE
    }

  def toExemplar(
      exemplarData: ExemplarData
  ): io.opentelemetry.proto.metrics.v1.metrics.Exemplar =
    io.opentelemetry.proto.metrics.v1.metrics.Exemplar(
      ProtoEncoder.toProto(exemplarData.filteredAttributes),
      exemplarData.timestamp.toNanos,
      exemplarData match {
        case ExemplarData.LongExemplar(_, _, value) =>
          io.opentelemetry.proto.metrics.v1.metrics.Exemplar.Value.AsInt(value)
        case ExemplarData.DoubleExemplar(_, _, value) =>
          io.opentelemetry.proto.metrics.v1.metrics.Exemplar.Value
            .AsDouble(value)
      }
    )

  implicit val dataToProto: ToProto[Data, MetricProto.Data] = {
    case Data.DoubleSum(points, isMonotonic, aggregationTemporality) =>
      MetricProto.Data.Sum(
        io.opentelemetry.proto.metrics.v1.metrics.Sum(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint(
              ProtoEncoder.toProto(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value =
                io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint.Value
                  .AsDouble(p.value),
              exemplars = p.exemplars.map(toExemplar),
            )
          ),
          temp(aggregationTemporality),
          isMonotonic
        )
      )

    case Data.LongSum(points, isMonotonic, aggregationTemporality) =>
      MetricProto.Data.Sum(
        io.opentelemetry.proto.metrics.v1.metrics.Sum(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint(
              ProtoEncoder.toProto(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value =
                io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint.Value
                  .AsInt(p.value),
              exemplars = p.exemplars.map(toExemplar),
            )
          ),
          temp(aggregationTemporality),
          isMonotonic
        )
      )

    case Data.DoubleGauge(points) =>
      MetricProto.Data.Gauge(
        io.opentelemetry.proto.metrics.v1.metrics.Gauge(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint(
              ProtoEncoder.toProto(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value =
                io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint.Value
                  .AsDouble(p.value),
              exemplars = p.exemplars.map(toExemplar),
            )
          )
        )
      )
    case Data.LongGauge(points) =>
      MetricProto.Data.Gauge(
        io.opentelemetry.proto.metrics.v1.metrics.Gauge(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint(
              ProtoEncoder.toProto(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value =
                io.opentelemetry.proto.metrics.v1.metrics.NumberDataPoint.Value
                  .AsInt(p.value),
              exemplars = p.exemplars.map(toExemplar),
            )
          )
        )
      )

    case Data.Summary(points) =>
      MetricProto.Data.Summary(
        io.opentelemetry.proto.metrics.v1.metrics.Summary(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.SummaryDataPoint(
              ProtoEncoder.toProto(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              p.count,
              p.sum,
              p.percentileValues.map { q =>
                io.opentelemetry.proto.metrics.v1.metrics.SummaryDataPoint
                  .ValueAtQuantile(q.quantile, q.value)
              }
            )
          )
        )
      )

    case Data.Histogram(points, aggregationTemporality) =>
      MetricProto.Data.Histogram(
        io.opentelemetry.proto.metrics.v1.metrics.Histogram(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics.HistogramDataPoint(
              attributes = ProtoEncoder.toProto(p.attributes),
              startTimeUnixNano = p.startTimestamp.toNanos,
              timeUnixNano = p.timestamp.toNanos,
              count = p.count,
              sum = p.sum,
              bucketCounts = p.counts,
              explicitBounds = p.boundaries,
              exemplars = p.exemplars.map(toExemplar),
              min = p.min,
              max = p.max
            )
          ),
          temp(aggregationTemporality)
        )
      )

    case Data.ExponentialHistogram(points, aggregationTemporality) =>
      MetricProto.Data.ExponentialHistogram(
        io.opentelemetry.proto.metrics.v1.metrics.ExponentialHistogram(
          points.map(p =>
            io.opentelemetry.proto.metrics.v1.metrics
              .ExponentialHistogramDataPoint(
                attributes = ProtoEncoder.toProto(p.attributes),
                startTimeUnixNano = p.startTimestamp.toNanos,
                timeUnixNano = p.timestamp.toNanos,
                count = p.count,
                sum = Some(p.sum),
                scale = 0, // todo scale
                zeroCount = p.zeroCount,
                positive = Some(
                  io.opentelemetry.proto.metrics.v1.metrics.ExponentialHistogramDataPoint
                    .Buckets(
                      p.positiveBuckets.offset,
                      p.positiveBuckets.bucketCounts
                    )
                ),
                negative = Some(
                  io.opentelemetry.proto.metrics.v1.metrics.ExponentialHistogramDataPoint
                    .Buckets(
                      p.negativeBuckets.offset,
                      p.negativeBuckets.bucketCounts
                    )
                ),
                exemplars = p.exemplars.map(toExemplar),
                min = Some(p.min),
                max = Some(p.max),
                // zeroThreshold = , // todo?
              )
          ),
          temp(aggregationTemporality)
        )
      )
  }

  implicit val metricDataToProto: ToProto[MetricData, MetricProto] = { metric =>
    MetricProto(
      metric.name,
      metric.description.getOrElse(""),
      unit = metric.unit.getOrElse(""),
      data = toProto(metric.data)
    )
  }

  implicit val metricDataToRequest
      : ToProto[List[MetricData], ExportMetricsServiceRequest] = { metrics =>
    val resourceSpans =
      metrics
        .groupBy(_.resource)
        .map { case (resource, resourceSpans) =>
          val scopeSpans: List[ScopeMetrics] =
            resourceSpans
              .groupBy(_.instrumentationScope)
              .map { case (scope, spans) =>
                ScopeMetrics(
                  scope = Some(toProto(scope)),
                  metrics = spans.map(metric => toProto(metric)),
                  schemaUrl = scope.schemaUrl.getOrElse("")
                )
              }
              .toList

          ResourceMetrics(
            Some(toProto(resource)),
            scopeSpans,
            resource.schemaUrl.getOrElse("")
          )
        }
        .toList

    ExportMetricsServiceRequest(resourceSpans)
  }

}
