package org.typelevel.otel4s.sdk.metrics.data

sealed trait ValueAtQuantile {
  def quantile: Double
  def value: Double
}
