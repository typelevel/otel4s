package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.Monad
import cats.effect.Concurrent
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.Data
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor

import scala.concurrent.duration.FiniteDuration

private final class LastValueAggregator[
    F[_]: Concurrent,
    Input,
    P <: PointData,
    E <: ExemplarData
](
    makeCurrent: F[Current[F, Input]],
    pointDataBuilder: PointDataBuilder[Input, P, E],
    createData: Vector[P] => Data
) extends Aggregator[F] {
  import LastValueAggregator.Handle

  type Point = P

  def createHandle: F[Aggregator.Handle[F, Point]] =
    for {
      current <- makeCurrent
    } yield new Handle[F, Input, Point, E](current, pointDataBuilder)

  def toMetricData(
      resource: Resource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: Vector[Point],
      temporality: AggregationTemporality
  ): F[MetricData] =
    Monad[F].pure(
      MetricData(
        resource,
        scope,
        descriptor.name,
        descriptor.description,
        descriptor.sourceInstrument.unit,
        createData(points)
      )
    )

}

private object LastValueAggregator {

  type OfLong[F[_]] =
    LastValueAggregator[F, Long, PointData.LongPoint, ExemplarData.LongExemplar]

  type OfDouble[F[_]] =
    LastValueAggregator[
      F,
      Double,
      PointData.DoublePoint,
      ExemplarData.DoubleExemplar
    ]

  def ofLong[F[_]: Concurrent]: OfLong[F] =
    new LastValueAggregator(
      Current.makeLong,
      PointDataBuilder.longPoint,
      Data.LongGauge
    )

  def ofDouble[F[_]: Concurrent]: OfDouble[F] =
    new LastValueAggregator(
      Current.makeDouble,
      PointDataBuilder.doublePoint,
      Data.DoubleGauge
    )

  private class Handle[F[_]: Monad, I, P <: PointData, E <: ExemplarData](
      current: Current[F, I],
      pointDataBuilder: PointDataBuilder[I, P, E]
  ) extends Aggregator.Handle[F, P] {

    def aggregate(
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[P]] =
      current.get(reset).map { value =>
        value.map { v =>
          pointDataBuilder.create(
            startTimestamp,
            collectTimestamp,
            attributes,
            Vector.empty,
            v
          )
        }
      }

    def record[A: MeasurementValue](
                                     value: A, attributes: Attributes, context: Context
                                   ): F[Unit] =
      MeasurementValue[A]

    def recordLong(value: Long, a: Attributes, c: Context): F[Unit] =
      current.setLong(value)

    def recordDouble(value: Double, a: Attributes, c: Context): F[Unit] =
      current.setDouble(value)
  }

}
