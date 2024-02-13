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

trait ObservableGauge

object ObservableGauge {

  /** A builder of [[ObservableGauge]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record. The type must have an instance of
    *   [[MeasurementValue]]. [[scala.Long]] and [[scala.Double]] are supported
    *   out of the box.
    */
  trait Builder[F[_], A] {

    /** Sets the unit of measure for this instrument.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit Instrument unit]]
      *
      * @param unit
      *   the measurement unit. Must be 63 or fewer ASCII characters.
      */
    def withUnit(unit: String): Builder[F, A]

    /** Sets the description for this instrument.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description Instrument Description]]
      *
      * @param description
      *   the description
      */
    def withDescription(description: String): Builder[F, A]

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
      *   the callback which observes measurements when invoked
      */
    def createWithCallback(
        cb: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge]

    /** Creates an asynchronous instrument based on an effect that produces a
      * number of measurements.
      *
      * The measurement effect will be evaluated when the instrument is being
      * observed.
      *
      * The measurement effect is expected to abide by the following
      * restrictions:
      *   - Short-living and (ideally) non-blocking
      *   - Run in a finite amount of time
      *   - Safe to call repeatedly, across multiple threads
      *
      * @param measurements
      *   effect that produces a number of measurements
      */
    def create(
        measurements: F[Iterable[Measurement[A]]]
    ): Resource[F, ObservableGauge]

    /** Creates an observer for this instrument to observe values from a
      * [[BatchCallback]].
      *
      * @note
      *   The observer '''must''' be registered via [[Meter.batchCallback]].
      *   Values observed outside registered callbacks are ignored.
      */
    def createObserver: F[ObservableMeasurement[F, A]]
  }

}
