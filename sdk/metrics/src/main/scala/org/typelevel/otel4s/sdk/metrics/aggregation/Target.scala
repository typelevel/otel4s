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

package org.typelevel.otel4s.sdk.metrics.aggregation

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData.TraceContext
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow

import scala.concurrent.duration.FiniteDuration

/** A utility trait to select matching `ExemplarData` and `PointData`.
  *
  * Used by the `Sum` and `LastValue` aggregators.
  *
  * @tparam A
  *   the type of the values to record
  */
private sealed trait Target[A] { self =>
  type Exemplar <: ExemplarData
  type Point <: PointData.NumberPoint

  /** Creates a `Point` with the given values.
    */
  def makePointData(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[Exemplar],
      value: A
  ): Point

  /** Creates an `Exemplar` with the given values.
    */
  def makeExemplar(
      attributes: Attributes,
      timestamp: FiniteDuration,
      traceContext: Option[TraceContext],
      value: A
  ): Exemplar

}

private object Target {

  def apply[A: MeasurementValue]: Target[A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        new LongTarget[A](cast)
      case MeasurementValue.DoubleMeasurementValue(cast) =>
        new DoubleTarget[A](cast)
    }

  private[aggregation] final class LongTarget[A](
      cast: A => Long
  ) extends Target[A] {

    type Exemplar = ExemplarData.LongExemplar
    type Point = PointData.LongNumber

    def makePointData(
        timeWindow: TimeWindow,
        attributes: Attributes,
        exemplars: Vector[ExemplarData.LongExemplar],
        value: A
    ): PointData.LongNumber =
      PointData.longNumber(
        timeWindow,
        attributes,
        exemplars,
        cast(value)
      )

    def makeExemplar(
        attributes: Attributes,
        timestamp: FiniteDuration,
        traceContext: Option[TraceContext],
        value: A
    ): ExemplarData.LongExemplar =
      ExemplarData.long(attributes, timestamp, traceContext, cast(value))
  }

  private[aggregation] final class DoubleTarget[A](
      cast: A => Double
  ) extends Target[A] {

    type Exemplar = ExemplarData.DoubleExemplar
    type Point = PointData.DoubleNumber

    def makePointData(
        timeWindow: TimeWindow,
        attributes: Attributes,
        exemplars: Vector[ExemplarData.DoubleExemplar],
        value: A
    ): PointData.DoubleNumber =
      PointData.doubleNumber(
        timeWindow,
        attributes,
        exemplars,
        cast(value)
      )

    def makeExemplar(
        attributes: Attributes,
        timestamp: FiniteDuration,
        traceContext: Option[TraceContext],
        value: A
    ): ExemplarData.DoubleExemplar =
      ExemplarData.double(
        attributes,
        timestamp,
        traceContext,
        cast(value)
      )
  }

}
