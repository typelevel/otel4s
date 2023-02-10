package org.typelevel.otel4s

trait Context {
  type Key[_]
  def get[A](key: Key[A]): Option[A]
  def set[A](key: Key[A], value: A): Context.Aux[Key]
}

object Context {
  type Aux[F[_]] = Context { type Key[A] = F[A] }
}
