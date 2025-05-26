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

import cats.Applicative
import cats.data.NonEmptyVector
import cats.effect.Concurrent
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarReservoir
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.AsynchronousMeasurement
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.utils.Adder

private object SumAggregator {

  /** Creates a sum aggregator for synchronous instruments. Calculates the arithmetic sum of the measurement values.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#sum-aggregation]]
    *
    * @param reservoirs
    *   the allocator of exemplar reservoirs
    *
    * @param reservoirSize
    *   the maximum number of exemplars to preserve
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  def synchronous[F[_]: Concurrent, A: MeasurementValue: Numeric](
      reservoirs: Reservoirs[F],
      reservoirSize: Int
  ): Aggregator.Synchronous[F, A] =
    new Synchronous(reservoirs, reservoirSize)

  /** Creates a sum aggregator for asynchronous instruments.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#sum-aggregation]]
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  def asynchronous[
      F[_]: Applicative,
      A: MeasurementValue: Numeric
  ]: Aggregator.Asynchronous[F, A] =
    new Asynchronous[F, A]

  private final class Synchronous[
      F[_]: Concurrent,
      A: MeasurementValue: Numeric
  ](reservoirs: Reservoirs[F], reservoirSize: Int)
      extends Aggregator.Synchronous[F, A] {

    val target: Target[A] = Target[A]

    type Point = target.Point

    def createAccumulator: F[Aggregator.Accumulator[F, A, Point]] =
      for {
        adder <- Adder.create[F, A]
        reservoir <- reservoirs.fixedSize(reservoirSize)
      } yield new Accumulator(adder, reservoir)

    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: NonEmptyVector[Point],
        temporality: AggregationTemporality
    ): F[MetricData] =
      Concurrent[F].pure(
        MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          MetricPoints.sum(points, isMonotonic(descriptor), temporality)
        )
      )

    private class Accumulator(
        adder: Adder[F, A],
        reservoir: ExemplarReservoir[F, A]
    ) extends Aggregator.Accumulator[F, A, Point] {

      def aggregate(
          timeWindow: TimeWindow,
          attributes: Attributes,
          reset: Boolean
      ): F[Option[Point]] =
        for {
          value <- adder.sum(reset)
          exemplars <- reservoir.collectAndReset(attributes)
        } yield Some(
          target.makePointData(
            timeWindow,
            attributes,
            exemplars.map { e =>
              target.makeExemplar(
                e.filteredAttributes,
                e.timestamp,
                e.traceContext,
                e.value
              )
            },
            value
          )
        )

      def record(value: A, attributes: Attributes, context: Context): F[Unit] =
        reservoir.offer(value, attributes, context) >> adder.add(value)

    }

  }

  private final class Asynchronous[
      F[_]: Applicative,
      A: MeasurementValue: Numeric
  ] extends Aggregator.Asynchronous[F, A] {

    private val target: Target[A] = Target[A]

    def diff(
        previous: AsynchronousMeasurement[A],
        current: AsynchronousMeasurement[A]
    ): AsynchronousMeasurement[A] =
      current.copy(value = Numeric[A].minus(current.value, previous.value))

    def combine(
        previous: AsynchronousMeasurement[A],
        current: AsynchronousMeasurement[A]
    ): AsynchronousMeasurement[A] =
      current.copy(value = Numeric[A].plus(previous.value, current.value))

    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        measurements: NonEmptyVector[AsynchronousMeasurement[A]],
        temporality: AggregationTemporality
    ): F[MetricData] = {
      val points = measurements.map { m =>
        target.makePointData(m.timeWindow, m.attributes, Vector.empty, m.value)
      }

      Applicative[F].pure(
        MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          MetricPoints.sum(points, isMonotonic(descriptor), temporality)
        )
      )
    }

  }

  private def isMonotonic(descriptor: MetricDescriptor): Boolean =
    descriptor.sourceInstrument.instrumentType match {
      case InstrumentType.Counter           => true
      case InstrumentType.Histogram         => true
      case InstrumentType.ObservableCounter => true
      case _                                => false
    }

}
