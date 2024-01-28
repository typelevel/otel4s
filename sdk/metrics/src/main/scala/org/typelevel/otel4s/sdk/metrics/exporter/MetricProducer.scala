package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.metrics.data.MetricData

trait MetricProducer[F[_]] {
  def produce(resource: TelemetryResource): F[Vector[MetricData]]
}
