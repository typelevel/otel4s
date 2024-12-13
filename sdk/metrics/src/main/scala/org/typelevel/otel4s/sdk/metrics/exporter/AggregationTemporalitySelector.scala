/*
 * Copyright 2024 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s.sdk.metrics.exporter

import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality

/** Used by the `MetricReader` to decide the default aggregation temporality.
  */
trait AggregationTemporalitySelector {

  /** Returns preferred aggregation temporality for the given [[InstrumentType]].
    */
  def select(instrumentType: InstrumentType): AggregationTemporality
}

object AggregationTemporalitySelector {

  /** Returns cumulative aggregation temporality for all instruments.
    */
  def alwaysCumulative: AggregationTemporalitySelector =
    _ => AggregationTemporality.Cumulative

  /** Returns cumulative aggregation temporality for `UpDownCounter` and `ObservableUpDownCounter`, and delta
    * aggregation temporality for any other instrument.
    */
  def deltaPreferred: AggregationTemporalitySelector = {
    case InstrumentType.UpDownCounter =>
      AggregationTemporality.Cumulative

    case InstrumentType.ObservableUpDownCounter =>
      AggregationTemporality.Cumulative

    case _ =>
      AggregationTemporality.Delta
  }

  /** Returns cumulative aggregation temporality for `UpDownCounter`, `ObservableUpDownCounter`, `ObservableCounter`,
    * and delta aggregation temporality for any other instrument.
    */
  def lowMemory: AggregationTemporalitySelector = {
    case InstrumentType.UpDownCounter =>
      AggregationTemporality.Cumulative

    case InstrumentType.ObservableUpDownCounter =>
      AggregationTemporality.Cumulative

    case InstrumentType.ObservableCounter =>
      AggregationTemporality.Cumulative

    case _ =>
      AggregationTemporality.Delta
  }

}
