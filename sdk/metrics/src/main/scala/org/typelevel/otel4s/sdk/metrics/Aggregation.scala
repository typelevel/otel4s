package org.typelevel.otel4s.sdk.metrics

import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

sealed abstract class Aggregation(
    supportedInstruments: Set[InstrumentType]
) {
  def compatibleWith(tpe: InstrumentType): Boolean =
    supportedInstruments.contains(tpe)
}

object Aggregation {

  def drop: Aggregation = Drop

  def default: Aggregation = Default

  def sum: Aggregation = Sum

  def lastValue: Aggregation = LastValue

  private[metrics] sealed trait HasAggregator

  private[metrics] case object Drop extends Aggregation(Compatability.Default)

  private[metrics] case object Default
      extends Aggregation(Compatability.Default)
      with HasAggregator

  private[metrics] case object Sum
      extends Aggregation(Compatability.Sum)
      with HasAggregator

  private[metrics] case object LastValue
      extends Aggregation(Compatability.LastValue)
      with HasAggregator

  private[metrics] final case class ExplicitBucketHistogram(
      boundaries: List[Double]
  ) extends Aggregation(Compatability.ExplicitBucketHistogram)
      with HasAggregator

  private[metrics] final case class Base2ExponentialHistogram(
      maxBuckets: Int,
      maxScale: Int
  ) extends Aggregation(Compatability.Base2ExponentialHistogram)
      with HasAggregator

  private object Compatability {
    val Drop: Set[InstrumentType] =
      InstrumentType.values

    val Default: Set[InstrumentType] =
      InstrumentType.values

    val Sum: Set[InstrumentType] = Set(
      InstrumentType.Counter,
      InstrumentType.UpDownCounter,
      InstrumentType.ObservableGauge,
      InstrumentType.ObservableUpDownCounter,
      InstrumentType.Histogram
    )

    val LastValue: Set[InstrumentType] =
      Set(InstrumentType.ObservableGauge)

    val ExplicitBucketHistogram: Set[InstrumentType] =
      Set(InstrumentType.Counter, InstrumentType.Histogram)

    val Base2ExponentialHistogram: Set[InstrumentType] =
      Set(InstrumentType.Counter, InstrumentType.Histogram)
  }

}

/*
import cats.Applicative
import org.typelevel.otel4s.sdk.metrics.data.{ExemplarData, PointData}
import org.typelevel.otel4s.sdk.metrics.internal.aggregation.Aggregator
import org.typelevel.otel4s.sdk.metrics.internal.{InstrumentDescriptor, InstrumentType, InstrumentValueType}

sealed trait Aggregation {
  def createAggregator[F[_]: Applicative](
      descriptor: InstrumentDescriptor,
      filter: ExemplarFilter
  ): Aggregator[F]
}

object Aggregation {
  /*def default: Aggregation =
    new Aggregation {
      def createAggregator[F[_]: Applicative](
          descriptor: InstrumentDescriptor,
          filter: ExemplarFilter
      ): Aggregator[F] =
        resolve(descriptor, true).createAggregator(descriptor, filter)

      private def resolve(
          descriptor: InstrumentDescriptor,
          withAdvice: Boolean
      ) =
        descriptor.instrumentType match {
          case InstrumentType.Counter                 => Aggregation.sum
          case InstrumentType.UpDownCounter           => Aggregation.sum
          case InstrumentType.ObservableCounter       => Aggregation.sum
          case InstrumentType.ObservableUpDownCounter => Aggregation.sum
          case InstrumentType.Histogram               => ???
          case InstrumentType.ObservableGauge         => ???
        }
    }

  def drop: Aggregation =
    new Aggregation {
      def createAggregator[F[_]: Applicative](
          descriptor: InstrumentDescriptor,
          filter: ExemplarFilter
      ): Aggregator.Aux[F] =
        new Aggregator.Drop

      def compatibleWith(descriptor: InstrumentDescriptor): Boolean =
        true
    }

  def sum: Aggregation =
    new Aggregation {
      def createAggregator[F[_]: Applicative](
          descriptor: InstrumentDescriptor,
          filter: ExemplarFilter
      ): Aggregator.Aux[F] =
        descriptor.valueType match {
          case InstrumentValueType.Long =>
            new Aggregator.LongSum(() => null).asInstanceOf[Aggregator.Aux[F]]

          case InstrumentValueType.Double =>
            new Aggregator.DoubleLastValue[F](() => null)
              .asInstanceOf[Aggregator.Aux[F]]
        }
    }*/

  def lastValue: Aggregation = ???

  def explicitBucketHistogram(
      boundaries: List[Double]
  ): Aggregation = ???

  def base2ExponentialHistogram: Aggregation = ???

  def base2ExponentialHistogram(maxBuckets: Int, maxScale: Int): Aggregation = ???

}
 */
