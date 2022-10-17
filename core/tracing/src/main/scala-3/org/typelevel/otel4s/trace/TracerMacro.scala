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

import cats.effect.kernel.Resource

import scala.quoted.*

private[otel4s] trait TracerMacro[F[_]] {
  self: Tracer[F] =>

  /** Creates a new child span. The span is automatically attached to a parent
    * span (based on the scope).
    *
    * The lifecycle of the span is managed automatically. That means the span is
    * ended upon the finalization of a resource.
    *
    * The abnormal termination (error, cancelation) is recorded by
    * [[SpanFinalizer.Strategy.reportAbnormal default finalization strategy]].
    *
    * To attach span to a specific parent, use [[childScope]] or
    * [[SpanBuilder.withParent]].
    *
    * @example
    *   attaching span to a specific parent
    *   {{{
    * val tracer: Tracer[F] = ???
    * val span: Span[F] = ???
    * val customParent: Resource[F, Span.Auto[F]] = tracer
    *   .spanBuilder("custom-parent")
    *   .withParent(span.context)
    *   .createAuto
    *   }}}
    *
    * @see
    *   [[spanBuilder]] to make a fully manual span (explicit end)
    *
    * @param name
    *   the name of the span
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  inline def span(
      inline name: String,
      inline attributes: Attribute[_]*
  ): Resource[F, Span[F]] =
    ${ TracerMacro.span('self, 'name, 'attributes) }

  /** Creates a new root span. Even if a parent span is available in the scope,
    * the span is created without a parent.
    *
    * The lifecycle of the span is managed automatically. That means the span is
    * ended upon the finalization of a resource.
    *
    * The abnormal termination (error, cancelation) is recorded by
    * [[SpanFinalizer.Strategy.reportAbnormal default finalization strategy]].
    *
    * @param name
    *   the name of the span
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  inline def rootSpan(
      inline name: String,
      inline attributes: Attribute[_]*
  ): Resource[F, Span[F]] =
    ${ TracerMacro.rootSpan('self, 'name, 'attributes) }

  /** Creates a new child span. The span is automatically attached to a parent
    * span (based on the scope).
    *
    * The lifecycle of the span is managed automatically. That means the span is
    * ended upon the finalization of a resource.
    *
    * The abnormal termination (error, cancelation) is recorded by
    * [[SpanFinalizer.Strategy.reportAbnormal default finalization strategy]].
    *
    * The structure of the inner spans:
    * {{{
    * > name
    *   > acquire
    *   > use
    *   > release
    * }}}
    *
    * @param name
    *   the name of the span
    *
    * @param attributes
    *   the set of attributes to associate with the span
    */
  inline def resourceSpan[A](
      inline name: String,
      inline attributes: Attribute[_]*
  )(inline resource: Resource[F, A]): Resource[F, Span.Res[F, A]] =
    ${ TracerMacro.resourceSpan('self, 'name, 'attributes, 'resource) }

}

object TracerMacro {

  def span[F[_]](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer.spanBuilder($name).withAttributes($attributes*).createAuto
      else $tracer.meta.noopSpan
    }

  def rootSpan[F[_]](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]]
  )(using Quotes, Type[F]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer.spanBuilder($name).root.withAttributes($attributes*).createAuto
      else $tracer.meta.noopSpan
    }

  def resourceSpan[F[_], A](
      tracer: Expr[Tracer[F]],
      name: Expr[String],
      attributes: Expr[Seq[Attribute[_]]],
      resource: Expr[Resource[F, A]]
  )(using Quotes, Type[F], Type[A]) =
    '{
      if ($tracer.meta.isEnabled)
        $tracer
          .spanBuilder($name)
          .withAttributes($attributes*)
          .createRes($resource)
      else $tracer.meta.noopResSpan($resource)
    }

}
