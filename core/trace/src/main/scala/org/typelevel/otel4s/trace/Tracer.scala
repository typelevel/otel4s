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
import cats.effect.MonadCancelThrow
import cats.~>
import org.typelevel.otel4s.meta.InstrumentMeta

@annotation.implicitNotFound("""
Could not find the `Tracer` for ${F}. `Tracer` can be one of the following:

1) No-operation (a.k.a. without tracing)

import Tracer.Implicits.noop

2) Manually from TracerProvider

val tracerProvider: TracerProvider[IO] = ???
tracerProvider
  .get("com.service.runtime")
  .flatMap { implicit tracer: Tracer[IO] => ??? }
""")
trait Tracer[F[_]] extends TracerMacro[F] {

  /** The instrument's metadata. Indicates whether instrumentation is enabled or
    * not.
    */
  def meta: Tracer.Meta[F]

  /** Returns the context of a span when it is available in the scope.
    */
  def currentSpanContext: F[Option[SpanContext]]

  /** Creates a new [[org.typelevel.otel4s.trace.SpanBuilder SpanBuilder]]. The
    * builder can be used to make a fully customized
    * [[org.typelevel.otel4s.trace.Span Span]].
    *
    * @param name
    *   the name of the span
    */
  def spanBuilder(name: String): SpanBuilder[F]

  /** Creates a new tracing scope with a custom parent. A newly created non-root
    * span will be a child of the given `parent`.
    *
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val span: Span[F] = ???
    * val customChild: F[A] =
    *   tracer.childScope(span.context) {
    *     tracer.span("custom-parent").use { span => ??? }
    *   }
    *   }}}
    *
    * @param parent
    *   the span context to use as a parent
    */
  def childScope[A](parent: SpanContext)(fa: F[A]): F[A]

  /** Creates a new tracing scope if the given `parent` is defined. A newly
    * created non-root span will be a child of the given `parent`.
    *
    * @see
    *   [[childScope]]
    *
    * @param parent
    *   the span context to use as a parent
    */
  final def childOrContinue[A](parent: Option[SpanContext])(fa: F[A]): F[A] =
    parent match {
      case Some(ctx) =>
        childScope(ctx)(fa)
      case None =>
        fa
    }

  /** Creates a new tracing scope if a parent can be extracted from the given
    * `carrier`. A newly created non-root span will be a child of the extracted
    * parent.
    *
    * If the context cannot be extracted from the `carrier`, the given effect
    * `fa` will be executed within the '''root''' span.
    *
    * To make the propagation and extraction work, you need to configure the
    * OpenTelemetry SDK. For example, you can use `OTEL_PROPAGATORS` environment
    * variable. See the official
    * [[https://opentelemetry.io/docs/reference/specification/sdk-environment-variables/#general-sdk-configuration SDK configuration guide]].
    *
    * ==Examples==
    *
    * ===Propagation via [[https://www.w3.org/TR/trace-context W3C headers]]:===
    * {{{
    * val w3cHeaders: Map[String, String] =
    *   Map("traceparent" -> "00-80f198ee56343ba864fe8b2a57d3eff7-e457b5a2e4d86bd1-01")
    *
    * Tracer[F].joinOrRoot(w3cHeaders) {
    *   Tracer[F].span("child").use { span => ??? } // a child of the external span
    * }
    * }}}
    *
    * ===Start a root span as a fallback:===
    * {{{
    * Tracer[F].span("process").surround {
    *   Tracer[F].joinOrRoot(Map.empty) { // cannot extract the context from the empty map
    *     Tracer[F].span("child").use { span => ??? } // a child of the new root span
    *   }
    * }
    * }}}
    *
    * @param carrier
    *   the carrier to extract the context from
    *
    * @tparam C
    *   the type of the carrier
    */
  def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A]

  /** Creates a new root tracing scope. The parent span will not be available
    * inside. Thus, a span created inside of the scope will be a root one.
    *
    * Can be useful, when an effect needs to be executed in the background and
    * the parent tracing info is not needed.
    *
    * @example
    *   the parent is not propagated:
    *   {{{
    * val tracer: Tracer[F] = ???
    * tracer.span("root-span").use { _ =>
    *   for {
    *     _ <- tracer.span("child-1").use(_ => ???) // a child of 'root-span'
    *     _ <- tracer.rootScope {
    *       tracer.span("child-2").use(_ => ???) // a root span that is not associated with 'root-span'
    *     }
    *   } yield ()
    * }
    *   }}}
    */
  def rootScope[A](fa: F[A]): F[A]

  /** Creates a no-op tracing scope. The tracing operations inside of the scope
    * are no-op.
    *
    * @example
    *   the parent is not propagated:
    *   {{{
    * val tracer: Tracer[F] = ???
    * tracer.span("root-span").use { _ =>
    *   for {
    *     _ <- tracer.span("child-1").use(_ => ???) // a child of 'root-span'
    *     _ <- tracer.noopScope {
    *       tracer.span("child-2").use(_ => ???) // 'child-2' is not created at all
    *     }
    *   } yield ()
    * }
    *   }}}
    */
  def noopScope[A](fa: F[A]): F[A]

  /** Propagates this tracer's context into an immutable carrier.
    *
    * @param carrier
    *   the immutable carrier to append the context to
    * @tparam C
    *   the type of the carrier
    * @return
    *   a copy of the immutable carrier with this tracer's context appended to
    *   it
    * @see
    *   [[org.typelevel.otel4s.TextMapPropagator.injected TextMapPropagator#injected]]
    */
  def propagate[C: TextMapUpdater](carrier: C): F[C]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to
    * `G`.
    */
  def mapK[G[_]: MonadCancelThrow](implicit
      F: MonadCancelThrow[F],
      kt: KindTransformer[F, G]
  ): Tracer[G] =
    new Tracer.MappedK(this)
}

object Tracer {

  def apply[F[_]](implicit ev: Tracer[F]): Tracer[F] = ev

  trait Meta[F[_]] extends InstrumentMeta[F] {
    def noopSpanBuilder: SpanBuilder[F]

    /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to
      * `G`.
      */
    def mapK[G[_]: MonadCancelThrow](implicit
        F: MonadCancelThrow[F],
        kt: KindTransformer[F, G]
    ): Meta[G] =
      new Meta.MappedK(this)
  }

  object Meta {

    def enabled[F[_]: Applicative]: Meta[F] = make(true)
    def disabled[F[_]: Applicative]: Meta[F] = make(false)

    private def make[F[_]: Applicative](enabled: Boolean): Meta[F] =
      new Meta[F] {
        private val noopBackend = Span.Backend.noop[F]

        val isEnabled: Boolean = enabled
        val unit: F[Unit] = Applicative[F].unit
        val noopSpanBuilder: SpanBuilder[F] =
          SpanBuilder.noop(noopBackend)
      }

    /** Implementation for [[Meta.mapK]]. */
    private class MappedK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](
        meta: Meta[F]
    )(implicit kt: KindTransformer[F, G])
        extends Meta[G] {
      def noopSpanBuilder: SpanBuilder[G] =
        meta.noopSpanBuilder.mapK[G]
      def isEnabled: Boolean = meta.isEnabled
      def unit: G[Unit] = kt.liftK(meta.unit)
    }
  }

  /** Creates a no-op implementation of the [[Tracer]].
    *
    * All tracing operations are no-op.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def noop[F[_]: Applicative]: Tracer[F] =
    new Tracer[F] {
      private val noopBackend = Span.Backend.noop
      private val builder = SpanBuilder.noop(noopBackend)
      val meta: Meta[F] = Meta.disabled
      val currentSpanContext: F[Option[SpanContext]] = Applicative[F].pure(None)
      def rootScope[A](fa: F[A]): F[A] = fa
      def noopScope[A](fa: F[A]): F[A] = fa
      def childScope[A](parent: SpanContext)(fa: F[A]): F[A] = fa
      def spanBuilder(name: String): SpanBuilder[F] = builder
      def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: F[A]): F[A] = fa
      def propagate[C: TextMapUpdater](carrier: C): F[C] =
        Applicative[F].pure(carrier)
    }

  /** Implementation for [[Tracer.mapK]]. */
  private class MappedK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](
      tracer: Tracer[F]
  )(implicit kt: KindTransformer[F, G])
      extends Tracer[G] {
    def meta: Meta[G] = tracer.meta.mapK[G]
    def currentSpanContext: G[Option[SpanContext]] =
      kt.liftK(tracer.currentSpanContext)
    def spanBuilder(name: String): SpanBuilder[G] =
      tracer.spanBuilder(name).mapK[G]
    def childScope[A](parent: SpanContext)(ga: G[A]): G[A] =
      kt.limitedMapK(ga)(new (F ~> F) {
        def apply[B](fb: F[B]): F[B] = tracer.childScope(parent)(fb)
      })
    def joinOrRoot[A, C: TextMapGetter](carrier: C)(ga: G[A]): G[A] =
      kt.limitedMapK(ga)(new (F ~> F) {
        def apply[B](fb: F[B]): F[B] = tracer.joinOrRoot(carrier)(fb)
      })
    def rootScope[A](ga: G[A]): G[A] =
      kt.limitedMapK(ga)(new (F ~> F) {
        def apply[B](fb: F[B]): F[B] = tracer.rootScope(fb)
      })
    def noopScope[A](ga: G[A]): G[A] =
      kt.limitedMapK(ga)(new (F ~> F) {
        def apply[B](fb: F[B]): F[B] = tracer.noopScope(fb)
      })
    def propagate[C: TextMapUpdater](carrier: C): G[C] =
      kt.liftK(tracer.propagate(carrier))
  }

  object Implicits {
    implicit def noop[F[_]: Applicative]: Tracer[F] = Tracer.noop
  }
}
