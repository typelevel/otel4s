/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.context

import cats.effect.Sync

trait Context {
  def set[A](key: Context.Key[A], value: A): Context
  def get[A](key: Context.Key[A]): Option[A]
}

object Context {

  final class Key[A](val name: String) {
    override def toString: String = s"Key($name)"
  }

  object Key {
    def unique[F[_]: Sync, A](name: String): F[Key[A]] =
      Sync[F].delay(new Key(name))
  }

  private final class MapContext(storage: Map[Context.Key[_], _])
      extends Context {
    def set[A](key: Key[A], value: A): Context =
      new MapContext(storage.updated(key, value))

    def get[A](key: Key[A]): Option[A] =
      storage.get(key).map(_.asInstanceOf[A])
  }

  val root: Context = new MapContext(Map.empty)
}
