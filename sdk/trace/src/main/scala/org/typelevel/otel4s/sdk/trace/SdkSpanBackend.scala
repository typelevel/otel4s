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

package org.typelevel.otel4s.sdk
package trace

import cats.Applicative
import cats.Monad
import cats.effect.Clock
import cats.effect.Temporal
import cats.effect.std.AtomicCell
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroup._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

import scala.concurrent.duration.FiniteDuration

final class SdkSpanBackend[F[_]: Monad: Clock] private (
    spanLimits: SpanLimits,
    spanProcessor: SpanProcessor[F],
    immutableState: SdkSpanBackend.ImmutableState,
    mutableState: AtomicCell[F, SdkSpanBackend.MutableState]
) extends Span.Backend[F] {

  def meta: InstrumentMeta[F] = InstrumentMeta.enabled

  def context: SpanContext = immutableState.context

  def updateName(name: String): F[Unit] =
    mutableState.update(_.copy(name = name))

  // todo: apply attribute span limits
  def addAttributes(attributes: Attribute[_]*): F[Unit] =
    if (attributes.isEmpty) Applicative[F].unit
    else
      mutableState.update { s =>
        s.copy(attributes = s.attributes |+| Attributes(attributes: _*))
      }

  def addEvent(name: String, attributes: Attribute[_]*): F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- addEvent(name, now, attributes: _*)
    } yield ()

  // todo: apply attribute span limits
  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: Attribute[_]*
  ): F[Unit] =
    addTimedEvent(
      EventData(name, timestamp, Attributes(attributes: _*))
    )

  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- addTimedEvent(
        EventData.fromException(
          now,
          exception,
          Attributes(attributes: _*),
          false
        )
      )
    } yield ()

  def setStatus(status: Status): F[Unit] =
    mutableState.update(s => s.copy(status = StatusData(status)))

  def setStatus(status: Status, description: String): F[Unit] =
    mutableState.update(s => s.copy(status = StatusData(status, description)))

  private[otel4s] def end: F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- end(now)
    } yield ()

  private[otel4s] def end(timestamp: FiniteDuration): F[Unit] =
    mutableState.update { s =>
      if (s.endTimestamp.isDefined) s // todo: log warn
      else s.copy(endTimestamp = Some(timestamp))
    } >> spanRef.toSpanData >>= spanProcessor.onEnd

  private def addTimedEvent(event: EventData): F[Unit] =
    mutableState.update { s =>
      if (s.events.sizeIs <= spanLimits.maxNumberOfEvents) {
        s.copy(
          events = s.events :+ event,
          totalRecordedEvents = s.totalRecordedEvents + 1
        )
      } else {
        s.copy(totalRecordedEvents = s.totalRecordedEvents + 1)
      }
    }

  private val spanRef: SpanRef[F] =
    new SpanRef[F] {
      def kind: SpanKind =
        immutableState.kind

      def scopeInfo: InstrumentationScope =
        immutableState.scopeInfo

      def spanContext: SpanContext =
        immutableState.context

      def parentSpanContext: Option[SpanContext] =
        immutableState.parentContext

      // todo: name can be mutated by internals
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
          links = immutableState.links,
          /*totalRecordedEvents = state.totalRecordedEvents,
          totalRecordedLinks = immutableState.totalRecordedLinks,
          totalAttributeCount =
            state.attributes.size, // todo: incorrect when limits are applied,*/
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
        } yield state.attributes.get(key).map(_.value)
    }

}

object SdkSpanBackend {

  private final case class ImmutableState(
      context: SpanContext,
      scopeInfo: InstrumentationScope,
      kind: SpanKind,
      parentContext: Option[SpanContext],
      resource: Resource,
      links: List[LinkData],
      totalRecordedLinks: Int,
      startTimestamp: FiniteDuration
  )

  private final case class MutableState(
      name: String,
      status: StatusData,
      attributes: Attributes,
      events: List[EventData],
      totalRecordedEvents: Int,
      endTimestamp: Option[FiniteDuration]
  )

  def start[F[_]: Temporal](
      context: SpanContext,
      name: String,
      scopeInfo: InstrumentationScope,
      resource: Resource,
      kind: SpanKind,
      parentContext: Option[SpanContext],
      spanLimits: SpanLimits,
      spanProcessor: SpanProcessor[F],
      attributes: Attributes,
      links: List[LinkData],
      totalRecordedLinks: Int,
      userStartTimestamp: Option[FiniteDuration]
  ): F[SdkSpanBackend[F]] = {
    def immutableState(startTimestamp: FiniteDuration) =
      ImmutableState(
        context = context,
        scopeInfo = scopeInfo,
        kind = kind,
        parentContext = parentContext,
        resource = resource,
        links = links,
        totalRecordedLinks = totalRecordedLinks,
        startTimestamp = startTimestamp
      )

    val mutableState = MutableState(
      name = name,
      status = StatusData.Unset,
      attributes = attributes,
      events = Nil,
      totalRecordedEvents = 0,
      endTimestamp = None
    )

    for {
      start <- userStartTimestamp.fold(Clock[F].realTime)(_.pure)
      state <- AtomicCell[F].of(mutableState)
      backend = new SdkSpanBackend[F](
        spanLimits,
        spanProcessor,
        immutableState(start),
        state
      )
      _ <- spanProcessor.onStart(parentContext, backend.spanRef)
    } yield backend
  }
}
