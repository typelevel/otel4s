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

trait TextMapPropagator[F[_]] extends TextMapInjector[F] with TextMapExtractor {
  type Key[_]
}

trait TextMapInjector[F[_]] {
  type Key[_]

  def inject[A](context: Context.Aux[Key], carrier: A)(implicit
      setter: TextMapSetter[F, A]
  ): F[A]
}

trait TextMapExtractor {
  type Key[_]

  def extract[A](context: Context.Aux[Key], carrier: A)(implicit
      getter: TextMapGetter[A]
  ): Context
}

trait TextMapCarrier[F[_], A] extends TextMapGetter[A] with TextMapSetter[F, A]

trait TextMapGetter[A] {
  def keys(carrier: A): List[String]
  def get(carrier: A, key: String): Option[A]
}

trait TextMapSetter[F[_], A] {
  def set(carrier: A, key: String, value: String): F[A]
}
