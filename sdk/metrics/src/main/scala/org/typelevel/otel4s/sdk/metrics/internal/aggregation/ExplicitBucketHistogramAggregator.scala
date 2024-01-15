package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.FlatMap
import cats.effect.{Concurrent, Ref}
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.{BucketBoundaries, ExemplarFilter}
import org.typelevel.otel4s.sdk.metrics.data.{
  AggregationTemporality,
  Data,
  ExemplarData,
  MetricData,
  PointData
}
import org.typelevel.otel4s.sdk.metrics.internal.{
  ExemplarReservoir,
  MetricDescriptor
}

import scala.concurrent.duration.FiniteDuration

private final class ExplicitBucketHistogramAggregator[F[_]: Concurrent](
    boundaries: BucketBoundaries,
    makeReservoir: F[ExemplarReservoir[F, ExemplarData.DoubleExemplar]]
) extends Aggregator[F] {
  import ExplicitBucketHistogramAggregator._

  type Point = PointData.Histogram

  def createHandle: F[Aggregator.Handle[F, PointData.Histogram]] =
    for {
      state <- Concurrent[F].ref(emptyState(boundaries.length))
      reservoir <- makeReservoir
    } yield new Handle(state, boundaries, reservoir)

  def toMetricData(
      resource: Resource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: Vector[PointData.Histogram],
      temporality: AggregationTemporality
  ): F[MetricData] =
    Concurrent[F].pure(
      MetricData(
        resource,
        scope,
        descriptor.name,
        descriptor.description,
        descriptor.sourceInstrument.unit,
        Data.Histogram(points, temporality)
      )
    )
}

private object ExplicitBucketHistogramAggregator {

  def apply[F[_]: Concurrent](
      boundaries: BucketBoundaries,
      filter: ExemplarFilter
  ): ExplicitBucketHistogramAggregator[F] = {
    val reservoir = ExemplarReservoir
      .histogramBucket[F](boundaries)
      .map(r => ExemplarReservoir.filtered(filter, r))

    new ExplicitBucketHistogramAggregator[F](boundaries, reservoir)
  }

  private final case class State(
      sum: Double,
      min: Double,
      max: Double,
      count: Long,
      counts: Vector[Long] // todo use array for memory efficiency?
  )

  private def emptyState(counts: Int): State =
    State(0, Double.MaxValue, -1, 0L, Vector.fill(counts)(0))

  private class Handle[F[_]: FlatMap, I](
      stateRef: Ref[F, State],
      boundaries: BucketBoundaries,
      reservoir: ExemplarReservoir[F, ExemplarData.DoubleExemplar]
  ) extends Aggregator.Handle[F, PointData.Histogram] {

    def aggregate(
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[PointData.Histogram]] =
      reservoir.collectAndReset(attributes).flatMap { exemplars =>
        stateRef.modify { state =>
          val histogram = PointData.Histogram(
            startTimestamp = startTimestamp,
            endTimestamp = collectTimestamp,
            attributes = attributes,
            exemplars = exemplars,
            sum = state.sum,
            hasMin = state.count > 0,
            min = state.min,
            hasMax = state.count > 0,
            max = state.max,
            boundaries = boundaries.boundaries,
            counts = state.counts
          )

          val next = if (reset) emptyState(boundaries.length) else state

          (next, Some(histogram))
        }
      }

    def recordDouble(
        value: Double,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      reservoir.offerDoubleMeasurement(value, attributes, context) >>
        stateRef.update { state =>
          val idx = boundaries.bucketIndex(value)
          state.copy(
            sum = state.sum + value,
            min = math.min(state.min, value),
            max = math.max(state.max, value),
            count = state.count + 1,
            counts = state.counts.updated(idx, state.counts(idx) + 1)
          )
        }

    def recordLong(
        value: Long,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      recordDouble(value.toDouble, attributes, context)
  }

}
