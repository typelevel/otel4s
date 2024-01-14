package org.typelevel.otel4s.sdk.metrics.internal.aggregation

private trait Adder[F[_], A] {
  def addLong(a: Long): F[Unit]
  def addDouble(a: Double): F[Unit]
  def sum(reset: Boolean): F[A]
}

private object Adder {
  def makeLong[F[_]]: F[Adder[F, Long]] = ???
  def makeDouble[F[_]]: F[Adder[F, Double]] = ???
}
