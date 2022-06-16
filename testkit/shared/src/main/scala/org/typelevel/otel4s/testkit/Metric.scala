package org.typelevel.otel4s.testkit

final case class Metric(
    name: String,
    description: Option[String],
    unit: Option[String],
    data: MetricData
)
