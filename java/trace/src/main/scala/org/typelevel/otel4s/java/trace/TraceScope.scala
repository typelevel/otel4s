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
import cats.effect.MonadCancelThrow
import cats.mtl.Local
import cats.~>
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.otel4s.java.trace.instances._
import org.typelevel.otel4s.trace.SpanContext

private[java] trait TraceScope[F[_]] {
  import TraceScope.Scope
  def root: F[Scope.Root]
  def current: F[Scope]
  def makeScope(span: JSpan): F[F ~> F]
  def rootScope: F[F ~> F]
  def noopScope: F ~> F
}

private[java] object TraceScope {

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

  def fromLocal[F[_]](
      default: JContext
  )(implicit L: Local[F, Scope]): TraceScope[F] = {
    val scopeRoot = Scope.Root(default)

    new TraceScope[F] {
      val root: F[Scope.Root] =
        L.applicative.pure(scopeRoot)

      def current: F[Scope] =
        L.ask[Scope]

      def makeScope(span: JSpan): F[F ~> F] =
        L.applicative.map(current)(scope => createScope(nextScope(scope, span)))

      def rootScope: F[F ~> F] =
        L.applicative.map(current) {
          case Scope.Root(_) =>
            createScope(scopeRoot)

          case Scope.Span(_, _, _) =>
            createScope(scopeRoot)

          case Scope.Noop =>
            createScope(Scope.Noop)
        }

      def noopScope: F ~> F =
        createScope(Scope.Noop)

      private def createScope(scope: Scope): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.scope(fa)(scope)
        }

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

  def fromIOLocal[F[_]: MonadCancelThrow: LiftIO](
      default: JContext
  ): F[TraceScope[F]] = {
    val scopeRoot = Scope.Root(default)
    IOLocal[Scope](scopeRoot)
      .map(implicit ioLocal => fromLocal[F](default))
      .to[F]
  }
}
