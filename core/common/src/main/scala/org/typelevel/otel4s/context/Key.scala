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

package org.typelevel.otel4s.context

import cats.Hash
import cats.Show

/** A key for a context.
  *
  * @note
  *   Implementations MUST treat different instances as non-equal.
  */
trait Key[A] {

  /** The debug name of the key. */
  val name: String

  override def toString: String = Key.defaultShowKey(this)
}

object Key {

  /** Something that provides context keys.
    *
    * @tparam K
    *   the type of keys
    */
  trait Provider[F[_], K[X] <: Key[X]] {

    /** Creates a unique key with the given (debug) name. */
    def uniqueKey[A](name: String): F[K[A]]
  }

  object Provider {

    /** Summons a [[`Provider`]] that is available implicitly. */
    def apply[F[_], K[X] <: Key[X]](implicit
        p: Provider[F, K]
    ): Provider[F, K] = p
  }

  @inline private def defaultShowKey(key: Key[_]): String =
    s"Key(${key.name})"

  implicit def showKey[K[X] <: Key[X], A]: Show[K[A]] =
    defaultShowKey(_)

  implicit def hashKey[K[X] <: Key[X], A]: Hash[K[A]] =
    new Hash[K[A]] {
      def hash(x: K[A]): Int = System.identityHashCode(x)
      def eqv(x: K[A], y: K[A]): Boolean = x eq y
    }
}
