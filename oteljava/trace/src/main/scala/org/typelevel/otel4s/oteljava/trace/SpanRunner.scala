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

import cats.arrow.FunctionK
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.~>
import io.opentelemetry.api.trace.{SpanBuilder => JSpanBuilder}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.oteljava.context.LocalContext
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanFinalizer
import org.typelevel.otel4s.trace.SpanOps

private[oteljava] sealed trait SpanRunner[F[_]] {
  def start(ctx: Option[SpanRunner.RunnerContext]): Resource[F, SpanOps.Res[F]]
}

private[oteljava] object SpanRunner {

  final case class RunnerContext(
      builder: JSpanBuilder,
      parent: JContext,
      hasStartTimestamp: Boolean,
      finalizationStrategy: SpanFinalizer.Strategy
  )

  def fromLocal[F[_]: Sync: LocalContext]: SpanRunner[F] =
    new SpanRunner[F] {
      def start(ctx: Option[RunnerContext]): Resource[F, SpanOps.Res[F]] = {
        ctx match {
          case Some(RunnerContext(builder, _, hasStartTs, finalization)) =>
            startManaged(
              builder = builder,
              hasStartTimestamp = hasStartTs,
              finalizationStrategy = finalization
            ).map { case (back, nt) => SpanOps.Res(Span.fromBackend(back), nt) }

          case None =>
            Resource.pure(
              SpanOps.Res(Span.fromBackend(Span.Backend.noop), FunctionK.id)
            )
        }
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
    } yield SpanBackendImpl.fromJSpan(jSpan)

  private def startManaged[F[_]: Sync](
      builder: JSpanBuilder,
      hasStartTimestamp: Boolean,
      finalizationStrategy: SpanFinalizer.Strategy
  )(implicit L: LocalContext[F]): Resource[F, (SpanBackendImpl[F], F ~> F)] = {

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
      nt <- Resource.eval {
        L.reader { ctx =>
          new (F ~> F) {
            def apply[A](fa: F[A]): F[A] =
              L.scope(fa)(ctx.map(backend.jSpan.storeInContext))
          }
        }
      }
    } yield (backend, nt)
  }
}
