package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.Attributes

import scala.concurrent.duration.FiniteDuration

sealed trait Measurement {
  def startTimestamp: FiniteDuration
  def collectTimestamp: FiniteDuration
  def attributes: Attributes

  def withAttributes(attributes: Attributes): Measurement
}

object Measurement {
  final case class LongMeasurement(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      value: Long
  ) extends Measurement {
    def withAttributes(attributes: Attributes): Measurement =
      copy(attributes = attributes)
  }

  final case class DoubleMeasurement(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      value: Double
  ) extends Measurement {
    def withAttributes(attributes: Attributes): Measurement =
      copy(attributes = attributes)
  }
}
