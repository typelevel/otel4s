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

import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.~>
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.ContextPropagators
import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanBuilder.Aux
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer
import org.typelevel.vault.Vault

private[java] class TracerImpl[F[_]: Sync](
    jTracer: JTracer,
    scope: TraceScope[F],
    propagators: ContextPropagators[F]
) extends Tracer[F] { self =>

  private val runner: SpanRunner[F, Span[F]] = SpanRunner.span(scope)

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def currentSpanContext: F[Option[SpanContext]] =
    scope.current.map {
      case Scope.Span(_, jSpan) if jSpan.getSpanContext.isValid =>
        Some(new WrappedSpanContext(jSpan.getSpanContext))

      case _ =>
        None
    }

  def spanBuilder(name: String): SpanBuilder.Aux[F, Span[F]] =
    new SpanBuilderImpl[F, Span[F]](jTracer, name, scope, runner)

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

  def translate[G[_]: Sync](fk: F ~> G, gk: G ~> F): Tracer[G] =
    new Tracer[G] {
      private val traceScope: TraceScope[G] =
        new TraceScope[G] {
          def root: G[Scope.Root] = fk(scope.root)

          def current: G[Scope] = fk(scope.current)

          def makeScope(span: JSpan): G[G ~> G] =
            fk(scope.makeScope(span)).map { transform =>
              new (G ~> G) {
                def apply[A](fa: G[A]): G[A] =
                  fk(transform.apply(gk(fa)))
              }
            }

          def rootScope: G[G ~> G] =
            fk(scope.rootScope).map { transform =>
              new (G ~> G) {
                def apply[A](fa: G[A]): G[A] =
                  fk(transform.apply(gk(fa)))
              }
            }

          def noopScope: G ~> G =
            new (G ~> G) {
              def apply[A](fa: G[A]): G[A] =
                fk(scope.noopScope(gk(fa)))
            }
        }

      private val spanRunner: SpanRunner[G, Span[G]] =
        SpanRunner.span(traceScope)

      val meta: Tracer.Meta[G] =
        new Tracer.Meta[G] {
          val noopSpanBuilder: SpanBuilder.Aux[G, Span[G]] =
            SpanBuilder.noop(Span.Backend.noop[G])
          val isEnabled: Boolean = self.meta.isEnabled
          val unit: G[Unit] = MonadCancelThrow[G].unit
        }

      def currentSpanContext: G[Option[SpanContext]] =
        fk(self.currentSpanContext)

      def spanBuilder(name: String): Aux[G, Span[G]] =
        new SpanBuilderImpl[G, Span[G]](
          jTracer,
          name,
          traceScope,
          spanRunner
        )

      def childScope[A](parent: SpanContext)(fa: G[A]): G[A] =
        fk(self.childScope(parent)(gk(fa)))

      def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: G[A]): G[A] =
        fk(self.joinOrRoot(carrier)(gk(fa)))

      def rootScope[A](fa: G[A]): G[A] =
        fk(self.rootScope(gk(fa)))

      def noopScope[A](fa: G[A]): G[A] =
        fk(self.noopScope(gk(fa)))

      def translate[Q[_]: Sync](fk1: G ~> Q, gk1: Q ~> G): Tracer[Q] =
        self.translate[Q](fk.andThen(fk1), gk1.andThen(gk))
    }
}
