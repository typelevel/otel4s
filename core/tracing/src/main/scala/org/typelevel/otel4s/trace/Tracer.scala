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
import cats.effect.kernel.Resource
import org.typelevel.otel4s.meta.InstrumentMeta

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
    * val customChild: Resource[F, Span[F]] =
    *   tracer.childScope(span.context).surround {
    *     tracer.span("custom-parent").use { span => ??? }
    *   }
    *   }}}
    *
    * @param parent
    *   the span context to use as a parent
    */
  def childScope(parent: SpanContext): Resource[F, Unit]

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
    *     _ <- tracer.rootScope.use { _ =>
    *       tracer.span("child-2").use(_ => ???) // a root span that is not associated with 'root-span'
    *     }
    *   } yield ()
    * }
    *   }}}
    */
  def rootScope: Resource[F, Unit]

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
    *     _ <- tracer.noopScope.use { _ =>
    *       tracer.span("child-2").use(_ => ???) // 'child-2' is not created at all
    *     }
    *   } yield ()
    * }
    *   }}}
    */
  def noopScope: Resource[F, Unit]

}

object Tracer {

  trait Meta[F[_]] extends InstrumentMeta[F] {
    def noopSpan: Resource[F, Span[F]]
    def noopResSpan[A](resource: Resource[F, A]): Resource[F, Span.Res[F, A]]
  }

  object Meta {

    def enabled[F[_]: Applicative]: Meta[F] = make(true)
    def disabled[F[_]: Applicative]: Meta[F] = make(false)

    private def make[F[_]: Applicative](enabled: Boolean): Meta[F] =
      new Meta[F] {
        private val noopBackend = Span.Backend.noop[F]

        val isEnabled: Boolean = enabled
        val unit: F[Unit] = Applicative[F].unit
        val noopSpan: Resource[F, Span[F]] =
          Resource.pure(Span.fromBackend(noopBackend))

        def noopResSpan[A](
            resource: Resource[F, A]
        ): Resource[F, Span.Res[F, A]] =
          resource.map(a => Span.Res.fromBackend(a, Span.Backend.noop))
      }
  }

  def noop[F[_]: Applicative]: Tracer[F] =
    new Tracer[F] {
      private val noopBackend = Span.Backend.noop
      private val builder = SpanBuilder.noop(noopBackend)
      private val resourceUnit = Resource.unit[F]
      val meta: Meta[F] = Meta.disabled
      val currentSpanContext: F[Option[SpanContext]] = Applicative[F].pure(None)
      def rootScope: Resource[F, Unit] = resourceUnit
      def noopScope: Resource[F, Unit] = resourceUnit
      def childScope(parent: SpanContext): Resource[F, Unit] = resourceUnit
      def spanBuilder(name: String): SpanBuilder[F] = builder
    }
}
