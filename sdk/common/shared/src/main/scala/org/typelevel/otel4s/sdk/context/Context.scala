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

package org.typelevel.otel4s
package sdk.context

import cats.Hash
import cats.Show
import cats.effect.kernel.Unique
import org.typelevel.scalaccompat.annotation.threadUnsafe3

/** A type-safe immutable storage.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/context]]
  */
sealed trait Context {

  /** Retrieves the value associated with the given `key` from the context, if such a value exists.
    */
  def get[A](key: Context.Key[A]): Option[A]

  /** Retrieves the value associated with the given key from the context, if such a value exists; otherwise, returns the
    * provided default value.
    */
  def getOrElse[A](key: Context.Key[A], default: => A): A =
    get(key).getOrElse(default)

  /** Creates a copy of this context with the given `value` associated with the given `key`.
    */
  def updated[A](key: Context.Key[A], value: A): Context

  override final def toString: String = Show[Context].show(this)
}

object Context {
  private val Empty: Context = new MapContext(Map.empty)

  /** A key for use with a [[Context]].
    *
    * @param name
    *   the name of the key
    *
    * @tparam A
    *   the type of the value that can be associated with this key
    */
  final class Key[A] private (
      val name: String,
      private[context] val unique: Unique.Token
  ) extends context.Key.Unsealed[A] {

    @threadUnsafe3
    override lazy val hashCode: Int = Hash[Key[A]].hash(this)

    override def equals(obj: Any): Boolean =
      obj match {
        case other: Key[A @unchecked] => Hash[Key[A]].eqv(this, other)
        case _                        => false
      }

    override def toString: String = Show[Key[A]].show(this)
  }

  object Key {

    /** Creates a unique key with the given '''debug''' name.
      *
      * '''Keys may have the same debug name but they aren't equal:'''
      * {{{
      *   for {
      *     key1 <- Key.unique[IO, Int]("key")
      *     key2 <- Key.unique[IO, Int]("key")
      *   } yield key1 == key2 // false
      * }}}
      *
      * @param name
      *   the '''debug''' name of the key
      *
      * @tparam A
      *   the type of the value that can be associated with this key
      */
    def unique[F[_]: Unique, A](name: String): F[Key[A]] =
      Unique[F].applicative.map(Unique[F].unique)(u => new Key(name, u))

    implicit def keyHash[A]: Hash[Key[A]] = Hash.by(_.unique)

    implicit def keyShow[A]: Show[Key[A]] = Show(k => s"Key(${k.name})")

    implicit def keyProvider[F[_]: Unique]: context.Key.Provider[F, Key] =
      new context.Key.Provider.Unsealed[F, Key] {
        def uniqueKey[A](name: String): F[Key[A]] = unique(name)
      }
  }

  /** The empty [[Context]].
    */
  def root: Context = Empty

  implicit val contextShow: Show[Context] = Show { case ctx: MapContext =>
    ctx.storage
      .map { case (key, value) => s"${key.name}=$value" }
      .mkString("Context{", ", ", "}")
  }

  implicit object Contextual extends context.Contextual.Unsealed[Context] {
    type Key[A] = Context.Key[A]

    def get[A](ctx: Context)(key: Key[A]): Option[A] =
      ctx.get(key)

    def updated[A](ctx: Context)(key: Key[A], value: A): Context =
      ctx.updated(key, value)

    def root: Context = Context.root
  }

  private[context] final class MapContext(
      private[context] val storage: Map[Context.Key[_], Any]
  ) extends Context {
    def get[A](key: Key[A]): Option[A] =
      storage.get(key).map(_.asInstanceOf[A])

    def updated[A](key: Key[A], value: A): Context =
      new MapContext(storage.updated(key, value))
  }
}
