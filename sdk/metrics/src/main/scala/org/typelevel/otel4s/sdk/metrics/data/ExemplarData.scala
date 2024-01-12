package org.typelevel.otel4s.sdk.metrics.data

import org.typelevel.otel4s.Attributes

import scala.concurrent.duration.FiniteDuration

sealed trait ExemplarData {

  def filteredAttributes: Attributes
  def timestamp: FiniteDuration
  // todo spanContext: SpanContext

}

object ExemplarData {

  final case class LongExemplar(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      value: Long
  ) extends ExemplarData

  final case class DoubleExemplar(
      filteredAttributes: Attributes,
      timestamp: FiniteDuration,
      value: Double
  ) extends ExemplarData

}
