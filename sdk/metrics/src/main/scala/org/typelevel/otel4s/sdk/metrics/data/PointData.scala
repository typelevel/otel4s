package org.typelevel.otel4s.sdk.metrics.data

import org.typelevel.otel4s.Attributes

import scala.concurrent.duration.FiniteDuration

sealed trait PointData {
  type Exemplar <: ExemplarData

  def startTimestamp: FiniteDuration
  def endTimestamp: FiniteDuration
  def attributes: Attributes
  def exemplars: Vector[Exemplar]

}

object PointData {

  final case class LongPoint(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.LongExemplar],
      value: Long
  ) extends PointData {
    type Exemplar = ExemplarData.LongExemplar
  }

  final case class DoublePoint(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      value: Double
  ) extends PointData {
    type Exemplar = ExemplarData.DoubleExemplar
  }

  final case class Summary(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      count: Long,
      sum: Double,
      percentileValues: Vector[ValueAtQuantile]
  ) extends PointData {
    type Exemplar = ExemplarData
    def exemplars: Vector[ExemplarData] = Vector.empty
  }

  final case class Histogram(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      sum: Double,
      hasMin: Boolean,
      min: Double,
      hasMax: Boolean,
      max: Double,
      boundaries: Vector[Double],
      counts: Vector[Long]
  ) extends PointData {
    type Exemplar = ExemplarData.DoubleExemplar

    require(counts.length == boundaries.size + 1)
    // todo require(isStrictlyIncreasing())

    val count: Long = counts.sum
  }

  final case class ExponentialHistogram(
      startTimestamp: FiniteDuration,
      endTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      sum: Double,
      zeroCount: Long,
      hasMin: Boolean,
      min: Double,
      hasMax: Boolean,
      max: Double,
      positiveBuckets: ExponentialHistogramBuckets,
      negativeBuckets: ExponentialHistogramBuckets
  ) extends PointData {
    type Exemplar = ExemplarData.DoubleExemplar

    val count: Long =
      zeroCount + positiveBuckets.totalCount + negativeBuckets.totalCount
  }

}
