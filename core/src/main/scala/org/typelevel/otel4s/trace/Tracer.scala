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

/** The API is responsible for creating [[Span]]'s.
  */
trait Tracer[F[_]] extends TracerMacro[F] {

  /** Instrument metadata. Indicates whether instrumentation is enabled or not.
    */
  def meta: Tracer.Meta[F]

  /** Returns trace identifier of a span that is available in a scope.
    */
  def traceId: F[Option[String]]

  /** Returns span identifier of a span that is available in a scope.
    */
  def spanId: F[Option[String]]

  /** Creates a new [[SpanBuilder]]. The builder can be used to make a fully
    * customized [[Span]].
    *
    * @param name
    *   the name of the span
    */
  def spanBuilder(name: String): SpanBuilder[F]

  /** Creates a new tracer as a child of the given span.
    *
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val span: Span[F] = ???
    * val customChild: Resource[F, Span[F]] = tracer.childOf(span).span("custom-parent")
    *   }}}
    *
    * @param span
    *   the parent span
    */
  def childOf(span: Span[F]): Tracer[F]

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
    * or no-op.
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
    def noopAutoSpan: Resource[F, Span.Auto[F]]
    def noopResSpan[A](resource: Resource[F, A]): Resource[F, Span.Res[F, A]]
  }

  object Meta {

    def enabled[F[_]: Applicative]: Meta[F] = make(true)
    def disabled[F[_]: Applicative]: Meta[F] = make(false)

    private def make[F[_]: Applicative](enabled: Boolean): Meta[F] =
      new Meta[F] {
        private val noopBackend = Span.noopBackend[F]

        val isEnabled: Boolean = enabled
        val unit: F[Unit] = Applicative[F].unit
        val resourceUnit: Resource[F, Unit] = Resource.unit
        val noopAutoSpan: Resource[F, Span.Auto[F]] =
          Resource.pure(Span.Auto.fromBackend(noopBackend))

        def noopResSpan[A](
            resource: Resource[F, A]
        ): Resource[F, Span.Res[F, A]] =
          resource.map(a => Span.Res.fromBackend(a, Span.noopBackend))
      }

  }

  def noop[F[_]](implicit F: Applicative[F]): Tracer[F] =
    new Tracer[F] {
      private val noopBackend = Span.noopBackend
      private val builder = SpanBuilder.noop(noopBackend)
      val meta: Meta[F] = Meta.disabled
      val traceId: F[Option[String]] = F.pure(None)
      val spanId: F[Option[String]] = F.pure(None)
      val rootScope: Resource[F, Unit] = Resource.unit
      def noopScope: Resource[F, Unit] = Resource.unit
      def spanBuilder(name: String): SpanBuilder[F] = builder
      def childOf(span: Span[F]): Tracer[F] = this
    }
}
