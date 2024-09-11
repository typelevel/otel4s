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

package org.typelevel.otel4s.sdk.metrics
package exporter

/** Used by the `MetricReader` to decide the default aggregation.
  */
trait AggregationSelector {

  /** Returns preferred [[Aggregation]] for the given [[InstrumentType]].
    */
  final def select(instrumentType: InstrumentType): Aggregation =
    instrumentType match {
      case synchronous: InstrumentType.Synchronous =>
        forSynchronous(synchronous)
      case asynchronous: InstrumentType.Asynchronous =>
        forAsynchronous(asynchronous)
    }

  /** Returns preferred [[Aggregation]] for the given [[InstrumentType.Synchronous]].
    */
  def forSynchronous(
      instrumentType: InstrumentType.Synchronous
  ): Aggregation with Aggregation.Synchronous

  /** Returns preferred [[Aggregation]] for the given [[InstrumentType.Asynchronous]].
    */
  def forAsynchronous(
      instrumentType: InstrumentType.Asynchronous
  ): Aggregation with Aggregation.Asynchronous
}

object AggregationSelector {

  /** Returns [[Aggregation.default]] for all instruments.
    */
  def default: AggregationSelector = Default

  private object Default extends AggregationSelector {
    def forSynchronous(
        instrumentType: InstrumentType.Synchronous
    ): Aggregation with Aggregation.Synchronous =
      Aggregation.Default

    def forAsynchronous(
        instrumentType: InstrumentType.Asynchronous
    ): Aggregation with Aggregation.Asynchronous =
      Aggregation.Default
  }
}
