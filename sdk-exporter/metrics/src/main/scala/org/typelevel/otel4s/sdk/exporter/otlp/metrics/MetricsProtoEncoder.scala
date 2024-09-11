/*
 * Copyright 2024 Typelevel
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

import com.google.protobuf.ByteString
import io.circe.Json
import io.opentelemetry.proto.collector.metrics.v1.metrics_service.ExportMetricsServiceRequest
import io.opentelemetry.proto.metrics.v1.{metrics => Proto}
import io.opentelemetry.proto.metrics.v1.metrics.ResourceMetrics
import io.opentelemetry.proto.metrics.v1.metrics.ScopeMetrics
import org.typelevel.otel4s.sdk.exporter.otlp.ProtoEncoder
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import scalapb.descriptors.FieldDescriptor
import scalapb.descriptors.PByteString
import scalapb.descriptors.PValue
import scalapb_circe.Printer
import scodec.bits.ByteVector

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.2.0/opentelemetry/proto/metrics/v1/metrics.proto]]
  */
private object MetricsProtoEncoder {
  implicit val jsonPrinter: Printer = new ProtoEncoder.JsonPrinter {
    private val EncodeAsHex = Set("trace_id", "span_id")

    /** The `traceId` and `spanId` byte arrays are represented as case-insensitive hex-encoded strings; they are not
      * base64-encoded as is defined in the standard Protobuf JSON Mapping. Hex encoding is used for traceId and spanId
      * fields in all OTLP Protobuf messages, e.g., the Span, Link, LogRecord, etc. messages.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.2.0/docs/specification.md#json-protobuf-encoding]]
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

  implicit val aggregationTemporalityEncoder: ProtoEncoder[
    AggregationTemporality,
    Proto.AggregationTemporality
  ] = {
    case AggregationTemporality.Delta =>
      Proto.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA
    case AggregationTemporality.Cumulative =>
      Proto.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE
  }

  implicit val exemplarEncoder: ProtoEncoder[
    ExemplarData,
    Proto.Exemplar
  ] = { exemplar =>
    val value = exemplar match {
      case e: ExemplarData.LongExemplar =>
        Proto.Exemplar.Value.AsInt(e.value)
      case e: ExemplarData.DoubleExemplar =>
        Proto.Exemplar.Value.AsDouble(e.value)
    }

    val traceId =
      exemplar.traceContext
        .map(v => ByteString.copyFrom(v.traceId.toArray))
        .getOrElse(ByteString.EMPTY)

    val spanId =
      exemplar.traceContext
        .map(v => ByteString.copyFrom(v.spanId.toArray))
        .getOrElse(ByteString.EMPTY)

    Proto.Exemplar(
      filteredAttributes = ProtoEncoder.encode(exemplar.filteredAttributes),
      timeUnixNano = exemplar.timestamp.toNanos,
      value = value,
      spanId = spanId,
      traceId = traceId
    )
  }

  implicit val numberPointEncoder: ProtoEncoder[
    PointData.NumberPoint,
    Proto.NumberDataPoint
  ] = { point =>
    val value = point match {
      case long: PointData.LongNumber =>
        Proto.NumberDataPoint.Value.AsInt(long.value)
      case double: PointData.DoubleNumber =>
        Proto.NumberDataPoint.Value.AsDouble(double.value)
    }

    Proto.NumberDataPoint(
      ProtoEncoder.encode(point.attributes),
      point.timeWindow.start.toNanos,
      point.timeWindow.end.toNanos,
      value = value,
      exemplars = point.exemplars.map(ProtoEncoder.encode(_)),
    )
  }

  implicit val histogramPointEncoder: ProtoEncoder[
    PointData.Histogram,
    Proto.HistogramDataPoint
  ] = { point =>
    Proto.HistogramDataPoint(
      attributes = ProtoEncoder.encode(point.attributes),
      startTimeUnixNano = point.timeWindow.start.toNanos,
      timeUnixNano = point.timeWindow.end.toNanos,
      count = point.stats.map(_.count).getOrElse(0L),
      sum = point.stats.map(_.sum),
      bucketCounts = point.counts,
      explicitBounds = point.boundaries.boundaries,
      exemplars = point.exemplars.map(ProtoEncoder.encode(_)),
      min = point.stats.map(_.min),
      max = point.stats.map(_.max)
    )
  }

  implicit val metricPointsEncoder: ProtoEncoder[
    MetricPoints,
    Proto.Metric.Data
  ] = {
    case sum: MetricPoints.Sum =>
      Proto.Metric.Data.Sum(
        Proto.Sum(
          sum.points.toVector.map(ProtoEncoder.encode(_)),
          ProtoEncoder.encode(sum.aggregationTemporality),
          sum.monotonic
        )
      )

    case gauge: MetricPoints.Gauge =>
      Proto.Metric.Data.Gauge(
        Proto.Gauge(gauge.points.toVector.map(ProtoEncoder.encode(_)))
      )

    case histogram: MetricPoints.Histogram =>
      Proto.Metric.Data.Histogram(
        Proto.Histogram(
          histogram.points.toVector.map(ProtoEncoder.encode(_)),
          ProtoEncoder.encode(histogram.aggregationTemporality)
        )
      )
  }

  implicit val metricDataEncoder: ProtoEncoder[
    MetricData,
    Proto.Metric
  ] = { metric =>
    Proto.Metric(
      metric.name,
      metric.description.getOrElse(""),
      unit = metric.unit.getOrElse(""),
      data = ProtoEncoder.encode(metric.data)
    )
  }

  implicit val exportMetricsRequest: ProtoEncoder[
    List[MetricData],
    ExportMetricsServiceRequest
  ] = { metrics =>
    val resourceMetrics =
      metrics
        .groupBy(_.resource)
        .map { case (resource, resourceMetrics) =>
          val scopeMetrics: List[ScopeMetrics] =
            resourceMetrics
              .groupBy(_.instrumentationScope)
              .map { case (scope, metrics) =>
                ScopeMetrics(
                  scope = Some(ProtoEncoder.encode(scope)),
                  metrics = metrics.map(metric => ProtoEncoder.encode(metric)),
                  schemaUrl = scope.schemaUrl.getOrElse("")
                )
              }
              .toList

          ResourceMetrics(
            Some(ProtoEncoder.encode(resource)),
            scopeMetrics,
            resource.schemaUrl.getOrElse("")
          )
        }
        .toList

    ExportMetricsServiceRequest(resourceMetrics)
  }

}
