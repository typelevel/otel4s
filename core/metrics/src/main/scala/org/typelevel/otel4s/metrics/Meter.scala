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

  /** Creates a builder of [[Counter]] instrument that records values.
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
  def counter[A: MeasurementValue](name: String): Counter.Builder[F, A]

  /** Creates a builder of [[Histogram]] instrument that records values.
    *
    * [[Histogram]] metric data points convey a population of recorded
    * measurements in a compressed format. A histogram bundles a set of events
    * into divided populations with an overall event count and aggregate sum for
    * all events.
    *
    * @param name
    *   the name of the instrument
    */
  def histogram[A: MeasurementValue](name: String): Histogram.Builder[F, A]

  /** Creates a builder of [[UpDownCounter]] instrument that records values.
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
  def upDownCounter[A: MeasurementValue](
      name: String
  ): UpDownCounter.Builder[F, A]

  /** Creates a builder of a gauge instrument that collects values from the
    * given callback.
    *
    * @param name
    *   the name of the instrument
    */
  def gauge[A: MeasurementValue](name: String): Gauge.Builder[F, A]

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
      def counter[A: MeasurementValue](
          name: String
      ): Counter.Builder[F, A] =
        new Counter.Builder[F, A] {
          def withUnit(unit: String): Counter.Builder[F, A] = this
          def withDescription(description: String): Counter.Builder[F, A] = this
          def create: F[Counter[F, A]] = F.pure(Counter.noop)
          def createWithCallback(
              cb: ObservableMeasurement[F, A] => F[Unit]
          ): Resource[F, Unit] =
            Resource.unit
          def createWithSupplier(
              measurements: F[List[Measurement[A]]]
          ): Resource[F, Unit] =
            Resource.unit
        }

      def histogram[A: MeasurementValue](
          name: String
      ): Histogram.Builder[F, A] =
        new Histogram.Builder[F, A] {
          def withUnit(unit: String): Histogram.Builder[F, A] = this
          def withDescription(description: String): Histogram.Builder[F, A] =
            this
          def withExplicitBucketBoundaries(
              boundaries: BucketBoundaries
          ): Histogram.Builder[F, A] = this
          def create: F[Histogram[F, A]] = F.pure(Histogram.noop)
        }

      def upDownCounter[A: MeasurementValue](
          name: String
      ): UpDownCounter.Builder[F, A] =
        new UpDownCounter.Builder[F, A] {
          def withUnit(unit: String): UpDownCounter.Builder[F, A] = this
          def withDescription(
              description: String
          ): UpDownCounter.Builder[F, A] = this
          def create: F[UpDownCounter[F, A]] = F.pure(UpDownCounter.noop)
          def createWithCallback(
              cb: ObservableMeasurement[F, A] => F[Unit]
          ): Resource[F, Unit] =
            Resource.unit
          def createWithSupplier(
              measurements: F[List[Measurement[A]]]
          ): Resource[F, Unit] =
            Resource.unit
        }

      def gauge[A: MeasurementValue](
          name: String
      ): Gauge.Builder[F, A] =
        new Gauge.Builder[F, A] {
          def withUnit(unit: String): Gauge.Builder[F, A] = this
          def withDescription(description: String): Gauge.Builder[F, A] = this
          def createWithSupplier(
              measurements: F[List[Measurement[A]]]
          ): Resource[F, Unit] =
            Resource.unit
          def createWithCallback(
              cb: ObservableMeasurement[F, A] => F[Unit]
          ): Resource[F, Unit] =
            Resource.unit
        }
    }

  object Implicits {
    implicit def noop[F[_]: Applicative]: Meter[F] = Meter.noop
  }
}
