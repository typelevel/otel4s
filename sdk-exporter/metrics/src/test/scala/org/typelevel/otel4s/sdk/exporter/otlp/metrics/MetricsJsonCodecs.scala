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
package exporter.otlp
package metrics

import cats.data.NonEmptyVector
import io.circe.Encoder
import io.circe.Json
import io.circe.syntax._
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData

// the instances mimic Protobuf encoding
private object MetricsJsonCodecs extends JsonCodecs {

  implicit val aggregationTemporalityJsonEncoder
      : Encoder[AggregationTemporality] =
    Encoder.instance {
      case AggregationTemporality.Delta      => Json.fromInt(1)
      case AggregationTemporality.Cumulative => Json.fromInt(2)
    }

  implicit val exemplarDataJsonEncoder: Encoder[ExemplarData] =
    Encoder.instance { exemplar =>
      val value = exemplar match {
        case exemplar: ExemplarData.LongExemplar =>
          Json.obj("asInt" := exemplar.value.toString)

        case exemplar: ExemplarData.DoubleExemplar =>
          Json.obj("asDouble" := exemplar.value)
      }

      Json
        .obj(
          "timeUnixNano" := exemplar.timestamp.toNanos.toString,
          "traceId" := exemplar.traceContext.map(_.traceId.toHex),
          "spanId" := exemplar.traceContext.map(_.spanId.toHex),
          "filteredAttributes" := exemplar.filteredAttributes
        )
        .deepMerge(value)
        .dropNullValues
        .dropEmptyValues
    }

  implicit val numberPointDataJsonEncoder: Encoder[PointData.NumberPoint] =
    Encoder.instance { point =>
      val value = point match {
        case number: PointData.LongNumber =>
          Json.obj("asInt" := number.value.toString)

        case number: PointData.DoubleNumber =>
          Json.obj("asDouble" := number.value)
      }

      Json
        .obj(
          "attributes" := point.attributes,
          "startTimeUnixNano" := point.timeWindow.start.toNanos.toString,
          "timeUnixNano" := point.timeWindow.end.toNanos.toString,
          "exemplars" := (point.exemplars: Vector[ExemplarData])
        )
        .deepMerge(value)
        .dropNullValues
        .dropEmptyValues
    }

  implicit val histogramPointDataJsonEncoder: Encoder[PointData.Histogram] =
    Encoder.instance { histogram =>
      Json
        .obj(
          "attributes" := histogram.attributes,
          "startTimeUnixNano" := histogram.timeWindow.start.toNanos.toString,
          "timeUnixNano" := histogram.timeWindow.end.toNanos.toString,
          "count" := histogram.stats.map(_.count.toString),
          "sum" := histogram.stats.map(_.sum),
          "min" := histogram.stats.map(_.min),
          "max" := histogram.stats.map(_.max),
          "bucketCounts" := histogram.counts.map(_.toString),
          "explicitBounds" := histogram.boundaries.boundaries,
          "exemplars" := (histogram.exemplars: Vector[ExemplarData])
        )
        .dropNullValues
        .dropEmptyValues
    }

  implicit val sumMetricPointsJsonEncoder: Encoder[MetricPoints.Sum] =
    Encoder.instance { sum =>
      val monotonic =
        if (sum.monotonic) Json.True else Json.Null

      Json
        .obj(
          "dataPoints" := (sum.points: NonEmptyVector[PointData.NumberPoint]),
          "aggregationTemporality" := sum.aggregationTemporality,
          "isMonotonic" := monotonic
        )
        .dropEmptyValues
        .dropNullValues
    }

  implicit val gaugeMetricPointsJsonEncoder: Encoder[MetricPoints.Gauge] =
    Encoder.instance { gauge =>
      Json
        .obj(
          "dataPoints" := (gauge.points: NonEmptyVector[PointData.NumberPoint])
        )
        .dropEmptyValues
    }

  implicit val histogramMetricPointsJsonEncoder
      : Encoder[MetricPoints.Histogram] =
    Encoder.instance { histogram =>
      Json
        .obj(
          "dataPoints" := histogram.points,
          "aggregationTemporality" := histogram.aggregationTemporality
        )
        .dropEmptyValues
    }

  implicit val metricPointsJsonEncoder: Encoder[MetricPoints] =
    Encoder.instance {
      case sum: MetricPoints.Sum =>
        Json.obj("sum" := sum)

      case gauge: MetricPoints.Gauge =>
        Json.obj("gauge" := gauge)

      case histogram: MetricPoints.Histogram =>
        Json.obj("histogram" := histogram)
    }

  implicit val metricDataJsonEncoder: Encoder[MetricData] =
    Encoder.instance { metricData =>
      Json
        .obj(
          "name" := metricData.name,
          "description" := metricData.description,
          "unit" := metricData.unit
        )
        .dropNullValues
        .dropEmptyValues
        .deepMerge(metricData.data.asJson)
    }

}
