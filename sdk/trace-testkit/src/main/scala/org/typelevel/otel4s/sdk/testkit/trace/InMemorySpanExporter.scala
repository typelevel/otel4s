/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.testkit.trace

import cats.Foldable
import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

final class InMemorySpanExporter[F[_]: Monad] private (
    queue: Queue[F, SpanData]
) extends SpanExporter[F] {
  val name: String = "InMemorySpanExporter"

  def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit] =
    spans.traverse_(span => queue.offer(span))

  def flush: F[Unit] =
    Monad[F].unit

  def finishedSpans: F[List[SpanData]] =
    queue.tryTakeN(None)

}

object InMemorySpanExporter {

  def create[F[_]: Concurrent](
      capacity: Option[Int]
  ): F[InMemorySpanExporter[F]] =
    for {
      queue <- capacity.fold(Queue.unbounded[F, SpanData])(Queue.bounded(_))
    } yield new InMemorySpanExporter[F](queue)

}
