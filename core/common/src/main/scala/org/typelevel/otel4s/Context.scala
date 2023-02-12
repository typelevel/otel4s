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

import org.typelevel.vault.Key
import org.typelevel.vault.Vault

sealed trait Context[F[_]] {
  def get[A](key: Key[A]): Option[A]
  def set[A](key: Key[A], value: A): Context[F]
}

object Context {
  type Key[A] = org.typelevel.vault.Key[A]
  val Key = org.typelevel.vault.Key

  def empty[F[_]] =
    fromVault[F](Vault.empty)

  private def fromVault[F[_]](vault: Vault): Context[F] =
    new Context[F] {
      def get[A](key: Key[A]): Option[A] =
        vault.lookup(key)
      def set[A](key: Key[A], value: A): Context[F] =
        fromVault(vault.insert(key, value))
    }

}
