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
package processor

import cats.Applicative
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

/** The span storage is used to keep the references of the active SpanRefs.
  *
  * @see
  *   [[SdkTracer.currentSpanOrNoop]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private[trace] class SpanStorage[F[_]: Applicative] private (
    storage: Ref[F, Map[SpanContext, SpanRef[F]]]
) extends SpanProcessor[F] {
  val name: String = "SpanStorage"

  def isStartRequired: Boolean = true
  def isEndRequired: Boolean = true

  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] =
    storage.update(_.updated(span.context, span))

  def onEnd(span: SpanData): F[Unit] =
    storage.update(_.removed(span.spanContext))

  def get(context: SpanContext): F[Option[SpanRef[F]]] =
    storage.get.map(_.get(context))

  def forceFlush: F[Unit] =
    Applicative[F].unit
}

private[trace] object SpanStorage {
  def create[F[_]: Concurrent]: F[SpanStorage[F]] =
    for {
      storage <- Ref[F].of(Map.empty[SpanContext, SpanRef[F]])
    } yield new SpanStorage[F](storage)
}
