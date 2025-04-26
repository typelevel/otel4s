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

package org.typelevel.otel4s
package oteljava
package trace

import cats.effect.Sync
import cats.syntax.flatMap._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{StatusCode => JStatusCode}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.StatusCode

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

private[oteljava] class SpanBackendImpl[F[_]: Sync](
    val jSpan: JSpan,
    spanContext: SpanContext
) extends Span.Backend[F] {
  import SpanBackendImpl._

  val meta: InstrumentMeta.Static[F] = InstrumentMeta.Static.enabled

  def context: SpanContext =
    spanContext

  def updateName(name: String): F[Unit] =
    Sync[F].delay {
      jSpan.updateName(name)
      ()
    }

  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
    Sync[F].delay {
      jSpan.setAllAttributes(attributes.toJavaAttributes)
      ()
    }

  def addEvent(
      name: String,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    Sync[F].delay {
      jSpan.addEvent(name, attributes.toJavaAttributes)
      ()
    }

  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    Sync[F].delay {
      jSpan.addEvent(
        name,
        attributes.toJavaAttributes,
        timestamp.length,
        timestamp.unit
      )
      ()
    }

  def addLink(
      spanContext: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    Sync[F].delay {
      jSpan.addLink(
        SpanContextConversions.toJava(spanContext),
        attributes.toJavaAttributes
      )
      ()
    }

  def setStatus(status: StatusCode): F[Unit] =
    Sync[F].delay {
      jSpan.setStatus(toJStatus(status))
      ()
    }

  def setStatus(status: StatusCode, description: String): F[Unit] =
    Sync[F].delay {
      jSpan.setStatus(toJStatus(status), description)
      ()
    }

  def recordException(
      exception: Throwable,
      attributes: immutable.Iterable[Attribute[_]]
  ): F[Unit] =
    Sync[F].delay {
      jSpan.recordException(exception, attributes.toJavaAttributes)
      ()
    }

  def end: F[Unit] =
    Sync[F].realTime.flatMap(now => end(now))

  def end(timestamp: FiniteDuration): F[Unit] =
    Sync[F].delay(jSpan.end(timestamp.length, timestamp.unit))

}

private[oteljava] object SpanBackendImpl {
  def fromJSpan[F[_]: Sync](jSpan: JSpan): SpanBackendImpl[F] =
    new SpanBackendImpl(
      jSpan,
      SpanContextConversions.toScala(jSpan.getSpanContext)
    )

  private def toJStatus(status: StatusCode): JStatusCode =
    status match {
      case StatusCode.Unset => JStatusCode.UNSET
      case StatusCode.Ok    => JStatusCode.OK
      case StatusCode.Error => JStatusCode.ERROR
    }

}
