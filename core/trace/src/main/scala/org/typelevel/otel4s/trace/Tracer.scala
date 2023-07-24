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
import cats.effect.MonadCancelThrow
import cats.effect.kernel.MonadCancel
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

  /** Creates a new [[SpanBuilder]]. The builder can be used to make a fully
    * customized [[Span]].
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

}

object Tracer {

  def apply[F[_]](implicit ev: Tracer[F]): Tracer[F] = ev

  trait Meta[F[_]] extends InstrumentMeta[F] {
    def noopSpanBuilder: SpanBuilder[F]
  }

  object Meta {

    def enabled[F[_]: Applicative]: Meta[F] = make(true)
    def disabled[F[_]: Applicative]: Meta[F] = make(false)

    private[Tracer] def make[F[_]: Applicative](enabled: Boolean): Meta[F] =
      new Meta[F] {
        private val noopBackend = Span.Backend.noop[F]

        val isEnabled: Boolean = enabled
        val unit: F[Unit] = Applicative[F].unit
        val noopSpanBuilder: SpanBuilder[F] =
          SpanBuilder.noop(noopBackend)
      }
  }

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
    }

  def liftOptionT[F[_]](tracer: Tracer[F])(implicit
      F: MonadCancel[F, _]
  ): Tracer[OptionT[F, *]] =
    new Tracer[OptionT[F, *]] {
      def meta: Meta[OptionT[F, *]] =
        Meta.make(tracer.meta.isEnabled)

      def currentSpanContext: OptionT[F, Option[SpanContext]] =
        OptionT.liftF(tracer.currentSpanContext)

      def spanBuilder(name: String): SpanBuilder[OptionT[F, *]] =
        SpanBuilder.liftOptionT(tracer.spanBuilder(name))

      def childScope[A](parent: SpanContext)(fa: OptionT[F, A]): OptionT[F, A] =
        OptionT(tracer.childScope(parent)(fa.value))

      def joinOrRoot[A, C: TextMapGetter](carrier: C)(
          fa: OptionT[F, A]
      ): OptionT[F, A] =
        OptionT(tracer.joinOrRoot(carrier)(fa.value))

      def rootScope[A](fa: OptionT[F, A]): OptionT[F, A] =
        OptionT(tracer.rootScope(fa.value))

      def noopScope[A](fa: OptionT[F, A]): OptionT[F, A] =
        OptionT(tracer.noopScope(fa.value))
    }

  object Implicits {
    implicit def noop[F[_]: Applicative]: Tracer[F] = Tracer.noop
  }

  implicit final class TracerSyntax[F[_]](
      private val tracer: Tracer[F]
  ) extends AnyVal {

    def translate[G[_]](fk: F ~> G, gk: G ~> F)(implicit
        F: MonadCancelThrow[F],
    ): Tracer[G] =
      new Tracer[G] {
        private implicit val G: Applicative[G] =
          new Applicative[G] {
            def pure[A](x: A): G[A] =
              fk(F.pure(x))
            def ap[A, B](ff: G[A => B])(fa: G[A]): G[B] =
              fk(F.ap(gk(ff))(gk(fa)))
          }

        def meta: Meta[G] =
          Meta.make[G](tracer.meta.isEnabled)

        def currentSpanContext: G[Option[SpanContext]] =
          fk(tracer.currentSpanContext)

        def spanBuilder(name: String): SpanBuilder[G] =
          tracer.spanBuilder(name).translate(fk, gk)

        def childScope[A](parent: SpanContext)(fa: G[A]): G[A] =
          fk(tracer.childScope(parent)(gk(fa)))

        def joinOrRoot[A, C: TextMapGetter](carrier: C)(fa: G[A]): G[A] =
          fk(tracer.joinOrRoot(carrier)(gk(fa)))

        def rootScope[A](fa: G[A]): G[A] =
          fk(tracer.rootScope(gk(fa)))

        def noopScope[A](fa: G[A]): G[A] =
          fk(tracer.noopScope(gk(fa)))
      }

  }
}
