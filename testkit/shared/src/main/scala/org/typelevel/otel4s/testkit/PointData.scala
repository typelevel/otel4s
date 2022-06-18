package org.typelevel.otel4s.testkit

import org.typelevel.otel4s.Attribute

final case class PointData[A](
    startEpochNanos: Long,
    epochNanos: Long,
    attributes: List[Attribute[_]],
    value: A
)

final case class QuantileData(
    quantile: Double,
    value: Double
)

final case class SummaryPointData(
    sum: Double,
    count: Long,
    values: List[QuantileData]
)

final case class HistogramPointData(
    sum: Double,
    count: Long,
    boundaries: List[Double],
    counts: List[Long]
)
