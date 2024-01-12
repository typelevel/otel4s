package org.typelevel.otel4s.sdk.metrics.data

sealed trait MetricDataType {

}

object MetricDataType {

  case object LongGauge extends MetricDataType
  case object DoubleGauge extends MetricDataType
  case object LongSum extends MetricDataType
  case object DoubleSum extends MetricDataType
  case object Summary extends MetricDataType
  case object Histogram extends MetricDataType
  case object ExponentialHistogram extends MetricDataType

}
