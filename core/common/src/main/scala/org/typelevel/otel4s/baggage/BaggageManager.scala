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

package org.typelevel.otel4s.baggage

import cats.Applicative
import cats.mtl.Local

/** A utility for accessing and modifying [[`Baggage`]].
  *
  * @example
  *   {{{
  * val baggageManager: BaggageManager[F] = ???
  *
  * val io =
  *   for {
  *     id <- BaggageManager[F].getValue("user_id")
  *     _  <- Console[F].println("user_id: " + id)
  *   } yield ()
  *
  * BaggageManager[F].modifiedScope(_.updated("user_id", "uid"))(io)
  *   }}}
  */
sealed trait BaggageManager[F[_]] {

  // TODO: remove after finishing migrating away from Local
  protected def applicative: Applicative[F]

  /** @return the current scope's `Baggage` */
  def current: F[Baggage]

  @deprecated("BaggageManager no longer extends Local. Use `current` instead", since = "0.13.0")
  final def ask[E2 >: Baggage]: F[E2] = applicative.widen(current)

  /** @return
    *   the [[Baggage.Entry entry]] to which the specified key is mapped, or `None` if the current `Baggage` contains no
    *   mapping for the key
    */
  def get(key: String): F[Option[Baggage.Entry]]

  /** @return
    *   the value (without [[Baggage.Metadata metadata]]) to which the specified key is mapped, or `None` if the current
    *   `Baggage` contains no mapping for the key
    */
  def getValue(key: String): F[Option[String]]

  /** Creates a new scope in which a modified version of the current `Baggage` is used. */
  def local[A](modify: Baggage => Baggage)(fa: F[A]): F[A]

  @deprecated("BaggageManager no longer extends Local. Reverse parameter list order", since = "0.13.0")
  final def local[A](fa: F[A])(modify: Baggage => Baggage): F[A] =
    local(modify)(fa)

  /** Creates a new scope in which the given `Baggage` is used. */
  def scope[A](baggage: Baggage)(fa: F[A]): F[A]

  @deprecated("BaggageManager no longer extends Local. Reverse parameter list order", since = "0.13.0")
  final def scope[A](fa: F[A])(baggage: Baggage): F[A] =
    scope(baggage)(fa)
}

object BaggageManager {
  private[otel4s] trait Unsealed[F[_]] extends BaggageManager[F]

  def apply[F[_]](implicit ev: BaggageManager[F]): BaggageManager[F] = ev

  @deprecated("BaggageManager no longer extends Local", since = "0.13.0")
  implicit def asExplicitLocal[F[_]](bm: BaggageManager[F]): Local[F, Baggage] =
    new Local[F, Baggage] {
      def applicative: Applicative[F] = bm.applicative
      def ask[E2 >: Baggage]: F[E2] = applicative.widen(bm.current)
      def local[A](fa: F[A])(f: Baggage => Baggage): F[A] =
        bm.local(f)(fa)
    }
}
