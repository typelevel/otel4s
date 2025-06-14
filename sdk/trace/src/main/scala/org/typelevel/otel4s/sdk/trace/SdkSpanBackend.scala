/*
 * Copyright 2023 Typelevel
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
package sdk
package trace

import cats.Monad
import cats.effect.Clock
import cats.effect.Ref
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.sdk.trace.SdkSpanBackend.MutableState
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

/** The backend of the span that manages it's internal state. It has mutable and immutable states:
  *   - immutable state - cannot be modified at any point of the span's lifecycle, for example: span context, parent's
  *     span context, span kind, and so on
  *   - mutable state - can be modified during the lifecycle, for example: name, attributes, events, etc
  *
  * All modifications of the mutable state are ignored once the span is ended.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/api]]
  *
  * @param spanProcessor
  *   the [[SpanProcessor]] to call on span's end
  *
  * @param immutableState
  *   the immutable state of the span
  *
  * @param mutableState
  *   the mutable state of the span
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private final class SdkSpanBackend[F[_]: Monad: Clock: Console] private (
    spanLimits: SpanLimits,
    spanProcessor: SpanProcessor[F],
    spanStorage: SpanStorage[F],
    immutableState: SdkSpanBackend.ImmutableState,
    mutableState: Ref[F, SdkSpanBackend.MutableState]
) extends Span.Backend[F]
    with SpanRef[F] {

  val meta: InstrumentMeta[F] =
    InstrumentMeta.enabled

  def context: SpanContext =
    immutableState.context

  def updateName(name: String): F[Unit] =
    updateState("updateName")(_.copy(name = name)).void

  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    if (attributes.nonEmpty) {
      updateState("addAttributes") { s =>
        s.copy(attributes = s.attributes.appendAll(attributes.to(Attributes)))
      }.void
    } else {
      Monad[F].unit
    }

  def addEvent(
      name: String,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- addEvent(name, now, attributes)
    } yield ()

  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    addTimedEvent(
      EventData(
        name,
        timestamp,
        LimitedData
          .attributes(
            spanLimits.maxNumberOfAttributesPerEvent,
            spanLimits.maxAttributeValueLength
          )
          .appendAll(attributes.to(Attributes))
      )
    )

  def addLink(
      context: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    updateState("addLink") { s =>
      s.copy(links =
        s.links.append(
          LinkData(
            context,
            LimitedData
              .attributes(
                spanLimits.maxNumberOfAttributesPerLink,
                spanLimits.maxAttributeValueLength
              )
              .appendAll(attributes.to(Attributes))
          )
        )
      )
    }.void

  def recordException(
      exception: Throwable,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- addTimedEvent(
        EventData.fromException(
          now,
          exception,
          LimitedData
            .attributes(
              spanLimits.maxNumberOfAttributesPerEvent,
              spanLimits.maxAttributeValueLength
            )
            .appendAll(attributes.to(Attributes))
        )
      )
    } yield ()

  def setStatus(status: StatusCode): F[Unit] =
    updateState("setStatus") { s =>
      s.copy(status = StatusData(status))
    }.void

  def setStatus(status: StatusCode, description: String): F[Unit] =
    updateState("setStatus") { s =>
      s.copy(status = StatusData(status, description))
    }.void

  def end: F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- end(now)
    } yield ()

  def end(timestamp: FiniteDuration): F[Unit] =
    for {
      updated <- updateState("end")(s => s.copy(endTimestamp = Some(timestamp)))
      _ <- spanStorage.remove(this)
      _ <- toSpanData.flatMap(span => spanProcessor.onEnd(span)).whenA(updated)
    } yield ()

  private def addTimedEvent(event: EventData): F[Unit] =
    updateState("addEvent")(s => s.copy(events = s.events.append(event))).void

  // applies modifications while the span is still active
  // modifications are ignored when the span is ended
  private def updateState(
      method: String
  )(update: MutableState => MutableState): F[Boolean] =
    mutableState
      .modify { state =>
        if (state.endTimestamp.isDefined) (state, false)
        else (update(state), true)
      }
      .flatTap { modified =>
        if (modified) {
          Monad[F].unit
        } else {
          Console[F].println(s"SdkSpanBackend: calling [$method] on the ended span $context")
        }
      }

  // SpanRef interfaces
  def kind: SpanKind =
    immutableState.kind

  def scopeInfo: InstrumentationScope =
    immutableState.scopeInfo

  def parentSpanContext: Option[SpanContext] =
    immutableState.parentContext

  def name: F[String] =
    mutableState.get.map(_.name)

  def toSpanData: F[SpanData] =
    for {
      state <- mutableState.get
    } yield SpanData(
      name = state.name,
      spanContext = immutableState.context,
      parentSpanContext = immutableState.parentContext,
      kind = immutableState.kind,
      startTimestamp = immutableState.startTimestamp,
      endTimestamp = state.endTimestamp,
      status = state.status,
      attributes = state.attributes,
      events = state.events,
      links = state.links,
      instrumentationScope = immutableState.scopeInfo,
      resource = immutableState.resource
    )

  def hasEnded: F[Boolean] =
    mutableState.get.map(_.endTimestamp.isDefined)

  def duration: F[FiniteDuration] =
    for {
      state <- mutableState.get
      endEpochNanos <- state.endTimestamp.fold(Clock[F].realTime)(_.pure)
    } yield endEpochNanos - immutableState.startTimestamp

  def getAttribute[A](key: AttributeKey[A]): F[Option[A]] =
    for {
      state <- mutableState.get
    } yield state.attributes.elements.get(key).map(_.value)

}

private object SdkSpanBackend {

  /** Starts a new span.
    *
    * @param context
    *   the [[SpanContext]] of the span
    *
    * @param name
    *   the name of the span
    *
    * @param scopeInfo
    *   the [[InstrumentationScope]] of the span
    *
    * @param resource
    *   the [[TelemetryResource]] of the span
    *
    * @param kind
    *   the [[SpanKind]] of the span
    *
    * @param parentContext
    *   the optional parent's [[SpanContext]]
    *
    * @param processor
    *   the [[SpanProcessor]] to call on span's start and end
    *
    * @param spanStorage
    *   the `SpanStorage` to store the span at
    *
    * @param attributes
    *   the [[Attributes]] of the span
    *
    * @param links
    *   the collection of [[LinkData]] of the span
    *
    * @param userStartTimestamp
    *   the explicit start timestamp. If `None` is passed the start time will be calculated automatically (via
    *   `Clock[F].realTime`)
    */
  def start[F[_]: Temporal: Console](
      context: SpanContext,
      name: String,
      scopeInfo: InstrumentationScope,
      resource: TelemetryResource,
      kind: SpanKind,
      parentContext: Option[SpanContext],
      spanLimits: SpanLimits,
      processor: SpanProcessor[F],
      spanStorage: SpanStorage[F],
      attributes: LimitedData[Attribute[_], Attributes],
      links: LimitedData[LinkData, Vector[LinkData]],
      userStartTimestamp: Option[FiniteDuration]
  ): F[SdkSpanBackend[F]] = {
    def immutableState(startTimestamp: FiniteDuration) =
      ImmutableState(
        context = context,
        scopeInfo = scopeInfo,
        kind = kind,
        parentContext = parentContext,
        resource = resource,
        startTimestamp = startTimestamp
      )

    val mutableState = MutableState(
      name = name,
      status = StatusData.Unset,
      attributes = attributes,
      links = links,
      events = LimitedData.vector[EventData](spanLimits.maxNumberOfEvents),
      endTimestamp = None
    )

    for {
      start <- userStartTimestamp.fold(Clock[F].realTime)(_.pure)
      state <- Ref[F].of(mutableState)
      backend = new SdkSpanBackend[F](spanLimits, processor, spanStorage, immutableState(start), state)
      _ <- spanStorage.add(backend)
      _ <- processor.onStart(parentContext, backend)
    } yield backend
  }

  private final case class ImmutableState(
      context: SpanContext,
      scopeInfo: InstrumentationScope,
      kind: SpanKind,
      parentContext: Option[SpanContext],
      resource: TelemetryResource,
      startTimestamp: FiniteDuration
  )

  /** The things that may change during the lifecycle of the span.
    */
  private final case class MutableState(
      name: String,
      status: StatusData,
      attributes: LimitedData[Attribute[_], Attributes],
      events: LimitedData[EventData, Vector[EventData]],
      links: LimitedData[LinkData, Vector[LinkData]],
      endTimestamp: Option[FiniteDuration]
  )

}
