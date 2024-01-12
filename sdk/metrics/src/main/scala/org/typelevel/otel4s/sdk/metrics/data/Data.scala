package org.typelevel.otel4s.sdk.metrics.data

sealed trait Data {
  type D <: PointData
  def points: Vector[D]
}

object Data {

  final case class LongSum(
      points: Vector[PointData.LongPoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data {
    type D = PointData.LongPoint
  }

  final case class DoubleSum(
      points: Vector[PointData.DoublePoint],
      isMonotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Data {
    type D = PointData.DoublePoint
  }

  final case class DoubleGauge(
      points: Vector[PointData.DoublePoint]
  ) extends Data {
    type D = PointData.DoublePoint
  }

  final case class Summary(
      points: Vector[PointData.Summary]
  ) extends Data {
    type D = PointData.Summary
  }

  final case class Histogram(
      points: Vector[PointData.Histogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data {
    type D = PointData.Histogram
  }

  final case class ExponentialHistogram(
      points: Vector[PointData.ExponentialHistogram],
      aggregationTemporality: AggregationTemporality
  ) extends Data {
    type D = PointData.ExponentialHistogram
  }
}
