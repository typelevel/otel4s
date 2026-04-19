/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s
package oteljava
package logs

import cats.effect.Sync
import cats.mtl.Ask
import io.opentelemetry.api.logs.{Logger => JLogger}
import io.opentelemetry.api.logs.{Severity => JSeverity}
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.logs.Logger
import org.typelevel.otel4s.logs.meta.InstrumentMeta
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

private[oteljava] final class LoggerImpl[F[_]: Sync: AskContext](jLogger: JLogger) extends Logger.Unsealed[F, Context] {
  val meta: InstrumentMeta[F, Context] =
    InstrumentMeta.dynamic { (ctx, severity, _) =>
      Sync[F].delay(
        jLogger.isEnabled(
          severity.fold(JSeverity.UNDEFINED_SEVERITY_NUMBER)(SeverityConversions.toJava),
          ctx.underlying
        )
      )
    }

  def currentContext: F[Context] = Ask[F, Context].ask

  def logRecordBuilder: LogRecordBuilder[F, Context] =
    new LogRecordBuilderImpl[F](jLogger.logRecordBuilder())
}
