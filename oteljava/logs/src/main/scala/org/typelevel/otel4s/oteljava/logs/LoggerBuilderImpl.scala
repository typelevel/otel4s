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
import org.typelevel.otel4s.logs.Logger
import org.typelevel.otel4s.logs.LoggerBuilder
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

private[oteljava] final case class LoggerBuilderImpl[F[_]: Sync: AskContext](
    jLoggerProvider: JLoggerProvider,
    name: String,
    version: Option[String] = None,
    schemaUrl: Option[String] = None
) extends LoggerBuilder.Unsealed[F, Context] {

  def withVersion(version: String): LoggerBuilder[F, Context] =
    copy(version = Option(version))

  def withSchemaUrl(schemaUrl: String): LoggerBuilder[F, Context] =
    copy(schemaUrl = Option(schemaUrl))

  def get: F[Logger[F, Context]] = Sync[F].delay {
    val b = jLoggerProvider.loggerBuilder(name)
    version.foreach(b.setInstrumentationVersion)
    schemaUrl.foreach(b.setSchemaUrl)
    new LoggerImpl(b.build())
  }
}
