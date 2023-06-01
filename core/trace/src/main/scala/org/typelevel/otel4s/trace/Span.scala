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
import cats.data.OptionT
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.concurrent.duration.FiniteDuration

/** The API to trace an operation.
  *
  * There are two types of span lifecycle managements: manual and auto.
  *
  * ==Manual==
  * A manual span requires to be ended '''explicitly''' by invoking `end`. This
  * strategy can be used when it's necessary to end a span outside of the scope
  * (e.g. async callback). Make sure the span is ended properly.
  *
  * Leaked span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val leaked: F[Unit] =
  *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
  *     span.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  *
  * Properly ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
  *     span.setStatus(Status.Ok, "all good") >> span.end
  *   }
  * }}}
  *
  * ==Auto==
  * Unlike the manual one, the auto strategy has a fully managed lifecycle. That
  * means the span is started upon resource allocation and ended upon
  * finalization.
  *
  * Automatically ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("auto-span").build.use { span =>
  *     span.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  */
trait Span[F[_]] extends SpanMacro[F] {
  def backend: Span.Backend[F]

  /** Returns the [[SpanContext]] associated with this span.
    */
  final def context: SpanContext =
    backend.context

  /** Marks the end of [[Span]] execution.
    *
    * Only the timing of the first end call for a given span will be recorded,
    * the subsequent calls will be ignored.
    *
    * The end timestamp is based on the `Clock[F].realTime`.
    */
  final def end: F[Unit] =
    backend.end

  /** Marks the end of [[Span]] execution with the specified timestamp.
    *
    * Only the timing of the first end call for a given span will be recorded,
    * the subsequent calls will be ignored.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using
    * `Clock[F].monotonic` may lead to a missing span.
    *
    * @param timestamp
    *   the explicit timestamp from the epoch
    */
  final def end(timestamp: FiniteDuration): F[Unit] =
    backend.end(timestamp)
}

object Span {

  trait Backend[F[_]] {
    def meta: InstrumentMeta[F]
    def context: SpanContext

    def addAttributes(attributes: Attribute[_]*): F[Unit]
    def addEvent(name: String, attributes: Attribute[_]*): F[Unit]

    def addEvent(
        name: String,
        timestamp: FiniteDuration,
        attributes: Attribute[_]*
    ): F[Unit]

    def recordException(
        exception: Throwable,
        attributes: Attribute[_]*
    ): F[Unit]

    def setStatus(status: Status): F[Unit]
    def setStatus(status: Status, description: String): F[Unit]

    private[otel4s] def end: F[Unit]
    private[otel4s] def end(timestamp: FiniteDuration): F[Unit]
  }

  object Backend {
    def noop[F[_]: Applicative]: Backend[F] =
      new Backend[F] {
        private val unit = Applicative[F].unit

        val meta: InstrumentMeta[F] = InstrumentMeta.disabled
        val context: SpanContext = SpanContext.invalid

        def addAttributes(attributes: Attribute[_]*): F[Unit] = unit
        def addEvent(name: String, attributes: Attribute[_]*): F[Unit] = unit

        def addEvent(
            name: String,
            timestamp: FiniteDuration,
            attributes: Attribute[_]*
        ): F[Unit] = unit

        def recordException(
            exception: Throwable,
            attributes: Attribute[_]*
        ): F[Unit] = unit

        def setStatus(status: Status): F[Unit] = unit
        def setStatus(status: Status, description: String): F[Unit] = unit

        private[otel4s] def end: F[Unit] = unit
        private[otel4s] def end(timestamp: FiniteDuration): F[Unit] = unit
      }

    def liftOptionT[F[_]: Applicative](
        backend: Backend[F]
    ): Backend[OptionT[F, *]] =
      new Backend[OptionT[F, *]] {
        def meta: InstrumentMeta[OptionT[F, *]] =
          InstrumentMeta.liftOptionT(backend.meta)
        def context: SpanContext =
          backend.context
        def addAttributes(attributes: Attribute[_]*): OptionT[F, Unit] =
          OptionT.liftF(backend.addAttributes(attributes: _*))
        def addEvent(
            name: String,
            attributes: Attribute[_]*
        ): OptionT[F, Unit] =
          OptionT.liftF(backend.addEvent(name, attributes: _*))
        def addEvent(
            name: String,
            timestamp: FiniteDuration,
            attributes: Attribute[_]*
        ): OptionT[F, Unit] =
          OptionT.liftF(backend.addEvent(name, timestamp, attributes: _*))
        def recordException(
            exception: Throwable,
            attributes: Attribute[_]*
        ): OptionT[F, Unit] =
          OptionT.liftF(backend.recordException(exception, attributes: _*))
        def setStatus(status: Status): OptionT[F, Unit] =
          OptionT.liftF(backend.setStatus(status))
        def setStatus(status: Status, description: String): OptionT[F, Unit] =
          OptionT.liftF(backend.setStatus(status, description))
        private[otel4s] def end: OptionT[F, Unit] =
          OptionT.liftF(backend.end)
        private[otel4s] def end(timestamp: FiniteDuration): OptionT[F, Unit] =
          OptionT.liftF(backend.end(timestamp))
      }
  }

  private[otel4s] def fromBackend[F[_]](back: Backend[F]): Span[F] =
    new Span[F] {
      def backend: Backend[F] = back
    }

  /** The allocation and release stages of a supplied resource are traced by
    * separate spans. Carries a value of a wrapped resource.
    *
    * The structure of the inner spans:
    * {{{
    * > span-name
    *   > acquire
    *   > use
    *   > release
    * }}}
    */
  trait Res[F[_], A] extends Span[F] {
    def value: A
  }

  object Res {
    def unapply[F[_], A](span: Span.Res[F, A]): Option[A] =
      Some(span.value)

    private[otel4s] def fromBackend[F[_], A](
        a: A,
        back: Backend[F]
    ): Res[F, A] =
      new Res[F, A] {
        def value: A = a
        def backend: Backend[F] = back
      }

    def liftOptionT[F[_]: Applicative, A](
        res: Span.Res[F, A]
    ): Span.Res[OptionT[F, *], A] =
      new Span.Res[OptionT[F, *], A] {
        def value: A = res.value
        def backend: Backend[OptionT[F, *]] = Backend.liftOptionT(res.backend)
      }
  }

  def liftOptionT[F[_]: Applicative](span: Span[F]): Span[OptionT[F, *]] =
    span match {
      case res: Span.Res[F, _] => Span.Res.liftOptionT(res)
      case _ =>
        new Span[OptionT[F, *]] {
          def backend: Backend[OptionT[F, *]] =
            Backend.liftOptionT(span.backend)
        }
    }
}
