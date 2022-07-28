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
package trace

import cats.Applicative
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.concurrent.duration.FiniteDuration

/** The API to trace an operation.
  *
  * There are two types of span: [[Span.Manual]] and [[Span.Auto]].
  *
  * ==[[Span.Manual]]==
  * The manual span requires an ''explicit'' end. Manual span can be used when
  * it's necessary to end a span outside of the resource scope (i.e. async
  * callback). Make sure the span is ended properly
  *
  * Leaked span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val leaked: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     this.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  *
  * Properly ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     this.setStatus(Status.Ok, "all good") >> span.end
  *   }
  * }}}
  *
  * ==[[Span.Auto]]==
  * Unlike [[Span.Manual]] the auto span has a fully managed lifecycle. That
  * means the span is started upon resource allocation and ended upon
  * finalization. Abnormal terminations (error, cancelation) are recorded as
  * well.
  *
  * Automatically ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createAuto.use { span =>
  *     this.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  */
trait Span[F[_]] extends SpanMacro[F] {
  def backend: Span.Backend[F]

  final def setStatus(status: Status): F[Unit] =
    backend.setStatus(status)

  final def setStatus(status: Status, description: String): F[Unit] =
    backend.setStatus(status, description)

  /** Returns trace identifier of the current span.
    *
    * @return
    *   `Some` for a valid span and `None` for noop
    */
  final def traceId: Option[String] =
    backend.traceId

  /** Returns span identifier of the current span.
    *
    * @return
    *   `Some` for a valid span and `None` for noop
    */
  final def spanId: Option[String] =
    backend.spanId

}

object Span {

  trait Backend[F[_]] {
    def meta: InstrumentMeta[F]

    def recordException(
        exception: Throwable,
        attributes: Attribute[_]*
    ): F[Unit]

    def setAttributes(attributes: Attribute[_]*): F[Unit]
    def setStatus(status: Status): F[Unit]
    def setStatus(status: Status, description: String): F[Unit]

    def traceId: Option[String]
    def spanId: Option[String]

    private[otel4s] def child(name: String): SpanBuilder[F]
    private[otel4s] def end: F[Unit]
    private[otel4s] def end(timestamp: FiniteDuration): F[Unit]
  }

  trait Auto[F[_]] extends Span[F]

  trait Manual[F[_]] extends Span[F] {

    final def end: F[Unit] =
      backend.end

    final def end(timestamp: FiniteDuration): F[Unit] =
      backend.end(timestamp)

  }

  def noopAuto[F[_]: Applicative]: Auto[F] =
    new Auto[F] {
      val backend: Backend[F] = noopBackend
    }

  def noopBackend[F[_]: Applicative]: Backend[F] =
    new Backend[F] {
      private val unit = Applicative[F].unit
      private val noopBuilder = SpanBuilder.noop(this)

      val meta: InstrumentMeta[F] = InstrumentMeta.disabled

      val traceId: Option[String] = None
      val spanId: Option[String] = None

      def recordException(
          exception: Throwable,
          attributes: Attribute[_]*
      ): F[Unit] = unit

      def setStatus(status: Status): F[Unit] = unit
      def setStatus(status: Status, description: String): F[Unit] = unit
      def setAttributes(attributes: Attribute[_]*): F[Unit] = unit

      private[otel4s] def child(name: String): SpanBuilder[F] = noopBuilder
      private[otel4s] def end: F[Unit] = unit
      private[otel4s] def end(timestamp: FiniteDuration): F[Unit] = unit
    }

}
