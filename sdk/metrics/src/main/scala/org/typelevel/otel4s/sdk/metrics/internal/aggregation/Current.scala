package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.effect.Concurrent
import cats.syntax.functor._

private trait Current[F[_], A] {
  def setLong(a: Long): F[Unit]
  def setDouble(a: Double): F[Unit]
  def get(reset: Boolean): F[Option[A]]
}

private object Current {
  def makeLong[F[_]: Concurrent]: F[Current[F, Long]] =
    Concurrent[F].ref(Option.empty[Long]).map { ref =>
      new Current[F, Long] {
        def setLong(a: Long): F[Unit] =
          ref.set(Some(a))

        def setDouble(a: Double): F[Unit] =
          Concurrent[F].unit

        def get(reset: Boolean): F[Option[Long]] =
          if (reset) ref.getAndSet(None) else ref.get
      }
    }

  def makeDouble[F[_]: Concurrent]: F[Current[F, Double]] =
    Concurrent[F].ref(Option.empty[Double]).map { ref =>
      new Current[F, Double] {
        def setLong(a: Long): F[Unit] =
          Concurrent[F].unit

        def setDouble(a: Double): F[Unit] =
          ref.set(Some(a))

        def get(reset: Boolean): F[Option[Double]] =
          if (reset) ref.getAndSet(None) else ref.get
      }
    }
}
