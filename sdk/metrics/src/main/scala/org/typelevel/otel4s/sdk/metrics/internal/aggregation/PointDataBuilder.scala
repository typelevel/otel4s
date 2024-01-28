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

package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData

import scala.concurrent.duration.FiniteDuration

private trait PointDataBuilder[I, P <: PointData, E <: ExemplarData] {
  def create(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[E],
      value: I
  ): P
}

private object PointDataBuilder {
  import PointData._
  import ExemplarData._

  def longPoint: PointDataBuilder[Long, LongPoint, LongExemplar] =
    (startTimestamp, collectTimestamp, attributes, exemplars, value) =>
      PointData.LongPoint(
        startTimestamp,
        collectTimestamp,
        attributes,
        exemplars,
        value
      )

  def doublePoint: PointDataBuilder[Double, DoublePoint, DoubleExemplar] =
    (startTimestamp, collectTimestamp, attributes, exemplars, value) =>
      PointData.DoublePoint(
        startTimestamp,
        collectTimestamp,
        attributes,
        exemplars,
        value
      )

}
