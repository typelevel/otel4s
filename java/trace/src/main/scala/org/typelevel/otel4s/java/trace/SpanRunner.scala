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

import cats.arrow.FunctionK
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.~>
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanFinalizer

private[java] sealed trait SpanRunner[F[_], Res <: Span[F]] {
  def start(ctx: Option[SpanRunner.RunnerContext]): Resource[F, (Res, F ~> F)]
}

private[java] object SpanRunner {

  final case class RunnerContext(
      builder: JSpanBuilder,
      parent: JContext,
      hasStartTimestamp: Boolean,
      finalizationStrategy: SpanFinalizer.Strategy
  )

  def span[F[_]: Sync](scope: TraceScope[F]): SpanRunner[F, Span[F]] =
    new SpanRunner[F, Span[F]] {
      def start(ctx: Option[RunnerContext]): Resource[F, (Span[F], F ~> F)] = {
        ctx match {
          case Some(RunnerContext(builder, _, hasStartTs, finalization)) =>
            startManaged(
              builder = builder,
              hasStartTimestamp = hasStartTs,
              finalizationStrategy = finalization,
              scope = scope
            ).map { case (back, nt) => (Span.fromBackend(back), nt) }

          case None =>
            Resource.pure((Span.fromBackend(Span.Backend.noop), FunctionK.id))
        }
      }
    }

  def resource[F[_]: Sync, A](
      scope: TraceScope[F],
      resource: Resource[F, A],
      jTracer: JTracer
  ): SpanRunner[F, Span.Res[F, A]] =
    new SpanRunner[F, Span.Res[F, A]] {
      def start(
          ctx: Option[RunnerContext]
      ): Resource[F, (Span.Res[F, A], F ~> F)] =
        ctx match {
          case Some(RunnerContext(builder, parent, hasStartTimestamp, fin)) =>
            def child(
                name: String,
                parent: JContext
            ): Resource[F, (SpanBackendImpl[F], F ~> F)] =
              startManaged(
                builder = jTracer.spanBuilder(name).setParent(parent),
                hasStartTimestamp = false,
                finalizationStrategy = fin,
                scope = scope
              )

            for {
              rootBackend <- startManaged(
                builder = builder,
                hasStartTimestamp = hasStartTimestamp,
                finalizationStrategy = fin,
                scope = scope
              )

              rootCtx <- Resource.pure(parent.`with`(rootBackend._1.jSpan))

              pair <- Resource.make(
                child("acquire", rootCtx).use(b => b._2(resource.allocated))
              ) { case (_, release) =>
                child("release", rootCtx).use(b => b._2(release))
              }
              (value, _) = pair

              pair2 <- child("use", rootCtx)
              (useSpanBackend, nt) = pair2
            } yield (Span.Res.fromBackend(value, useSpanBackend), nt)

          case None =>
            resource.map(a =>
              (Span.Res.fromBackend(a, Span.Backend.noop), FunctionK.id)
            )
        }

    }

  def startUnmanaged[F[_]: Sync](context: Option[RunnerContext]): F[Span[F]] =
    context match {
      case Some(RunnerContext(builder, _, ts, _)) =>
        for {
          back <- SpanRunner.startSpan(builder, ts)
        } yield Span.fromBackend(back)

      case None =>
        Sync[F].pure(Span.fromBackend(Span.Backend.noop))
    }

  private def startSpan[F[_]: Sync](
      b: JSpanBuilder,
      hasStartTimestamp: Boolean
  ): F[SpanBackendImpl[F]] =
    for {
      builder <-
        if (hasStartTimestamp) Sync[F].pure(b)
        else Sync[F].realTime.map(t => b.setStartTimestamp(t.length, t.unit))
      jSpan <- Sync[F].delay(builder.startSpan())
    } yield new SpanBackendImpl(
      jSpan,
      WrappedSpanContext.wrap(jSpan.getSpanContext)
    )

  private def startManaged[F[_]: Sync](
      builder: JSpanBuilder,
      hasStartTimestamp: Boolean,
      finalizationStrategy: SpanFinalizer.Strategy,
      scope: TraceScope[F]
  ): Resource[F, (SpanBackendImpl[F], F ~> F)] = {

    def acquire: F[SpanBackendImpl[F]] =
      startSpan(builder, hasStartTimestamp)

    def release(backend: Span.Backend[F], ec: Resource.ExitCase): F[Unit] =
      for {
        _ <- finalizationStrategy
          .lift(ec)
          .foldMapM(SpanFinalizer.run(backend, _))
        _ <- backend.end
      } yield ()

    for {
      backend <- Resource.makeCase(acquire) { case (b, ec) => release(b, ec) }
      nt <- Resource.eval(scope.makeScope(backend.jSpan))
    } yield (backend, nt)
  }

}
