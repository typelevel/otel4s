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
import cats.syntax.flatMap._
import io.opentelemetry.api.logs.{LogRecordBuilder => JLogRecordBuilder}
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.AnyValueConverters._
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

private[oteljava] final case class LogRecordBuilderImpl[F[_]: Sync: AskContext](
    jBuilder: JLogRecordBuilder
) extends LogRecordBuilder.Unsealed[F, Context] {

  def withTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setTimestamp(timestamp.toNanos, TimeUnit.NANOSECONDS))

  def withTimestamp(timestamp: Instant): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setTimestamp(timestamp))

  def withObservedTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setObservedTimestamp(timestamp.toNanos, TimeUnit.NANOSECONDS))

  def withObservedTimestamp(timestamp: Instant): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setObservedTimestamp(timestamp))

  def withContext(context: Context): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setContext(context.underlying))

  def withSeverity(severity: Severity): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setSeverity(SeverityConversions.toJava(severity)))

  def withSeverityText(severityText: String): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setSeverityText(severityText))

  def withBody(value: AnyValue): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setBody(value.toJava))

  def withEventName(eventName: String): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setEventName(eventName))

  def withException(exception: Throwable): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setException(exception))

  def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[F, Context] =
    addAttributes(attribute)

  def addAttributes(attributes: Attribute[_]*): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setAllAttributes(attributes.toJavaAttributes))

  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setAllAttributes(attributes.toJavaAttributes))

  def emit: F[Unit] =
    Ask[F, Context].ask.flatMap { ctx =>
      Sync[F].delay {
        // make the current context active
        val scope = ctx.underlying.makeCurrent()
        try {
          jBuilder.emit()
        } finally {
          scope.close() // release the context
        }
      }
    }

}
