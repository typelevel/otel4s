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

trait ObservableInstrumentBuilder[F[_], Input, Instrument[_[_], _]] {
  type Self <: ObservableInstrumentBuilder[F, Input, Instrument]

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

  /** Creates an instrument with the given `unit` and `description` (if any).
    */
  def create(callback: F[Input]): F[Instrument[F, Input]]

  /** Creates an instrument with the given callback, using `unit` and
    * `description` (if any).
    *
    * The callback will be called when the instrument is being observed.
    *
    * Callbacks are expected to abide by the following restrictions:
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
}

trait ObservableMeasurement[F[_], A] {

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def record(value: A, attributes: Attribute[_]*): F[Unit]
}
