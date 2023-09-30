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
import cats.effect.Concurrent
import cats.effect.kernel.Clock
import cats.effect.std.AtomicCell
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.semigroup._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.data.EventData
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.data.StatusData
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.Status

import scala.concurrent.duration.FiniteDuration

final class SdkSpanBackend[F[_]: Monad: Clock](
    spanLimits: SpanLimits,
    spanProcessor: SpanProcessor[F],
    immutableState: SdkSpanBackend.ImmutableState,
    mutableState: AtomicCell[F, SdkSpanBackend.MutableState]
) extends Span.Backend[F]
    with ReadWriteSpan[F] {

  def meta: InstrumentMeta[F] = InstrumentMeta.enabled

  def context: SpanContext = immutableState.context

  // todo: apply attribute span limits
  def addAttributes(attributes: Attribute[_]*): F[Unit] =
    if (attributes.isEmpty) Applicative[F].unit
    else
      mutableState.update(s =>
        s.copy(attributes = s.attributes |+| Attributes(attributes: _*))
      )

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
      EventData.general(name, timestamp.toNanos, Attributes(attributes: _*))
    )

  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- addTimedEvent(
        EventData.exception(
          spanLimits,
          now.toNanos,
          exception,
          Attributes(attributes: _*)
        )
      )
    } yield ()

  def setStatus(status: Status): F[Unit] =
    mutableState.update(s => s.copy(status = StatusData.create(status)))

  def setStatus(status: Status, description: String): F[Unit] =
    mutableState.update(s =>
      s.copy(status = StatusData.create(status, description))
    )

  private[otel4s] def end: F[Unit] =
    for {
      now <- Clock[F].realTime
      _ <- end(now)
    } yield ()

  private[otel4s] def end(timestamp: FiniteDuration): F[Unit] =
    mutableState.update { s =>
      if (s.hasEnded) s // todo: log warn
      else s.copy(endEpochNanos = timestamp.toNanos, hasEnded = true)
    } >> spanProcessor.onEnd(this)

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

  def name: String =
    immutableState.name // todo: name can be mutated by internals

  def kind: SpanKind =
    immutableState.kind

  def scopeInfo: InstrumentationScopeInfo =
    immutableState.scopeInfo

  def spanContext: SpanContext =
    immutableState.context

  def toSpanData: F[SpanData] =
    for {
      state <- mutableState.get
    } yield new SpanData {
      def name: String = immutableState.name
      def resource: Resource = immutableState.resource
      def instrumentationScopeInfo: InstrumentationScopeInfo =
        immutableState.scopeInfo
      def kind: SpanKind = immutableState.kind
      def spanContext: SpanContext = immutableState.context
      def parentSpanContext: Option[SpanContext] = immutableState.parentContext
      def startEpochNanos: Long = immutableState.startEpochNanos
      def links: List[LinkData] = immutableState.links
      def totalRecordedLinks: Int = immutableState.totalRecordedLinks
      def status: StatusData = state.status
      def attributes: Attributes = state.attributes
      def totalAttributeCount: Int =
        state.attributes.size // todo: incorrect when limits are applied
      def events: List[EventData] = state.events
      def totalRecordedEvents: Int = state.totalRecordedEvents
      def endEpochNanos: Long = state.endEpochNanos
      def hasEnded: Boolean = state.hasEnded

    }

  def hasEnded: F[Boolean] =
    mutableState.get.map(_.hasEnded)

  def latencyNanos: F[Long] =
    for {
      state <- mutableState.get
      endEpochNanos <-
        if (state.hasEnded) Monad[F].pure(state.endEpochNanos)
        else Clock[F].realTime.map(_.toNanos)
    } yield endEpochNanos - immutableState.startEpochNanos

  def getAttribute[A](key: AttributeKey[A]): F[Option[A]] =
    for {
      state <- mutableState.get
    } yield state.attributes.get(key).map(_.value)
}

object SdkSpanBackend {

  private final case class ImmutableState(
      name: String,
      context: SpanContext,
      scopeInfo: InstrumentationScopeInfo,
      kind: SpanKind,
      parentContext: Option[SpanContext],
      resource: Resource,
      links: List[LinkData],
      totalRecordedLinks: Int,
      startEpochNanos: Long
  )

  private final case class MutableState(
      status: StatusData,
      attributes: Attributes,
      events: List[EventData],
      totalRecordedEvents: Int,
      endEpochNanos: Long,
      hasEnded: Boolean
  )

  def start[F[_]: Concurrent: Clock](
      context: SpanContext,
      name: String,
      scopeInfo: InstrumentationScopeInfo,
      resource: Resource,
      kind: SpanKind,
      //  parentSpan: Span[F],
      parentContext: Option[SpanContext],
      spanLimits: SpanLimits,
      spanProcessor: SpanProcessor[F],
      attributes: Attributes,
      links: List[LinkData],
      totalRecordedLinks: Int,
      userStartEpochNanos: Long
  ): F[SdkSpanBackend[F]] = {

    /*val (clock: Clock[F], createdAnchoredClock: Boolean) =
      if (parentSpan.isInstanceOf[Span[F]]) {
        (parentSpan.clock, false)
      } else {
        (AnchoredClock.create(tracerClock), true)
      }*/

    val createdAnchoredClock = false
    val clock = Clock[F]

    val computeNow =
      if (userStartEpochNanos != 0) {
        Monad[F].pure(userStartEpochNanos)
      } else if (createdAnchoredClock) {
        clock.realTime.map(_.toNanos) // clock.startTime
      } else {
        clock.realTime.map(_.toNanos)
      }

    def immutableState(startEpochNanos: Long) =
      ImmutableState(
        name = name,
        context = context,
        scopeInfo = scopeInfo,
        kind = kind,
        parentContext = parentContext,
        resource = resource,
        links = links,
        totalRecordedLinks = totalRecordedLinks,
        startEpochNanos = startEpochNanos
      )

    val mutableState = MutableState(
      status = StatusData.Unset,
      attributes = attributes,
      events = Nil,
      totalRecordedEvents = 0,
      endEpochNanos = 0,
      hasEnded = false
    )

    for {
      start <- computeNow
      ms <- AtomicCell[F].of(mutableState)
      backend = new SdkSpanBackend[F](
        spanLimits,
        spanProcessor,
        immutableState(start),
        ms
      )
      _ <- spanProcessor.onStart(parentContext, backend)
    } yield backend
  }
}
