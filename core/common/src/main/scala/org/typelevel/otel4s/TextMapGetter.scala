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

import scala.collection.generic.{IsMap, IsSeq}

/** Offers a way to get a string value associated with a given key.
  *
  * A type class can be implemented for any data structure that stores key-value
  * pairs.
  *
  * Implicit instances of `TextMapGetter` are provided for [[scala.collection.Map]]
  * and [[scala.collection.Seq]] types. The behavior of
  * `TextMapGetter[Seq[(String, String)]]` when duplicate keys are present is
  * unspecified, and may change at any time. In particular, if the behavior of
  * `Seq` types with duplicate keys is ever specified by open telemetry, the
  * behavior of such implicit instances will be made to match the specification.
  *
  * @see
  *   See [[TextMapSetter]] to set a value to a mutable carrier
  * @see
  *   See [[TextMapUpdater]] to update values of an immutable carrier
  *
  * @tparam A
  *   the type of the key-value carrier
  */
@annotation.implicitNotFound("""
Could not find the `TextMapGetter` for ${A}. The `TextMapGetter` is available out-of-the-box for the `Map[String, String]` type.
""")
trait TextMapGetter[A] {

  /** Gets a string value from the `carrier` associated with the given `key`.
    *
    * @param carrier
    *   the carrier to get a value from
    *
    * @param key
    *   the key to get the value at
    *
    * @return
    *   a [[scala.Some]] if the `key` is present in `carrier` and [[scala.None]]
    *   otherwise
    */
  def get(carrier: A, key: String): Option[String]

  /** Returns a list of all available keys in the given `carrier`.
    *
    * @param carrier
    *   the carrier to get keys from
    *
    * @return
    *   an [[scala.collection.Iterable]] of all the keys in the carrier
    */
  def keys(carrier: A): Iterable[String]
}

object TextMapGetter {
  def apply[A](implicit getter: TextMapGetter[A]): TextMapGetter[A] = getter

  implicit def forMapLike[Repr](implicit
      conv: IsMap[Repr] { type K = String; type V = String }
  ): TextMapGetter[Repr] =
    new TextMapGetter[Repr] {
      override def get(carrier: Repr, key: String): Option[String] =
        conv(carrier).get(key)
      override def keys(carrier: Repr): Iterable[String] =
        conv(carrier).keys
    }

  implicit def forSeqLike[Repr](implicit
      conv: IsSeq[Repr] { type A = (String, String) }
  ): TextMapGetter[Repr] =
    new TextMapGetter[Repr] {
      override def get(carrier: Repr, key: String): Option[String] =
        conv(carrier).collectFirst { case (`key`, value) => value }
      override def keys(carrier: Repr): Iterable[String] =
        conv(carrier).view.map(_._1)
    }
}
