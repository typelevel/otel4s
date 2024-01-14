package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.Attributes
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

  def collectAndReset(pointAttributes: Attributes): F[Vector[E]]
}

object ExemplarReservoir {

  def filtered[F[_], E <: ExemplarData](
      filter: ExemplarFilter,
      original: ExemplarReservoir[F, E]
  ): ExemplarReservoir[F, E] = ???

  // size = availableProcessors
  def longFixedSize[F[_]](
      size: Int
  ): F[ExemplarReservoir[F, ExemplarData.LongExemplar]] = ???

  def doubleFixedSize[F[_]](
      size: Int
  ): F[ExemplarReservoir[F, ExemplarData.DoubleExemplar]] = ???

  def longNoSamples[F[_]]: ExemplarReservoir[F, ExemplarData.LongExemplar] =
    ???

  def doubleNoSamples[F[_]]: ExemplarReservoir[F, ExemplarData.DoubleExemplar] =
    ???
}
