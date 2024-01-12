package org.typelevel.otel4s.sdk.metrics.internal

import java.util.concurrent.atomic.{AtomicReference, DoubleAdder, LongAdder}

import cats.Applicative
import cats.effect.std.AtomicCell
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.{AggregationTemporality, Data, ExemplarData, MetricData, PointData}

import scala.concurrent.duration.FiniteDuration

private[metrics] trait Aggregator[F[_]] {
  import Aggregator._
  type P <: PointData
  type U <: ExemplarData

  def createHandle: AggregatorHandle[F, P, U]

  def toMetricData(
      resource: Resource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: Vector[P],
      temporality: AggregationTemporality
  ): F[MetricData]

}

private[metrics] object Aggregator {

  type Aux[F[_]] = Aggregator[F] {
    type P = PointData
    type U = ExemplarData
  }

  trait AggregatorHandle[F[_], A <: PointData, U <: ExemplarData] {

    def aggregate(
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[A]]

    def recordLong(
        value: Long,
        attributes: Attributes,
        context: Context
    ): F[Unit]

    def recordDouble(
        value: Double,
        attributes: Attributes,
        context: Context
    ): F[Unit]
  }

  final class LongSum[F[_]: Applicative](
                                            reservoir: () => ExemplarReservoir[F, ExemplarData.DoubleExemplar]
                                          ) extends Aggregator[F] {
    type P = PointData.LongPoint
    type U = ExemplarData.LongExemplar

    def createHandle: AggregatorHandle[F, P, U] =
      new AggregatorHandle[F, P, U] {
        private val current = new LongAdder

        def aggregate(
                       startTimestamp: FiniteDuration,
                       collectTimestamp: FiniteDuration,
                       attributes: Attributes,
                       reset: Boolean
                     ): F[Option[PointData.LongPoint]] = {
          val value = if (reset) current.sumThenReset() else current.sum()

          Applicative[F].pure(
            Some(
              PointData.LongPoint(
                startTimestamp,
                collectTimestamp,
                attributes,
                Vector.empty, // todo
                value
              )
            )
          )
        }

        def recordLong(
                        value: Long,
                        attributes: Attributes,
                        context: Context
                      ): F[Unit] =
          Applicative[F].pure(current.add(value))

        def recordDouble(
                          value: Double,
                          attributes: Attributes,
                          context: Context
                        ): F[Unit] =
          ???
      }

    def toMetricData(
                      resource: Resource,
                      scope: InstrumentationScope,
                      descriptor: MetricDescriptor,
                      points: Vector[PointData.LongPoint],
                      temporality: AggregationTemporality
                    ): F[MetricData] =
      Applicative[F].pure(
        MetricData.longSum(
          resource,
          scope,
          descriptor.name,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          Data.LongSum(
            points,
            isMonotonic = true,
            temporality
          ) // todo: true -> isMonotonic
        )
      )
  }

  final class DoubleSum[F[_]: Applicative](
      reservoir: () => ExemplarReservoir[F, ExemplarData.DoubleExemplar]
  ) extends Aggregator[F] {
    type P = PointData.DoublePoint
    type U = ExemplarData.DoubleExemplar

    def createHandle: AggregatorHandle[F, P, U] =
      new AggregatorHandle[F, P, U] {
        private val current = new DoubleAdder

        def aggregate(
            startTimestamp: FiniteDuration,
            collectTimestamp: FiniteDuration,
            attributes: Attributes,
            reset: Boolean
        ): F[Option[PointData.DoublePoint]] = {
          val value = if (reset) current.sumThenReset() else current.sum()

          Applicative[F].pure(
            Some(
              PointData.DoublePoint(
                startTimestamp,
                collectTimestamp,
                attributes,
                Vector.empty, // todo
                value
              )
            )
          )
        }

        def recordLong(
            value: Long,
            attributes: Attributes,
            context: Context
        ): F[Unit] = ???

        def recordDouble(
            value: Double,
            attributes: Attributes,
            context: Context
        ): F[Unit] =
          Applicative[F].pure(current.add(value))
      }

    def toMetricData(
        resource: Resource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: Vector[PointData.DoublePoint],
        temporality: AggregationTemporality
    ): F[MetricData] =
      Applicative[F].pure(
        MetricData.doubleSum(
          resource,
          scope,
          descriptor.name,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          Data.DoubleSum(
            points,
            isMonotonic = true,
            temporality
          ) // todo: true -> isMonotonic
        )
      )
  }

  final class DoubleLastValue[F[_]: Applicative](
      reservoir: () => ExemplarReservoir[F, ExemplarData.DoubleExemplar]
  ) extends Aggregator[F] {
    type P = PointData.DoublePoint
    type U = ExemplarData.DoubleExemplar

    def createHandle: AggregatorHandle[F, P, U] =
      new AggregatorHandle[F, P, U] {
        private val current = new AtomicReference[Option[Double]](None)

        def aggregate(
            startTimestamp: FiniteDuration,
            collectTimestamp: FiniteDuration,
            attributes: Attributes,
            reset: Boolean
        ): F[Option[PointData.DoublePoint]] = {
          val value = if (reset) current.getAndSet(None) else current.get()

          Applicative[F].pure(
            value.map { v =>
              PointData.DoublePoint(
                startTimestamp,
                collectTimestamp,
                attributes,
                Vector.empty, // todo
                v
              )
            }
          )
        }

        def recordLong(
            value: Long,
            attributes: Attributes,
            context: Context
        ): F[Unit] = ???

        def recordDouble(
            value: Double,
            attributes: Attributes,
            context: Context
        ): F[Unit] = ???
      }

    def toMetricData(
        resource: Resource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: Vector[PointData.DoublePoint],
        temporality: AggregationTemporality
    ): F[MetricData] =
      Applicative[F].pure(
        MetricData.doubleGauge(
          resource,
          scope,
          descriptor.name,
          descriptor.description,
          descriptor.sourceInstrument.unit,
          Data.DoubleGauge(points)
        )
      )

  }

  final class Drop[F[_], P0 <: PointData, U0 <: ExemplarData]
      extends Aggregator[F] {

    type P = P0
    type U = U0

    def createHandle: AggregatorHandle[F, P, U] =
      ???


    def toMetricData(
        resource: Resource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: Vector[P],
        temporality: AggregationTemporality
    ): F[MetricData] = ???
  }

}
