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
import cats.effect.Concurrent
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.AsynchronousMeasurement
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.utils.Current

private object LastValueAggregator {

  /** Creates a last value aggregator for synchronous instruments. Retains the
    * last seen measurement.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#last-value-aggregation]]
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  def synchronous[
      F[_]: Concurrent,
      A: MeasurementValue
  ]: Aggregator.Synchronous[F, A] =
    new Synchronous[F, A]

  /** Creates a last value aggregator for asynchronous instruments.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#last-value-aggregation]]
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  def asynchronous[
      F[_]: Applicative,
      A: MeasurementValue
  ]: Aggregator.Asynchronous[F, A] =
    new Asynchronous[F, A]

  private final class Synchronous[
      F[_]: Concurrent,
      A: MeasurementValue
  ] extends Aggregator.Synchronous[F, A] {
    val target: Target[A] = Target[A]

    type Point = target.Point

    def createAccumulator: F[Aggregator.Accumulator[F, A, Point]] =
      for {
        current <- Current.create[F, A]
      } yield new Accumulator(current)

    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: Vector[Point],
        temporality: AggregationTemporality
    ): F[MetricData] =
      Concurrent[F].pure(
        MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          MetricPoints.gauge(points)
        )
      )

    private class Accumulator(
        current: Current[F, A]
    ) extends Aggregator.Accumulator[F, A, Point] {

      def aggregate(
          timeWindow: TimeWindow,
          attributes: Attributes,
          reset: Boolean
      ): F[Option[Point]] =
        current.get(reset).map { value =>
          value.map { v =>
            target.makePointData(
              timeWindow,
              attributes,
              Vector.empty,
              v
            )
          }
        }

      def record(value: A, attributes: Attributes, context: Context): F[Unit] =
        current.set(value)
    }
  }

  private final class Asynchronous[
      F[_]: Applicative,
      A: MeasurementValue
  ] extends Aggregator.Asynchronous[F, A] {

    private val target: Target[A] = Target[A]

    def diff(
        previous: AsynchronousMeasurement[A],
        current: AsynchronousMeasurement[A]
    ): AsynchronousMeasurement[A] =
      current

    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        measurements: Vector[AsynchronousMeasurement[A]],
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
          MetricPoints.gauge(points)
        )
      )
    }

  }

}
