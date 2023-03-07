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

/** Offers a way to get a string value associated with a given key.
  *
  * A type class can be implemented for any data structure that stores key-value
  * pairs.
  *
  * @see
  *   See [[TextMapSetter]] to set a value into the carrier
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
    *   a [[scala.List]] of all the keys in the carrier
    */
  def keys(carrier: A): List[String]
}

object TextMapGetter {
  implicit val forMapStringString: TextMapGetter[Map[String, String]] =
    new TextMapGetter[Map[String, String]] {
      def get(carrier: Map[String, String], key: String): Option[String] =
        carrier.get(key)
      def keys(carrier: Map[String, String]): List[String] =
        carrier.keys.toList
    }
}
