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

package org.typelevel.otel4s.sdk
package trace

import cats.data.OptionT
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.processor.SpanStorage
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer

final class SdkTracer[F[_]: Temporal: Console] private[trace] (
    sharedState: TracerSharedState[F],
    scopeInfo: InstrumentationScope,
    propagators: ContextPropagators[Context],
    scope: SdkTraceScope[F],
    storage: SpanStorage[F]
) extends Tracer[F] {

  def meta: Tracer.Meta[F] = Tracer.Meta.enabled[F]

  def currentSpanContext: F[Option[SpanContext]] =
    scope.current.map(current => current.filter(_.isValid))

  def currentSpanOrNoop: F[Span[F]] =
    OptionT(scope.current)
      .flatMapF(ctx => storage.get(ctx))
      .map(ref => Span.fromBackend(ref))
      .getOrElse(Span.fromBackend(Span.Backend.noop))

  def spanBuilder(name: String): SpanBuilder[F] =
    new SdkSpanBuilder[F](name, scopeInfo, sharedState, scope)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    scope.childScope(parent).flatMap(trace => trace(fa))

  def rootScope[A](fa: F[A]): F[A] =
    scope.rootScope.flatMap(trace => trace(fa))

  def noopScope[A](fa: F[A]): F[A] =
    scope.noopScope(fa)

  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = {
    val context = propagators.textMapPropagator.extract(Context.root, carrier)

    val f = context.get(SdkContextKeys.SpanContextKey) match {
      case Some(parent) => childScope(parent)(fa)
      case None         => fa
    }

    // use external context to bring extracted headers
    scope.withContext(context)(f)
  }

  def propagate[C: TextMapUpdater](carrier: C): F[C] =
    scope.contextReader(ctx =>
      propagators.textMapPropagator.inject(ctx, carrier)
    )

}
