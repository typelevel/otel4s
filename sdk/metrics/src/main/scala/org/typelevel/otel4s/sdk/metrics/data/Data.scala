package org.typelevel.otel4s.sdk.metrics.data

sealed abstract class Data(val tpe: MetricDataType) {
  def points: Vector[PointData]
}

object Data {

  final case class DoubleSum(
      points: Vector[PointData.DoublePoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.DoubleSum)

  final case class LongSum(
      points: Vector[PointData.LongPoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.LongSum)

  final case class DoubleGauge(
      points: Vector[PointData.DoublePoint]
  ) extends Data(MetricDataType.DoubleGauge)

  final case class LongGauge(
      points: Vector[PointData.LongPoint]
  ) extends Data(MetricDataType.LongGauge)

  final case class Summary(
      points: Vector[PointData.Summary]
  ) extends Data(MetricDataType.Summary)

  final case class Histogram(
      points: Vector[PointData.Histogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.Histogram)

  final case class ExponentialHistogram(
      points: Vector[PointData.ExponentialHistogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data(MetricDataType.ExponentialHistogram)

}
