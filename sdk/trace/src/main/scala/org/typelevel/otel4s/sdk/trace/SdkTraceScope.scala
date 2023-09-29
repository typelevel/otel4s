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
  def current: F[Option[SpanContext]]
  def makeScope(span: SpanContext): F[F ~> F]
  def rootScope: F ~> F
  def noopScope: F ~> F
}

private[trace] object SdkTraceScope {
  private val SpanContextKey =
    Key
      .unique[SyncIO, SpanContext]("otel4s-trace-span-context-key")
      .unsafeRunSync()

  def fromContext(context: Context): SpanContext =
    context.get(SpanContextKey).getOrElse(SpanContext.invalid)

  def storeInContext(
      context: Context,
      spanContext: SpanContext
  ): Context =
    context.set(SpanContextKey, spanContext)

  def fromLocal[F[_]](implicit L: Local[F, Context]): SdkTraceScope[F] = {
    new SdkTraceScope[F] {
      /*val root: F[SdkScope.Root] =
        L.applicative.pure(scopeRoot)*/

      def current: F[Option[SpanContext]] =
        L.applicative.map(L.ask[Context])(context =>
          context.get(SpanContextKey)
        )

      def makeScope(span: SpanContext): F[F ~> F] =
        L.applicative.map(current)(context =>
          createScope(nextScope(context, span))
        )

      def rootScope: F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.scope(fa)(Context.root)
        }

      def noopScope: F ~> F =
        createScope(SpanContext.invalid)

      private def createScope(spanContext: SpanContext): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.local(fa)(context => storeInContext(context, spanContext))
        }

      private def nextScope(
          current: Option[SpanContext],
          next: SpanContext
      ): SpanContext =
        current match {
          case Some(value) if value.isValid => next
          case Some(value)                  => SpanContext.invalid
          case None                         => next
        }
    }
  }
}
