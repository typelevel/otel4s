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

import cats.mtl.Local
import cats.~>
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.trace.SpanContext

/** The context-manipulation interface.
  *
  * The implementation uses [[Local]] to propagate [[Context]] within the
  * fibers.
  *
  * The [[Context]] is a type-safe key-value storage. It may carry
  * [[SpanContext]], baggage attributes, and so on.
  *
  * In terms of the span propagation, there can be 3 scenarios:
  *   - the [[SpanContext]] is missing in the context
  *   - the '''valid''' [[SpanContext]] is present in the context
  *   - the '''invalid''' [[SpanContext]] is present in the context
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private trait SdkTraceScope[F[_]] {

  /** Returns a [[SpanContext]] if it's available in the current scope.
    */
  def current: F[Option[SpanContext]]

  /** Creates a new scope using the given `spanContext` context if the
    * requirement are met.
    *
    * ==The propagation logic==
    *
    * The propagation is based on the presence and the validity of the
    * [[SpanContext]].
    *
    * All context values except for the [[SpanContext]] remains intact. To
    * demonstrate that values remain intact, the context has some baggage data.
    *
    * ===The [[SpanContext]] is missing===
    *
    * We use the given span context. The baggage data remains.
    *
    * {{{
    * ┌───────────────────────┐        ┌──────────────────────────────────────────────┐
    * │   Context             │        │   Context                                    │
    * │                       │        │                                              │
    * │  ┌─────────────────┐  │        │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │  │        │  │                 │    │                 │  │
    * │  │     Baggage     │  │  --->  │  │   SpanContext   │    │     Baggage     │  │
    * │  │                 │  │        │  │ span_id = "new" │    │                 │  │
    * │  └─────────────────┘  │        │  └─────────────────┘    └─────────────────┘  │
    * │                       │        │                                              │
    * └───────────────────────┘        └──────────────────────────────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is valid===
    *
    * We override the existing span context with the given one. The baggage data
    * remains.
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
    * │   Context                                    │       │   Context                                    │
    * │                                              │       │                                              │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │    │                 │  │       │  │                 │    │                 │  │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │  │   SpanContext   │    │     Baggage     │  │
    * │  │ span_id = "old" │    │                 │  │       │  │ span_id = "new" │    │                 │  │
    * │  └─────────────────┘    └─────────────────┘  │       │  └─────────────────┘    └─────────────────┘  │
    * │                                              │       │                                              │
    * └──────────────────────────────────────────────┘       └──────────────────────────────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is invalid===
    *
    * The propagation logic is no-op. We keep the context as is. The baggage
    * data remains.
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
    * │   Context                                    │       │   Context                                    │
    * │                                              │       │                                              │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │    │                 │  │       │  │                 │    │                 │  │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │  │   SpanContext   │    │     Baggage     │  │
    * │  │     invalid     │    │                 │  │       │  │     invalid     │    │                 │  │
    * │  └─────────────────┘    └─────────────────┘  │       │  └─────────────────┘    └─────────────────┘  │
    * │                                              │       │                                              │
    * └──────────────────────────────────────────────┘       └──────────────────────────────────────────────┘
    * }}}
    *
    * @param spanContext
    *   the span context to use
    */
  def childScope(spanContext: SpanContext): F[F ~> F]

  /** Creates a root scope. The difference with the [[childScope]] is that we
    * override the whole [[Context]], rather then only a [[SpanContext]] within
    * the context.
    *
    * ==The propagation logic==
    *
    * The propagation is based on the presence and the validity of the
    * [[SpanContext]].
    *
    * To demonstrate that the whole context may be replaced, the context has
    * some baggage data.
    *
    * ===The [[SpanContext]] is missing===
    *
    * The scope is already root, so we keep the context as is.
    *
    * {{{
    * ┌───────────────────────┐        ┌───────────────────────┐
    * │   Context             │        │   Context             │
    * │                       │        │                       │
    * │  ┌─────────────────┐  │        │                       │
    * │  │                 │  │        │                       │
    * │  │     Baggage     │  │  --->  │                       │
    * │  │                 │  │        │                       │
    * │  └─────────────────┘  │        │                       │
    * │                       │        │                       │
    * └───────────────────────┘        └───────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is valid===
    *
    * There is a valid span, we forcefully use [[Context.root]].
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌───────────────────────┐
    * │   Context                                    │       │   Context             │
    * │                                              │       │                       │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │                       │
    * │  │                 │    │                 │  │       │                       │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │                       │
    * │  │ span_id = "abc" │    │                 │  │       │                       │
    * │  └─────────────────┘    └─────────────────┘  │       │                       │
    * │                                              │       │                       │
    * └──────────────────────────────────────────────┘       └───────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is invalid===
    *
    * The current propagation strategy is no-op, so we keep the context as is.
    * The baggage data remains.
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
    * │   Context                                    │       │   Context                                    │
    * │                                              │       │                                              │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │    │                 │  │       │  │                 │    │                 │  │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │  │   SpanContext   │    │     Baggage     │  │
    * │  │     invalid     │    │                 │  │       │  │     invalid     │    │                 │  │
    * │  └─────────────────┘    └─────────────────┘  │       │  └─────────────────┘    └─────────────────┘  │
    * │                                              │       │                                              │
    * └──────────────────────────────────────────────┘       └──────────────────────────────────────────────┘
    * }}}
    */
  def rootScope: F[F ~> F]

  /** Creates a no-op scope.
    *
    * No-op scope means the tracing operations are no-op and the spans created
    * within this scope will not be exported anywhere.
    *
    * We use [[SpanContext.invalid]] as a mark that the propagation logic is
    * no-op.
    *
    * A shortcut for `childScope(SpanContext.invalid)`.
    *
    * ==The propagation logic==
    *
    * All context values except for the [[SpanContext]] remains intact. To
    * demonstrate that values remain intact, the context has some baggage data.
    *
    * ===The [[SpanContext]] is missing===
    *
    * The scope is already root, so we keep the context as is. The baggage data
    * remains.
    *
    * {{{
    * ┌───────────────────────┐        ┌──────────────────────────────────────────────┐
    * │   Context             │        │   Context                                    │
    * │                       │        │                                              │
    * │  ┌─────────────────┐  │        │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │  │        │  │                 │    │                 │  │
    * │  │     Baggage     │  │  --->  │  │   SpanContext   │    │     Baggage     │  │
    * │  │                 │  │        │  │     invalid     │    │                 │  │
    * │  └─────────────────┘  │        │  └─────────────────┘    └─────────────────┘  │
    * │                       │        │                                              │
    * └───────────────────────┘        └──────────────────────────────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is valid===
    *
    * There is a valid span, we forcefully use [[SpanContext.invalid]]. The
    * baggage data remains.
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
    * │   Context                                    │       │   Context                                    │
    * │                                              │       │                                              │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │    │                 │  │       │  │                 │    │                 │  │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │  │   SpanContext   │    │     Baggage     │  │
    * │  │ span_id = "abc" │    │                 │  │       │  │     invalid     │    │                 │  │
    * │  └─────────────────┘    └─────────────────┘  │       │  └─────────────────┘    └─────────────────┘  │
    * │                                              │       │                                              │
    * └──────────────────────────────────────────────┘       └──────────────────────────────────────────────┘
    * }}}
    *
    * ===The [[SpanContext]] is invalid===
    *
    * The current propagation strategy is already no-op, so we keep the context
    * as is. The baggage data remains.
    *
    * {{{
    * ┌──────────────────────────────────────────────┐       ┌──────────────────────────────────────────────┐
    * │   Context                                    │       │   Context                                    │
    * │                                              │       │                                              │
    * │  ┌─────────────────┐    ┌─────────────────┐  │       │  ┌─────────────────┐    ┌─────────────────┐  │
    * │  │                 │    │                 │  │       │  │                 │    │                 │  │
    * │  │   SpanContext   │    │     Baggage     │  │  ---> │  │   SpanContext   │    │     Baggage     │  │
    * │  │     invalid     │    │                 │  │       │  │     invalid     │    │                 │  │
    * │  └─────────────────┘    └─────────────────┘  │       │  └─────────────────┘    └─────────────────┘  │
    * │                                              │       │                                              │
    * └──────────────────────────────────────────────┘       └──────────────────────────────────────────────┘
    * }}}
    */
  def noopScope: F ~> F

  /** Creates a scope that uses the given `context`.
    *
    * A shortcut for
    * {{{
    * Local[F, Context].scope(context)
    * }}}
    *
    * @param context
    *   the context to use
    */
  def withContext(context: Context): F ~> F

  /** Runs the `f` with the current [[Context]].
    *
    * A shortcut for
    * {{{
    * Local[F, Context].reader(f)
    * }}}
    */
  def contextReader[A](f: Context => A): F[A]
}

private object SdkTraceScope {

  def fromLocal[F[_]](implicit L: Local[F, Context]): SdkTraceScope[F] =
    new SdkTraceScope[F] {
      import SdkContextKeys.SpanContextKey

      def current: F[Option[SpanContext]] =
        L.reader(_.get(SpanContextKey))

      def childScope(span: SpanContext): F[F ~> F] =
        L.applicative.map(current) { context =>
          createScope(nextScope(context, span))
        }

      def rootScope: F[F ~> F] =
        L.reader { context =>
          val ctx = context.get(SpanContextKey) match {
            // the SpanContext exist and it's invalid.
            // It means, the propagation strategy is noop and we should keep the current context
            case Some(ctx) if !ctx.isValid => context
            // the SpanContext exist and it's valid, hence we start with the fresh one
            case Some(_) => Context.root
            // there is no existing SpanContext, we start with the fresh one
            case None => Context.root
          }

          withContext(ctx)
        }

      def noopScope: F ~> F =
        createScope(SpanContext.invalid)

      def withContext(context: Context): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.scope(fa)(context)
        }

      def contextReader[A](f: Context => A): F[A] =
        L.reader(f)

      private def createScope(spanContext: SpanContext): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.local(fa)(context => context.updated(SpanContextKey, spanContext))
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
