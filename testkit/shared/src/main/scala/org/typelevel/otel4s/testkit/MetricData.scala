package org.typelevel.otel4s.testkit

sealed trait MetricData extends Product with Serializable

object MetricData {
  final case class QuantileData(quantile: Double, value: Double)

  final case class SummaryPoint(
      sum: Double,
      count: Long,
      values: List[QuantileData]
  )

  final case class HistogramPoint(
      sum: Double,
      count: Long,
      boundaries: List[Double],
      counts: List[Long]
  )

  final case class LongGauge(points: List[Long]) extends MetricData

  final case class DoubleGauge(points: List[Double]) extends MetricData

  final case class LongSum(points: List[Long]) extends MetricData

  final case class DoubleSum(points: List[Double]) extends MetricData

  final case class Summary(points: List[SummaryPoint]) extends MetricData

  final case class Histogram(point: List[HistogramPoint]) extends MetricData

  final case class ExponentialHistogram(points: List[HistogramPoint])
      extends MetricData
}
