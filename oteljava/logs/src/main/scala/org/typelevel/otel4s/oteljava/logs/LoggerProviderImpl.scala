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
import io.opentelemetry.api.logs.{LoggerProvider => JLoggerProvider}
import org.typelevel.otel4s.logs.LoggerBuilder
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

private[oteljava] final class LoggerProviderImpl[F[_]: Sync: AskContext](
    jLoggerProvider: JLoggerProvider
) extends LoggerProvider.Unsealed[F, Context] {

  def logger(name: String): LoggerBuilder[F, Context] =
    LoggerBuilderImpl(jLoggerProvider, name)

}
