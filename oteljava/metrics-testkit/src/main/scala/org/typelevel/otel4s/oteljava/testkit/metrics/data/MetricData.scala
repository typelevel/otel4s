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

package org.typelevel.otel4s.oteljava.testkit.metrics.data

import cats.Hash
import cats.Show
import cats.syntax.show._

sealed trait MetricData {
  def points: List[PointData[_]]

  override final def hashCode(): Int =
    Hash[MetricData].hash(this)

  override final def toString: String =
    Show[MetricData].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: MetricData =>
        Hash[MetricData].eqv(this, other)
      case _ =>
        false
    }

}

object MetricData {

  final case class LongGauge(points: List[PointData[Long]]) extends MetricData

  final case class DoubleGauge(points: List[PointData[Double]])
      extends MetricData

  final case class LongSum(points: List[PointData[Long]]) extends MetricData

  final case class DoubleSum(points: List[PointData[Double]]) extends MetricData

  final case class Summary(points: List[PointData[SummaryPointData]])
      extends MetricData

  final case class Histogram(points: List[PointData[HistogramPointData]])
      extends MetricData

  final case class ExponentialHistogram(
      points: List[PointData[HistogramPointData]]
  ) extends MetricData

  implicit val metricDataHash: Hash[MetricData] = Hash.by {
    case LongGauge(points) =>
      Hash[List[PointData[Long]]].hash(points)
    case DoubleGauge(points) =>
      Hash[List[PointData[Double]]].hash(points)
    case LongSum(points) =>
      Hash[List[PointData[Long]]].hash(points)
    case DoubleSum(points) =>
      Hash[List[PointData[Double]]].hash(points)
    case Summary(points) =>
      Hash[List[PointData[SummaryPointData]]].hash(points)
    case Histogram(points) =>
      Hash[List[PointData[HistogramPointData]]].hash(points)
    case ExponentialHistogram(points) =>
      Hash[List[PointData[HistogramPointData]]].hash(points)
  }

  implicit val metricDataShow: Show[MetricData] = Show.show {
    case LongGauge(points)            => show"LongGauge($points)"
    case DoubleGauge(points)          => show"DoubleGauge($points)"
    case LongSum(points)              => show"LongSum($points)"
    case DoubleSum(points)            => show"DoubleSum($points)"
    case Summary(points)              => show"Summary($points)"
    case Histogram(points)            => show"Histogram($points)"
    case ExponentialHistogram(points) => show"ExponentialHistogram($points)"
  }

}
