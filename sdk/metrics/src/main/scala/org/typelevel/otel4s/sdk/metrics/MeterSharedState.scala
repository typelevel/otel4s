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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.Concurrent
import cats.effect.MonadCancelThrow
import cats.effect.Ref
import cats.effect.std.Mutex
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.internal.CallbackRegistration
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MetricStorageRegistry
import org.typelevel.otel4s.sdk.metrics.internal.SdkObservableMeasurement
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

import scala.concurrent.duration.FiniteDuration

private[metrics] final class MeterSharedState[F[_]: Concurrent](
    mutex: Mutex[F],
    resource: TelemetryResource,
    val scope: InstrumentationScope,
    startTimestamp: FiniteDuration,
    exemplarFilter: ExemplarFilter,
    callbacks: Ref[F, Vector[CallbackRegistration[F]]],
    registries: Map[RegisteredReader[F], MetricStorageRegistry[F]]
) {

  def registerMetricStorage(
      descriptor: InstrumentDescriptor
  ): F[MetricStorage.Writeable[F]] =
    registries.toVector
      .flatTraverse { case (reader, registry) =>
        reader.viewRegistry.findViews(descriptor, scope).flatTraverse {
          registeredView =>
            if (registeredView.view.aggregation == Aggregation.drop) {
              MonadCancelThrow[F]
                .pure(Vector.empty[MetricStorage.Synchronous[F]])
            } else {
              for {
                s <- MetricStorage.synchronous(
                  reader,
                  registeredView,
                  descriptor,
                  exemplarFilter
                )
                _ <- registry.register(s)
              } yield Vector(s)
            }
        }
      }
      .map { storages =>
        MetricStorage.Writeable.of(storages: _*)
      }

  def registerObservableMeasurement[A: MeasurementValue](
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, A]] =
    registries.toVector
      .flatTraverse { case (reader, registry) =>
        reader.viewRegistry.findViews(descriptor, scope).flatTraverse {
          registeredView =>
            if (registeredView.view.aggregation == Aggregation.drop) {
              MonadCancelThrow[F]
                .pure(Vector.empty[MetricStorage.Asynchronous[F]])
            } else {
              for {
                s <- MetricStorage.asynchronous(
                  reader,
                  registeredView,
                  descriptor
                )
                _ <- registry.register(s)
              } yield Vector(s)
            }
        }
      }
      .flatMap { storages =>
        SdkObservableMeasurement.create(storages, scope, descriptor)
      }

  def collectAll(
      reader: RegisteredReader[F],
      collectTimestamp: FiniteDuration
  ): F[Vector[MetricData]] =
    callbacks.get.flatMap { currentCallbacks =>
      mutex.lock.surround {
        currentCallbacks
          .traverse_ { callback =>
            callback.invokeCallback(reader, startTimestamp, collectTimestamp)
          }
          .flatMap { _ =>
            for {
              storages <- registries.get(reader).foldMapA(_.allStorages)
              result <- storages.traverse { storage =>
                storage.collect(
                  resource,
                  scope,
                  startTimestamp,
                  collectTimestamp
                )
              }
            } yield result.flatten.filter(_.nonEmpty)
          }

      }
    }

  def removeCallback(callback: CallbackRegistration[F]): F[Unit] =
    callbacks.update(_.filter(_ != callback))

  def registerCallback(callback: CallbackRegistration[F]): F[Unit] =
    callbacks.update(_ :+ callback)

}

private object MeterSharedState {

  def create[F[_]: Concurrent](
      resource: TelemetryResource,
      scope: InstrumentationScope,
      startTimestamp: FiniteDuration,
      exemplarFilter: ExemplarFilter,
      registeredReaders: Vector[RegisteredReader[F]]
  ): F[MeterSharedState[F]] =
    for {
      mutex <- Mutex[F]
      callbacks <- Ref.empty[F, Vector[CallbackRegistration[F]]]
      registries <- registeredReaders.traverse { reader =>
        MetricStorageRegistry.create[F].tupleLeft(reader)
      }
    } yield new MeterSharedState(
      mutex,
      resource,
      scope,
      startTimestamp,
      exemplarFilter,
      callbacks,
      registries.toMap
    )
}
