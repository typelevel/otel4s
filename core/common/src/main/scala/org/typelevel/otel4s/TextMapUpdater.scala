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

/** Offers a way to store a string value associated with a given key to an
  * immutable carrier type.
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
