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

import cats.effect.Concurrent
import cats.effect.MonadCancelThrow
import cats.effect.Ref
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage

/** Stores metric storages using metric descriptor as a key.
  */
private[metrics] final class MetricStorageRegistry[
    F[_]: MonadCancelThrow: Console
](
    registry: Ref[F, Map[MetricDescriptor, MetricStorage[F]]]
) {

  /** Returns all registered storages.
    */
  def storages: F[Vector[MetricStorage[F]]] =
    registry.get.map(_.values.toVector)

  /** Registers a storage to this registry.
    *
    * If a metric with compatible identity was previously registered, returns
    * the previously registered storage.
    *
    * If a metric with the same name (case-insensitive) but incompatible
    * descriptor was previously registered, logs a diagnostic warning.
    *
    * @param storage
    *   the metric storage to register
    */
  def register(storage: MetricStorage[F]): F[MetricStorage[F]] =
    registry.flatModify { map =>
      val descriptor = storage.metricDescriptor

      map.get(descriptor) match {
        case Some(value) =>
          (map, MonadCancelThrow[F].pure(value))

        case None =>
          val updated = map.updated(descriptor, storage)

          // run a duplicate check
          map.keySet.filter(_.name == descriptor.name) match {
            case duplicates if duplicates.nonEmpty =>
              def warn: F[Unit] =
                Console[F].errorln(
                  "MetricStorageRegistry: found a duplicate. " +
                    s"The $descriptor has similar descriptors in the storage: ${duplicates.mkString(", ")}."
                )

              (updated, warn.as(storage))

            case _ =>
              (updated, MonadCancelThrow[F].pure(storage))
          }
      }
    }

}

private[metrics] object MetricStorageRegistry {

  def create[F[_]: Concurrent: Console]: F[MetricStorageRegistry[F]] =
    for {
      registry <- Ref.of[F, Map[MetricDescriptor, MetricStorage[F]]](Map.empty)
    } yield new MetricStorageRegistry[F](registry)

}
