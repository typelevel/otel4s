package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

trait DefaultAggregationSelector {
  def get(instrumentType: InstrumentType): Aggregation
}

object DefaultAggregationSelector {

}
