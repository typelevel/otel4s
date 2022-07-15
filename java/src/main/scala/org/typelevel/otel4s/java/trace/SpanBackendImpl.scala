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
import cats.syntax.functor._
import io.opentelemetry.api.trace.{Span => JSpan}
import io.opentelemetry.api.trace.{StatusCode => JStatusCode}
import io.opentelemetry.api.trace.{Tracer => JTracer}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.trace._

import scala.concurrent.duration.FiniteDuration

private[otel4s] class SpanBackendImpl[F[_]](
    jTracer: JTracer,
    val jSpan: JSpan,
    scope: TraceScope[F]
)(implicit F: Sync[F])
    extends Span.Backend[F] {

  import SpanBackendImpl._

  val meta: InstrumentMeta[F] = InstrumentMeta.enabled

  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit] =
    F.delay(
      jSpan.recordException(exception, Conversions.toJAttributes(attributes))
    ).void

  def setAttributes(attributes: Attribute[_]*): F[Unit] =
    F.delay(jSpan.setAllAttributes(Conversions.toJAttributes(attributes))).void

  def setStatus(status: Status): F[Unit] =
    F.delay(jSpan.setStatus(toJStatus(status))).void

  def setStatus(status: Status, description: String): F[Unit] =
    F.delay(jSpan.setStatus(toJStatus(status), description)).void

  private[otel4s] def child(name: String): SpanBuilder[F] =
    new SpanBuilderImpl[F](
      jTracer,
      name,
      scope,
      parent = SpanBuilderImpl.Parent.Explicit(jSpan)
    )

  private[otel4s] def end: F[Unit] =
    F.delay(jSpan.end())

  private[otel4s] def end(timestamp: FiniteDuration): F[Unit] =
    F.delay(jSpan.end(timestamp.length, timestamp.unit))
}

object SpanBackendImpl {

  private def toJStatus(status: Status): JStatusCode =
    status match {
      case Status.Unset => JStatusCode.UNSET
      case Status.Ok    => JStatusCode.OK
      case Status.Error => JStatusCode.ERROR
    }

}
