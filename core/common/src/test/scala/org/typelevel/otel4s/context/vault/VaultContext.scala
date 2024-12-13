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
package context
package vault

import cats.Functor
import cats.effect.Unique
import cats.syntax.functor._
import org.typelevel.vault.{Key => VaultKey}
import org.typelevel.vault.Vault

/** A context implementation backed by a [[org.typelevel.vault.Vault `Vault`]].
  */
final class VaultContext private (vault: Vault) {

  /** Retrieves the value associated with the given key from the context, if such a value exists.
    */
  def get[A](key: VaultContext.Key[A]): Option[A] =
    vault.lookup(key.underlying)

  /** Retrieves the value associated with the given key from the context, if such a value exists; otherwise, returns the
    * provided default value.
    */
  def getOrElse[A](key: VaultContext.Key[A], default: => A): A =
    get(key).getOrElse(default)

  /** Creates a copy of this context with the given value associated with the given key.
    */
  def updated[A](key: VaultContext.Key[A], value: A): VaultContext =
    new VaultContext(vault.insert(key.underlying, value))
}

object VaultContext {

  /** A key for use with a [[`VaultContext`]]. */
  final class Key[A] private (
      val name: String,
      private[VaultContext] val underlying: VaultKey[A]
  ) extends context.Key[A]

  object Key {

    /** Creates a unique key with the given (debug) name. */
    def unique[F[_]: Functor: Unique, A](name: String): F[Key[A]] =
      VaultKey.newKey[F, A].map(new Key[A](name, _))

    implicit def provider[F[_]: Functor: Unique]: context.Key.Provider[F, Key] =
      new context.Key.Provider[F, Key] {
        def uniqueKey[A](name: String): F[Key[A]] = unique(name)
      }
  }

  /** The root [[`VaultContext`]], from which all other contexts are derived. */
  val root: VaultContext = new VaultContext(Vault.empty)

  implicit object Contextual extends context.Contextual[VaultContext] {
    type Key[A] = VaultContext.Key[A]

    def get[A](ctx: VaultContext)(key: Key[A]): Option[A] =
      ctx.get(key)
    def updated[A](ctx: VaultContext)(key: Key[A], value: A): VaultContext =
      ctx.updated(key, value)
    def root: VaultContext = VaultContext.root
  }
}
