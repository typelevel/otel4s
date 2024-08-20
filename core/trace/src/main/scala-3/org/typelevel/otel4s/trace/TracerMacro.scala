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
    * val customParent: SpanOps[F] = tracer
    *   .spanBuilder("custom-parent", Attribute("key", "value"))
    *   .withParent(span.context)
    *   .build
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
  ): SpanOps[F] =
    spanBuilder(name).addAttributes(attributes).build

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
    * val customParent: SpanOps[F] = tracer
    *   .spanBuilder("custom-parent", Attributes(Attribute("key", "value")))
    *   .withParent(span.context)
    *   .build
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
      inline attributes: immutable.Iterable[Attribute[_]]
  ): SpanOps[F] =
    spanBuilder(name).addAttributes(attributes).build

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
  ): SpanOps[F] =
    spanBuilder(name).addAttributes(attributes).root.build

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
      inline attributes: immutable.Iterable[Attribute[_]]
  ): SpanOps[F] =
    spanBuilder(name).addAttributes(attributes).root.build

}
