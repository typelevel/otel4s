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
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps
import org.typelevel.otel4s.trace.TraceScope

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

private[oteljava] final case class SpanBuilderImpl[F[_]: Sync](
    jTracer: JTracer,
    name: String,
    runner: SpanRunner[F],
    scope: TraceScope[F, Context],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Propagate,
    finalizationStrategy: SpanFinalizer.Strategy =
      SpanFinalizer.Strategy.reportAbnormal,
    kind: Option[SpanKind] = None,
    links: Seq[(SpanContext, Attributes)] = Nil,
    attributes: Attributes = Attributes.empty,
    startTimestamp: Option[FiniteDuration] = None
) extends SpanBuilder[F] {
  import SpanBuilderImpl._

  def withSpanKind(spanKind: SpanKind): SpanBuilder[F] =
    copy(kind = Some(spanKind))

  def addAttribute[A](attribute: Attribute[A]): SpanBuilder[F] =
    copy(attributes = attributes + attribute)

  def addAttributes(
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    copy(attributes = this.attributes ++ attributes)

  def addLink(
      spanContext: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    copy(links = links :+ (spanContext, attributes.to(Attributes)))

  def root: SpanBuilder[F] =
    copy(parent = Parent.Root)

  def withParent(parent: SpanContext): SpanBuilder[F] =
    copy(parent = Parent.Explicit(parent))

  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] =
    copy(startTimestamp = Some(timestamp))

  def withFinalizationStrategy(
      strategy: SpanFinalizer.Strategy
  ): SpanBuilder[F] =
    copy(finalizationStrategy = strategy)

  def build: SpanOps[F] = new SpanOps[F] {
    def startUnmanaged: F[Span[F]] =
      runnerContext.flatMap(ctx => SpanRunner.startUnmanaged(ctx))

    def resource: Resource[F, SpanOps.Res[F]] =
      Resource.eval(runnerContext).flatMap(ctx => runner.start(ctx))

    override def use[A](f: Span[F] => F[A]): F[A] =
      resource.use { res => res.trace(f(res.span)) }

    override def use_ : F[Unit] = use(_ => Sync[F].unit)
  }

  private[trace] def makeJBuilder(parent: JContext): JSpanBuilder = {
    val b = jTracer
      .spanBuilder(name)
      .setAllAttributes(attributes.toJava)
      .setParent(parent)

    kind.foreach(k => b.setSpanKind(toJSpanKind(k)))
    startTimestamp.foreach(d => b.setStartTimestamp(d.length, d.unit))
    links.foreach { case (ctx, attributes) =>
      b.addLink(
        SpanContextConversions.toJava(ctx),
        attributes.toJava
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
    scope.contextReader { case Context.Wrapped(underlying) =>
      def explicit(parent: SpanContext) =
        JSpan
          .wrap(SpanContextConversions.toJava(parent))
          .storeInContext(underlying)

      Option(JSpan.fromContextOrNull(underlying)) match {
        // there is a valid span in the current context = child scope
        case Some(current) if current.getSpanContext.isValid =>
          parent match {
            case Parent.Root             => Some(JContext.root)
            case Parent.Propagate        => Some(underlying)
            case Parent.Explicit(parent) => Some(explicit(parent))
          }

        // there is no span in the current context = root scope
        case None =>
          parent match {
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

  sealed trait Parent
  private object Parent {
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
