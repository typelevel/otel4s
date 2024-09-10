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
package context

trait Contextual[C] { outer =>

  /** The type of [[context.Key `Key`]] used by contexts of type `C`. */
  type Key[A] <: context.Key[A]

  /** Retrieves the value associated with the given key from the context, if such a value exists.
    */
  def get[A](ctx: C)(key: Key[A]): Option[A]

  /** Retrieves the value associated with the given key from the context, if such a value exists; otherwise, returns the
    * provided default value.
    */
  def getOrElse[A](ctx: C)(key: Key[A], default: => A): A =
    get(ctx)(key).getOrElse(default)

  /** Creates a copy of this context with the given value associated with the given key.
    */
  def updated[A](ctx: C)(key: Key[A], value: A): C

  /** The root context, from which all other contexts are derived. */
  def root: C

  class ContextSyntax(ctx: C) {

    /** Retrieves the value associated with the given key from the context, if such a value exists.
      */
    def get[A](key: Key[A]): Option[A] =
      outer.get(ctx)(key)

    /** Retrieves the value associated with the given key from the context, if such a value exists; otherwise, returns
      * the provided default value.
      */
    def getOrElse[A](key: Key[A], default: => A): A =
      outer.getOrElse(ctx)(key, default)

    /** Creates a copy of this context with the given value associated with the given key.
      */
    def updated[A](key: Key[A], value: A): C =
      outer.updated(ctx)(key, value)
  }
}

object Contextual {

  /** A type alias for a [[`Contextual`]] explicitly parameterized by its [[Contextual.Key `Key`]] type.
    */
  type Keyed[C, K[X] <: Key[X]] = Contextual[C] { type Key[A] = K[A] }

  /** Summons a [[`Contextual`]] that is available implicitly. */
  def apply[C](implicit c: Contextual[C]): Contextual[C] = c

  trait Syntax {
    implicit def toContextSyntax[C](ctx: C)(implicit
        c: Contextual[C]
    ): c.ContextSyntax =
      new c.ContextSyntax(ctx)
  }
}
