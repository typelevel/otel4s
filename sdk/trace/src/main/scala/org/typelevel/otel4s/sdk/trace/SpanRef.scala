/*
 * Copyright 2023 Typelevel
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
package sdk
package trace

import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

import scala.concurrent.duration.FiniteDuration

/** An extended Span interface that provides access to internal state.
  *
  * Since the span's internal state can be mutated during the lifetime, some
  * operations are effectful.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait SpanRef[F[_]] { self: Span.Backend[F] =>

  /** Returns the kind of the span. */
  def kind: SpanKind

  /** Returns the instrumentation scope specified when creating the tracer which
    * produced this span.
    */
  def scopeInfo: InstrumentationScope

  /** Returns the parent's span context of the span. */
  def parentSpanContext: Option[SpanContext]

  /** Returns the name of the span.
    *
    * @note
    *   the name of the span can be changed during the lifetime of the span by
    *   using [[org.typelevel.otel4s.trace.Span.updateName Span.updateName]], so
    *   this value cannot be cached.
    */
  def name: F[String]

  /** Returns an immutable instance of the [[data.SpanData SpanData]], for use
    * in export.
    */
  def toSpanData: F[SpanData]

  /** Indicates whether the span has already been ended. */
  def hasEnded: F[Boolean]

  /** Returns the duration of the span.
    *
    * If still active then returns `Clock[F].realTime - start` time.
    */
  def duration: F[FiniteDuration]

  /** Returns the attribute value for the given `key`. Returns `None` if the key
    * is absent in the storage.
    *
    * @note
    *   the attribute values can be changed during the lifetime of the span by
    *   using
    *   [[org.typelevel.otel4s.trace.Span.addAttribute Span.addAttribute]], so
    *   this value cannot be cached.
    */
  def getAttribute[A](key: AttributeKey[A]): F[Option[A]]

}
