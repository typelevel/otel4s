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

import cats.effect.Resource

import scala.concurrent.duration.FiniteDuration

trait SpanBuilder[F[_]] {

  def withSpanKind(spanKind: SpanKind): SpanBuilder[F]
  def withAttribute[A](attribute: Attribute[A]): SpanBuilder[F]
  def withAttributes(attributes: Attribute[_]*): SpanBuilder[F]
  def withLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanBuilder[F]
  def root: SpanBuilder[F]
  def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F]

  def createManual: Resource[F, Span.Manual[F]]
  def createAuto: Resource[F, Span.Auto[F]]

}

object SpanBuilder {

  def noop[F[_]](back: Span.Backend[F]): SpanBuilder[F] =
    new SpanBuilder[F] {
      private val manual: Span.Manual[F] = new Span.Manual[F] {
        def backend: Span.Backend[F] = back
      }
      private val auto: Span.Auto[F] = new Span.Auto[F] {
        def backend: Span.Backend[F] = back
      }

      def withSpanKind(spanKind: SpanKind): SpanBuilder[F] = this
      def withAttribute[A](attribute: Attribute[A]): SpanBuilder[F] = this
      def withAttributes(attributes: Attribute[_]*): SpanBuilder[F] = this
      def withLink(
          spanContext: SpanContext,
          attributes: Attribute[_]*
      ): SpanBuilder[F] = this
      def root: SpanBuilder[F] = this
      def withStartTimestamp(timestamp: FiniteDuration): SpanBuilder[F] = this

      val createManual: Resource[F, Span.Manual[F]] =
        Resource.pure(manual)

      val createAuto: Resource[F, Span.Auto[F]] =
        Resource.pure(auto)
    }
}
