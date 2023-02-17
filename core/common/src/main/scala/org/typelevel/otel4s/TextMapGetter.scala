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

trait TextMapGetter[A] {
  def get(carrier: A, key: String): Option[String]
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
