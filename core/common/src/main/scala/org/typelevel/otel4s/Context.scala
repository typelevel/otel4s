/*
 * Copyright 2022 Typelevel
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.typelevel.otel4s

import cats.Functor
import cats.effect.Sync

import org.typelevel.vault.{Key => VaultKey}
import org.typelevel.vault.Vault

sealed trait Context[F[_]] {
  def get[A](key: Context.Key[A]): Option[A]
  def set[A](key: Context.Key[A], value: A): Context[F]
}

object Context {
  sealed trait Key[A] {
    protected[Context] def vaultKey: VaultKey[A]
  }

  def empty[F[_]] =
    fromVault[F](Vault.empty)

  def newKey[F[_]: Sync, A]: F[Key[A]] =
    Functor[F].map(VaultKey.newKey[F, A])(vk =>
      new Key[A] { val vaultKey = vk }
    )

  private def fromVault[F[_]](vault: Vault): Context[F] =
    new Context[F] {
      def get[A](key: Key[A]): Option[A] =
        vault.lookup(key.vaultKey)
      def set[A](key: Key[A], value: A): Context[F] =
        fromVault(vault.insert(key.vaultKey, value))
    }

}
