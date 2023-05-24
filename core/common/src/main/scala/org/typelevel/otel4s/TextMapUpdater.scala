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

import cats.Invariant

import scala.collection.immutable.MapOps
import scala.collection.immutable.SeqOps
import scala.collection.immutable.SortedMapOps

/** Offers a way to store a string value associated with a given key to an
  * immutable carrier type.
  *
  * Implicit instances of `TextMapUpdater` are provided for
  * [[scala.collection.immutable.Map]] and [[scala.collection.immutable.Seq]]
  * types.The behavior of `TextMapUpdater[Seq[(String, String)]]` when duplicate
  * keys are present is unspecified, and may change at any time. In particular,
  * if the behavior of `Seq` types with duplicate keys is ever specified by open
  * telemetry, the behavior of such implicit instances will be made to match the
  * specification.
  *
  * @see
  *   See [[TextMapGetter]] to get a value from the carrier
  * @see
  *   See [[TextMapSetter]] to set values to a mutable carrier
  *
  * @tparam A
  *   the type of the carrier
  */
trait TextMapUpdater[A] {

  /** Updates a carrier with the given `key` associated with the given `value`.
    * The original `carrier` is unmodified.
    *
    * '''Important:''' the carrier must to be '''immutable'''.
    *
    * @param carrier
    *   the carrier to update with a key-value pair
    *
    * @param key
    *   the key to associate the value with
    *
    * @param value
    *   the value to associate with the key
    */
  def updated(carrier: A, key: String, value: String): A
}

object TextMapUpdater {
  def apply[A](implicit updater: TextMapUpdater[A]): TextMapUpdater[A] = updater

  implicit def forMap[CC[x, +y] <: MapOps[x, y, CC, CC[x, y]]]
      : TextMapUpdater[CC[String, String]] =
    (carrier: CC[String, String], key: String, value: String) =>
      carrier.updated(key, value)

  implicit def forSortedMap[
      CC[x, +y] <: Map[x, y] with SortedMapOps[x, y, CC, CC[x, y]]
  ]: TextMapUpdater[CC[String, String]] =
    (carrier: CC[String, String], key: String, value: String) =>
      carrier.updated(key, value)

  implicit def forSeq[CC[x] <: SeqOps[x, CC, CC[x]]]
      : TextMapUpdater[CC[(String, String)]] =
    (carrier: CC[(String, String)], key: String, value: String) =>
      carrier.appended(key -> value)

  implicit val invariant: Invariant[TextMapUpdater] =
    new Invariant[TextMapUpdater] {
      override def imap[A, B](
          fa: TextMapUpdater[A]
      )(f: A => B)(g: B => A): TextMapUpdater[B] =
        (carrier: B, key: String, value: String) =>
          f(fa.updated(g(carrier), key, value))
    }
}
