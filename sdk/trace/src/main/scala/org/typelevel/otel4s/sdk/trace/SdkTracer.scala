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

import cats.effect.Temporal
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.TextMapUpdater
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.propagation.ContextPropagators
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer

final class SdkTracer[F[_]: Temporal] private[trace] (
    sharedState: TracerSharedState[F],
    scopeInfo: InstrumentationScopeInfo,
    propagators: ContextPropagators,
    scope: SdkTraceScope[F]
) extends Tracer[F] {

  def meta: Tracer.Meta[F] = Tracer.Meta.enabled[F]

  def currentSpanContext: F[Option[SpanContext]] =
    scope.current.map(current => current.filter(_.isValid))

  def spanBuilder(name: String): SpanBuilder[F] =
    new SdkSpanBuilder[F](name, scopeInfo, sharedState, scope)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    scope.makeScope(parent).flatMap(trace => trace(fa))

  def rootScope[A](fa: F[A]): F[A] =
    scope.rootScope(fa)

  def noopScope[A](fa: F[A]): F[A] =
    scope.noopScope(fa)

  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = {
    val context = propagators.textMapPropagator.extract(Context.root, carrier)

    val f = SdkTraceScope.fromContext(context) match {
      case Some(parent) => childScope(parent)(fa)
      case None         => fa
    }

    // use external context to bring extracted headers
    scope.withExplicitContext(context)(f)
  }

  def propagate[C: TextMapUpdater](carrier: C): F[C] =
    scope.reader(ctx => propagators.textMapPropagator.injected(ctx, carrier))

}
