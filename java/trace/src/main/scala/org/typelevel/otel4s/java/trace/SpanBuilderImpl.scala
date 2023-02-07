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

import cats.~>
import cats.arrow.FunctionK
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
import org.typelevel.otel4s.trace.SpanFinalizer.Strategy
import org.typelevel.otel4s.trace.SpanKind

import scala.concurrent.duration.FiniteDuration

private[java] final case class SpanBuilderImpl[F[_]: Sync, Res <: Span[F]](
    jTracer: JTracer,
    name: String,
    scope: TraceScope[F],
    runner: SpanBuilderImpl.Runner[F, Res],
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

  def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): Builder =
    copy(links = links :+ (spanContext, attributes))

  def root: Builder =
    copy(parent = Parent.Root)

  def withParent(parent: SpanContext): Builder =
    copy(parent = Parent.Explicit(parent))

  def withStartTimestamp(timestamp: FiniteDuration): Builder =
    copy(startTimestamp = Some(timestamp))

  def withFinalizationStrategy(
      strategy: SpanFinalizer.Strategy
  ): Builder =
    copy(finalizationStrategy = strategy)

  def wrapResource[A](
      resource: Resource[F, A]
  )(implicit ev: Result =:= Span[F]): SpanBuilder.Aux[F, Span.Res[F, A]] =
    copy(runner = SpanBuilderImpl.Runner.resource(resource, jTracer))

  def startUnmanaged(implicit ev: Result =:= Span[F]): F[Span[F]] =
    parentContext.flatMap {
      case Some(parent) =>
        for {
          back <- Runner.startSpan(
            makeJBuilder(parent),
            TimestampSelect.Delegate,
            startTimestamp
          )
        } yield Span.fromBackend(back)

      case None =>
        Sync[F].pure(Span.fromBackend(Span.Backend.noop))
    }

  def use[A](f: Res => F[A]): F[A] =
    start.use { case (span, nt) => nt(f(span)) }

  def use_ : F[Unit] =
    start.use_

  def surround[A](fa: F[A]): F[A] =
    start.surround(fa)

  /*  private def start: Resource[F, (Span[F], F ~> F)] =
    Resource.eval(parentContext).flatMap {
      case Some(parent) =>
        for {
          (back, nt) <- startManaged(
            makeJBuilder(parent),
            TimestampSelect.Explicit
          )
        } yield (Span.fromBackend(back), nt)*/

  private def start: Resource[F, (Res, F ~> F)] =
    Resource.eval(parentContext).flatMap { parent =>
      runner.start(
        parent.map(p => (makeJBuilder(p), p)),
        startTimestamp,
        finalizationStrategy,
        scope
      )
    }

  private def makeJBuilder(parent: JContext): JSpanBuilder = {
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

private[java] object SpanBuilderImpl {

  sealed trait Runner[F[_], Res] {
    def start(
        builder: Option[(JSpanBuilder, JContext)],
        startTimestamp: Option[FiniteDuration],
        finalizationStrategy: SpanFinalizer.Strategy,
        scope: TraceScope[F]
    ): Resource[F, (Res, F ~> F)]
  }

  object Runner {

    def span[F[_]: Sync]: Runner[F, Span[F]] =
      new Runner[F, Span[F]] {
        def start(
            builder: Option[(JSpanBuilder, JContext)],
            startTimestamp: Option[FiniteDuration],
            finalizationStrategy: SpanFinalizer.Strategy,
            scope: TraceScope[F]
        ): Resource[F, (Span[F], F ~> F)] =
          builder match {
            case Some((builder, _)) =>
              for {
                (back, nt) <- startManaged(
                  builder,
                  TimestampSelect.Explicit,
                  startTimestamp,
                  finalizationStrategy,
                  scope
                )
              } yield (Span.fromBackend(back), nt)

            case None =>
              Resource.pure((Span.fromBackend(Span.Backend.noop), FunctionK.id))
          }
      }

    def resource[F[_]: Sync, A](
        resource: Resource[F, A],
        jTracer: JTracer
    ): Runner[F, Span.Res[F, A]] =
      new Runner[F, Span.Res[F, A]] {
        def start(
            builder: Option[(JSpanBuilder, JContext)],
            startTimestamp: Option[FiniteDuration],
            finalizationStrategy: Strategy,
            scope: TraceScope[F]
        ): Resource[F, (Span.Res[F, A], F ~> F)] = {
          def child(name: String, parent: JContext): Resource[F, (SpanBackendImpl[F], F ~> F)] =
            startManaged(
              jTracer.spanBuilder(name).setParent(parent),
              TimestampSelect.Explicit,
              startTimestamp,
              finalizationStrategy,
              scope
            )

          builder match {
            case Some((builder, parent)) =>
              for {
                rootBackend <- startManaged(
                  builder,
                  TimestampSelect.Explicit,
                  startTimestamp,
                  finalizationStrategy,
                  scope
                )

                rootCtx <- Resource.pure(parent.`with`(rootBackend._1.jSpan))

                (value, _) <- Resource.make(
                  child("acquire", rootCtx).use(b => b._2(resource.allocated))
                ) { case (_, release) =>
                  child("release", rootCtx).use(b => b._2(release))
                }

                (useSpanBackend, nt) <- child("use", rootCtx)
              } yield (Span.Res.fromBackend(value, useSpanBackend), nt)

            case None =>
              resource.map(a => (Span.Res.fromBackend(a, Span.Backend.noop), FunctionK.id))
          }
        }
      }

  private def startManaged[F[_]: Sync](
      builder: JSpanBuilder,
      timestampSelect: TimestampSelect,
      startTimestamp: Option[FiniteDuration],
      finalizationStrategy: SpanFinalizer.Strategy,
      scope: TraceScope[F]
  ): Resource[F, (SpanBackendImpl[F], F ~> F)] = {

    def acquire: F[SpanBackendImpl[F]] =
      startSpan(builder, timestampSelect, startTimestamp)

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
      nt <- Resource.eval(scope.makeScope(backend.jSpan))
    } yield (backend, nt)
  }

  def startSpan[F[_]: Sync](
      builder: JSpanBuilder,
      timestampSelect: TimestampSelect,
      startTimestamp: Option[FiniteDuration]
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


  }

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
