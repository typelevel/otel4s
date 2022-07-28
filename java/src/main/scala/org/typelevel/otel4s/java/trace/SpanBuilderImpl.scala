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

private[trace] final case class SpanBuilderImpl[F[_]: Sync](
    jTracer: JTracer,
    name: String,
    scope: TraceScope[F],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Auto,
    kind: Option[SpanKind] = None,
    links: Seq[(SpanContext, Seq[Attribute[_]])] = Nil,
    attributes: Seq[Attribute[_]] = Nil,
    startTimestamp: Option[FiniteDuration] = None
) extends SpanBuilder[F] {

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
      jSpan <- Resource.eval(Sync[F].delay(jBuilder.startSpan()))
      _ <- scope.make(jSpan)
    } yield new ManualSpanImpl(new SpanBackendImpl(jTracer, jSpan, scope))

  def createAuto: Resource[F, Span.Auto[F]] =
    for {
      ctx <- Resource.eval(parentContext)
      span <- createAutoSpan(jTracer, scope, makeJBuilder(ctx))
    } yield span

  def createRes[A](resource: Resource[F, A]): Resource[F, Span.Res[F, A]] = {
    def child(name: String, ctx: JContext) =
      createAutoSpan(jTracer, scope, jTracer.spanBuilder(name).setParent(ctx))

    for {
      _ <- createAuto
      ctx <- Resource.eval(scope.current)

      result <- Resource.make(
        child("acquire", ctx).surround(resource.allocated)
      )(a => child("release", ctx).surround(a._2))

      useSpan <- child("use", ctx)
    } yield new Span.Res[F, A] {
      def value: A = result._1
      def backend: Span.Backend[F] = useSpan.backend
    }
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

  private def createAutoSpan[F[_]: Sync](
      jTracer: JTracer,
      scope: TraceScope[F],
      builder: JSpanBuilder
  ): Resource[F, AutoSpanImpl[F]] = {

    def acquire: F[SpanBackendImpl[F]] =
      for {
        now <- Sync[F].realTime
        jSpan <- Sync[F].delay {
          builder
            .setStartTimestamp(now.length, now.unit)
            .startSpan()
        }
      } yield new SpanBackendImpl(jTracer, jSpan, scope)

    def reportStatus(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] =
      ec match {
        case Resource.ExitCase.Succeeded =>
          Sync[F].unit

        case Resource.ExitCase.Errored(e) =>
          backend.recordException(e) >> backend.setStatus(Status.Error)

        case Resource.ExitCase.Canceled =>
          backend.setStatus(Status.Error, "canceled")
      }

    def release(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] =
      for {
        now <- Sync[F].realTime
        _ <- reportStatus(backend, ec)
        _ <- backend.end(now)
      } yield ()

    for {
      backend <- Resource.makeCase(acquire) { case (backend, ec) =>
        release(backend, ec)
      }
      _ <- scope.make(backend.jSpan)
    } yield new AutoSpanImpl[F](backend)
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
