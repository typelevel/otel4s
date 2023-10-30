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
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.api.trace.{SpanKind => JSpanKind}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.java.context.Context
import org.typelevel.otel4s.java.context.LocalContext
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.SpanOps

import scala.concurrent.duration.FiniteDuration

private[java] final case class SpanBuilderImpl[F[_]: Sync](
    jTracer: JTracer,
    name: String,
    runner: SpanRunner[F],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Propagate,
    finalizationStrategy: SpanFinalizer.Strategy =
      SpanFinalizer.Strategy.reportAbnormal,
    kind: Option[SpanKind] = None,
    links: Seq[(SpanContext, Seq[Attribute[_]])] = Nil,
    attributes: Seq[Attribute[_]] = Nil,
    startTimestamp: Option[FiniteDuration] = None
)(implicit L: LocalContext[F])
    extends SpanBuilder[F] {
  import SpanBuilderImpl._

  def withSpanKind(spanKind: SpanKind): SpanBuilder[F] =
    copy(kind = Some(spanKind))

  def addAttribute[A](attribute: Attribute[A]): SpanBuilder[F] =
    copy(attributes = attributes :+ attribute)

  def addAttributes(attributes: Attribute[_]*): SpanBuilder[F] =
    copy(attributes = this.attributes ++ attributes)

  def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanBuilder[F] =
    copy(links = links :+ (spanContext, attributes))

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
    def startUnmanaged: F[Span.Manual[F]] =
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
    L.reader {
      case Context.Noop => None
      case Context.Wrapped(underlying) =>
        Some {
          parent match {
            case Parent.Root =>
              Context.root.underlying
            case Parent.Propagate => underlying
            case Parent.Explicit(parent) =>
              JSpan
                .wrap(WrappedSpanContext.unwrap(parent))
                .storeInContext(underlying)
          }
        }
    }
}

private[java] object SpanBuilderImpl {

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
