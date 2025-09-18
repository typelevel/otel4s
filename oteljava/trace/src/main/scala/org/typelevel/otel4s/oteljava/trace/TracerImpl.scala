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

package org.typelevel.otel4s.oteljava.trace

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceScope
import org.typelevel.otel4s.trace.Tracer

private[oteljava] class TracerImpl[F[_]](
    jTracer: JTracer,
    propagators: ContextPropagators[Context],
    traceScope: TraceScope[F, Context],
)(implicit F: Sync[F])
    extends Tracer.Unsealed[F] {

  private val runner: SpanRunner[F] = SpanRunner.fromTraceScope(traceScope)

  private val spanBuilderMeta: SpanBuilder.Meta[F] =
    SpanBuilder.Meta.enabled

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def currentSpanContext: F[Option[SpanContext]] =
    traceScope.current.map(_.filter(_.isValid))

  def currentSpanOrNoop: F[Span[F]] =
    traceScope.contextReader { ctx =>
      Span.fromBackend(
        SpanBackendImpl.fromJSpan(
          JSpan.fromContext(ctx.underlying)
        )
      )
    }

  def currentSpanOrThrow: F[Span[F]] =
    traceScope.contextReader { ctx =>
      Option(JSpan.fromContextOrNull(ctx.underlying))
        .map { jSpan =>
          F.pure(Span.fromBackend(SpanBackendImpl.fromJSpan(jSpan)))
        }
        .getOrElse(Tracer.raiseNoCurrentSpan)
    }.flatten

  def spanBuilder(name: String): SpanBuilder[F] =
    SpanBuilderImpl[F](jTracer, name, spanBuilderMeta, runner, traceScope)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    traceScope.childScope(parent).flatMap(trace => trace(fa))

  def rootScope[A](fa: F[A]): F[A] =
    traceScope.rootScope.flatMap(trace => trace(fa))

  def noopScope[A](fa: F[A]): F[A] =
    traceScope.noopScope(fa)

  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = {
    val context = propagators.textMapPropagator.extract(Context.root, carrier)
    traceScope.withContext(context)(fa)
  }

  def propagate[C: TextMapUpdater](carrier: C): F[C] =
    traceScope.contextReader(ctx => propagators.textMapPropagator.inject(ctx, carrier))
}
