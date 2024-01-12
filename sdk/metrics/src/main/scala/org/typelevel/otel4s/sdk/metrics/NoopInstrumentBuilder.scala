package org.typelevel.otel4s.sdk.metrics

import cats.Functor
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.{
  Measurement,
  ObservableInstrumentBuilder,
  ObservableMeasurement,
  SyncInstrumentBuilder
}

object NoopInstrumentBuilder {

  def sync[F[_]: Functor: Console, A](
      name: String,
      noop: => A
  ): SyncInstrumentBuilder[F, A] =
    new SyncInstrumentBuilder[F, A] {
      type Self = this.type

      def withUnit(unit: String): Self = this

      def withDescription(description: String): Self = this

      def create: F[A] =
        warn(name).as(noop)
    }

  def observable[F[_]: Functor: Console, A, Instrument](
      name: String,
      noop: => Instrument
  ): ObservableInstrumentBuilder[F, A, Instrument] =
    new ObservableInstrumentBuilder[F, A, Instrument] {
      type Self = this.type

      def withUnit(unit: String): Self = this
      def withDescription(description: String): Self = this

      def createWithCallback(
          cb: ObservableMeasurement[F, A] => F[Unit]
      ): Resource[F, Instrument] =
        createNoop

      def create(
          measurements: F[List[Measurement[A]]]
      ): Resource[F, Instrument] =
        createNoop

      private def createNoop: Resource[F, Instrument] =
        Resource.eval(warn(name)).as(noop)
    }

  private def warn[F[_]: Console](name: String): F[Unit] =
    Console[F].error(
      s"Instrument name [$name] is invalid. Using noop instrument. Instrument names must consist of 255 or fewer characters including alphanumeric, _, ., -, and start with a letter."
    )

}
