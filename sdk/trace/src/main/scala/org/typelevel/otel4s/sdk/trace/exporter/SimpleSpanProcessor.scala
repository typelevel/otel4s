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

package org.typelevel.otel4s.sdk
package trace
package exporter

import cats.Monad
import cats.effect.Concurrent
import cats.effect.Resource
import cats.effect.std.Supervisor
import cats.syntax.applicative._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

/** An implementation of the [[SpanProcessor]] that passes
  * [[data.SpanData SpanData]] directly to the configured exporter.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
final class SimpleSpanProcessor[F[_]: Monad] private (
    supervisor: Supervisor[F],
    exporter: SpanExporter[F],
    sampled: Boolean
) extends SpanProcessor[F] {

  val isStartRequired: Boolean = false
  val isEndRequired: Boolean = true

  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] =
    Monad[F].unit

  def onEnd(span: SpanData): F[Unit] = {
    val canExport = !sampled || span.spanContext.isSampled

    def exportSpans: F[Unit] = // todo: log error on failure
      exporter.exportSpans(List(span))

    supervisor.supervise(exportSpans).void.whenA(canExport)
  }

  def forceFlush: F[Unit] =
    Monad[F].unit
}

object SimpleSpanProcessor {

  def create[F[_]: Concurrent](
      exporter: SpanExporter[F]
  ): Resource[F, SimpleSpanProcessor[F]] =
    create(exporter, sampled = true)

  def create[F[_]: Concurrent](
      exporter: SpanExporter[F],
      sampled: Boolean
  ): Resource[F, SimpleSpanProcessor[F]] = {
    for {
      supervisor <- Supervisor[F](await = true)
    } yield new SimpleSpanProcessor[F](supervisor, exporter, sampled)
  }

}
