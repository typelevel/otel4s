package org.typelevel.otel4s.sdk.metrics.internal

import cats.Monad
import cats.effect.Ref
import cats.effect.Concurrent
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
