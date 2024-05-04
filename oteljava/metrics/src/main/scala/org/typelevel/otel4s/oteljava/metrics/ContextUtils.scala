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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.Sync
import cats.mtl.Ask
import cats.syntax.flatMap._
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

private object ContextUtils {

  def delayWithContext[F[_]: Sync: AskContext, A](f: () => A): F[A] =
    Ask[F, Context].ask.flatMap { ctx =>
      Sync[F].delay {
        // make the current context active
        val scope = ctx.underlying.makeCurrent()
        try {
          f()
        } finally {
          scope.close() // release the context
        }
      }
    }

}
