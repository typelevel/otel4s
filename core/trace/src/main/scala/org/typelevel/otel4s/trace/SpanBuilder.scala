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
import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Resource

import scala.concurrent.duration.FiniteDuration

trait SpanBuilder[F[_]] {
  type Result <: Span[F]
  type Builder = SpanBuilder.Aux[F, Result]

  /** Adds an attribute to the newly created span. If [[SpanBuilder]] previously
    * contained a mapping for the key, the old value is replaced by the
    * specified value.
    *
    * @param attribute
    *   the attribute to associate with the span
    */
  def addAttribute[A](attribute: Attribute[A]): Builder

  /** Adds attributes to the [[SpanBuilder]]. If the SpanBuilder previously
    * contained a mapping for any of the keys, the old values are replaced by
    * the specified values.
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  def addAttributes(attributes: Attribute[_]*): Builder

  /** Adds a link to the newly created span.
    *
    * Links are used to link spans in different traces. Used (for example) in
    * batching operations, where a single batch handler processes multiple
    * requests from different traces or the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associate with the link
    */
  def addLink(spanContext: SpanContext, attributes: Attribute[_]*): Builder

  /** Sets the finalization strategy for the newly created span.
    *
    * The span finalizers are executed upon resource finalization.
    *
    * The default strategy is [[SpanFinalizer.Strategy.reportAbnormal]].
    *
    * @param strategy
    *   the strategy to apply upon span finalization
    */
  def withFinalizationStrategy(strategy: SpanFinalizer.Strategy): Builder

  /** Sets the [[SpanKind]] for the newly created span. If not called, the
    * implementation will provide a default value [[SpanKind.Internal]].
    *
    * @param spanKind
    *   the kind of the newly created span
    */
  def withSpanKind(spanKind: SpanKind): Builder

  /** Sets an explicit start timestamp for the newly created span.
    *
    * Use this method to specify an explicit start timestamp. If not called, the
    * implementation will use the timestamp value from the method called on
    * [[build]], which should be the default case.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using
    * `Clock[F].monotonic` may lead to a missing span.
    *
    * @param timestamp
    *   the explicit start timestamp from the epoch
    */
  def withStartTimestamp(timestamp: FiniteDuration): Builder

  /** Indicates that the span should be the root one and the scope parent should
    * be ignored.
    */
  def root: Builder

  /** Sets the parent to use from the specified [[SpanContext]]. If not set, the
    * span that is currently available in the scope will be used as parent.
    *
    * '''Note''': if called multiple times, only the last specified value will
    * be used.
    *
    * '''Note''': the previous call of [[root]] will be ignored.
    *
    * @param parent
    *   the span context to use as a parent
    */
  def withParent(parent: SpanContext): Builder

  /** Wraps the given resource to trace it upon the start.
    *
    * The span is started upon resource allocation and ended upon finalization.
    * The allocation and release stages of the `resource` are traced by separate
    * spans. Carries a value of the given `resource`.
    *
    * The structure of the inner spans:
    * {{{
    * > span-name
    *   > acquire
    *   > use
    *   > release
    * }}}
    *
    * The finalization strategy is determined by [[SpanFinalizer.Strategy]]. By
    * default, the abnormal termination (error, cancelation) is recorded.
    *
    * @see
    *   default finalization strategy [[SpanFinalizer.Strategy.reportAbnormal]]
    * @example
    *   {{{
    * val tracer: Tracer[F] = ???
    * val resource: Resource[F, String] = Resource.eval(Sync[F].delay("string"))
    * val ok: F[Unit] =
    *   tracer.spanBuilder("wrapped-resource").wrapResource(resource).build.use { case span @ Span.Res(value) =>
    *     span.setStatus(Status.Ok, s"all good. resource value: $${value}")
    *   }
    *   }}}
    * @param resource
    *   the resource to trace
    */
  def wrapResource[A](
      resource: Resource[F, A]
  )(implicit ev: Result =:= Span[F]): SpanBuilder.Aux[F, Span.Res[F, A]]

  def build: SpanOps.Aux[F, Result]
}

object SpanBuilder {

  type Aux[F[_], A] = SpanBuilder[F] {
    type Result = A
  }

  def noop[F[_]: MonadCancelThrow](
      back: Span.Backend[F]
  ): SpanBuilder.Aux[F, Span[F]] =
    make(back, Resource.pure(Span.fromBackend(back)))

  private def make[F[_]: MonadCancelThrow, Res <: Span[F]](
      back: Span.Backend[F],
      startSpan: Resource[F, Res]
  ): SpanBuilder.Aux[F, Res] =
    new SpanBuilder[F] {
      type Result = Res

      private val span: Span[F] = Span.fromBackend(back)

      def wrapResource[A](
          resource: Resource[F, A]
      )(implicit ev: Result =:= Span[F]): SpanBuilder.Aux[F, Span.Res[F, A]] =
        make(
          back,
          resource.map(r => Span.Res.fromBackend(r, back))
        )

      def addAttribute[A](attribute: Attribute[A]): Builder = this

      def addAttributes(attributes: Attribute[_]*): Builder = this

      def addLink(ctx: SpanContext, attributes: Attribute[_]*): Builder = this

      def root: Builder = this

      def withFinalizationStrategy(strategy: SpanFinalizer.Strategy): Builder =
        this

      def withParent(parent: SpanContext): Builder = this

      def withSpanKind(spanKind: SpanKind): Builder = this

      def withStartTimestamp(timestamp: FiniteDuration): Builder = this

      def build: SpanOps.Aux[F, Result] = new SpanOps[F] {
        type Result = Res

        def startUnmanaged(implicit ev: Result =:= Span[F]): F[Span[F]] =
          Applicative[F].pure(span)

        def use[A](f: Res => F[A]): F[A] =
          startSpan.use(res => f(res))

        def use_ : F[Unit] =
          startSpan.use_

        def surround[A](fa: F[A]): F[A] =
          fa
      }
    }

}
