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

package org.typelevel.otel4s.sdk.metrics.data

sealed abstract class Data(val tpe: MetricDataType) {
  def points: Vector[PointData]
}

object Data {

  final case class DoubleSum(
      points: Vector[PointData.DoublePoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.DoubleSum)

  final case class LongSum(
      points: Vector[PointData.LongPoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.LongSum)

  final case class DoubleGauge(
      points: Vector[PointData.DoublePoint]
  ) extends Data(MetricDataType.DoubleGauge)

  final case class LongGauge(
      points: Vector[PointData.LongPoint]
  ) extends Data(MetricDataType.LongGauge)

  final case class Summary(
      points: Vector[PointData.Summary]
  ) extends Data(MetricDataType.Summary)

  final case class Histogram(
      points: Vector[PointData.Histogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.Histogram)

  final case class ExponentialHistogram(
      points: Vector[PointData.ExponentialHistogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.ExponentialHistogram)

}
