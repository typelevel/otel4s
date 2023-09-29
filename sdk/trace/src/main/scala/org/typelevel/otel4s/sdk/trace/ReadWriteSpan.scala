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

import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.Span
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

trait ReadWriteSpan[F[_]] extends ReadableSpan[F] { self: Span.Backend[F] => }

trait ReadableSpan[F[_]] {
  def name: String
  def kind: SpanKind
  def scopeInfo: InstrumentationScopeInfo
  def spanContext: SpanContext
  // def parentSpanContext: SpanContext
  def toSpanData: F[SpanData]
  def hasEnded: F[Boolean]
  def latencyNanos: F[Long]
  def getAttribute[A](key: AttributeKey[A]): F[Option[A]]
}
