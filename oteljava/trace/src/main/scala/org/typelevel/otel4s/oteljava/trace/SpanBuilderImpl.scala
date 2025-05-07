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

package org.typelevel.otel4s.oteljava
package trace

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.api.trace.{SpanKind => JSpanKind}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps
import org.typelevel.otel4s.trace.TraceScope

private[oteljava] final case class SpanBuilderImpl[F[_]: Sync] private (
    jTracer: JTracer,
    name: String,
    meta: InstrumentMeta[F],
    runner: SpanRunner[F],
    scope: TraceScope[F, Context],
    stateModifier: SpanBuilder.State => SpanBuilder.State
) extends SpanBuilder[F] {
  import SpanBuilder.Parent
  import SpanBuilderImpl._

  def modifyState(f: SpanBuilder.State => SpanBuilder.State): SpanBuilder[F] =
    copy(stateModifier = this.stateModifier.andThen(f))

  def build: SpanOps[F] = new SpanOps[F] {
    def startUnmanaged: F[Span[F]] =
      runnerContext.flatMap(ctx => SpanRunner.startUnmanaged(ctx))

    def resource: Resource[F, SpanOps.Res[F]] =
      Resource.eval(runnerContext).flatMap(ctx => runner.start(ctx))

    override def use[A](f: Span[F] => F[A]): F[A] =
      resource.use(res => res.trace(Sync[F].defer(f(res.span))))

    override def use_ : F[Unit] = use(_ => Sync[F].unit)
  }

  private[trace] def makeJBuilder(state: SpanBuilder.State, parent: JContext): JSpanBuilder = {
    val b = jTracer
      .spanBuilder(name)
      .setAllAttributes(state.attributes.toJava)
      .setParent(parent)

    state.spanKind.foreach(k => b.setSpanKind(toJSpanKind(k)))
    state.startTimestamp.foreach(d => b.setStartTimestamp(d.length, d.unit))
    state.links.foreach { case (ctx, attributes) =>
      b.addLink(
        SpanContextConversions.toJava(ctx),
        attributes.toJava
      )
    }

    b
  }

  private def runnerContext: F[Option[SpanRunner.RunnerContext]] =
    for {
      state <- Sync[F].delay(stateModifier(SpanBuilder.State.init))
      parentOpt <- parentContext(state)
    } yield parentOpt.map { parent =>
      SpanRunner.RunnerContext(
        builder = makeJBuilder(state, parent),
        parent = parent,
        hasStartTimestamp = state.startTimestamp.isDefined,
        finalizationStrategy = state.finalizationStrategy
      )
    }

  private def parentContext(state: SpanBuilder.State): F[Option[JContext]] =
    scope.contextReader { case Context.Wrapped(underlying) =>
      def explicit(parent: SpanContext) =
        JSpan
          .wrap(SpanContextConversions.toJava(parent))
          .storeInContext(underlying)

      Option(JSpan.fromContextOrNull(underlying)) match {
        // there is a valid span in the current context = child scope
        case Some(current) if current.getSpanContext.isValid =>
          state.parent match {
            case Parent.Root             => Some(JContext.root)
            case Parent.Propagate        => Some(underlying)
            case Parent.Explicit(parent) => Some(explicit(parent))
          }

        // there is no span in the current context = root scope
        case None =>
          state.parent match {
            case Parent.Root             => Some(underlying)
            case Parent.Propagate        => Some(underlying)
            case Parent.Explicit(parent) => Some(explicit(parent))
          }

        // there is an invalid span = noop scope
        case Some(_) =>
          None
      }
    }
}

private[oteljava] object SpanBuilderImpl {

  def apply[F[_]: Sync](
      jTracer: JTracer,
      name: String,
      meta: InstrumentMeta[F],
      runner: SpanRunner[F],
      scope: TraceScope[F, Context]
  ): SpanBuilder[F] =
    SpanBuilderImpl(
      jTracer,
      name,
      meta,
      runner,
      scope,
      identity
    )

  private def toJSpanKind(spanKind: SpanKind): JSpanKind =
    spanKind match {
      case SpanKind.Internal => JSpanKind.INTERNAL
      case SpanKind.Server   => JSpanKind.SERVER
      case SpanKind.Client   => JSpanKind.CLIENT
      case SpanKind.Producer => JSpanKind.PRODUCER
      case SpanKind.Consumer => JSpanKind.CONSUMER
    }

}
