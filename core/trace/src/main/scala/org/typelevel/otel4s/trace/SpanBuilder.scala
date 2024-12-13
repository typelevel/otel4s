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
import cats.arrow.FunctionK
import cats.effect.kernel.MonadCancelThrow
import cats.effect.kernel.Resource
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.trace.SpanFinalizer.Strategy

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

trait SpanBuilder[F[_]] extends SpanBuilderMacro[F] {
  import SpanBuilder.State

  /** The instrument's metadata. Indicates whether instrumentation is enabled.
    */
  def meta: InstrumentMeta[F]

  /** Modifies the state using `f` and returns the modified builder.
    *
    * @param f
    *   the modification function
    */
  def modifyState(f: State => State): SpanBuilder[F]

  /** Indicates that the span should be the root one and the scope parent should be ignored.
    */
  def root: SpanBuilder[F] =
    modifyState(_.withParent(SpanBuilder.Parent.Root))

  /** Creates [[SpanOps]] using the current state of the builder.
    */
  def build: SpanOps[F]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def mapK[G[_]: MonadCancelThrow](implicit
      F: MonadCancelThrow[F],
      kt: KindTransformer[F, G]
  ): SpanBuilder[G] =
    new SpanBuilder.MappedK(this)
}

object SpanBuilder {

  /** The parent selection strategy.
    */
  sealed trait Parent
  object Parent {

    /** Use the span context that is currently available in the scope as a parent (if any).
      */
    def propagate: Parent = Propagate

    /** A span must be the root one.
      */
    def root: Parent = Root

    /** Use the given `parent` span context as a parent.
      *
      * @param parent
      *   the parent to use
      */
    def explicit(parent: SpanContext): Parent = Explicit(parent)

    private[otel4s] case object Propagate extends Parent
    private[otel4s] case object Root extends Parent
    private[otel4s] final case class Explicit(parent: SpanContext) extends Parent
  }

  /** The state of the [[SpanBuilder]].
    */
  sealed trait State {

    /** The [[Attributes]] added to the state.
      */
    def attributes: Attributes

    /** The links added to the state.
      */
    def links: Vector[(SpanContext, Attributes)]

    /** The parent selection strategy.
      */
    def parent: Parent

    /** The selected [[SpanFinalizer.Strategy finalization strategy]].
      */
    def finalizationStrategy: SpanFinalizer.Strategy

    /** The selected [[SpanKind span kind]].
      */
    def spanKind: Option[SpanKind]

    /** The [[Attributes]] added to the state.
      */
    def startTimestamp: Option[FiniteDuration]

    /** Adds the given attribute to the state.
      *
      * @note
      *   if the state previously contained a mapping for the key, the old value is replaced by the specified value
      *
      * @param attribute
      *   the attribute to add
      */
    def addAttribute[A](attribute: Attribute[A]): State

    /** Adds attributes to the state.
      *
      * @note
      *   if the state previously contained a mapping for any of the keys, the old values are replaced by the specified
      *   values
      *
      * @param attributes
      *   the set of attributes to add
      */
    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): State

    /** Adds a link to the state.
      *
      * @param spanContext
      *   the context of the linked span
      *
      * @param attributes
      *   the set of attributes to associate with the link
      */
    def addLink(
        spanContext: SpanContext,
        attributes: immutable.Iterable[Attribute[_]]
    ): State

    /** Sets the finalization strategy.
      *
      * @param strategy
      *   the strategy to use
      */
    def withFinalizationStrategy(strategy: SpanFinalizer.Strategy): State

    /** Sets the [[SpanKind]].
      *
      * @param spanKind
      *   the kind to use
      */
    def withSpanKind(spanKind: SpanKind): State

    /** Sets an explicit start timestamp.
      *
      * @note
      *   the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to a missing span
      *
      * @param timestamp
      *   the explicit start timestamp from the epoch
      */
    def withStartTimestamp(timestamp: FiniteDuration): State

    /** Sets the parent to use.
      *
      * @param parent
      *   the parent to use
      */
    def withParent(parent: Parent): State
  }

  object State {
    private val Init =
      Impl(
        attributes = Attributes.empty,
        links = Vector.empty,
        finalizationStrategy = SpanFinalizer.Strategy.reportAbnormal,
        spanKind = None,
        startTimestamp = None,
        parent = Parent.Propagate
      )

    def init: State =
      Init

    private final case class Impl(
        attributes: Attributes,
        links: Vector[(SpanContext, Attributes)],
        finalizationStrategy: SpanFinalizer.Strategy,
        spanKind: Option[SpanKind],
        startTimestamp: Option[FiniteDuration],
        parent: Parent
    ) extends State {

      def addAttribute[A](attribute: Attribute[A]): State =
        copy(attributes = this.attributes + attribute)

      def addAttributes(attributes: immutable.Iterable[Attribute[_]]): State =
        copy(attributes = this.attributes ++ attributes)

      def addLink(
          spanContext: SpanContext,
          attributes: immutable.Iterable[Attribute[_]]
      ): State =
        copy(links = this.links :+ (spanContext, attributes.to(Attributes)))

      def withFinalizationStrategy(strategy: Strategy): State =
        copy(finalizationStrategy = strategy)

      def withSpanKind(spanKind: SpanKind): State =
        copy(spanKind = Some(spanKind))

      def withStartTimestamp(timestamp: FiniteDuration): State =
        copy(startTimestamp = Some(timestamp))

      def withParent(parent: Parent): State =
        copy(parent = parent)
    }
  }

  def noop[F[_]: Applicative](back: Span.Backend[F]): SpanBuilder[F] =
    new SpanBuilder[F] {
      private val span: Span[F] = Span.fromBackend(back)
      val meta: InstrumentMeta[F] = InstrumentMeta.disabled
      def modifyState(f: State => State): SpanBuilder[F] = this

      def build: SpanOps[F] = new SpanOps[F] {
        def startUnmanaged: F[Span[F]] =
          Applicative[F].pure(span)

        def resource: Resource[F, SpanOps.Res[F]] =
          Resource.pure(SpanOps.Res(span, FunctionK.id))

        def use[A](f: Span[F] => F[A]): F[A] = f(span)

        override def use_ : F[Unit] = Applicative[F].unit
      }
    }

  /** Implementation for [[SpanBuilder.mapK]]. */
  private class MappedK[F[_]: MonadCancelThrow, G[_]: MonadCancelThrow](
      builder: SpanBuilder[F]
  )(implicit kt: KindTransformer[F, G])
      extends SpanBuilder[G] {
    def meta: InstrumentMeta[G] =
      builder.meta.mapK[G]

    def modifyState(f: State => State): SpanBuilder[G] =
      builder.modifyState(f).mapK[G]

    def build: SpanOps[G] =
      builder.build.mapK[G]
  }
}
