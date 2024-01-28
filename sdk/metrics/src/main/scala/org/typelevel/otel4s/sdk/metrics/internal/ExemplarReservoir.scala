package org.typelevel.otel4s.sdk.metrics.internal

import cats.Applicative
import cats.syntax.applicative._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData

trait ExemplarReservoir[F[_], E <: ExemplarData] {
  def offerDoubleMeasurement(
      value: Double,
      attributes: Attributes,
      context: Context
  ): F[Unit]

  def offerLongMeasurement(
      value: Long,
      attributes: Attributes,
      context: Context
  ): F[Unit]

  def collectAndReset(attributes: Attributes): F[Vector[E]]
}

object ExemplarReservoir {

  def filtered[F[_]: Applicative, E <: ExemplarData](
      filter: ExemplarFilter,
      original: ExemplarReservoir[F, E]
  ): ExemplarReservoir[F, E] =
    new ExemplarReservoir[F, E] {
      def offerDoubleMeasurement(
          value: Double,
          attributes: Attributes,
          context: Context
      ): F[Unit] =
        original
          .offerDoubleMeasurement(value, attributes, context)
          .whenA(
            filter.shouldSample(value, attributes, context)
          )

      def offerLongMeasurement(
          value: Long,
          attributes: Attributes,
          context: Context
      ): F[Unit] =
        original
          .offerLongMeasurement(value, attributes, context)
          .whenA(
            filter.shouldSample(value, attributes, context)
          )

      def collectAndReset(attributes: Attributes): F[Vector[E]] =
        original.collectAndReset(attributes)
    }

  // size = availableProcessors
  def longFixedSize[F[_]: Applicative](
      size: Int
  ): F[ExemplarReservoir[F, ExemplarData.LongExemplar]] =
    Applicative[F].pure(noop)

  def doubleFixedSize[F[_]: Applicative](
      size: Int
  ): F[ExemplarReservoir[F, ExemplarData.DoubleExemplar]] =
    Applicative[F].pure(noop)

  def histogramBucket[F[_]: Applicative](
      boundaries: BucketBoundaries
  ): F[ExemplarReservoir[F, ExemplarData.DoubleExemplar]] =
    Applicative[F].pure(noop)

  private def noop[
      F[_]: Applicative,
      E <: ExemplarData
  ]: ExemplarReservoir[F, E] =
    new ExemplarReservoir[F, E] {
      def offerDoubleMeasurement(
          value: Double,
          attributes: Attributes,
          context: Context
      ): F[Unit] =
        Applicative[F].unit

      def offerLongMeasurement(
          value: Long,
          attributes: Attributes,
          context: Context
      ): F[Unit] =
        Applicative[F].unit

      def collectAndReset(attributes: Attributes): F[Vector[E]] =
        Applicative[F].pure(Vector.empty)
    }
}
