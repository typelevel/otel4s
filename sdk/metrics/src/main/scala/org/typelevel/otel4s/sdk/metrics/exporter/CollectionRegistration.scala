package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.metrics.data.MetricData

trait CollectionRegistration[F[_]] {
  def collectAllMetrics: F[Vector[MetricData]]
}
