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
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.TextMapUpdater
import org.typelevel.otel4s.context.AskVault
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.vault.Vault

private[java] class TracerImpl[F[_]: Sync: AskVault](
    jTracer: JTracer,
    scope: TraceScope[F],
    propagators: ContextPropagators[F]
) extends Tracer[F] {

  private val runner: SpanRunner[F] = SpanRunner.span(scope)

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def currentSpanContext: F[Option[SpanContext]] =
    scope.current.map {
      case Scope.Span(_, jSpan) if jSpan.getSpanContext.isValid =>
        Some(new WrappedSpanContext(jSpan.getSpanContext))

      case _ =>
        None
    }

  def spanBuilder(name: String): SpanBuilder[F] =
    new SpanBuilderImpl[F](jTracer, name, scope, runner)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    scope
      .makeScope(JSpan.wrap(WrappedSpanContext.unwrap(parent)))
      .flatMap(_(fa))

  def rootScope[A](fa: F[A]): F[A] =
    scope.rootScope.flatMap(_(fa))

  def noopScope[A](fa: F[A]): F[A] =
    scope.noopScope(fa)

  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = {
    val context = propagators.textMapPropagator.extract(Vault.empty, carrier)

    SpanContext.fromContext(context) match {
      case Some(parent) =>
        childScope(parent)(fa)
      case None =>
        rootScope(fa)
    }
  }

  def propagate[C: TextMapUpdater](carrier: C): F[C] =
    Ask[F, Vault].reader(propagators.textMapPropagator.injected(_, carrier))
}
