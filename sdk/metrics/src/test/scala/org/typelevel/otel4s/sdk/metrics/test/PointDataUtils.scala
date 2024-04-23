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

package org.typelevel.otel4s.sdk.metrics.test

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow

object PointDataUtils {

  def toNumberPoints[A: MeasurementValue](
      values: Vector[A],
      attributes: Attributes,
      timeWindow: TimeWindow
  ): Vector[PointData.NumberPoint] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        values.map { a =>
          PointData.longNumber(
            timeWindow,
            attributes,
            Vector.empty,
            cast(a)
          )
        }

      case MeasurementValue.DoubleMeasurementValue(cast) =>
        values.map { a =>
          PointData.doubleNumber(
            timeWindow,
            attributes,
            Vector.empty,
            cast(a)
          )
        }
    }

  def toHistogramPoint[A](
      values: Vector[A],
      attributes: Attributes,
      timeWindow: TimeWindow,
      boundaries: BucketBoundaries
  )(implicit N: Numeric[A]): PointData.Histogram = {
    import N.mkNumericOps

    val stats: Option[PointData.Histogram.Stats] =
      Option.when(values.nonEmpty)(
        PointData.Histogram.Stats(
          sum = values.sum.toDouble,
          min = values.min.toDouble,
          max = values.max.toDouble,
          count = values.size.toLong
        )
      )

    val counts: Vector[Long] =
      values.foldLeft(Vector.fill(boundaries.length + 1)(0L)) {
        case (acc, value) =>
          val i = boundaries.boundaries.indexWhere(b => value.toDouble <= b)
          val idx = if (i == -1) boundaries.length else i

          acc.updated(idx, acc(idx) + 1L)
      }

    PointData.histogram(
      timeWindow,
      attributes,
      Vector.empty,
      stats,
      boundaries,
      counts
    )
  }

}
