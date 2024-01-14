package org.typelevel.otel4s.sdk.metrics.data

sealed trait AggregationTemporality

object AggregationTemporality {
  case object Delta extends AggregationTemporality
  case object Cumulative extends AggregationTemporality
}
