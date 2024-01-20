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
import cats.effect.kernel.Clock
import cats.effect.kernel.Resource
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.concurrent.duration.TimeUnit

/** A `Histogram` instrument that records values of type `A`.
  *
  * [[Histogram]] metric data points convey a population of recorded
  * measurements in a compressed format. A histogram bundles a set of events
  * into divided populations with an overall event count and aggregate sum for
  * all events.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record. OpenTelemetry specification expects `A`
  *   to be either [[scala.Long]] or [[scala.Double]].
  */
trait Histogram[F[_], A] extends HistogramMacro[F, A]

object Histogram {

  /** A builder of [[Histogram]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record. OpenTelemetry specification expects
    *   `A` to be either [[scala.Long]] or [[scala.Double]].
    */
  trait Builder[F[_], A] {

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

    /** Creates a [[Histogram]] with the given `unit`, `description`, and
      * `bucket boundaries` (if any).
      */
    def create: F[Histogram[F, A]]
  }

  trait Meta[F[_]] extends InstrumentMeta[F] {
    def resourceUnit: Resource[F, Unit]
  }

  object Meta {
    def enabled[F[_]: Applicative]: Meta[F] = make(enabled = true)
    def disabled[F[_]: Applicative]: Meta[F] = make(enabled = false)

    private def make[F[_]: Applicative](enabled: Boolean): Meta[F] =
      new Meta[F] {
        val isEnabled: Boolean = enabled
        val unit: F[Unit] = Applicative[F].unit
        val resourceUnit: Resource[F, Unit] = Resource.unit
      }
  }

  trait Backend[F[_], A] {
    def meta: Meta[F]

    /** Records a value with a set of attributes.
      *
      * @param value
      *   the value to record
      *
      * @param attributes
      *   the set of attributes to associate with the value
      */
    def record(value: A, attributes: Attribute[_]*): F[Unit]

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
      *   the set of attributes to associate with the value
      */
    def recordDuration(
        timeUnit: TimeUnit,
        attributes: Attribute[_]*
    ): Resource[F, Unit]

  }

  abstract class DoubleBackend[F[_]: Monad: Clock] extends Backend[F, Double] {

    final val unit: F[Unit] = Monad[F].unit

    final def recordDuration(
        timeUnit: TimeUnit,
        attributes: Attribute[_]*
    ): Resource[F, Unit] =
      Resource
        .makeCase(Clock[F].monotonic) { case (start, ec) =>
          for {
            end <- Clock[F].monotonic
            _ <- record(
              (end - start).toUnit(timeUnit),
              attributes ++ causeAttributes(ec): _*
            )
          } yield ()
        }
        .void

  }

  def noop[F[_], A](implicit F: Applicative[F]): Histogram[F, A] =
    new Histogram[F, A] {
      val backend: Backend[F, A] =
        new Backend[F, A] {
          val meta: Meta[F] = Meta.disabled
          def record(value: A, attributes: Attribute[_]*): F[Unit] = meta.unit
          def recordDuration(
              timeUnit: TimeUnit,
              attributes: Attribute[_]*
          ): Resource[F, Unit] = meta.resourceUnit
        }
    }

  private val CauseKey: AttributeKey[String] = AttributeKey.string("cause")

  def causeAttributes(ec: Resource.ExitCase): List[Attribute[String]] =
    ec match {
      case Resource.ExitCase.Succeeded =>
        Nil
      case Resource.ExitCase.Errored(e) =>
        List(Attribute(CauseKey, e.getClass.getName))
      case Resource.ExitCase.Canceled =>
        List(Attribute(CauseKey, "canceled"))
    }

}
