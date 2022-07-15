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

import cats.effect.Clock
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.api.trace.{SpanContext => JSpanContext}
import io.opentelemetry.api.trace.{SpanKind => JSpanKind}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanBuilder
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

import scala.concurrent.duration.FiniteDuration

private[trace] final case class SpanBuilderImpl[F[_]](
    jTracer: JTracer,
    name: String,
    scope: TraceScope[F],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Auto,
    kind: Option[SpanKind] = None,
    links: Seq[(SpanContext, Seq[Attribute[_]])] = Nil,
    attributes: Seq[Attribute[_]] = Nil,
    startTimestamp: Option[FiniteDuration] = None
)(implicit F: Sync[F], C: Clock[F])
    extends SpanBuilder[F] {

  import SpanBuilderImpl._

  def withSpanKind(spanKind: SpanKind): SpanBuilder[F] =
    copy(kind = Some(spanKind))

  def withAttribute[A](attribute: Attribute[A]): SpanBuilder[F] =
    copy(attributes = attributes :+ attribute)

  def withAttributes(attributes: Attribute[_]*): SpanBuilder[F] =
    copy(attributes = attributes ++ attributes)

  def withLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanBuilder[F] =
    copy(links = links :+ (spanContext, attributes))

  def root: SpanBuilder[F] =
    copy(parent = Parent.Root)

  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] =
    copy(startTimestamp = Some(timestamp))

  def createManual: Resource[F, Span.Manual[F]] =
    for {
      parent <- Resource.eval(parentContext)
      jBuilder <- Resource.pure(makeJBuilder(parent))
      jSpan <- Resource.eval(F.delay(jBuilder.startSpan()))
      _ <- scope.make(jSpan)
    } yield new ManualSpanImpl(new SpanBackendImpl(jTracer, jSpan, scope))

  def createAuto: Resource[F, Span.Auto[F]] = {
    def acquire: F[ManualSpanImpl[F]] =
      for {
        now <- C.monotonic
        parent <- parentContext
        jSpan <- F.delay {
          makeJBuilder(parent)
            .setStartTimestamp(now.length, now.unit)
            .startSpan()
        }
      } yield new ManualSpanImpl(new SpanBackendImpl(jTracer, jSpan, scope))

    def reportStatus(span: Span[F], ec: Resource.ExitCase): F[Unit] =
      ec match {
        case Resource.ExitCase.Succeeded =>
          F.unit

        case Resource.ExitCase.Errored(e) =>
          span.recordException(e) >> span.setStatus(Status.Error)

        case Resource.ExitCase.Canceled =>
          span.setStatus(Status.Error, "canceled")
      }

    def release(span: Span.Manual[F], ec: Resource.ExitCase): F[Unit] =
      for {
        now <- C.monotonic
        _ <- reportStatus(span, ec)
        _ <- span.end(now)
      } yield ()

    for {
      manual <- Resource.makeCase(acquire) { case (span, ec) =>
        release(span, ec)
      }
      _ <- scope.make(manual.backend.jSpan)
    } yield new AutoSpanImpl[F](manual.backend)
  }

  private def makeJBuilder(parent: JContext): JSpanBuilder = {
    val b = jTracer
      .spanBuilder(name)
      .setAllAttributes(Conversions.toJAttributes(attributes))
      .setParent(parent)

    kind.foreach(k => b.setSpanKind(toJSpanKind(k)))
    b.setAllAttributes(Conversions.toJAttributes(attributes))
    startTimestamp.foreach(d => b.setStartTimestamp(d.length, d.unit))
    links.foreach { case (ctx, attributes) =>
      b.addLink(toJSpanContext(ctx), Conversions.toJAttributes(attributes))
    }

    b
  }

  private def parentContext: F[JContext] =
    parent match {
      case Parent.Auto =>
        scope.current
      case Parent.Root =>
        scope.root
      case Parent.Explicit(parent) =>
        scope.current.map(ctx => ctx.`with`(parent))
    }
}

object SpanBuilderImpl {

  sealed trait Parent

  object Parent {
    case object Auto extends Parent
    case object Root extends Parent
    final case class Explicit(parent: JSpan) extends Parent
  }

  private def toJSpanKind(spanKind: SpanKind): JSpanKind =
    spanKind match {
      case SpanKind.Internal => JSpanKind.INTERNAL
      case SpanKind.Server   => JSpanKind.SERVER
      case SpanKind.Client   => JSpanKind.CLIENT
      case SpanKind.Producer => JSpanKind.PRODUCER
      case SpanKind.Consumer => JSpanKind.CONSUMER
    }

  private def toJSpanContext(context: SpanContext): JSpanContext =
    context match {
      case ctx: WrappedSpanContext =>
        ctx.jSpanContext

      case other =>
        JSpanContext.create(
          other.traceId,
          other.spanId,
          TraceFlags.getDefault,
          TraceState.getDefault
        )
    }

}
