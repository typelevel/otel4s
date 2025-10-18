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
  * BaggageManager[F].local(_.updated("user_id", "uid"))(io)
  *   }}}
  */
sealed trait BaggageManager[F[_]] {

  /** @return the current scope's `Baggage` */
  def current: F[Baggage]

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

  /** Creates a new scope in which the given `Baggage` is used. */
  def scope[A](baggage: Baggage)(fa: F[A]): F[A]

}

object BaggageManager {
  private[otel4s] trait Unsealed[F[_]] extends BaggageManager[F]

  def apply[F[_]](implicit ev: BaggageManager[F]): BaggageManager[F] = ev

  /** Creates a no-op implementation of the [[BaggageManager]].
    *
    * [[BaggageManager.current]] returns an empty [[Baggage]].
    *
    * [[BaggageManager.scope]] and [[BaggageManager.local]] are no-ops.
    */
  def noop[F[_]: Applicative]: BaggageManager[F] =
    new Noop

  private final class Noop[F[_]: Applicative] extends BaggageManager[F] {
    val current: F[Baggage] = Applicative[F].pure(Baggage.empty)
    def get(key: String): F[Option[Baggage.Entry]] = Applicative[F].pure(None)
    def getValue(key: String): F[Option[String]] = Applicative[F].pure(None)
    def local[A](modify: Baggage => Baggage)(fa: F[A]): F[A] = fa
    def scope[A](baggage: Baggage)(fa: F[A]): F[A] = fa
    override def toString: String = "BaggageManager.Noop"
  }

}
