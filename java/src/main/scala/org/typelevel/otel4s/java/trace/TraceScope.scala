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

package org.typelevel.otel4s.java.trace

import cats.effect.IOLocal
import cats.effect.LiftIO
import cats.effect.Resource
import cats.effect.Sync
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.trace.SpanContext

trait TraceScope[F[_]] {
  import TraceScope.Scope
  def root: F[Scope.Root]
  def current: F[Scope]
  def makeScope(span: JSpan): Resource[F, Unit]
  def rootScope: Resource[F, Unit]
  def noopScope: Resource[F, Unit]
}

object TraceScope {

  sealed trait Scope
  object Scope {
    final case class Root(ctx: JContext) extends Scope
    final case class Span(
        ctx: JContext,
        span: JSpan,
        spanContext: SpanContext
    ) extends Scope
    case object Noop extends Scope
  }

  def fromIOLocal[F[_]: LiftIO: Sync](
      default: JContext
  ): F[TraceScope[F]] = {
    val scopeRoot = Scope.Root(default)

    IOLocal[Scope](scopeRoot).to[F].map { local =>
      new TraceScope[F] {
        val root: F[Scope.Root] =
          Sync[F].pure(scopeRoot)

        def current: F[Scope] =
          local.get.to[F]

        def makeScope(span: JSpan): Resource[F, Unit] =
          for {
            current <- Resource.eval(current)
            _ <- createScope(nextScope(current, span))
          } yield ()

        def rootScope: Resource[F, Unit] =
          Resource.eval(current).flatMap {
            case Scope.Root(_) =>
              createScope(scopeRoot)

            case Scope.Span(_, _, _) =>
              createScope(scopeRoot)

            case Scope.Noop =>
              createScope(Scope.Noop)
          }

        def noopScope: Resource[F, Unit] =
          createScope(Scope.Noop)

        private def createScope(scope: Scope): Resource[F, Unit] =
          Resource
            .make(local.getAndSet(scope).to[F])(p => local.set(p).to[F])
            .void

        private def nextScope(scope: Scope, span: JSpan): Scope =
          scope match {
            case Scope.Root(ctx) =>
              Scope.Span(
                ctx.`with`(span),
                span,
                WrappedSpanContext(span.getSpanContext)
              )

            case Scope.Span(ctx, _, _) =>
              Scope.Span(
                ctx.`with`(span),
                span,
                WrappedSpanContext(span.getSpanContext)
              )

            case Scope.Noop =>
              Scope.Noop
          }

      }
    }
  }

}
