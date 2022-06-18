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

import cats.{Applicative, Show, Hash}
import cats.syntax.show._

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
      extends AttributeKey[A] {
    override final def toString: String = Show[AttributeKey[A]].show(this)
    override final def hashCode(): Int = Hash[AttributeKey[A]].hash(this)
    override final def equals(obj: Any): Boolean =
      obj match {
        case other: AttributeKey[A @unchecked] =>
          Hash[AttributeKey[A]].eqv(this, other)
        case _ =>
          false
      }
  }

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

  implicit def attributeKeyHash[A]: Hash[AttributeKey[A]] =
    Hash.by(key => (key.name, key.`type`))

  implicit def attributeKeyShow[A]: Show[AttributeKey[A]] =
    Show.show(key => show"${key.`type`}(${key.name})")
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

  implicit def attributeTypeHash[A]: Hash[AttributeType[A]] =
    Hash.fromUniversalHashCode

  implicit def attributeTypeShow[A]: Show[AttributeType[A]] =
    Show.fromToString
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
  def withVersion(version: String): MeterBuilder[F]
  def withSchemaUrl(schemaUrl: String): MeterBuilder[F]
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
  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]]
  // def observableCounter(name: String): ObservableInstrumentBuilder[F, ObservableCounter[F, Long]]

  def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Double]]

  // def observableGauge(name: String): ObservableInstrumentBuilder[F, ObservableGauge[F, Long]]

  // def upDownCounter(name: String): SyncInstrumentBuilder[F, UpDownCounter[F, Long]]
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
    }
}

trait SyncInstrumentBuilder[F[_], A] {
  type Self <: SyncInstrumentBuilder[F, A]

  def withUnit(unit: String): Self
  def withDescription(description: String): Self
  def create: F[A]
}

trait ObservableInstrumentBuilder[F[_], A] {
  type Self <: ObservableInstrumentBuilder[F, A]

  def withUnit(unit: String): Self
  def withDescription(description: String): Self
  def create: F[A]
}

trait Counter[F[_], A] {
  def add(value: A, attribute: Attribute[_]*): F[Unit]
}
object Counter {
  def noop[F[_], A](implicit F: Applicative[F]): Counter[F, A] =
    new Counter[F, A] {
      def add(value: A, attribute: Attribute[_]*) = F.unit
    }
}

trait ObservableCounter[F[_], A]

trait Histogram[F[_], A] {
  def record(value: A, attributes: Attribute[_]*): F[Unit]
}
object Histogram {
  def noop[F[_], A](implicit F: Applicative[F]): Histogram[F, A] =
    new Histogram[F, A] {
      def record(value: A, attribute: Attribute[_]*) = F.unit
    }
}

trait ObservableGauge[F[_], A]

trait UpDownCounter[F[_], A] {
  def add(value: A, attributes: Attribute[_]*): F[Unit]
}

trait ObservableUpDownCounter[F, A]
