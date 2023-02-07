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

package org.typelevel.otel4s.java.trace

import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer

private[java] class TracerImpl[F[_]: Sync](
    jTracer: JTracer,
    scope: TraceScope[F]
) extends Tracer[F] {

  private val simple = SpanBuilderImpl.Runner.span

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def currentSpanContext: F[Option[SpanContext]] =
    scope.current.map {
      case TraceScope.Scope.Span(_, _, spanCtx) if spanCtx.isValid =>
        Some(spanCtx)

      case _ =>
        None
    }

  def spanBuilder(name: String): SpanBuilder.Aux[F, Span[F]] =
    new SpanBuilderImpl[F, Span[F]](jTracer, name, scope, simple)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    scope
      .makeScope(JSpan.wrap(WrappedSpanContext.unwrap(parent)))
      .flatMap(_(fa))

  def rootScope[A](fa: F[A]): F[A] =
    scope.rootScope.flatMap(_(fa))

  def noopScope[A](fa: F[A]): F[A] =
    scope.noopScope(fa)
}
