/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.testkit

sealed trait MetricData extends Product with Serializable

object MetricData {
  final case class QuantileData(quantile: Double, value: Double)

  final case class SummaryPoint(
      sum: Double,
      count: Long,
      values: List[QuantileData]
  )

  final case class HistogramPoint(
      sum: Double,
      count: Long,
      boundaries: List[Double],
      counts: List[Long]
  )

  final case class LongGauge(points: List[Long]) extends MetricData

  final case class DoubleGauge(points: List[Double]) extends MetricData

  final case class LongSum(points: List[Long]) extends MetricData

  final case class DoubleSum(points: List[Double]) extends MetricData

  final case class Summary(points: List[SummaryPoint]) extends MetricData

  final case class Histogram(point: List[HistogramPoint]) extends MetricData

  final case class ExponentialHistogram(points: List[HistogramPoint])
      extends MetricData
}
