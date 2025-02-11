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
  * BaggageManager[F].local(io)(_.updated("user_id", "uid"))
  *   }}}
  */
trait BaggageManager[F[_]] extends Local[F, Baggage] {

  /** @return the current `Baggage` */
  final def current: F[Baggage] = ask[Baggage]

  /** @return
    *   the [[Baggage.Entry entry]] to which the specified key is mapped, or `None` if the current `Baggage` contains no
    *   mapping for the key
    */
  def get(key: String): F[Option[Baggage.Entry]] =
    reader(_.get(key))

  /** @return
    *   the value (without [[Baggage.Metadata metadata]]) to which the specified key is mapped, or `None` if the current
    *   `Baggage` contains no mapping for the key
    */
  def getValue(key: String): F[Option[String]] =
    reader(_.get(key).map(_.value))
}

object BaggageManager {
  def apply[F[_]](implicit ev: BaggageManager[F]): BaggageManager[F] = ev
}
