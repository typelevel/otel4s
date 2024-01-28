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

package org.typelevel.otel4s
package metrics

import cats.Applicative
import org.typelevel.otel4s.meta.InstrumentMeta

/** A `Counter` instrument that records values of type `A`.
  *
  * The [[Counter]] is monotonic. This means the aggregated value is nominally
  * increasing.
  *
  * @see
  *   See [[UpDownCounter]] for non-monotonic alternative
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record. The type must have an instance of
  *   [[MeasurementValue]]. [[scala.Long]] and [[scala.Double]] are supported
  *   out of the box.
  */
trait Counter[F[_], A] extends CounterMacro[F, A]

object Counter {

  /** A builder of [[Counter]].
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

    /** Sets the unit of measure for this counter.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit Instrument Unit]]
      *
      * @param unit
      *   the measurement unit. Must be 63 or fewer ASCII characters.
      */
    def withUnit(unit: String): Builder[F, A]

    /** Sets the description for this counter.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description Instrument Description]]
      *
      * @param description
      *   the description
      */
    def withDescription(description: String): Builder[F, A]

    /** Creates a [[Counter]] with the given `unit` and `description` (if any).
      */
    def create: F[Counter[F, A]]
  }

  trait Backend[F[_], A] {
    def meta: InstrumentMeta[F]

    /** Records a value with a set of attributes.
      *
      * @param value
      *   the value to increment a counter with. Must be '''non-negative'''
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def add(value: A, attributes: Attribute[_]*): F[Unit]

    /** Increments a counter by one.
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def inc(attributes: Attribute[_]*): F[Unit]
  }

  trait LongBackend[F[_]] extends Backend[F, Long] {
    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1L, attributes: _*)
  }

  trait DoubleBackend[F[_]] extends Backend[F, Double] {
    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1.0, attributes: _*)
  }

  def noop[F[_], A](implicit F: Applicative[F]): Counter[F, A] =
    new Counter[F, A] {
      val backend: Backend[F, A] =
        new Backend[F, A] {
          val meta: InstrumentMeta[F] = InstrumentMeta.disabled
          def add(value: A, attributes: Attribute[_]*): F[Unit] = meta.unit
          def inc(attributes: Attribute[_]*): F[Unit] = meta.unit
        }
    }

  private[otel4s] def fromBackend[F[_], A](b: Backend[F, A]): Counter[F, A] =
    new Counter[F, A] {
      def backend: Backend[F, A] = b
    }

}
