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
import cats.syntax.foldable._
import cats.syntax.functor._
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

import scala.concurrent.duration.FiniteDuration

private[trace] final case class SpanBuilderImpl[F[_]: Sync](
    jTracer: JTracer,
    name: String,
    scope: TraceScope[F],
    parent: SpanBuilderImpl.Parent = SpanBuilderImpl.Parent.Propagate,
    finalizationStrategy: SpanFinalizer.Strategy =
      SpanFinalizer.Strategy.reportAbnormal,
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

  def withParent(parent: SpanContext): SpanBuilder[F] =
    copy(parent = Parent.Explicit(parent))

  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] =
    copy(startTimestamp = Some(timestamp))

  def withFinalizationStrategy(
      strategy: SpanFinalizer.Strategy
  ): SpanBuilder[F] =
    copy(finalizationStrategy = strategy)

  def createManual: F[Span[F]] = {
    parentContext.flatMap {
      case Some(parent) =>
        for {
          back <- startSpan(makeJBuilder(parent), TimestampSelect.Delegate)
        } yield Span.fromBackend(back)

      case None =>
        Sync[F].pure(Span.fromBackend(Span.Backend.noop))
    }
  }

  def createAuto: Resource[F, Span[F]] =
    Resource.eval(parentContext).flatMap {
      case Some(parent) =>
        for {
          back <- startManaged(makeJBuilder(parent), TimestampSelect.Explicit)
        } yield Span.fromBackend(back)

      case None =>
        Resource.pure(Span.fromBackend(Span.Backend.noop))
    }

  def createRes[A](resource: Resource[F, A]): Resource[F, Span.Res[F, A]] = {
    def child(name: String, parent: JContext) =
      startManaged(
        jTracer.spanBuilder(name).setParent(parent),
        TimestampSelect.Explicit
      )

    Resource.eval(parentContext).flatMap {
      case Some(parent) =>
        for {
          rootBackend <- startManaged(
            makeJBuilder(parent),
            TimestampSelect.Explicit
          )

          rootCtx <- Resource.pure(parent.`with`(rootBackend.jSpan))

          result <- Resource.make(
            child("acquire", rootCtx).surround(resource.allocated)
          ) { case (_, release) =>
            child("release", rootCtx).surround(release)
          }

          useSpanBackend <- child("use", rootCtx)
        } yield Span.Res.fromBackend(result._1, useSpanBackend)

      case None =>
        resource.map(a => Span.Res.fromBackend(a, Span.Backend.noop))
    }
  }

  private def startManaged(
      builder: JSpanBuilder,
      timestampSelect: TimestampSelect
  ): Resource[F, SpanBackendImpl[F]] = {

    def acquire: F[SpanBackendImpl[F]] =
      startSpan(builder, timestampSelect)

    def release(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] = {
      def end: F[Unit] =
        timestampSelect match {
          case TimestampSelect.Explicit =>
            for {
              now <- Sync[F].realTime
              _ <- backend.end(now)
            } yield ()

          case TimestampSelect.Delegate =>
            backend.end
        }

      for {
        _ <- finalizationStrategy
          .lift(ec)
          .foldMapM(SpanFinalizer.run(backend, _))
        _ <- end
      } yield ()
    }

    for {
      backend <- Resource.makeCase(acquire) { case (b, ec) => release(b, ec) }
      _ <- scope.makeScope(backend.jSpan)
    } yield backend
  }

  private def startSpan(
      builder: JSpanBuilder,
      timestampSelect: TimestampSelect
  ): F[SpanBackendImpl[F]] =
    for {
      builder <- timestampSelect match {
        case TimestampSelect.Explicit if startTimestamp.isEmpty =>
          for {
            now <- Sync[F].realTime
          } yield builder.setStartTimestamp(now.length, now.unit)

        case _ =>
          Sync[F].pure(builder)
      }
      jSpan <- Sync[F].delay(builder.startSpan())
    } yield new SpanBackendImpl(
      jSpan,
      WrappedSpanContext(jSpan.getSpanContext)
    )

  private def makeJBuilder(parent: JContext): JSpanBuilder = {
    val b = jTracer
      .spanBuilder(name)
      .setAllAttributes(Conversions.toJAttributes(attributes))
      .setParent(parent)

    kind.foreach(k => b.setSpanKind(toJSpanKind(k)))
    b.setAllAttributes(Conversions.toJAttributes(attributes))
    startTimestamp.foreach(d => b.setStartTimestamp(d.length, d.unit))
    links.foreach { case (ctx, attributes) =>
      b.addLink(
        WrappedSpanContext.unwrap(ctx),
        Conversions.toJAttributes(attributes)
      )
    }

    b
  }

  private def parentContext: F[Option[JContext]] =
    parent match {
      case Parent.Root =>
        scope.current.flatMap {
          case TraceScope.Scope.Root(ctx) =>
            Sync[F].pure(Some(ctx))

          case TraceScope.Scope.Span(_, _, _) =>
            scope.root.map(s => Some(s.ctx))

          case TraceScope.Scope.Noop =>
            Sync[F].pure(None)
        }

      case Parent.Propagate =>
        scope.current.map {
          case TraceScope.Scope.Root(ctx)       => Some(ctx)
          case TraceScope.Scope.Span(ctx, _, _) => Some(ctx)
          case TraceScope.Scope.Noop            => None
        }

      case Parent.Explicit(parent) =>
        def parentSpan = JSpan.wrap(WrappedSpanContext.unwrap(parent))
        scope.current.map {
          case TraceScope.Scope.Root(ctx)       => Some(ctx.`with`(parentSpan))
          case TraceScope.Scope.Span(ctx, _, _) => Some(ctx.`with`(parentSpan))
          case TraceScope.Scope.Noop            => None
        }
    }
}

object SpanBuilderImpl {

  sealed trait TimestampSelect
  object TimestampSelect {

    /** Relies on `start` timestamp from `SpanBuilder` if defined and
      * `Clock[F].realTime` otherwise.
      *
      * Relies on `Clock[F].realTime` for the `end` timestamp.
      */
    case object Explicit extends TimestampSelect

    /** The `start` and `end` timestamps are delegated to `JSpan` and managed by
      * `startSpan()` and `end()` methods.
      *
      * The explicitly configured `start` timestamp in the `SpanBuilder` is
      * respected.
      */
    case object Delegate extends TimestampSelect
  }

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
