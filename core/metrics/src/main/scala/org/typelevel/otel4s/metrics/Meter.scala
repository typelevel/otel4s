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

import cats.Applicative
import cats.effect.Resource

trait Meter[F[_]] {

  /** Creates a builder of [[Counter]] instrument that records [[scala.Long]]
    * values.
    *
    * The [[Counter]] is monotonic. This means the aggregated value is nominally
    * increasing.
    *
    * @see
    *   See [[upDownCounter]] for non-monotonic alternative
    *
    * @param name
    *   the name of the instrument
    */
  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]]

  /** Creates a builder of [[Histogram]] instrument that records
    * [[scala.Double]] values.
    *
    * [[Histogram]] metric data points convey a population of recorded
    * measurements in a compressed format. A histogram bundles a set of events
    * into divided populations with an overall event count and aggregate sum for
    * all events.
    *
    * @param name
    *   the name of the instrument
    */
  def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Double]]

  /** Creates a builder of [[UpDownCounter]] instrument that records
    * [[scala.Long]] values.
    *
    * The [[UpDownCounter]] is non-monotonic. This means the aggregated value
    * can increase and decrease.
    *
    * @see
    *   See [[counter]] for monotonic alternative
    *
    * @param name
    *   the name of the instrument
    */
  def upDownCounter(
      name: String
  ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]]

  def observableGauge(
      name: String
  ): ObservableInstrumentBuilder[F, Double, ObservableGauge]
  def observableCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableCounter]
  def observableUpDownCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter]

}

object Meter {
  def noop[F[_]](implicit F: Applicative[F]): Meter[F] =
    new Meter[F] {
      def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
        new SyncInstrumentBuilder[F, Counter[F, Long]] {
          type Self = this.type
          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def create: F[Counter[F, Long]] = F.pure(Counter.noop)
        }

      def histogram(
          name: String
      ): SyncInstrumentBuilder[F, Histogram[F, Double]] =
        new SyncInstrumentBuilder[F, Histogram[F, Double]] {
          type Self = this.type
          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def create: F[Histogram[F, Double]] = F.pure(Histogram.noop)
        }

      def upDownCounter(
          name: String
      ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]] =
        new SyncInstrumentBuilder[F, UpDownCounter[F, Long]] {
          type Self = this.type

          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def create: F[UpDownCounter[F, Long]] = F.pure(UpDownCounter.noop)
        }
      def observableGauge(
          name: String
      ): ObservableInstrumentBuilder[F, Double, ObservableGauge] =
        new ObservableInstrumentBuilder[F, Double, ObservableGauge] {
          type Self = this.type

          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def createWithCallback(
              _cb: ObservableMeasurement[F, Double] => F[Unit]
          ): Resource[F, ObservableGauge] =
            Resource.pure(new ObservableGauge {})
        }

      def observableCounter(
          name: String
      ): ObservableInstrumentBuilder[F, Long, ObservableCounter] =
        new ObservableInstrumentBuilder[F, Long, ObservableCounter] {
          type Self = this.type

          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def createWithCallback(
              _cb: ObservableMeasurement[F, Long] => F[Unit]
          ): Resource[F, ObservableCounter] =
            Resource.pure(new ObservableCounter {})
        }

      def observableUpDownCounter(
          name: String
      ): ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter] =
        new ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter] {
          type Self = this.type

          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def createWithCallback(
              _cb: ObservableMeasurement[F, Long] => F[Unit]
          ): Resource[F, ObservableUpDownCounter] =
            Resource.pure(new ObservableUpDownCounter {})
        }
    }
}
