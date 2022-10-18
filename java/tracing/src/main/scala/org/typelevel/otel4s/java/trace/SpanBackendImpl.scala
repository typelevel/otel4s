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
package java
package trace

import cats.effect.Sync
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{StatusCode => JStatusCode}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.Status

import scala.concurrent.duration.FiniteDuration

private[java] class SpanBackendImpl[F[_]: Sync](
    val jSpan: JSpan,
    spanContext: SpanContext
) extends Span.Backend[F] {
  import SpanBackendImpl._

  val meta: InstrumentMeta[F] = InstrumentMeta.enabled

  def context: SpanContext =
    spanContext

  def addEvent(name: String, attributes: Attribute[_]*): F[Unit] =
    Sync[F].delay {
      jSpan.addEvent(name, Conversions.toJAttributes(attributes))
      ()
    }

  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: Attribute[_]*
  ): F[Unit] =
    Sync[F].delay {
      jSpan.addEvent(
        name,
        Conversions.toJAttributes(attributes),
        timestamp.length,
        timestamp.unit
      )
      ()
    }

  def setAttributes(attributes: Attribute[_]*): F[Unit] =
    Sync[F].delay {
      jSpan.setAllAttributes(Conversions.toJAttributes(attributes))
      ()
    }

  def setStatus(status: Status): F[Unit] =
    Sync[F].delay {
      jSpan.setStatus(toJStatus(status))
      ()
    }

  def setStatus(status: Status, description: String): F[Unit] =
    Sync[F].delay {
      jSpan.setStatus(toJStatus(status), description)
      ()
    }

  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit] =
    Sync[F].delay {
      jSpan.recordException(exception, Conversions.toJAttributes(attributes))
      ()
    }

  private[otel4s] def end: F[Unit] =
    Sync[F].delay(jSpan.end())

  private[otel4s] def end(timestamp: FiniteDuration): F[Unit] =
    Sync[F].delay(jSpan.end(timestamp.length, timestamp.unit))

}

private[java] object SpanBackendImpl {

  private def toJStatus(status: Status): JStatusCode =
    status match {
      case Status.Unset => JStatusCode.UNSET
      case Status.Ok    => JStatusCode.OK
      case Status.Error => JStatusCode.ERROR
    }

}
