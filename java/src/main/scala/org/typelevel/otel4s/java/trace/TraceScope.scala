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

trait TraceScope[F[_]] {
  def root: F[JContext]
  def current: F[JContext]
  def make(span: JSpan): Resource[F, Unit]
  def rootScope: Resource[F, Unit]
}

object TraceScope {

  def fromIOLocal[F[_]: LiftIO: Sync](
      default: JContext
  ): F[TraceScope[F]] = {
    val lift = LiftIO.liftK

    lift(IOLocal(default)).map { local =>
      new TraceScope[F] {
        val root: F[JContext] =
          Sync[F].pure(default)

        def current: F[JContext] =
          lift(local.get)

        def make(span: JSpan): Resource[F, Unit] =
          for {
            current <- Resource.eval(lift(local.get))
            _ <- makeScope(current.`with`(span))
          } yield ()

        def rootScope: Resource[F, Unit] =
          makeScope(default)

        private def makeScope(ctx: JContext): Resource[F, Unit] =
          Resource
            .make(lift(local.getAndSet(ctx))) { previous =>
              lift(local.set(previous))
            }
            .void

      }
    }
  }

}
