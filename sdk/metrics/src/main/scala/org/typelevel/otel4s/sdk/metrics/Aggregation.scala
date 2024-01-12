package org.typelevel.otel4s.sdk.metrics

import cats.Applicative
import org.typelevel.otel4s.sdk.metrics.data.{ExemplarData, PointData}
import org.typelevel.otel4s.sdk.metrics.internal.{
  Aggregator,
  InstrumentDescriptor,
  InstrumentType,
  InstrumentValueType
}

sealed trait Aggregation {
  def createAggregator[F[_]: Applicative](
      descriptor: InstrumentDescriptor,
      filter: ExemplarFilter
  ): Aggregator.Aux[F]

  def compatibleWith(descriptor: InstrumentDescriptor): Boolean
}

object Aggregation {
  def default: Aggregation =
    new Aggregation {
      def createAggregator[F[_]: Applicative](
          descriptor: InstrumentDescriptor,
          filter: ExemplarFilter
      ): Aggregator.Aux[F] =
        resolve(descriptor, true).createAggregator(descriptor, filter)

      def compatibleWith(descriptor: InstrumentDescriptor): Boolean =
        resolve(descriptor, false).compatibleWith(descriptor)

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

      def compatibleWith(descriptor: InstrumentDescriptor): Boolean =
        descriptor.instrumentType match {
          case InstrumentType.Counter                 => true
          case InstrumentType.UpDownCounter           => true
          case InstrumentType.ObservableCounter       => true
          case InstrumentType.ObservableUpDownCounter => true
          case InstrumentType.Histogram               => true
          case _                                      => false
        }
    }

}
