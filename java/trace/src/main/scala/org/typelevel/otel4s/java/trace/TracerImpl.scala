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
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.java.context.Context
import org.typelevel.otel4s.java.context.LocalContext
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Tracer

private[java] class TracerImpl[F[_]: Sync](
    jTracer: JTracer,
    propagators: ContextPropagators[Context]
)(implicit L: LocalContext[F])
    extends Tracer[F] {

  private val runner: SpanRunner[F] = SpanRunner.fromLocal

  val meta: Tracer.Meta[F] =
    Tracer.Meta.enabled

  def currentSpanContext: F[Option[SpanContext]] =
    L.reader {
      case Context.Noop => None
      case Context.Wrapped(underlying) =>
        Option(JSpan.fromContextOrNull(underlying))
          .map(jSpan => SpanContextConversions.toScala(jSpan.getSpanContext))
    }

  def currentSpanOrNoop: F[Span[F]] =
    L.reader { ctx =>
      Span.fromBackend(
        SpanBackendImpl.fromJSpan(
          JSpan.fromContext(ctx.underlying)
        )
      )
    }

  def spanBuilder(name: String): SpanBuilder[F] =
    new SpanBuilderImpl[F](jTracer, name, runner)

  def childScope[A](parent: SpanContext)(fa: F[A]): F[A] =
    L.local(fa) {
      _.map(JSpan.wrap(SpanContextConversions.toJava(parent)).storeInContext)
    }

  def rootScope[A](fa: F[A]): F[A] =
    L.local(fa) {
      case Context.Noop       => Context.Noop
      case Context.Wrapped(_) => Context.root
    }

  def noopScope[A](fa: F[A]): F[A] =
    L.scope(fa)(Context.Noop)

  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = {
    val context = propagators.textMapPropagator.extract(Context.root, carrier)
    L.scope(fa)(context)
  }

  def propagate[C: TextMapUpdater](carrier: C): F[C] =
    L.reader(propagators.textMapPropagator.inject(_, carrier))
}
