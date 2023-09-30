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

package org.typelevel.otel4s.sdk.trace.exporter

import cats.data.Chain
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData

final class InMemorySpanExporter[F[_]] private (
    storage: Ref[F, Chain[SpanData]]
) extends SpanExporter[F] {
  def exportSpans(span: List[SpanData]): F[Unit] =
    storage.update(_.concat(Chain.fromSeq(span)))

  def finishedSpans: F[Chain[SpanData]] =
    storage.get

  def reset: F[Unit] =
    storage.set(Chain.empty)
}

object InMemorySpanExporter {

  def create[F[_]: Concurrent]: F[InMemorySpanExporter[F]] =
    for {
      storage <- Ref.of(Chain.empty[SpanData])
    } yield new InMemorySpanExporter[F](storage)

}
