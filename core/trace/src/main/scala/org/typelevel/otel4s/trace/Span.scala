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

import cats.{Applicative, Monad, ~>}
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

/** The API to trace an operation.
  *
  * There are two types of span lifecycle managements: manual and auto.
  *
  * ==Manual==
  * A manual span requires to be ended '''explicitly''' by invoking `end`. This strategy can be used when it's necessary
  * to end a span outside of the scope (e.g. async callback). Make sure the span is ended properly.
  *
  * Leaked span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val leaked: F[Unit] =
  *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
  *     span.setStatus(StatusCode.Ok, "all good")
  *   }
  * }}}
  *
  * Properly ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").build.startUnmanaged.flatMap { span =>
  *     span.setStatus(StatusCode.Ok, "all good") >> span.end
  *   }
  * }}}
  *
  * ==Auto==
  * Unlike the manual one, the auto strategy has a fully managed lifecycle. That means the span is started upon resource
  * allocation and ended upon finalization.
  *
  * Automatically ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("auto-span").build.use { span =>
  *     span.setStatus(StatusCode.Ok, "all good")
  *   }
  * }}}
  */
trait Span[F[_]] extends SpanMacro[F] {
  def backend: Span.Backend[F]

  /** Returns the [[SpanContext]] associated with this span.
    */
  final def context: SpanContext =
    backend.context

  /** Updates the name of the [[Span]].
    *
    * @note
    *   if used, this will override the name provided via the [[SpanBuilder]].
    *
    * '''Caution''': upon this update, any sampling behavior based on span's name will depend on the implementation.
    *
    * @param name
    *   the new name of the span
    */
  final def updateName(name: String): F[Unit] =
    backend.updateName(name)

  /** Marks the end of [[Span]] execution.
    *
    * Only the timing of the first end call for a given span will be recorded, the subsequent calls will be ignored.
    *
    * The end timestamp is based on the `Clock[F].realTime`.
    */
  final def end: F[Unit] =
    backend.end

  /** Marks the end of [[Span]] execution with the specified timestamp.
    *
    * Only the timing of the first end call for a given span will be recorded, the subsequent calls will be ignored.
    *
    * @note
    *   the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to a missing span.
    *
    * @param timestamp
    *   the explicit timestamp from the epoch
    */
  final def end(timestamp: FiniteDuration): F[Unit] =
    backend.end(timestamp)

  /** Modify the context `F` using the transformation `f`. */
  def mapK[G[_]: Monad](f: F ~> G): Span[G] = Span.fromBackend(backend.mapK(f))

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  final def mapK[G[_]: Monad](implicit kt: KindTransformer[F, G]): Span[G] =
    mapK(kt.liftK)
}

object Span {

  trait Backend[F[_]] {
    def meta: InstrumentMeta[F]
    def context: SpanContext

    def updateName(name: String): F[Unit]

    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): F[Unit]
    def addEvent(
        name: String,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit]

    def addEvent(
        name: String,
        timestamp: FiniteDuration,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit]

    def addLink(
        spanContext: SpanContext,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit]

    def recordException(
        exception: Throwable,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit]

    def setStatus(status: StatusCode): F[Unit]
    def setStatus(status: StatusCode, description: String): F[Unit]

    def end: F[Unit]
    def end(timestamp: FiniteDuration): F[Unit]

    /** Modify the context `F` using the transformation `f`. */
    def mapK[G[_]: Monad](f: F ~> G): Backend[G] = new Backend.MappedK(this)(f)

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
      */
    final def mapK[G[_]: Monad](implicit kt: KindTransformer[F, G]): Backend[G] =
      mapK(kt.liftK)
  }

  object Backend {

    /** Returns a non-recording backend that holds the provided [[SpanContext]] but all operations have no effect. The
      * span will not be exported and all tracing operations are no-op, but it can be used to propagate a valid
      * [[SpanContext]] downstream.
      *
      * @param context
      *   the context to propagate
      */
    private[otel4s] def propagating[F[_]: Applicative](meta: InstrumentMeta[F], context: SpanContext): Backend[F] =
      make(meta, context)

    def noop[F[_]: Applicative]: Backend[F] =
      make(InstrumentMeta.disabled, SpanContext.invalid)

    private def make[F[_]: Applicative](
        m: InstrumentMeta[F],
        ctx: SpanContext
    ): Backend[F] =
      new Backend[F] {
        private val unit = Applicative[F].unit

        val meta: InstrumentMeta[F] = m
        val context: SpanContext = ctx

        def updateName(name: String): F[Unit] = unit
        def addAttributes(attributes: immutable.Iterable[Attribute[_]]): F[Unit] = unit
        def addEvent(name: String, attributes: immutable.Iterable[Attribute[_]]): F[Unit] = unit
        def addEvent(name: String, timestamp: FiniteDuration, attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
          unit

        def addLink(spanContext: SpanContext, attributes: immutable.Iterable[Attribute[_]]): F[Unit] = unit
        def recordException(exception: Throwable, attributes: immutable.Iterable[Attribute[_]]): F[Unit] = unit

        def setStatus(status: StatusCode): F[Unit] = unit
        def setStatus(status: StatusCode, description: String): F[Unit] = unit

        def end: F[Unit] = unit
        def end(timestamp: FiniteDuration): F[Unit] = unit
      }

    /** Implementation for [[Backend.mapK]]. */
    private class MappedK[F[_], G[_]: Monad](backend: Backend[F])(f: F ~> G) extends Backend[G] {
      def meta: InstrumentMeta[G] =
        backend.meta.mapK(f)
      def context: SpanContext = backend.context
      def updateName(name: String): G[Unit] =
        f(backend.updateName(name))
      def addAttributes(attributes: immutable.Iterable[Attribute[_]]): G[Unit] =
        f(backend.addAttributes(attributes))
      def addEvent(
          name: String,
          attributes: immutable.Iterable[Attribute[_]]
      ): G[Unit] =
        f(backend.addEvent(name, attributes))
      def addEvent(
          name: String,
          timestamp: FiniteDuration,
          attributes: immutable.Iterable[Attribute[_]]
      ): G[Unit] =
        f(backend.addEvent(name, timestamp, attributes))
      def addLink(
          spanContext: SpanContext,
          attributes: immutable.Iterable[Attribute[_]]
      ): G[Unit] =
        f(backend.addLink(spanContext, attributes))
      def recordException(
          exception: Throwable,
          attributes: immutable.Iterable[Attribute[_]]
      ): G[Unit] =
        f(backend.recordException(exception, attributes))
      def setStatus(status: StatusCode): G[Unit] =
        f(backend.setStatus(status))
      def setStatus(status: StatusCode, description: String): G[Unit] =
        f(backend.setStatus(status, description))
      def end: G[Unit] = f(backend.end)
      def end(timestamp: FiniteDuration): G[Unit] =
        f(backend.end(timestamp))
    }
  }

  private[otel4s] def fromBackend[F[_]](back: Backend[F]): Span[F] =
    new Span[F] {
      def backend: Backend[F] = back
    }
}
