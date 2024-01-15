package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.Monad
import java.util.concurrent.atomic.{DoubleAdder, LongAdder}

private trait Adder[F[_], A] {
  def addLong(a: Long): F[Unit]
  def addDouble(a: Double): F[Unit]
  def sum(reset: Boolean): F[A]
}

private object Adder {
  def makeLong[F[_]: Monad]: F[Adder[F, Long]] =
    Monad[F].pure(
      new Adder[F, Long] {
        private val adder = new LongAdder

        def addLong(a: Long): F[Unit] =
          Monad[F].pure(adder.add(a))

        def addDouble(a: Double): F[Unit] =
          Monad[F].unit

        def sum(reset: Boolean): F[Long] =
          Monad[F].pure(if (reset) adder.sumThenReset() else adder.sum())
      }
    )

  def makeDouble[F[_]: Monad]: F[Adder[F, Double]] =
    Monad[F].pure(
      new Adder[F, Double] {
        private val adder = new DoubleAdder

        def addLong(a: Long): F[Unit] =
          Monad[F].pure(adder.add(a))

        def addDouble(a: Double): F[Unit] =
          Monad[F].unit

        def sum(reset: Boolean): F[Double] =
          Monad[F].pure(if (reset) adder.sumThenReset() else adder.sum())
      }
    )
}
