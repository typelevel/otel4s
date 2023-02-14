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

package org.typelevel.otel4s.java
package trace

import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.~>
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.api.trace.{SpanKind => JSpanKind}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps

import scala.concurrent.duration.FiniteDuration

private[java] final case class SpanBuilderImpl[F[_]: Sync, Res <: Span[F]](
    jTracer: JTracer,
    name: String,
    scope: TraceScope[F],
    runner: SpanRunner[F, Res],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Propagate,
    finalizationStrategy: SpanFinalizer.Strategy =
      SpanFinalizer.Strategy.reportAbnormal,
    kind: Option[SpanKind] = None,
    links: Seq[(SpanContext, Seq[Attribute[_]])] = Nil,
    attributes: Seq[Attribute[_]] = Nil,
    startTimestamp: Option[FiniteDuration] = None
) extends SpanBuilder[F] {
  type Result = Res

  import SpanBuilderImpl._

  def withSpanKind(spanKind: SpanKind): Builder =
    copy(kind = Some(spanKind))

  def addAttribute[A](attribute: Attribute[A]): Builder =
    copy(attributes = attributes :+ attribute)

  def addAttributes(attributes: Attribute[_]*): Builder =
    copy(attributes = attributes ++ attributes)

  def addLink(spanContext: SpanContext, attributes: Attribute[_]*): Builder =
    copy(links = links :+ (spanContext, attributes))

  def root: Builder =
    copy(parent = Parent.Root)

  def withParent(parent: SpanContext): Builder =
    copy(parent = Parent.Explicit(parent))

  def withStartTimestamp(timestamp: FiniteDuration): Builder =
    copy(startTimestamp = Some(timestamp))

  def withFinalizationStrategy(strategy: SpanFinalizer.Strategy): Builder =
    copy(finalizationStrategy = strategy)

  def wrapResource[A](
      resource: Resource[F, A]
  )(implicit ev: Result =:= Span[F]): SpanBuilder.Aux[F, Span.Res[F, A]] =
    copy(runner = SpanRunner.resource(scope, resource, jTracer))

  def build: SpanOps.Aux[F, Result] = new SpanOps[F] {
    type Result = Res

    def startUnmanaged(implicit ev: Result =:= Span[F]): F[Span[F]] =
      runnerContext.flatMap(ctx => SpanRunner.startUnmanaged(ctx))

    def use[A](f: Result => F[A]): F[A] =
      start.use { case (span, nt) => nt(f(span)) }

    def use_ : F[Unit] =
      start.use_

    def surround[A](fa: F[A]): F[A] =
      use(_ => fa)

    private def start: Resource[F, (Result, F ~> F)] =
      Resource.eval(runnerContext).flatMap(ctx => runner.start(ctx))
  }

  private[trace] def makeJBuilder(parent: JContext): JSpanBuilder = {
    val b = jTracer
      .spanBuilder(name)
      .setAllAttributes(Conversions.toJAttributes(attributes))
      .setParent(parent)

    kind.foreach(k => b.setSpanKind(toJSpanKind(k)))
    startTimestamp.foreach(d => b.setStartTimestamp(d.length, d.unit))
    links.foreach { case (ctx, attributes) =>
      b.addLink(
        WrappedSpanContext.unwrap(ctx),
        Conversions.toJAttributes(attributes)
      )
    }

    b
  }

  private def runnerContext: F[Option[SpanRunner.RunnerContext]] =
    for {
      parentOpt <- parentContext
    } yield parentOpt.map { parent =>
      SpanRunner.RunnerContext(
        builder = makeJBuilder(parent),
        parent = parent,
        hasStartTimestamp = startTimestamp.isDefined,
        finalizationStrategy = finalizationStrategy
      )
    }

  private def parentContext: F[Option[JContext]] =
    parent match {
      case Parent.Root =>
        scope.current.flatMap {
          case Scope.Root(ctx) =>
            Sync[F].pure(Some(ctx))

          case Scope.Span(_, _, _) =>
            scope.root.map(s => Some(s.ctx))

          case Scope.Noop =>
            Sync[F].pure(None)
        }

      case Parent.Propagate =>
        scope.current.map {
          case Scope.Root(ctx)       => Some(ctx)
          case Scope.Span(ctx, _, _) => Some(ctx)
          case Scope.Noop            => None
        }

      case Parent.Explicit(parent) =>
        def parentSpan = JSpan.wrap(WrappedSpanContext.unwrap(parent))
        scope.current.map {
          case Scope.Root(ctx)       => Some(ctx.`with`(parentSpan))
          case Scope.Span(ctx, _, _) => Some(ctx.`with`(parentSpan))
          case Scope.Noop            => None
        }
    }
}

private[java] object SpanBuilderImpl {

  sealed trait Parent
  object Parent {
    case object Propagate extends Parent
    case object Root extends Parent
    final case class Explicit(parent: SpanContext) extends Parent
  }

  private def toJSpanKind(spanKind: SpanKind): JSpanKind =
    spanKind match {
      case SpanKind.Internal => JSpanKind.INTERNAL
      case SpanKind.Server   => JSpanKind.SERVER
      case SpanKind.Client   => JSpanKind.CLIENT
      case SpanKind.Producer => JSpanKind.PRODUCER
      case SpanKind.Consumer => JSpanKind.CONSUMER
    }

}
