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

/** Offers a way to store a string value associated with a given key.
  *
  * A trait that defines a method to set a value in a key-value store.
  *
  * @see
  *   See [[TextMapGetter]] to get a value from the carrier
  *
  * @tparam A
  *   the type of the carrier
  */
trait TextMapSetter[A] {

  /** Sets the `value` associated with the given `key` in the `carrier`.
    *
    * '''Important:''' the carrier must to be '''mutable'''.
    *
    * @param carrier
    *   the carrier to store the key-value pair at
    *
    * @param key
    *   the key to associated the value with
    *
    * @param value
    *   the value to set
    */
  def unsafeSet(carrier: A, key: String, value: String): Unit
}
