package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

trait MetricReader[F[_]] {
  def defaultAggregationSelector: DefaultAggregationSelector
  def register(registration: CollectionRegistration[F]): F[Unit]
  def forceFlush: F[Unit]

  // todo: use DefaultAggregationSelector and AggregationTemporalitySelector
  def aggregationTemporality(instrumentType: InstrumentType): AggregationTemporality
}

object MetricReader {


}