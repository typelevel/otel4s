package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.PointData

import scala.concurrent.duration.FiniteDuration

private trait PointDataBuilder[I, P <: PointData, E <: ExemplarData] {
  def create(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      exemplars: Vector[E],
      value: I
  ): P
}

private object PointDataBuilder {
  import PointData._
  import ExemplarData._

  def longPoint: PointDataBuilder[Long, LongPoint, LongExemplar] =
    (startTimestamp, collectTimestamp, attributes, exemplars, value) =>
      PointData.LongPoint(
        startTimestamp,
        collectTimestamp,
        attributes,
        exemplars,
        value
      )

  def doublePoint: PointDataBuilder[Double, DoublePoint, DoubleExemplar] =
    (startTimestamp, collectTimestamp, attributes, exemplars, value) =>
      PointData.DoublePoint(
        startTimestamp,
        collectTimestamp,
        attributes,
        exemplars,
        value
      )

}
