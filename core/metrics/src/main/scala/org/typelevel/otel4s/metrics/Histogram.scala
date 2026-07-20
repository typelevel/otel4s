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
import cats.effect.kernel.MonadCancel
import cats.effect.kernel.Resource
import cats.mtl.LiftValue
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.meta.InstrumentMeta

import scala.collection.immutable
import scala.concurrent.duration.TimeUnit

/** A `Histogram` instrument that records values of type `A`.
  *
  * [[Histogram]] metric data points convey a population of recorded measurements in a compressed format. A histogram
  * bundles a set of events into divided populations with an overall event count and aggregate sum for all events.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record. The type must have an instance of [[MeasurementValue]]. [[scala.Long]] and
  *   [[scala.Double]] are supported out of the box.
  */
sealed trait Histogram[F[_], A] extends HistogramMacro[F, A] {

  /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
    */
  def liftTo[G[_]](implicit F: MonadCancel[F, _], G: MonadCancel[G, _], lift: LiftValue[F, G]): Histogram[G, A] =
    Histogram.fromBackend(backend.liftTo[G])

}

object Histogram {
  private[otel4s] trait Unsealed[F[_], A] extends Histogram[F, A]

  /** A builder of [[Histogram]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record. The type must have an instance of [[MeasurementValue]]. [[scala.Long]] and
    *   [[scala.Double]] are supported out of the box.
    */
  sealed trait Builder[F[_], A] {

    /** Sets the unit of measure for this histogram.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit Instrument Unit]]
      *
      * @param unit
      *   the measurement unit. Must be 63 or fewer ASCII characters.
      */
    def withUnit(unit: String): Builder[F, A]

    /** Sets the description for this histogram.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description Instrument Description]]
      *
      * @param description
      *   the description to use
      */
    def withDescription(description: String): Builder[F, A]

    /** Sets the explicit bucket boundaries for this histogram.
      *
      * @see
      *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-advisory-parameter-explicitbucketboundaries Explicit bucket boundaries]]
      *
      * @param boundaries
      *   the boundaries to use
      */
    def withExplicitBucketBoundaries(
        boundaries: BucketBoundaries
    ): Builder[F, A]

    /** Creates a [[Histogram]] with the given `unit`, `description`, and `bucket boundaries` (if any).
      */
    def create: F[Histogram[F, A]]

    /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit F: MonadCancel[F, _], G: MonadCancel[G, _], lift: LiftValue[F, G]): Builder[G, A] =
      new Builder.Lifted[F, G, A](this)
  }

  object Builder {
    private[otel4s] trait Unsealed[F[_], A] extends Builder[F, A]

    private[otel4s] final class Lifted[F[_], G[_], A](
        builder: Builder[F, A]
    )(implicit F: MonadCancel[F, _], G: MonadCancel[G, _], lift: LiftValue[F, G])
        extends Builder[G, A] {

      def withUnit(unit: String): Builder[G, A] =
        builder.withUnit(unit).liftTo[G]

      def withDescription(description: String): Builder[G, A] =
        builder.withDescription(description).liftTo[G]

      def withExplicitBucketBoundaries(boundaries: BucketBoundaries): Builder[G, A] =
        builder.withExplicitBucketBoundaries(boundaries).liftTo[G]

      def create: G[Histogram[G, A]] =
        lift(builder.create).map(_.liftTo[G])
    }

  }

  sealed trait Backend[F[_], A] {
    def meta: InstrumentMeta[F]

    /** Records a value with a set of attributes.
      *
      * @param value
      *   the value to record
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def record(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit]

    /** Records duration of the given effect.
      *
      * @example
      *   {{{
      * val histogram: Histogram[F] = ???
      * val attributeKey = AttributeKey.string("query_name")
      *
      * def findUser(name: String) =
      *   histogram.recordDuration(TimeUnit.MILLISECONDS, Attribute(attributeKey, "find_user")).use { _ =>
      *     db.findUser(name)
      *   }
      *   }}}
      *
      * @param timeUnit
      *   the time unit of the duration measurement
      *
      * @param attributes
      *   the function to build set of attributes to associate with the value
      */
    def recordDuration(
        timeUnit: TimeUnit,
        attributes: Resource.ExitCase => immutable.Iterable[Attribute[_]]
    ): Resource[F, Unit]

    /** Modify the context `F` using an implicit [[cats.mtl.LiftValue]] from `F` to `G`.
      */
    def liftTo[G[_]](implicit F: MonadCancel[F, ?], G: MonadCancel[G, _], lift: LiftValue[F, G]): Backend[G, A] =
      new Backend.Lifted[F, G, A](this)
  }

  object Backend {
    private[otel4s] trait Unsealed[F[_], A] extends Backend[F, A]

    private[otel4s] final class Lifted[F[_], G[_], A](
        backend: Backend[F, A]
    )(implicit F: MonadCancel[F, ?], G: MonadCancel[G, _], lift: LiftValue[F, G])
        extends Backend[G, A] {

      def meta: InstrumentMeta[G] =
        backend.meta.liftTo[G]

      def record(value: A, attributes: immutable.Iterable[Attribute[_]]): G[Unit] =
        lift(backend.record(value, attributes))

      def recordDuration(
          timeUnit: TimeUnit,
          attributes: Resource.ExitCase => immutable.Iterable[Attribute[_]]
      ): Resource[G, Unit] =
        backend.recordDuration(timeUnit, attributes).mapK(lift)
    }

  }

  def noop[F[_], A](implicit F: Applicative[F]): Histogram[F, A] =
    new Histogram[F, A] {
      val backend: Backend[F, A] =
        new Backend[F, A] {
          val meta: InstrumentMeta[F] = InstrumentMeta.disabled
          def record(
              value: A,
              attributes: immutable.Iterable[Attribute[_]]
          ): F[Unit] = meta.unit
          def recordDuration(
              timeUnit: TimeUnit,
              attributes: Resource.ExitCase => immutable.Iterable[Attribute[_]]
          ): Resource[F, Unit] = Resource.unit
        }
    }

  private val CauseKey: AttributeKey[String] = AttributeKey("cause")

  def causeAttributes(ec: Resource.ExitCase): List[Attribute[String]] =
    ec match {
      case Resource.ExitCase.Succeeded =>
        Nil
      case Resource.ExitCase.Errored(e) =>
        List(Attribute(CauseKey, e.getClass.getName))
      case Resource.ExitCase.Canceled =>
        List(Attribute(CauseKey, "canceled"))
    }

  private[otel4s] def fromBackend[F[_], A](b: Backend[F, A]): Histogram[F, A] =
    new Histogram[F, A] {
      def backend: Backend[F, A] = b
    }

}
