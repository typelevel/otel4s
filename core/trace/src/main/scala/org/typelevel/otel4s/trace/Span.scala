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
import cats.~>
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.concurrent.duration.FiniteDuration

/** An implementation detail of [[Span]]. Users SHOULD NOT refer to this type.
  */
sealed abstract class SpanAPI[F[_]] extends SpanMacro[F] {
  def backend: Span.Backend[F]

  /** Returns the [[SpanContext]] associated with this span. */
  final def context: SpanContext =
    backend.context

  /** Updates the name of the [[Span]].
    *
    * '''Note''': if used, this will override the name provided via the
    * [[SpanBuilder]].
    *
    * '''Caution''': upon this update, any sampling behavior based on span's
    * name will depend on the implementation.
    *
    * @param name
    *   the new name of the span
    */
  final def updateName(name: String): F[Unit] =
    backend.updateName(name)
}

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
final class Span[F[_]] private (val backend: Span.Backend[F])
    extends SpanAPI[F] {

  /** Modify the context `F` using the transformation `f`. */
  def mapK[G[_]](f: F ~> G): Span[G] = Span.fromBackend(backend.mapK(f))

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to
    * `G`.
    */
  def mapK[G[_]](implicit kt: KindTransformer[F, G]): Span[G] = mapK(kt.liftK)
}

object Span {

  trait Backend[F[_]] {
    def meta: InstrumentMeta[F]
    def context: SpanContext

    def updateName(name: String): F[Unit]

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

    /** Modify the context `F` using the transformation `f`. */
    def mapK[G[_]](f: F ~> G): Backend[G] = new Backend.MappedK(this)(f)
  }

  object Backend {
    def noop[F[_]: Applicative]: Backend[F] =
      new Backend[F] {
        private val unit = Applicative[F].unit

        val meta: InstrumentMeta[F] = InstrumentMeta.disabled
        val context: SpanContext = SpanContext.invalid

        def updateName(name: String): F[Unit] = unit

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

    /** Implementation for [[Backend.mapK]]. */
    private class MappedK[F[_], G[_]](backend: Backend[F])(f: F ~> G)
        extends Backend[G] {
      def meta: InstrumentMeta[G] =
        backend.meta.mapK(f)
      def context: SpanContext = backend.context
      def updateName(name: String): G[Unit] =
        f(backend.updateName(name))
      def addAttributes(attributes: Attribute[_]*): G[Unit] =
        f(backend.addAttributes(attributes: _*))
      def addEvent(name: String, attributes: Attribute[_]*): G[Unit] =
        f(backend.addEvent(name, attributes: _*))
      def addEvent(
          name: String,
          timestamp: FiniteDuration,
          attributes: Attribute[_]*
      ): G[Unit] =
        f(backend.addEvent(name, timestamp, attributes: _*))
      def recordException(
          exception: Throwable,
          attributes: Attribute[_]*
      ): G[Unit] =
        f(backend.recordException(exception, attributes: _*))
      def setStatus(status: Status): G[Unit] =
        f(backend.setStatus(status))
      def setStatus(status: Status, description: String): G[Unit] =
        f(backend.setStatus(status, description))
      private[otel4s] def end: G[Unit] = f(backend.end)
      private[otel4s] def end(timestamp: FiniteDuration): G[Unit] =
        f(backend.end(timestamp))
    }
  }

  /** The API for a manually managed trace operation, including the ability to
    * end the operation. A `Span.Manual` can be converted into a [[Span]] by
    * calling [[unmanageable]], and the `Span` type should be used for the
    * majority of operations on spans.
    *
    * @see
    *   [[Span]]
    */
  final class Manual[F[_]] private (val backend: Backend[F])
      extends SpanAPI[F] {

    /** This span, without the ability to end it. */
    lazy val unmanageable: Span[F] = Span.fromBackend(backend)

    /** Marks the end of span execution.
      *
      * Only the timing of the first end call for a given span will be recorded,
      * the subsequent calls will be ignored.
      *
      * The end timestamp is based on the `Clock[F].realTime`.
      */
    def end: F[Unit] =
      backend.end

    /** Marks the end of span execution with the specified timestamp.
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
    def end(timestamp: FiniteDuration): F[Unit] =
      backend.end(timestamp)

    /** Modify the context `F` using the transformation `f`. */
    def mapK[G[_]](f: F ~> G): Manual[G] = Manual.fromBackend(backend.mapK(f))

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to
      * `G`.
      */
    def mapK[G[_]](implicit kt: KindTransformer[F, G]): Manual[G] =
      mapK(kt.liftK)
  }

  private[otel4s] object Manual {
    private[otel4s] def fromBackend[F[_]](backend: Backend[F]): Manual[F] =
      new Manual(backend)
  }

  private[otel4s] def fromBackend[F[_]](backend: Backend[F]): Span[F] =
    new Span(backend)
}
