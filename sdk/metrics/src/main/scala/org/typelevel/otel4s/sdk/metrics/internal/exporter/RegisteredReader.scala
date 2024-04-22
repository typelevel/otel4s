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

package org.typelevel.otel4s.sdk.metrics.internal.exporter

import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader

import scala.concurrent.duration.FiniteDuration

private[metrics] final class RegisteredReader[F[_]](
    val reader: MetricReader[F],
    lastCollectTimestampRef: Ref[F, FiniteDuration]
) {

  def lastCollectTimestamp: F[FiniteDuration] =
    lastCollectTimestampRef.get

  def setLastCollectTimestamp(timestamp: FiniteDuration): F[Unit] =
    lastCollectTimestampRef.set(timestamp)
}

private[metrics] object RegisteredReader {

  def create[F[_]: Concurrent](
      start: FiniteDuration,
      reader: MetricReader[F],
  ): F[RegisteredReader[F]] =
    for {
      ref <- Concurrent[F].ref(start)
    } yield new RegisteredReader[F](reader, ref)

}
