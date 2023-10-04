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

/** A key for a context. */
trait Key[A] {

  /** The debug name of the key. */
  val name: String

  override def toString: String = s"Key($name)"
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
}
