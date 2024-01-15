package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.effect.Concurrent
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.{
  Aggregation,
  BucketBoundaries,
  ExemplarFilter,
  MeasurementValue
}
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor

import scala.concurrent.duration.FiniteDuration

private[metrics] trait Aggregator[F[_]] {
  type Point <: PointData

  def createHandle: F[Aggregator.Handle[F, Point]]

  def toMetricData(
      resource: Resource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: Vector[Point],
      temporality: AggregationTemporality
  ): F[MetricData]
}

private[metrics] object Aggregator {

  type Aux[F[_], P <: PointData] = Aggregator[F] {
    type Point = P
  }

  trait Handle[F[_], P <: PointData] {
    def aggregate(
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[P]]

    def record[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): F[Unit]
  }

  def create[F[_]: Concurrent](
      aggregation: Aggregation.HasAggregator,
      descriptor: InstrumentDescriptor,
      filter: ExemplarFilter
  ): Aggregator[F] = {
    def sum: Aggregator[F] =
      descriptor.valueType match {
        case InstrumentValueType.Long   => SumAggregator.ofLong(1, filter)
        case InstrumentValueType.Double => SumAggregator.ofDouble(1, filter)
      }

    def lastValue: Aggregator[F] =
      descriptor.valueType match {
        case InstrumentValueType.Long   => LastValueAggregator.ofLong
        case InstrumentValueType.Double => LastValueAggregator.ofDouble
      }

    def histogram: Aggregator[F] = {
      val boundaries =
        descriptor.advice.explicitBoundaries.getOrElse(BucketBoundaries.default)
      ExplicitBucketHistogramAggregator(boundaries, filter)
    }

    aggregation match {
      case Aggregation.Default =>
        descriptor.instrumentType match {
          case InstrumentType.Counter                 => sum
          case InstrumentType.UpDownCounter           => sum
          case InstrumentType.ObservableCounter       => sum
          case InstrumentType.ObservableUpDownCounter => sum
          case InstrumentType.Histogram               => histogram
          case InstrumentType.ObservableGauge         => lastValue
        }

      case Aggregation.Sum       => sum
      case Aggregation.LastValue => lastValue

      case Aggregation.ExplicitBucketHistogram(boundaries) =>
        ExplicitBucketHistogramAggregator(boundaries, filter)

      case Aggregation.Base2ExponentialHistogram(maxBuckets, maxScale) =>
        ???
    }
  }

}
