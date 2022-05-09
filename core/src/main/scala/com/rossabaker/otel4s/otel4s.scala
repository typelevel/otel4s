package com.rossabaker.otel4s

trait Otel4s[F[_]] {
  def meterProvider: MeterProvider[F]

  // Preallocated keys for efficiency
  def stringKey(name: String): AttributeKey[String]
  def longKey(name: String): AttributeKey[Long]
  def doubleKey(name: String): AttributeKey[Double]
  def booleanKey(name: String): AttributeKey[Boolean]
  def stringListKey(name: String): AttributeKey[List[String]]
  def longListKey(name: String): AttributeKey[List[Long]]
  // def doubleListKey(name: String): AttributeKey[List[Double]]
  // def booleanListKey(name: String): AttributeKey[List[Boolean]]
}

case class Attribute[A](key: AttributeKey[A], value: A)

trait AttributeKey[A] {
  def name: String
  def `type`: AttributeType[A]
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

  // def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Long]]

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
