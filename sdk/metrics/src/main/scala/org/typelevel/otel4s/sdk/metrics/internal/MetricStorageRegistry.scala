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

package org.typelevel.otel4s.sdk.metrics.internal

import cats.Monad
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

private[metrics] final class MetricStorageRegistry[F[_]: Monad](
    registry: Ref[F, Map[MetricDescriptor, MetricStorage[F]]]
) {

  def allStorages: F[Vector[MetricStorage[F]]] =
    registry.get.map(_.values.toVector)

  def register(storage: MetricStorage[F]): F[MetricStorage[F]] = {
    val descriptor = storage.metricDescriptor
    registry.modify { map =>
      map.get(descriptor) match {
        case Some(value) =>
          // todo: if (value != storage) log warning
          (map, value)

        case None =>
          (map.updated(descriptor, storage), storage)
      }
    }
  }

}

object MetricStorageRegistry {

  def create[F[_]: Concurrent]: F[MetricStorageRegistry[F]] =
    for {
      registry <- Ref.of[F, Map[MetricDescriptor, MetricStorage[F]]](Map.empty)
    } yield new MetricStorageRegistry[F](registry)

}
