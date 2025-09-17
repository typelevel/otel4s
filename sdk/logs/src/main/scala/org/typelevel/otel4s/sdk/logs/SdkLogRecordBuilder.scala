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

package org.typelevel.otel4s.sdk.logs

import cats.effect.Temporal
import cats.mtl.Ask
import cats.syntax.all._
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.LogRecordBuilder
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor

import java.time.Instant
import scala.collection.immutable
import scala.concurrent.duration._

private final case class SdkLogRecordBuilder[F[_]: Temporal: AskContext](
    processor: LogRecordProcessor[F],
    instrumentationScope: InstrumentationScope,
    resource: TelemetryResource,
    traceContextLookup: TraceContext.Lookup,
    state: SdkLogRecordBuilder.State
) extends LogRecordBuilder.Unsealed[F, Context] {

  def withTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Context] =
    copy(state = state.copy(timestamp = Some(timestamp)))

  def withTimestamp(timestamp: Instant): LogRecordBuilder[F, Context] =
    copy(state = state.copy(timestamp = Some(timestamp.toEpochMilli.millis)))

  def withObservedTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Context] =
    copy(state = state.copy(observedTimestamp = Some(timestamp)))

  def withObservedTimestamp(timestamp: Instant): LogRecordBuilder[F, Context] =
    copy(state = state.copy(observedTimestamp = Some(timestamp.toEpochMilli.millis)))

  def withContext(context: Context): LogRecordBuilder[F, Context] =
    copy(state = state.copy(context = Some(context)))

  def withSeverity(severity: Severity): LogRecordBuilder[F, Context] =
    copy(state = state.copy(severity = Some(severity)))

  def withSeverityText(severityText: String): LogRecordBuilder[F, Context] =
    copy(state = state.copy(severityText = Some(severityText)))

  def withBody(body: AnyValue): LogRecordBuilder[F, Context] =
    copy(state = state.copy(body = Some(body)))

  def withEventName(eventName: String): LogRecordBuilder[F, Context] =
    copy(state = state.copy(eventName = Some(eventName)))

  def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[F, Context] =
    copy(state = state.copy(attributes = state.attributes.append(attribute)))

  def addAttributes(attributes: Attribute[_]*): LogRecordBuilder[F, Context] =
    copy(state = state.copy(attributes = state.attributes.appendAll(attributes.to(Attributes))))

  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): LogRecordBuilder[F, Context] =
    copy(state = state.copy(attributes = state.attributes.appendAll(attributes.to(Attributes))))

  def emit: F[Unit] =
    for {
      context <- Ask[F, Context].ask
      observedTimestamp <- state.observedTimestamp.fold(Temporal[F].realTime)(Temporal[F].pure(_))
      record <- LogRecordRef.create(toLogRecord(observedTimestamp, context))
      _ <- processor.onEmit(context, record)
    } yield ()

  private def toLogRecord(observedTimestamp: FiniteDuration, context: Context): LogRecordData =
    LogRecordData(
      timestamp = state.timestamp,
      observedTimestamp = observedTimestamp,
      traceContext = traceContextLookup.get(state.context.getOrElse(context)),
      severity = state.severity,
      severityText = state.severityText,
      body = state.body,
      eventName = state.eventName,
      attributes = state.attributes,
      instrumentationScope = instrumentationScope,
      resource = resource
    )
}

private object SdkLogRecordBuilder {

  private val DefaultEmptyState = emptyState(LogRecordLimits.default)

  def empty[F[_]: Temporal: AskContext](
      processor: LogRecordProcessor[F],
      instrumentationScope: InstrumentationScope,
      resource: TelemetryResource,
      traceContextLookup: TraceContext.Lookup,
      limits: LogRecordLimits
  ): SdkLogRecordBuilder[F] =
    SdkLogRecordBuilder(
      processor = processor,
      instrumentationScope = instrumentationScope,
      resource = resource,
      traceContextLookup = traceContextLookup,
      state = if (limits == LogRecordLimits.default) DefaultEmptyState else emptyState(limits)
    )

  private def emptyState(limits: LogRecordLimits): State = State(
    timestamp = None,
    observedTimestamp = None,
    context = None,
    severity = None,
    severityText = None,
    body = None,
    eventName = None,
    attributes = LimitedData.attributes(
      limits.maxNumberOfAttributes,
      limits.maxAttributeValueLength
    )
  )

  private[SdkLogRecordBuilder] final case class State(
      timestamp: Option[FiniteDuration],
      observedTimestamp: Option[FiniteDuration],
      context: Option[Context],
      severity: Option[Severity],
      severityText: Option[String],
      body: Option[AnyValue],
      eventName: Option[String],
      attributes: LimitedData[Attribute[_], Attributes]
  )

}
