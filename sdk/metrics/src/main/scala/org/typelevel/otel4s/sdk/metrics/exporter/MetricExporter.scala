package org.typelevel.otel4s.sdk.metrics.exporter

import cats.Foldable
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

trait MetricExporter[F[_]] {
  def aggregationTemporality(
      instrumentType: InstrumentType
  ): AggregationTemporality

  def name: String

  def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit]

  def flush: F[Unit]

  override def toString: String =
    name

}

object MetricExporter {}
