package com.rossabaker.otel4s

trait Attributes {
  def isEmpty = true
  def size = 0
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
  def add(value: A, attributes: Attributes): F[Unit]
}

trait ObservableCounter[F[_], A]

trait Histogram[F[_], A] {
  def record(value: A, attributes: Attributes): F[Unit]
}

trait ObservableGauge[F[_], A]

trait UpDownCounter[F[_], A] {
  def add(value: A, attributes: Attributes): F[Unit]
}

trait ObservableUpDownCounter[F, A]
