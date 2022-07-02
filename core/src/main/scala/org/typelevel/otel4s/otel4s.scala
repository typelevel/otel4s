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

import cats.{Applicative, Monad}
import cats.effect.{Clock, Resource}
import cats.syntax.flatMap._
import cats.syntax.functor._

import scala.concurrent.duration.TimeUnit

trait Otel4s[F[_]] {
  def meterProvider: MeterProvider[F]
}

case class Attribute[A](key: AttributeKey[A], value: A)

sealed trait AttributeKey[A] {
  def name: String
  def `type`: AttributeType[A]
}
object AttributeKey {
  private class Impl[A](val name: String, val `type`: AttributeType[A])
      extends AttributeKey[A]

  def string(name: String): AttributeKey[String] =
    new Impl(name, AttributeType.String)
  def boolean(name: String): AttributeKey[Boolean] =
    new Impl(name, AttributeType.Boolean)
  def long(name: String): AttributeKey[Long] =
    new Impl(name, AttributeType.Long)
  def double(name: String): AttributeKey[Double] =
    new Impl(name, AttributeType.Double)

  def stringList(name: String): AttributeKey[List[String]] =
    new Impl(name, AttributeType.StringList)
  def booleanList(name: String): AttributeKey[List[Boolean]] =
    new Impl(name, AttributeType.BooleanList)
  def longList(name: String): AttributeKey[List[Long]] =
    new Impl(name, AttributeType.LongList)
  def doubleList(name: String): AttributeKey[List[Double]] =
    new Impl(name, AttributeType.DoubleList)
}

sealed trait AttributeType[A]
object AttributeType {
  case object Boolean extends AttributeType[Boolean]
  case object Double extends AttributeType[Double]
  case object String extends AttributeType[String]
  case object Long extends AttributeType[Long]

  case object BooleanList extends AttributeType[List[Boolean]]
  case object DoubleList extends AttributeType[List[Double]]
  case object StringList extends AttributeType[List[String]]
  case object LongList extends AttributeType[List[Long]]
}

trait MeterProvider[F[_]] {
  def get(name: String): F[Meter[F]] =
    meter(name).get

  def meter(name: String): MeterBuilder[F]
}

object MeterProvider {
  def noop[F[_]: Applicative]: MeterProvider[F] =
    new MeterProvider[F] {
      def meter(name: String): MeterBuilder[F] =
        MeterBuilder.noop
    }
}

trait MeterBuilder[F[_]] {

  /** Assigns a version to the resulting Meter.
    *
    * @param version
    *   the version of the instrumentation scope
    */
  def withVersion(version: String): MeterBuilder[F]

  /** Assigns an OpenTelemetry schema URL to the resulting Meter.
    *
    * @param schemaUrl
    *   the URL of the OpenTelemetry schema
    */
  def withSchemaUrl(schemaUrl: String): MeterBuilder[F]

  /** Creates a [[Meter]] with the given `version` and `schemaUrl` (if any)
    */
  def get: F[Meter[F]]
}

object MeterBuilder {
  def noop[F[_]](implicit F: Applicative[F]): MeterBuilder[F] =
    new MeterBuilder[F] {
      def withVersion(version: String) = this
      def withSchemaUrl(schemaUrl: String) = this
      def get = F.pure(Meter.noop)
    }
}

trait Meter[F[_]] {

  /** Creates a builder of [[Counter]] instrument that records [[scala.Long]]
    * values.
    *
    * @param name
    *   the name of the instrument
    */
  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]]
  // def observableCounter(name: String): ObservableInstrumentBuilder[F, ObservableCounter[F, Long]]

  /** Creates a builder of [[Histogram]] instrument that records
    * [[scala.Double]] values.
    *
    * @param name
    *   the name of the instrument
    */
  def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Double]]

  // def observableGauge(name: String): ObservableInstrumentBuilder[F, ObservableGauge[F, Long]]

  /** Creates a builder of [[UpDownCounter]] instrument that records
    * [[scala.Long]] values.
    *
    * @param name
    *   the name of the instrument
    */
  def upDownCounter(
      name: String
  ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]]
  // def observableUpDownCounter(name: String): ObservableInstrumentBuilder[F, UpDownCounter[F, Long]]
}

object Meter {
  def noop[F[_]](implicit F: Applicative[F]): Meter[F] =
    new Meter[F] {
      def counter(name: String) =
        new SyncInstrumentBuilder[F, Counter[F, Long]] {
          type Self = this.type
          def withUnit(unit: String) = this
          def withDescription(description: String) = this
          def create = F.pure(Counter.noop)
        }

      def histogram(name: String) =
        new SyncInstrumentBuilder[F, Histogram[F, Double]] {
          type Self = this.type
          def withUnit(unit: String) = this
          def withDescription(description: String) = this
          def create = F.pure(Histogram.noop)
        }

      def upDownCounter(
          name: String
      ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]] =
        new SyncInstrumentBuilder[F, UpDownCounter[F, Long]] {
          type Self = this.type

          def withUnit(unit: String) = this
          def withDescription(description: String) = this
          def create = F.pure(UpDownCounter.noop)
        }
    }
}

trait SyncInstrumentBuilder[F[_], A] {
  type Self <: SyncInstrumentBuilder[F, A]

  /** Sets the unit of measure for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
    *   Unit</a>
    *
    * @param unit
    *   the measurement unit. Must be 63 or fewer ASCII characters.
    */
  def withUnit(unit: String): Self

  /** Sets the description for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
    *   Description</a>
    *
    * @param description
    *   the description
    */
  def withDescription(description: String): Self

  /** Creates an instrument with the given `unit` and `description` (if any).
    */
  def create: F[A]
}

trait ObservableInstrumentBuilder[F[_], A] {
  type Self <: ObservableInstrumentBuilder[F, A]

  /** Sets the unit of measure for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-unit">Instrument
    *   Unit</a>
    *
    * @param unit
    *   the measurement unit. Must be 63 or fewer ASCII characters.
    */
  def withUnit(unit: String): Self

  /** Sets the description for this instrument.
    *
    * @see
    *   <a
    *   href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/metrics/api.md#instrument-description">Instrument
    *   Description</a>
    *
    * @param description
    *   the description
    */
  def withDescription(description: String): Self

  /** Creates an instrument with the given `unit` and `description` (if any).
    */
  def create: F[A]
}

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
  * @tparam A
  *   the type of the values to record. OpenTelemetry specification expects `A`
  *   to be either [[scala.Long]] or [[scala.Double]]
  */
trait Counter[F[_], A] extends CounterMacro[F, A]

object Counter {

  trait Backend[F[_], A] extends InstrumentBackend[F] {

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

  abstract class LongBackend[F[_]: Applicative] extends Backend[F, Long] {
    final val unit: F[Unit] = Applicative[F].unit

    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1L, attributes: _*)
  }

  abstract class DoubleBackend[F[_]: Applicative] extends Backend[F, Double] {
    final val unit: F[Unit] = Applicative[F].unit

    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1.0, attributes: _*)
  }

  def noop[F[_], A](implicit F: Applicative[F]): Counter[F, A] =
    new Counter[F, A] {
      val backend: Backend[F, A] =
        new Backend[F, A] {
          val isEnabled: Boolean = false
          val unit: F[Unit] = F.unit
          def add(value: A, attributes: Attribute[_]*): F[Unit] = unit
          def inc(attributes: Attribute[_]*): F[Unit] = unit
        }
    }

}

trait ObservableCounter[F[_], A]

/** A `Histogram` instrument that records values of type `A`.
  *
  * [[Histogram]] metric data points convey a population of recorded
  * measurements in a compressed format. A histogram bundles a set of events
  * into divided populations with an overall event count and aggregate sum for
  * all events.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  * @tparam A
  *   the type of the values to record. OpenTelemetry specification expects `A`
  *   to be either [[scala.Long]] or [[scala.Double]].
  */
trait Histogram[F[_], A] extends HistogramMacro[F, A]

object Histogram {

  trait Backend[F[_], A] extends InstrumentBackend[F] {
    final val resourceUnit: Resource[F, Unit] = Resource.unit

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
      * @example {{{
      * val histogram: Histogram[F] = ???
      * val attributeKey = AttributeKey.string("query_name")
      *
      * def findUser(name: String) =
      *  histogram.recordDuration(TimeUnit.MILLISECONDS, Attribute(attributeKey, "find_user")).use { _ =>
      *    db.findUser(name)
      *  }
      * }}}
      * @param timeUnit
      *   the time unit. Must match
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
          val isEnabled: Boolean = false
          val unit: F[Unit] = F.unit
          def record(value: A, attributes: Attribute[_]*): F[Unit] = unit
          def recordDuration(
              timeUnit: TimeUnit,
              attributes: Attribute[_]*
          ): Resource[F, Unit] = resourceUnit
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

trait ObservableGauge[F[_], A]

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
trait UpDownCounter[F[_], A] extends UpDownCounterMacro[F, A]

object UpDownCounter {

  trait Backend[F[_], A] extends InstrumentBackend[F] {

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

  abstract class LongBackend[F[_]: Applicative] extends Backend[F, Long] {
    final val unit: F[Unit] = Applicative[F].unit

    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1L, attributes: _*)

    final def dec(attributes: Attribute[_]*): F[Unit] =
      add(-1L, attributes: _*)
  }

  abstract class DoubleBackend[F[_]: Applicative] extends Backend[F, Double] {
    val unit: F[Unit] = Applicative[F].unit

    final def inc(attributes: Attribute[_]*): F[Unit] =
      add(1.0, attributes: _*)

    final def dec(attributes: Attribute[_]*): F[Unit] =
      add(1.0, attributes: _*)
  }

  def noop[F[_], A](implicit F: Applicative[F]): UpDownCounter[F, A] =
    new UpDownCounter[F, A] {
      val backend: UpDownCounter.Backend[F, A] =
        new UpDownCounter.Backend[F, A] {
          val unit: F[Unit] = F.unit
          val isEnabled: Boolean = false
          def add(value: A, attributes: Attribute[_]*): F[Unit] = unit
          def inc(attributes: Attribute[_]*): F[Unit] = unit
          def dec(attributes: Attribute[_]*): F[Unit] = unit
        }
    }

}

trait ObservableUpDownCounter[F, A]
