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

private[otel4s] trait SpanBuilderMacro[F[_]] { self: SpanBuilder[F] =>

  /** Adds an attribute to the newly created span. If [[SpanBuilder]] previously contained a mapping for the key, the
    * old value is replaced by the specified value.
    *
    * @param attribute
    *   the attribute to associate with the span
    */
  def addAttribute[A](attribute: Attribute[A]): SpanBuilder[F] =
    macro SpanBuilderMacro.addAttribute[A]

  /** Adds attributes to the [[SpanBuilder]]. If the SpanBuilder previously contained a mapping for any of the keys, the
    * old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  def addAttributes(attributes: Attribute[_]*): SpanBuilder[F] =
    macro SpanBuilderMacro.addAttributes

  /** Adds attributes to the [[SpanBuilder]]. If the SpanBuilder previously contained a mapping for any of the keys, the
    * old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  def addAttributes(
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    macro SpanBuilderMacro.addAttributesColl

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
  def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanBuilder[F] =
    macro SpanBuilderMacro.addLink

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
  def addLink(
      spanContext: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanBuilder[F] =
    macro SpanBuilderMacro.addLinkColl

  /** Sets the finalization strategy for the newly created span.
    *
    * The span finalizers are executed upon resource finalization.
    *
    * The default strategy is [[SpanFinalizer.Strategy.reportAbnormal]].
    *
    * @param strategy
    *   the strategy to apply upon span finalization
    */
  def withFinalizationStrategy(
      strategy: SpanFinalizer.Strategy
  ): SpanBuilder[F] =
    macro SpanBuilderMacro.withFinalizationStrategy

  /** Sets the [[SpanKind]] for the newly created span. If not called, the implementation will provide a default value
    * [[SpanKind.Internal]].
    *
    * @param spanKind
    *   the kind of the newly created span
    */
  def withSpanKind(spanKind: SpanKind): SpanBuilder[F] =
    macro SpanBuilderMacro.withSpanKind

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
  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] =
    macro SpanBuilderMacro.withStartTimestamp

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
  def withParent(parent: SpanContext): SpanBuilder[F] =
    macro SpanBuilderMacro.withParent

}

object SpanBuilderMacro {
  import scala.reflect.macros.blackbox

  def addAttribute[A](c: blackbox.Context)(
      attribute: c.Expr[Attribute[A]]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.addAttribute($attribute)")
  }

  def addAttributes(c: blackbox.Context)(
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    if (attributes.nonEmpty) {
      whenEnabled(c)(q"_.addAttributes(_root_.scala.Seq(..$attributes))")
    } else {
      c.prefix.tree
    }
  }

  def addAttributesColl(c: blackbox.Context)(
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.addAttributes($attributes)")
  }

  def addLink(c: blackbox.Context)(
      spanContext: c.Expr[SpanContext],
      attributes: c.Expr[Attribute[_]]*
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.addLink($spanContext, _root_.scala.Seq(..$attributes))")
  }

  def addLinkColl(c: blackbox.Context)(
      spanContext: c.Expr[SpanContext],
      attributes: c.Expr[immutable.Iterable[Attribute[_]]]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.addLink($spanContext, $attributes)")
  }

  def withFinalizationStrategy(c: blackbox.Context)(
      strategy: c.Expr[SpanFinalizer.Strategy]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.withFinalizationStrategy($strategy)")
  }

  def withSpanKind(c: blackbox.Context)(
      spanKind: c.Expr[SpanKind]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.withSpanKind($spanKind)")
  }

  def withStartTimestamp(c: blackbox.Context)(
      timestamp: c.Expr[FiniteDuration]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(q"_.withStartTimestamp($timestamp)")
  }

  def withParent(c: blackbox.Context)(
      parent: c.Expr[SpanContext]
  ): c.universe.Tree = {
    import c.universe._
    whenEnabled(c)(
      q"_.withParent(_root_.org.typelevel.otel4s.trace.SpanBuilder.Parent.explicit($parent))"
    )
  }

  /** Scala 2 compiler doesn't optimize chained macro calls out of the box.
    *
    * For example, the following code may not compile in some cases:
    * {{{
    *   Tracer[F]
    *     .spanBuilder("name")
    *     .addAttribute(Attribute("key", "value"))
    *     .addLink(ctx)
    *     .... // 5+ more operations
    *     .build
    * }}}
    *
    * The compilation could fail with: 'Method too large: ...'.
    *
    * By default, the chained calls are unwrapped as:
    * {{{
    *   val builder = {
    *    val builder = {
    *       val builder = Tracer[F].spanBuilder("name")
    *       if (builder.meta.isEnabled) {
    *         builder.modifyState(_.addAttribute(Attribute("key", "value")))
    *       } else {
    *         builder
    *       }
    *    }
    *    if (builder.meta.isEnabled) {
    *      builder.modifyState(_.addLink(ctx))
    *    } else {
    *      builder
    *    }
    *  }
    *  if (builder.meta.isEnabled) {
    *    // and so on
    *  } else {
    *    builder
    *  }
    * }}}
    *
    * To optimize this madness, we can inspect the current tree and chain `modify` operations instead:
    * {{{
    *  val builder = Tracer[F].spanBuilder("name")
    *  if (builder.meta.isEnabled) {
    *    builder.modifyState(_.addAttribute(Attribute("key", "value")).addLink(ctx)./*and so on*/)
    *  } else {
    *    builder
    *  }
    * }}}
    *
    * That way, we have exactly one if-else statement.
    */
  private def whenEnabled(c: blackbox.Context)(modify: c.universe.Tree): c.universe.Tree = {
    import c.universe._

    object Matchers {
      object ChainBuilder {
        def unapply(tree: Tree): Option[(Tree, Tree)] =
          tree match {
            case Typed(
                  Apply(
                    Select(left0, TermName("modifyState")),
                    List(left)
                  ),
                  _ // the type, e.g. org.typelevel.otel4s.trace.SpanBuilder[*]
                ) =>
              Some((left0, left))

            case _ =>
              None
          }
      }

      object ModifyState {
        def unapply(tree: Tree): Option[Tree] =
          tree match {
            case func: Function => // first chain call
              Some(func)

            case Apply(
                  TypeApply(Select(_, TermName("andThen")), _),
                  List(Function(_, _))
                ) => // subsequent calls
              Some(tree)

            case _ =>
              None
          }
      }
    }

    val next = c.prefix.tree match {
      case Matchers.ChainBuilder(src, Matchers.ModifyState(self)) =>
        q"""$src.modifyState($self.andThen($modify))"""

      case _ =>
        q"""${c.prefix}.modifyState($modify)"""
    }

    next
  }

}
