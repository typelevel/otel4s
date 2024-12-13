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

import cats.FlatMap
import cats.data.NonEmptyVector
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data._
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarReservoir
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor

/** The histogram aggregation that aggregates values into the corresponding buckets.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#explicit-bucket-histogram-aggregation]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record
  */
private final class ExplicitBucketHistogramAggregator[
    F[_]: Concurrent,
    A: MeasurementValue: Numeric
](
    reservoirs: Reservoirs[F],
    boundaries: BucketBoundaries
) extends Aggregator.Synchronous[F, A] {
  import ExplicitBucketHistogramAggregator._

  type Point = PointData.Histogram

  def createAccumulator: F[Aggregator.Accumulator[F, A, PointData.Histogram]] =
    for {
      state <- Concurrent[F].ref(emptyState(boundaries.length))
      reservoir <- reservoirs.histogramBucket(boundaries)
    } yield new Accumulator(state, boundaries, reservoir)

  def toMetricData(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: NonEmptyVector[PointData.Histogram],
      temporality: AggregationTemporality
  ): F[MetricData] =
    Concurrent[F].pure(
      MetricData(
        resource,
        scope,
        descriptor.name.toString,
        descriptor.description,
        descriptor.sourceInstrument.unit,
        MetricPoints.histogram(points, temporality)
      )
    )
}

private object ExplicitBucketHistogramAggregator {

  /** Creates a histogram aggregation with the given values.
    *
    * @param reservoirs
    *   the allocator of exemplar reservoirs
    *
    * @param boundaries
    *   the bucket boundaries to aggregate values at
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  def apply[F[_]: Concurrent, A: MeasurementValue: Numeric](
      reservoirs: Reservoirs[F],
      boundaries: BucketBoundaries
  ): Aggregator.Synchronous[F, A] =
    new ExplicitBucketHistogramAggregator[F, A](reservoirs, boundaries)

  private final case class State(
      sum: Double,
      min: Double,
      max: Double,
      count: Long,
      counts: Vector[Long]
  )

  private def emptyState(buckets: Int): State =
    State(0, Double.MaxValue, Double.MinValue, 0L, Vector.fill(buckets + 1)(0))

  private class Accumulator[F[_]: FlatMap, A: MeasurementValue](
      stateRef: Ref[F, State],
      boundaries: BucketBoundaries,
      reservoir: ExemplarReservoir[F, A]
  ) extends Aggregator.Accumulator[F, A, PointData.Histogram] {

    private val toDouble: A => Double =
      MeasurementValue[A] match {
        case MeasurementValue.LongMeasurementValue(cast) =>
          cast.andThen(_.toDouble)
        case MeasurementValue.DoubleMeasurementValue(cast) =>
          cast
      }

    def aggregate(
        timeWindow: TimeWindow,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[PointData.Histogram]] =
      reservoir.collectAndReset(attributes).flatMap { rawExemplars =>
        stateRef.modify { state =>
          val exemplars = rawExemplars.map { e =>
            ExemplarData.double(
              e.filteredAttributes,
              e.timestamp,
              e.traceContext,
              toDouble(e.value)
            )
          }

          val stats = Option.when(state.count > 0) {
            PointData.Histogram.Stats(
              state.sum,
              state.min,
              state.max,
              state.count
            )
          }

          val histogram = PointData.histogram(
            timeWindow = timeWindow,
            attributes = attributes,
            exemplars = exemplars,
            stats = stats,
            boundaries = boundaries,
            counts = state.counts
          )

          val next = if (reset) emptyState(boundaries.length) else state

          (next, Some(histogram))
        }
      }

    def record(value: A, attributes: Attributes, context: Context): F[Unit] = {
      val doubleValue = toDouble(value)

      reservoir.offer(value, attributes, context) >> stateRef.update { state =>
        val idx = bucketIndex(doubleValue)
        state.copy(
          sum = state.sum + doubleValue,
          min = math.min(state.min, doubleValue),
          max = math.max(state.max, doubleValue),
          count = state.count + 1,
          counts = state.counts.updated(idx, state.counts(idx) + 1)
        )
      }
    }

    private def bucketIndex(value: Double): Int = {
      val idx = boundaries.boundaries.indexWhere(b => value <= b)
      if (idx == -1) boundaries.length else idx
    }

  }

}
