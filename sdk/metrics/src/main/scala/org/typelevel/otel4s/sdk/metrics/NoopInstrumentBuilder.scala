package org.typelevel.otel4s.sdk.metrics

import cats.{Applicative, Functor}
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.{
  BucketBoundaries,
  Counter,
  Histogram,
  Measurement,
  ObservableCounter,
  ObservableGauge,
  ObservableMeasurement,
  ObservableUpDownCounter,
  UpDownCounter
}

object NoopInstrumentBuilder {

  def counter[F[_]: Applicative: Console, A](
      name: String
  ): Counter.Builder[F, A] =
    new Counter.Builder[F, A] {
      def withUnit(unit: String): Counter.Builder[F, A] = this
      def withDescription(description: String): Counter.Builder[F, A] = this
      def create: F[Counter[F, A]] = warn(name).as(Counter.noop)
    }

  def histogram[F[_]: Applicative: Console, A](
      name: String
  ): Histogram.Builder[F, A] =
    new Histogram.Builder[F, A] {
      def withUnit(unit: String): Histogram.Builder[F, A] = this
      def withDescription(description: String): Histogram.Builder[F, A] = this
      def withExplicitBucketBoundaries(
          boundaries: BucketBoundaries
      ): Histogram.Builder[F, A] = this
      def create: F[Histogram[F, A]] = warn(name).as(Histogram.noop)
    }

  def upDownCounter[F[_]: Applicative: Console, A](
      name: String
  ): UpDownCounter.Builder[F, A] =
    new UpDownCounter.Builder[F, A] {
      def withUnit(unit: String): UpDownCounter.Builder[F, A] = this
      def withDescription(description: String): UpDownCounter.Builder[F, A] =
        this
      def create: F[UpDownCounter[F, A]] = warn(name).as(UpDownCounter.noop)
    }

  def observableGauge[F[_]: Console, A](
      name: String
  ): ObservableGauge.Builder[F, A] =
    new ObservableGauge.Builder[F, A] {
      def withUnit(unit: String): ObservableGauge.Builder[F, A] = this
      def withDescription(description: String): ObservableGauge.Builder[F, A] =
        this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableGauge] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableGauge] =
        createNoop

      private def createNoop: Resource[F, ObservableGauge] =
        Resource.eval(warn(name)).as(new ObservableGauge {})
    }

  def observableCounter[F[_]: Console, A](
      name: String
  ): ObservableCounter.Builder[F, A] =
    new ObservableCounter.Builder[F, A] {
      def withUnit(unit: String): ObservableCounter.Builder[F, A] = this
      def withDescription(
          description: String
      ): ObservableCounter.Builder[F, A] = this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableCounter] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableCounter] =
        createNoop

      private def createNoop: Resource[F, ObservableCounter] =
        Resource.eval(warn(name)).as(new ObservableCounter {})
    }

  def observableUpDownCounter[F[_]: Console, A](
      name: String
  ): ObservableUpDownCounter.Builder[F, A] =
    new ObservableUpDownCounter.Builder[F, A] {
      def withUnit(unit: String): ObservableUpDownCounter.Builder[F, A] = this
      def withDescription(
          description: String
      ): ObservableUpDownCounter.Builder[F, A] = this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, ObservableUpDownCounter] =
        createNoop

      def create(
          measurements: F[Iterable[Measurement[A]]]
      ): Resource[F, ObservableUpDownCounter] =
        createNoop

      private def createNoop: Resource[F, ObservableUpDownCounter] =
        Resource.eval(warn(name)).as(new ObservableUpDownCounter {})
    }

  private def warn[F[_]: Console](name: String): F[Unit] =
    Console[F].error(
      s"Instrument name [$name] is invalid. Using noop instrument. Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter."
    )

}
