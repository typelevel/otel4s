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
import cats.Monad
import cats.mtl.LiftValue
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.meta.InstrumentMeta

import scala.collection.immutable

/** A `Counter` instrument that records values of type `A`.
  *
  * The [[UpDownCounter]] is non-monotonic. This means the aggregated value can increase and decrease.
  *
  * @see
  *   See [[Counter]] for monotonic alternative
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record. The type must have an instance of [[MeasurementValue]]. [[scala.Long]] and
  *   [[scala.Double]] are supported out of the box.
  */
sealed trait UpDownCounter[F[_], A] extends UpDownCounterMacro[F, A] {

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit G: Monad[G], lift: LiftValue[F, G]): UpDownCounter[G, A] =
    UpDownCounter.fromBackend(backend.liftTo[G])

}

object UpDownCounter {
  private[otel4s] trait Unsealed[F[_], A] extends UpDownCounter[F, A]

  /** A builder of [[UpDownCounter]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record. The type must have an instance of [[MeasurementValue]]. [[scala.Long]] and
    *   [[scala.Double]] are supported out of the box.
    */
  sealed trait Builder[F[_], A] {

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

    /** Creates an [[UpDownCounter]] with the given `unit` and `description` (if any).
      */
    def create: F[UpDownCounter[F, A]]

    /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit G: Monad[G], lift: LiftValue[F, G]): Builder[G, A] =
      new Builder.Lifted[F, G, A](this)
  }

  object Builder {
    private[otel4s] trait Unsealed[F[_], A] extends Builder[F, A]

    private[otel4s] final class Lifted[F[_], G[_], A](
        builder: Builder[F, A]
    )(implicit G: Monad[G], lift: LiftValue[F, G])
        extends Builder[G, A] {

      def withUnit(unit: String): Builder[G, A] =
        builder.withUnit(unit).liftTo[G]

      def withDescription(description: String): Builder[G, A] =
        builder.withDescription(description).liftTo[G]

      def create: G[UpDownCounter[G, A]] =
        lift(builder.create).map(_.liftTo[G])
    }

  }

  sealed trait Backend[F[_], A] {
    def meta: InstrumentMeta[F]

    /** Records a value with a set of attributes.
      *
      * @param value
      *   the value to add to the counter
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def add(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit]

    /** Increments a counter by one.
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit]

    /** Decrements a counter by one.
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def dec(attributes: immutable.Iterable[Attribute[_]]): F[Unit]

    /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit G: Monad[G], lift: LiftValue[F, G]): Backend[G, A] =
      new Backend.Lifted[F, G, A](this)

  }

  object Backend {
    private[otel4s] trait Unsealed[F[_], A] extends Backend[F, A]

    private[otel4s] final class Lifted[F[_], G[_], A](
        backend: Backend[F, A]
    )(implicit G: Monad[G], lift: LiftValue[F, G])
        extends Backend[G, A] {

      def meta: InstrumentMeta[G] =
        backend.meta.liftTo[G]

      def add(value: A, attributes: immutable.Iterable[Attribute[_]]): G[Unit] =
        lift(backend.add(value, attributes))

      def inc(attributes: immutable.Iterable[Attribute[_]]): G[Unit] =
        lift(backend.inc(attributes))

      def dec(attributes: immutable.Iterable[Attribute[_]]): G[Unit] =
        lift(backend.dec(attributes))
    }

  }

  def noop[F[_], A](implicit F: Applicative[F]): UpDownCounter[F, A] =
    new UpDownCounter[F, A] {
      val backend: UpDownCounter.Backend[F, A] =
        new UpDownCounter.Backend[F, A] {
          val meta: InstrumentMeta[F] = InstrumentMeta.disabled
          def add(
              value: A,
              attributes: immutable.Iterable[Attribute[_]]
          ): F[Unit] = meta.unit
          def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
            meta.unit
          def dec(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
            meta.unit
        }
    }

  private[otel4s] def fromBackend[F[_], A](
      b: Backend[F, A]
  ): UpDownCounter[F, A] =
    new UpDownCounter[F, A] {
      def backend: Backend[F, A] = b
    }

}
