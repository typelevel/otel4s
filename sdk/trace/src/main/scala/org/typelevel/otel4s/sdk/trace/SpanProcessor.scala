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

package org.typelevel.otel4s.sdk.trace

import cats.Applicative
import cats.syntax.foldable._
import org.typelevel.otel4s.trace.SpanContext

trait SpanProcessor[F[_]] {
  def onStart(
      parentContext: Option[SpanContext],
      span: ReadWriteSpan[F]
  ): F[Unit]
  def isStartRequired: Boolean
  def onEnd(span: ReadableSpan[F]): F[Unit]
  def isEndRequired: Boolean
}

object SpanProcessor {

  private final class Noop[F[_]: Applicative] extends SpanProcessor[F] {
    def isStartRequired: Boolean = false
    def isEndRequired: Boolean = false

    def onStart(
        parentContext: Option[SpanContext],
        span: ReadWriteSpan[F]
    ): F[Unit] =
      Applicative[F].unit

    def onEnd(span: ReadableSpan[F]): F[Unit] =
      Applicative[F].unit
  }

  private final class Multi[F[_]: Applicative](
      processors: List[SpanProcessor[F]]
  ) extends SpanProcessor[F] {
    private val startOnly: List[SpanProcessor[F]] =
      processors.filter(_.isStartRequired)

    private val endOnly: List[SpanProcessor[F]] =
      processors.filter(_.isEndRequired)

    def isStartRequired: Boolean = startOnly.nonEmpty
    def isEndRequired: Boolean = endOnly.nonEmpty

    def onStart(
        parentContext: Option[SpanContext],
        span: ReadWriteSpan[F]
    ): F[Unit] =
      startOnly.traverse_(_.onStart(parentContext, span))

    def onEnd(span: ReadableSpan[F]): F[Unit] =
      endOnly.traverse_(_.onEnd(span))
  }

  def composite[F[_]: Applicative](
      processors: List[SpanProcessor[F]]
  ): SpanProcessor[F] =
    processors match {
      case Nil         => new Noop
      case head :: Nil => head
      case _           => new Multi[F](processors)
    }
}
