package com.rossabaker.otel4s

trait Otel4s[F[_]] {
  def meterProvider: MeterProvider[F]
}

case class Attribute[A](key: AttributeKey[A], value: A)

sealed trait AttributeKey[A] {
  def name: String
  def `type`: AttributeType[A]
}
object AttributeKey {
  private class Impl[A](val name: String, val `type`: AttributeType[A]) extends AttributeKey[A]

  def string(name: String): AttributeKey[String] = new Impl(name, AttributeType.String)
  def boolean(name: String): AttributeKey[Boolean] = new Impl(name, AttributeType.Boolean)
  def long(name: String): AttributeKey[Long] = new Impl(name, AttributeType.Long)
  def double(name: String): AttributeKey[Double] = new Impl(name, AttributeType.Double)

  def stringList(name: String): AttributeKey[List[String]] = new Impl(name, AttributeType.StringList)
  def booleanList(name: String): AttributeKey[List[Boolean]] = new Impl(name, AttributeType.BooleanList)
  def longList(name: String): AttributeKey[List[Long]] = new Impl(name, AttributeType.LongList)
  def doubleList(name: String): AttributeKey[List[Double]] = new Impl(name, AttributeType.DoubleList)
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

trait MeterProvider[F[ _]] {
  def get(name: String): F[Meter[F]] =
    meter(name).get

  def meter(name: String): MeterBuilder[F]
}

trait MeterBuilder[F[_]] {
  def withVersion(version: String): MeterBuilder[F]
  def withSchemaUrl(schemaUrl: String): MeterBuilder[F]
  def get: F[Meter[F]]
}

trait Meter[F[_]] {
  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]]
  // def observableCounter(name: String): ObservableInstrumentBuilder[F, ObservableCounter[F, Long]]

  def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Double]]

  // def observableGauge(name: String): ObservableInstrumentBuilder[F, ObservableGauge[F, Long]]

  // def upDownCounter(name: String): SyncInstrumentBuilder[F, UpDownCounter[F, Long]]
  // def observableUpDownCounter(name: String): ObservableInstrumentBuilder[F, UpDownCounter[F, Long]]
}

trait SyncInstrumentBuilder[F[_], A] {
  type Self

  def withUnit(unit: String): Self
  def withDescription(description: String): Self
  def create: F[A]
}

trait ObservableInstrumentBuilder[F[_], A] {
  type Self

  def withUnit(unit: String): Self
  def withDescription(description: String): Self
  def create: F[A]
}

trait Counter[F[_], A] {
  def add(value: A, attribute: Attribute[_]*): F[Unit]
}

trait ObservableCounter[F[_], A]

trait Histogram[F[_], A] {
  def record(value: A, attributes: Attribute[_]*): F[Unit]
}

trait ObservableGauge[F[_], A]

trait UpDownCounter[F[_], A] {
  def add(value: A, attributes: Attribute[_]*): F[Unit]
}

trait ObservableUpDownCounter[F, A]
