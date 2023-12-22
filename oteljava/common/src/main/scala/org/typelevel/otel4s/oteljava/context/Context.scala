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
package oteljava.context

import cats.effect.Sync
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.context.ContextKey

/** A context wrapping a context from the Java open telemetry library (referred
  * to in further documentation as a "Java context").
  */
sealed trait Context {

  /** The underlying Java context. */
  def underlying: JContext

  /** Retrieves the value associated with the given key from the context, if
    * such a value exists.
    */
  def get[A](key: Context.Key[A]): Option[A]

  /** Retrieves the value associated with the given key from the context, if
    * such a value exists; otherwise, returns the provided default value.
    */
  final def getOrElse[A](key: Context.Key[A], default: => A): A =
    get(key).getOrElse(default)

  /** Creates a copy of this context with the given value associated with the
    * given key.
    */
  def updated[A](key: Context.Key[A], value: A): Context

  /** Maps the underlying Java context. */
  private[oteljava] def map(f: JContext => JContext): Context
}

object Context {
  private[oteljava] object Noop extends Context {
    val underlying: JContext =
      JSpan.getInvalid.storeInContext(root.underlying)
    def get[A](key: Context.Key[A]): Option[A] = None
    def updated[A](key: Context.Key[A], value: A): Context = this
    private[oteljava] def map(f: JContext => JContext): Context = this
  }

  private[oteljava] final case class Wrapped private[Context] (
      underlying: JContext
  ) extends Context {
    def get[A](key: Context.Key[A]): Option[A] =
      Option(underlying.get(key))
    def updated[A](key: Context.Key[A], value: A): Context =
      Wrapped(underlying.`with`(key, value))
    private[oteljava] def map(f: JContext => JContext): Context =
      wrap(f(underlying))
  }

  /** A key for use with a [[`Context`]] */
  final class Key[A] private (val name: String)
      extends context.Key[A]
      with ContextKey[A]

  object Key {

    /** Creates a unique key with the given (debug) name. */
    def unique[F[_]: Sync, A](name: String): F[Key[A]] =
      Sync[F].delay(new Key(name))

    implicit def provider[F[_]: Sync]: context.Key.Provider[F, Key] =
      new context.Key.Provider[F, Key] {
        def uniqueKey[A](name: String): F[Key[A]] = unique(name)
      }
  }

  /** Wraps a Java context as a [[`Context`]]. */
  def wrap(context: JContext): Context = {
    val isNoop =
      Option(JSpan.fromContextOrNull(context))
        .exists(!_.getSpanContext.isValid)
    if (isNoop) Noop else Wrapped(context)
  }

  /** The root [[`Context`]], from which all other contexts are derived. */
  val root: Context = wrap(JContext.root())

  implicit object Contextual extends context.Contextual[Context] {
    type Key[A] = Context.Key[A]

    def get[A](ctx: Context)(key: Key[A]): Option[A] =
      ctx.get(key)
    def updated[A](ctx: Context)(key: Key[A], value: A): Context =
      ctx.updated(key, value)
    def root: Context = Context.root
  }
}
