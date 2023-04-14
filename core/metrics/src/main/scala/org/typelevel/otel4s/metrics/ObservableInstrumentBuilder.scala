/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.metrics

import cats.effect.Resource

trait ObservableInstrumentBuilder[F[_], A, Instrument] {
  type Self <: ObservableInstrumentBuilder[F, A, Instrument]

  /** Sets the unit of measure for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
    *   Unit</a>
    *
    * @param unit
    *   the measurement unit. Must be 63 or fewer ASCII characters.
    */
  def withUnit(unit: String): Self

  /** Sets the description for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
    *   Description</a>
    *
    * @param description
    *   the description
    */
  def withDescription(description: String): Self

  /** Creates an instrument with the given callback, using `unit` and
    * `description` (if any).
    *
    * The callback will be called when the instrument is being observed.
    *
    * The callback is expected to abide by the following restrictions:
    *   - Short-living and (ideally) non-blocking
    *   - Run in a finite amount of time
    *   - Safe to call repeatedly, across multiple threads
    *
    * @param cb
    *   The callback which observes measurements when invoked
    */
  def createWithCallback(
      cb: ObservableMeasurement[F, A] => F[Unit]
  ): Resource[F, Instrument]

  /** Creates an asynchronous instrument based on an effect that produces a
    * number of measurements.
    *
    * The measurement effect will be evaluted when the instrument is being
    * observed.
    *
    * The measurement effect is expected to abide by the following restrictions:
    *   - Short-living and (ideally) non-blocking
    *   - Run in a finite amount of time
    *   - Safe to call repeatedly, across multiple threads
    *
    * @param measurements
    *   Effect that produces a number of measurements
    */
  def create(measurements: F[List[Measurement[A]]]): Resource[F, Instrument]
}
