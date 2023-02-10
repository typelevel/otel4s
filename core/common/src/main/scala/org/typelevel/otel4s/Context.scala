package org.typelevel.otel4s

import cats.Functor
import cats.effect.kernel.Unique
import org.typelevel.vault

sealed trait Context {
  import Context._

  def get[A](key: Key[A]): Option[A]
  def set[A](key: Key[A], value: A): Context
}

object Context {
  sealed trait Key[A] {
    def name: String
    private[Context] def toVaultKey: vault.Key[A]
  }

  object Key {
    def create[F[_]: Functor: Unique, A](name: String): F[Key[A]] = {
      val name_ = name
      Functor[F].map(vault.Key.newKey[F, A])(vaultKey =>
        new Key[A] {
          def name = name_
          def toVaultKey = vaultKey
        }
      )
    }
  }
}
