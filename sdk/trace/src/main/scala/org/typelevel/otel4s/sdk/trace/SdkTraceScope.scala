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

package org.typelevel.otel4s.sdk.trace

import cats.effect.SyncIO
import cats.mtl.Local
import cats.~>
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.Context.Key
import org.typelevel.otel4s.trace.SpanContext

private[trace] trait SdkTraceScope[F[_]] {

  /** Returns a [[SpanContext]] if it's available in the current scope.
    */
  def current: F[Option[SpanContext]]

  /** Creates a new scope using the given `spanContext` context if the
    * requirement are met.
    *
    * The propagation logic is based on the [[SpanContext]], that may be present
    * in the [[Context]]:
    *
    *   - the [[SpanContext]] is missing -> we use the given `spanContext`
    *
    *   - the [[SpanContext]] is valid -> we use the given `spanContext`
    *
    *   - the [[SpanContext]] is invalid -> we use [[SpanContext.invalid]]
    *
    * @param spanContext
    *   the span context to use
    */
  def makeScope(spanContext: SpanContext): F[F ~> F]

  /** Creates a root scope. The difference with the [[makeScope]] is that we
    * override the whole [[Context]], rather then only a [[SpanContext]] within
    * the context.
    *
    * The propagation logic is based on the [[SpanContext]], that may be present
    * in the [[Context]]:
    *
    *   - the [[SpanContext]] is missing -> the scope is already root, so we
    *     keep the context as is
    *
    *   - the [[SpanContext]] is valid -> there is a valid span, we forcefully
    *     use [[Context.root]]
    *
    *   - the [[SpanContext]] is invalid -> the current propagation strategy is
    *     no-op, so we keep the context as is
    */
  def rootScope: F[F ~> F]

  /** Creates a no-op scope.
    *
    * No-op scope means the tracing operations are no-op and the spans created
    * within this scope will not be exported anywhere.
    *
    * We use [[SpanContext.invalid]] as a mark the segment.
    *
    * The propagation logic is based on the [[SpanContext]], that may be present
    * in the [[Context]]:
    *
    *   - the [[SpanContext]] is missing -> we use [[SpanContext.invalid]]
    *
    *   - the [[SpanContext]] is valid -> we use [[SpanContext.invalid]]
    *
    *   - the [[SpanContext]] is invalid -> we use [[SpanContext.invalid]]
    */
  def noopScope: F ~> F

  def withExplicitContext(context: Context): F ~> F
  def reader[A](f: Context => A): F[A]
}

private[trace] object SdkTraceScope {
  private val SpanContextKey =
    Key
      .unique[SyncIO, SpanContext]("otel4s-trace-span-context-key")
      .unsafeRunSync()

  def fromContext(context: Context): Option[SpanContext] =
    context.get(SpanContextKey)

  def storeInContext(
      context: Context,
      spanContext: SpanContext
  ): Context =
    context.updated(SpanContextKey, spanContext)

  def fromLocal[F[_]](implicit L: Local[F, Context]): SdkTraceScope[F] =
    new SdkTraceScope[F] {
      def current: F[Option[SpanContext]] =
        L.reader(_.get(SpanContextKey))

      def makeScope(span: SpanContext): F[F ~> F] =
        L.applicative.map(current) { context =>
          createScope(nextScope(context, span))
        }

      def rootScope: F[F ~> F] =
        L.reader { context =>
          val ctx = fromContext(context) match {
            // the SpanContext exist and it's invalid.
            // It means, the propagation strategy is noop and we should keep the current context
            case Some(ctx) if !ctx.isValid => context
            // the SpanContext exist and it's valid, hence we start with the fresh one
            case Some(_) => Context.root
            // there is no existing SpanContext, we can continue using the context
            case None => context
          }

          withExplicitContext(ctx)
        }

      def noopScope: F ~> F =
        createScope(SpanContext.invalid)

      def withExplicitContext(context: Context): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.scope(fa)(context)
        }

      def reader[A](f: Context => A): F[A] =
        L.reader(f)

      private def createScope(spanContext: SpanContext): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.local(fa)(context => storeInContext(context, spanContext))
        }

      // the context propagation logic
      private def nextScope(
          current: Option[SpanContext],
          next: SpanContext
      ): SpanContext =
        current match {
          // the current span context is valid, so we can switch to the next one
          case Some(value) if value.isValid => next
          // the current span context is invalid, so we cannot switch and keep the current one
          case Some(value) => value
          // the current span context does not exist, so we start with the next one
          case None => next
        }
    }
}
