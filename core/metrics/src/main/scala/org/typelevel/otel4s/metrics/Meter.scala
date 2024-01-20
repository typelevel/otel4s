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
import cats.effect.kernel.Resource

@annotation.implicitNotFound("""
Could not find the `Meter` for ${F}. `Meter` can be one of the following:

1) No-operation (a.k.a. without measurements)

import Meter.Implicits.noop

2) Manually from MeterProvider

val meterProvider: MeterProvider[IO] = ???
meterProvider
  .get("com.service.runtime")
  .flatMap { implicit meter: Meter[IO] => ??? }
""")
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
  def counter(name: String): Counter.Builder[F, Long]

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
  def histogram(name: String): Histogram.Builder[F, Double]

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
  def upDownCounter(name: String): UpDownCounter.Builder[F, Long]

  /** Creates a builder of [[ObservableGauge]] instrument that collects
    * [[scala.Double]] values from the given callback.
    *
    * @param name
    *   the name of the instrument
    */
  def observableGauge(
      name: String
  ): ObservableInstrumentBuilder[F, Double, ObservableGauge]

  /** Creates a builder of [[ObservableCounter]] instrument that collects
    * [[scala.Long]] values from the given callback.
    *
    * The [[ObservableCounter]] is monotonic. This means the aggregated value is
    * nominally increasing.
    *
    * @see
    *   See [[observableUpDownCounter]] for non-monotonic alternative
    *
    * @param name
    *   the name of the instrument
    */
  def observableCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableCounter]

  /** Creates a builder of [[ObservableUpDownCounter]] instrument that collects
    * [[scala.Long]] values from the given callback.
    *
    * The [[ObservableUpDownCounter]] is non-monotonic. This means the
    * aggregated value can increase and decrease.
    *
    * @see
    *   See [[observableCounter]] for monotonic alternative
    *
    * @param name
    *   the name of the instrument
    */
  def observableUpDownCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter]

}

object Meter {

  def apply[F[_]](implicit ev: Meter[F]): Meter[F] = ev

  /** Creates a no-op implementation of the [[Meter]].
    *
    * All meter instruments ([[Counter]], [[Histogram]], etc) have no-op
    * implementation too.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]](implicit F: Applicative[F]): Meter[F] =
    new Meter[F] {
      def counter(name: String): Counter.Builder[F, Long] =
        noopCounter[Long]

      def histogram(name: String): Histogram.Builder[F, Double] =
        noopHistogram

      def upDownCounter(name: String): UpDownCounter.Builder[F, Long] =
        noopUpDownCounter

      def observableGauge(
          name: String
      ): ObservableInstrumentBuilder[F, Double, ObservableGauge] =
        new ObservableInstrumentBuilder[F, Double, ObservableGauge] {
          type Self = this.type

          def withUnit(unit: String): Self = this
          def withDescription(description: String): Self = this
          def create(
              measurements: F[List[Measurement[Double]]]
          ): Resource[F, ObservableGauge] =
            Resource.pure(new ObservableGauge {})
          def createWithCallback(
              cb: ObservableMeasurement[F, Double] => F[Unit]
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
          def create(
              measurements: F[List[Measurement[Long]]]
          ): Resource[F, ObservableCounter] =
            Resource.pure(new ObservableCounter {})
          def createWithCallback(
              cb: ObservableMeasurement[F, Long] => F[Unit]
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
          def create(
              measurements: F[List[Measurement[Long]]]
          ): Resource[F, ObservableUpDownCounter] =
            Resource.pure(new ObservableUpDownCounter {})
          def createWithCallback(
              cb: ObservableMeasurement[F, Long] => F[Unit]
          ): Resource[F, ObservableUpDownCounter] =
            Resource.pure(new ObservableUpDownCounter {})
        }

      private def noopCounter[A]: Counter.Builder[F, A] =
        new Counter.Builder[F, A] {
          def withUnit(unit: String): Counter.Builder[F, A] = this
          def withDescription(description: String): Counter.Builder[F, A] = this
          def of[B: MeasurementValue]: Counter.Builder[F, B] = noopCounter
          def create: F[Counter[F, A]] = Applicative[F].pure(Counter.noop)
        }

      private def noopHistogram[A]: Histogram.Builder[F, A] =
        new Histogram.Builder[F, A] {
          def withUnit(unit: String): Histogram.Builder[F, A] = this
          def withDescription(description: String): Histogram.Builder[F, A] =
            this
          def withExplicitBucketBoundaries(
              boundaries: BucketBoundaries
          ): Histogram.Builder[F, A] = this
          def of[B: MeasurementValue]: Histogram.Builder[F, B] = noopHistogram
          def create: F[Histogram[F, A]] = F.pure(Histogram.noop)
        }

      private def noopUpDownCounter[A]: UpDownCounter.Builder[F, A] =
        new UpDownCounter.Builder[F, A] {
          def withUnit(unit: String): UpDownCounter.Builder[F, A] = this
          def withDescription(
              description: String
          ): UpDownCounter.Builder[F, A] = this
          def of[B: MeasurementValue]: UpDownCounter.Builder[F, B] =
            noopUpDownCounter
          def create: F[UpDownCounter[F, A]] = F.pure(UpDownCounter.noop)
        }
    }

  object Implicits {
    implicit def noop[F[_]: Applicative]: Meter[F] = Meter.noop
  }
}
