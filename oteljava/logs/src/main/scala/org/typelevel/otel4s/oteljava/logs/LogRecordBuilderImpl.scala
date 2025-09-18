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
import io.opentelemetry.api.common.{AttributeKey => JAttributeKey}
import io.opentelemetry.api.common.{Value => JValue}
import io.opentelemetry.api.logs.{LogRecordBuilder => JLogRecordBuilder}
import io.opentelemetry.api.logs.{Severity => JSeverity}
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context

import java.time.Instant
import java.util.concurrent.TimeUnit
import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.jdk.CollectionConverters._

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
    copy(jBuilder = jBuilder.setSeverity(toJSeverity(severity)))

  def withSeverityText(severityText: String): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setSeverityText(severityText))

  def withBody(value: AnyValue): LogRecordBuilder[F, Context] =
    copy(jBuilder = toJValue(value).fold(jBuilder)(jBuilder.setBody))

  def withEventName(eventName: String): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setEventName(eventName))

  def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[F, Context] =
    copy(jBuilder = jBuilder.setAttribute(attribute.key.toJava.asInstanceOf[JAttributeKey[Any]], attribute.value))

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

  private def toJSeverity(severity: Severity): JSeverity =
    severity match {
      case Severity.Trace1 => JSeverity.TRACE
      case Severity.Trace2 => JSeverity.TRACE2
      case Severity.Trace3 => JSeverity.TRACE3
      case Severity.Trace4 => JSeverity.TRACE4

      case Severity.Debug1 => JSeverity.DEBUG
      case Severity.Debug2 => JSeverity.DEBUG2
      case Severity.Debug3 => JSeverity.DEBUG3
      case Severity.Debug4 => JSeverity.DEBUG4

      case Severity.Info1 => JSeverity.INFO
      case Severity.Info2 => JSeverity.INFO2
      case Severity.Info3 => JSeverity.INFO3
      case Severity.Info4 => JSeverity.INFO4

      case Severity.Warn1 => JSeverity.WARN
      case Severity.Warn2 => JSeverity.WARN2
      case Severity.Warn3 => JSeverity.WARN3
      case Severity.Warn4 => JSeverity.WARN4

      case Severity.Error1 => JSeverity.ERROR
      case Severity.Error2 => JSeverity.ERROR2
      case Severity.Error3 => JSeverity.ERROR3
      case Severity.Error4 => JSeverity.ERROR4

      case Severity.Fatal1 => JSeverity.FATAL
      case Severity.Fatal2 => JSeverity.FATAL2
      case Severity.Fatal3 => JSeverity.FATAL3
      case Severity.Fatal4 => JSeverity.FATAL4
    }

  private def toJValue(value: AnyValue): Option[JValue[_]] =
    value match {
      case string: AnyValue.StringValue =>
        Some(JValue.of(string.value))

      case boolean: AnyValue.BooleanValue =>
        Some(JValue.of(boolean.value))

      case long: AnyValue.LongValue =>
        Some(JValue.of(long.value))

      case double: AnyValue.DoubleValue =>
        Some(JValue.of(double.value))

      case AnyValue.ByteArrayValueImpl(bytes) =>
        Some(JValue.of(bytes))

      case list: AnyValue.SeqValue =>
        Some(JValue.of(list.value.flatMap(toJValue).asJava))

      case map: AnyValue.MapValue =>
        Some(
          JValue.of(
            map.value.flatMap { case (key, value) => toJValue(value).map(v => (key, v)) }.asJava
          )
        )

      case _: AnyValue.EmptyValue =>
        None
    }
}
