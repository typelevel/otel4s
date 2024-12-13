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

import cats.Hash
import cats.Show

/** The type of an instrument.
  *
  * @note
  *   the terms synchronous and asynchronous have nothing to do with asynchronous programming. We follow naming
  *   according to the OpenTelemetry specification.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/api/#synchronous-and-asynchronous-instruments]]
  */
sealed trait InstrumentType extends Product with Serializable

object InstrumentType {

  /** Synchronous instruments (e.g. [[org.typelevel.otel4s.metrics.Counter Counter]]) are meant to be invoked inline
    * with application/business processing logic.
    *
    * For instance, an HTTP client might utilize a counter to record the number of received bytes.
    *
    * The measurements captured by synchronous instruments can be linked with the tracing information (span id, trace
    * id).
    */
  sealed trait Synchronous extends InstrumentType

  /** Asynchronous instruments (e.g. [[org.typelevel.otel4s.metrics.ObservableGauge ObservableGauge]]) offer users the
    * ability to register callback functions, which are only triggered on demand.
    *
    * For example, an asynchronous gauge can be used to collect the temperature from a sensor every 15 seconds, which
    * means the callback function will only be invoked every 15 seconds.
    *
    * Unlike synchronous instruments, the measurements captured by asynchronous instruments '''cannot''' be linked with
    * the tracing information.
    */
  sealed trait Asynchronous extends InstrumentType

  case object Counter extends Synchronous
  case object UpDownCounter extends Synchronous
  case object Histogram extends Synchronous
  case object Gauge extends Synchronous
  case object ObservableCounter extends Asynchronous
  case object ObservableUpDownCounter extends Asynchronous
  case object ObservableGauge extends Asynchronous

  val values: Set[InstrumentType] = Set(
    Counter,
    UpDownCounter,
    Histogram,
    Gauge,
    ObservableCounter,
    ObservableUpDownCounter,
    ObservableGauge
  )

  implicit val instrumentTypeHash: Hash[InstrumentType] =
    Hash.fromUniversalHashCode

  implicit val instrumentTypeShow: Show[InstrumentType] =
    Show.fromToString
}
