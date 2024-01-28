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

import org.typelevel.otel4s.Attributes

import scala.concurrent.duration.FiniteDuration

sealed trait PointData {
  def startTimestamp: FiniteDuration
  def endTimestamp: FiniteDuration
  def attributes: Attributes
  def exemplars: Vector[ExemplarData]
}

object PointData {

  final case class LongPoint(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.LongExemplar],
      value: Long
  ) extends PointData

  final case class DoublePoint(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      value: Double
  ) extends PointData

  final case class Summary(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      count: Long,
      sum: Double,
      percentileValues: Vector[ValueAtQuantile]
  ) extends PointData {
    def exemplars: Vector[ExemplarData] = Vector.empty
  }

  final case class Histogram(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      sum: Double,
      hasMin: Boolean,
      min: Double,
      hasMax: Boolean,
      max: Double,
      boundaries: Vector[Double],
      counts: Vector[Long]
  ) extends PointData {
    require(counts.length == boundaries.size + 1)
    // todo require(isStrictlyIncreasing())

    val count: Long = counts.sum
  }

  final case class ExponentialHistogram(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      sum: Double,
      zeroCount: Long,
      hasMin: Boolean,
      min: Double,
      hasMax: Boolean,
      max: Double,
      positiveBuckets: ExponentialHistogramBuckets,
      negativeBuckets: ExponentialHistogramBuckets
  ) extends PointData {
    val count: Long =
      zeroCount + positiveBuckets.totalCount + negativeBuckets.totalCount
  }

}
