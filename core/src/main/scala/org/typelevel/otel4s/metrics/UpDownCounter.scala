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

/** A `Counter` instrument that records values of type `A`.
  *
  * The [[UpDownCounter]] is non-monotonic. This means the aggregated value can
  * increase and decrease.
  *
  * @see
  *   See [[Counter]] for monotonic alternative
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  * @tparam A
  *   the type of the values to record. OpenTelemetry specification expects `A`
  *   to be either [[scala.Long]] or [[scala.Double]]
  */
trait UpDownCounter[F[_], A] {

  /** Records a value with a set of attributes.
    *
    * @param value
    *   the value to record
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

  /** Decrements a counter by one.
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def dec(attributes: Attribute[_]*): F[Unit]
}

object UpDownCounter {

  trait LongUpDownCounter[F[_]] extends UpDownCounter[F, Long] {
    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1L, attributes: _*)

    final def dec(attributes: Attribute[_]*): F[Unit] =
      add(-1L, attributes: _*)
  }

  trait DoubleUpDownCounter[F[_]] extends UpDownCounter[F, Double] {
    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1.0, attributes: _*)

    final def dec(attributes: Attribute[_]*): F[Unit] =
      add(-1.0, attributes: _*)
  }

  def noop[F[_], A](implicit F: Applicative[F]): UpDownCounter[F, A] =
    new UpDownCounter[F, A] {
      def add(value: A, attributes: Attribute[_]*): F[Unit] = F.unit
      def inc(attributes: Attribute[_]*): F[Unit] = F.unit
      def dec(attributes: Attribute[_]*): F[Unit] = F.unit
    }

}
