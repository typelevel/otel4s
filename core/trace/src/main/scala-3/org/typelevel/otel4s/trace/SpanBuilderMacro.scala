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

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration
import scala.quoted.*

private[otel4s] trait SpanBuilderMacro[F[_]] { self: SpanBuilder[F] =>

  /** Adds an attribute to the newly created span. If [[SpanBuilder]] previously contained a mapping for the key, the
    * old value is replaced by the specified value.
    *
    * @param attribute
    *   the attribute to associate with the span
    */
  inline def addAttribute[A](inline attribute: Attribute[A]): SpanBuilder[F] =
    ${ SpanBuilderMacro.addAttribute('self, 'attribute) }

  /** Adds attributes to the [[SpanBuilder]]. If the SpanBuilder previously contained a mapping for any of the keys, the
    * old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  inline def addAttributes(inline attributes: Attribute[_]*): SpanBuilder[F] =
    ${ SpanBuilderMacro.addAttributes('self, 'attributes) }

  /** Adds attributes to the [[SpanBuilder]]. If the SpanBuilder previously contained a mapping for any of the keys, the
    * old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  inline def addAttributes(
      inline attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    ${ SpanBuilderMacro.addAttributes('self, 'attributes) }

  /** Adds a link to the newly created span.
    *
    * Links are used to link spans in different traces. Used (for example) in batching operations, where a single batch
    * handler processes multiple requests from different traces or the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associate with the link
    */
  inline def addLink(
      inline spanContext: SpanContext,
      inline attributes: Attribute[_]*
  ): SpanBuilder[F] =
    ${ SpanBuilderMacro.addLink('self, 'spanContext, 'attributes) }

  /** Adds a link to the newly created span.
    *
    * Links are used to link spans in different traces. Used (for example) in batching operations, where a single batch
    * handler processes multiple requests from different traces or the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associate with the link
    */
  inline def addLink(
      inline spanContext: SpanContext,
      inline attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    ${ SpanBuilderMacro.addLink('self, 'spanContext, 'attributes) }

  /** Sets the finalization strategy for the newly created span.
    *
    * The span finalizers are executed upon resource finalization.
    *
    * The default strategy is [[SpanFinalizer.Strategy.reportAbnormal]].
    *
    * @param strategy
    *   the strategy to apply upon span finalization
    */
  inline def withFinalizationStrategy(
      inline strategy: SpanFinalizer.Strategy
  ): SpanBuilder[F] =
    ${ SpanBuilderMacro.withFinalizationStrategy('self, 'strategy) }

  /** Sets the [[SpanKind]] for the newly created span. If not called, the implementation will provide a default value
    * [[SpanKind.Internal]].
    *
    * @param spanKind
    *   the kind of the newly created span
    */
  inline def withSpanKind(inline spanKind: SpanKind): SpanBuilder[F] =
    ${ SpanBuilderMacro.withSpanKind('self, 'spanKind) }

  /** Sets an explicit start timestamp for the newly created span.
    *
    * Use this method to specify an explicit start timestamp. If not called, the implementation will use the timestamp
    * value from the method called on [[build]], which should be the default case.
    *
    * @note
    *   the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to a missing span.
    *
    * @param timestamp
    *   the explicit start timestamp from the epoch
    */
  inline def withStartTimestamp(
      inline timestamp: FiniteDuration
  ): SpanBuilder[F] =
    ${ SpanBuilderMacro.withStartTimestamp('self, 'timestamp) }

  /** Sets the parent to use from the specified [[SpanContext]]. If not set, the span that is currently available in the
    * scope will be used as parent.
    *
    * @note
    *   if called multiple times, only the last specified value will be used.
    *
    * @note
    *   the previous call of [[root]] will be ignored.
    *
    * @param parent
    *   the span context to use as a parent
    */
  inline def withParent(inline parent: SpanContext): SpanBuilder[F] =
    ${ SpanBuilderMacro.withParent('self, 'parent) }

}

object SpanBuilderMacro {

  def addAttribute[F[_], A](
      builder: Expr[SpanBuilder[F]],
      attribute: Expr[Attribute[A]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      $builder.modifyState(_.addAttributes(List($attribute)))
    }

  def addAttributes[F[_]](
      builder: Expr[SpanBuilder[F]],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    (attributes: @unchecked) match {
      case Varargs(args) if args.isEmpty =>
        builder
      case other =>
        '{ $builder.modifyState(_.addAttributes($attributes)) }
    }

  def addLink[F[_]](
      builder: Expr[SpanBuilder[F]],
      spanContext: Expr[SpanContext],
      attributes: Expr[immutable.Iterable[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      $builder.modifyState(_.addLink($spanContext, $attributes))
    }

  def withFinalizationStrategy[F[_]](
      builder: Expr[SpanBuilder[F]],
      strategy: Expr[SpanFinalizer.Strategy]
  )(using Quotes, Type[F]) =
    '{
      $builder.modifyState(_.withFinalizationStrategy($strategy))
    }

  def withSpanKind[F[_]](
      builder: Expr[SpanBuilder[F]],
      kind: Expr[SpanKind]
  )(using Quotes, Type[F]) =
    '{
      $builder.modifyState(_.withSpanKind($kind))
    }

  def withStartTimestamp[F[_]](
      builder: Expr[SpanBuilder[F]],
      timestamp: Expr[FiniteDuration]
  )(using Quotes, Type[F]) =
    '{
      $builder.modifyState(_.withStartTimestamp($timestamp))
    }

  def withParent[F[_]](
      builder: Expr[SpanBuilder[F]],
      parent: Expr[SpanContext]
  )(using Quotes, Type[F]) =
    '{
      $builder.modifyState(
        _.withParent(
          _root_.org.typelevel.otel4s.trace.SpanBuilder.Parent.explicit($parent)
        )
      )
    }

}
