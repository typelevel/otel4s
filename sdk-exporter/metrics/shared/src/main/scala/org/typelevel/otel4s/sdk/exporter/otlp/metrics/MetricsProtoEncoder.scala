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

import io.opentelemetry.proto.collector.metrics.v1.metrics_service.ExportMetricsServiceRequest
import io.opentelemetry.proto.metrics.v1.{metrics => Proto}
import io.opentelemetry.proto.metrics.v1.metrics.ResourceMetrics
import io.opentelemetry.proto.metrics.v1.metrics.ScopeMetrics
import org.typelevel.otel4s.sdk.exporter.otlp.ProtoEncoder
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.Data
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import scalapb_circe.Printer

/** @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.0.0/opentelemetry/proto/common/v1/common.proto]]
  */
private object MetricsProtoEncoder {
  implicit val jsonPrinter: Printer = new ProtoEncoder.JsonPrinter

  implicit val aggregationTemporalityEncoder: ProtoEncoder[
    AggregationTemporality,
    Proto.AggregationTemporality
  ] = {
    case AggregationTemporality.Delta =>
      Proto.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA
    case AggregationTemporality.Cumulative =>
      Proto.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE
  }

  implicit val exemplarEncoder: ProtoEncoder[ExemplarData, Proto.Exemplar] = {
    exemplar =>
      Proto.Exemplar(
        ProtoEncoder.encode(exemplar.filteredAttributes),
        exemplar.timestamp.toNanos,
        exemplar match {
          case ExemplarData.LongExemplar(_, _, value) =>
            Proto.Exemplar.Value.AsInt(value)
          case ExemplarData.DoubleExemplar(_, _, value) =>
            Proto.Exemplar.Value.AsDouble(value)
        }
      )
  }

  implicit val dataEncoder: ProtoEncoder[Data, Proto.Metric.Data] = {
    case Data.DoubleSum(points, isMonotonic, aggregationTemporality) =>
      Proto.Metric.Data.Sum(
        Proto.Sum(
          points.map(p =>
            Proto.NumberDataPoint(
              ProtoEncoder.encode(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value = Proto.NumberDataPoint.Value.AsDouble(p.value),
              exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
            )
          ),
          ProtoEncoder.encode(aggregationTemporality),
          isMonotonic
        )
      )

    case Data.LongSum(points, isMonotonic, aggregationTemporality) =>
      Proto.Metric.Data.Sum(
        Proto.Sum(
          points.map(p =>
            Proto.NumberDataPoint(
              ProtoEncoder.encode(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value = Proto.NumberDataPoint.Value.AsInt(p.value),
              exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
            )
          ),
          ProtoEncoder.encode(aggregationTemporality),
          isMonotonic
        )
      )

    case Data.DoubleGauge(points) =>
      Proto.Metric.Data.Gauge(
        Proto.Gauge(
          points.map(p =>
            Proto.NumberDataPoint(
              ProtoEncoder.encode(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value = Proto.NumberDataPoint.Value.AsDouble(p.value),
              exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
            )
          )
        )
      )
    case Data.LongGauge(points) =>
      Proto.Metric.Data.Gauge(
        Proto.Gauge(
          points.map(p =>
            Proto.NumberDataPoint(
              ProtoEncoder.encode(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              value = Proto.NumberDataPoint.Value.AsInt(p.value),
              exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
            )
          )
        )
      )

    case Data.Summary(points) =>
      Proto.Metric.Data.Summary(
        Proto.Summary(
          points.map(p =>
            Proto.SummaryDataPoint(
              ProtoEncoder.encode(p.attributes),
              p.startTimestamp.toNanos,
              p.timestamp.toNanos,
              p.count,
              p.sum,
              p.percentileValues.map { q =>
                Proto.SummaryDataPoint
                  .ValueAtQuantile(q.quantile, q.value)
              }
            )
          )
        )
      )

    case Data.Histogram(points, aggregationTemporality) =>
      Proto.Metric.Data.Histogram(
        Proto.Histogram(
          points.map(p =>
            Proto.HistogramDataPoint(
              attributes = ProtoEncoder.encode(p.attributes),
              startTimeUnixNano = p.startTimestamp.toNanos,
              timeUnixNano = p.timestamp.toNanos,
              count = p.count,
              sum = p.sum,
              bucketCounts = p.counts,
              explicitBounds = p.boundaries,
              exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
              min = p.min,
              max = p.max
            )
          ),
          ProtoEncoder.encode(aggregationTemporality)
        )
      )

    case Data.ExponentialHistogram(points, aggregationTemporality) =>
      Proto.Metric.Data.ExponentialHistogram(
        Proto.ExponentialHistogram(
          points.map(p =>
            Proto
              .ExponentialHistogramDataPoint(
                attributes = ProtoEncoder.encode(p.attributes),
                startTimeUnixNano = p.startTimestamp.toNanos,
                timeUnixNano = p.timestamp.toNanos,
                count = p.count,
                sum = Some(p.sum),
                scale = 0, // todo scale
                zeroCount = p.zeroCount,
                positive = Some(
                  Proto.ExponentialHistogramDataPoint.Buckets(
                    p.positiveBuckets.offset,
                    p.positiveBuckets.bucketCounts
                  )
                ),
                negative = Some(
                  Proto.ExponentialHistogramDataPoint.Buckets(
                    p.negativeBuckets.offset,
                    p.negativeBuckets.bucketCounts
                  )
                ),
                exemplars = p.exemplars.map(ProtoEncoder.encode(_)),
                min = Some(p.min),
                max = Some(p.max),
                // zeroThreshold = , // todo?
              )
          ),
          ProtoEncoder.encode(aggregationTemporality)
        )
      )
  }

  implicit val metricDataEncoder: ProtoEncoder[MetricData, Proto.Metric] = {
    metric =>
      Proto.Metric(
        metric.name,
        metric.description.getOrElse(""),
        unit = metric.unit.getOrElse(""),
        data = ProtoEncoder.encode(metric.data)
      )
  }

  implicit val exportMetricsRequest
      : ProtoEncoder[List[MetricData], ExportMetricsServiceRequest] = {
    metrics =>
      val resourceSpans =
        metrics
          .groupBy(_.resource)
          .map { case (resource, resourceSpans) =>
            val scopeSpans: List[ScopeMetrics] =
              resourceSpans
                .groupBy(_.instrumentationScope)
                .map { case (scope, spans) =>
                  ScopeMetrics(
                    scope = Some(ProtoEncoder.encode(scope)),
                    metrics = spans.map(metric => ProtoEncoder.encode(metric)),
                    schemaUrl = scope.schemaUrl.getOrElse("")
                  )
                }
                .toList

            ResourceMetrics(
              Some(ProtoEncoder.encode(resource)),
              scopeSpans,
              resource.schemaUrl.getOrElse("")
            )
          }
          .toList

      ExportMetricsServiceRequest(resourceSpans)
  }

}
