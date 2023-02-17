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

import cats.mtl.Local
import cats.~>
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.context.{Context => JContext}
import org.typelevel.vault.Vault

private[java] trait TraceScope[F[_]] {
  def root: F[Scope.Root]
  def current: F[Scope]
  def makeScope(span: JSpan): F[F ~> F]
  def rootScope: F[F ~> F]
  def noopScope: F ~> F
}

private[java] object TraceScope {

  def fromLocal[F[_]](implicit L: Local[F, Vault]): TraceScope[F] = {
    val scopeRoot = Scope.Root(JContext.root())

    new TraceScope[F] {
      val root: F[Scope.Root] =
        L.applicative.pure(scopeRoot)

      def current: F[Scope] =
        L.applicative.map(L.ask[Vault])(Scope.fromContext)

      def makeScope(span: JSpan): F[F ~> F] =
        L.applicative.map(current)(scope => createScope(nextScope(scope, span)))

      def rootScope: F[F ~> F] =
        L.applicative.map(current) {
          case Scope.Root(_) =>
            createScope(scopeRoot)

          case Scope.Span(_, _) =>
            createScope(scopeRoot)

          case Scope.Noop =>
            createScope(Scope.Noop)
        }

      def noopScope: F ~> F =
        createScope(Scope.Noop)

      private def createScope(scope: Scope): F ~> F =
        new (F ~> F) {
          def apply[A](fa: F[A]): F[A] =
            L.local(fa)(scope.storeInContext)
        }

      private def nextScope(scope: Scope, span: JSpan): Scope =
        scope match {
          case Scope.Root(ctx) =>
            Scope.Span(
              ctx.`with`(span),
              span
            )

          case Scope.Span(ctx, _) =>
            Scope.Span(
              ctx.`with`(span),
              span
            )

          case Scope.Noop =>
            Scope.Noop
        }
    }
  }
}
