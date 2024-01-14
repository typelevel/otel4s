package org.typelevel.otel4s.sdk.metrics

import cats.effect.Concurrent
import cats.effect.MonadCancelThrow
import cats.effect.Ref
import cats.effect.std.Mutex
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.sdk.Resource
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
    resource: Resource,
    scope: InstrumentationScope,
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

  def registerLongObservableMeasurement(
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, Long]] = {
    registries.toVector
      .flatTraverse { case (reader, registry) =>
        reader.viewRegistry.findViews(descriptor, scope).flatTraverse {
          registeredView =>
            if (registeredView.view.aggregation == Aggregation.drop) {
              MonadCancelThrow[F]
                .pure(Vector.empty[MetricStorage.Asynchronous[F]])
            } else {
              for {
                s <- MetricStorage
                  .asynchronous(reader, registeredView, descriptor)
                _ <- registry.register(s)
              } yield Vector(s)
            }
        }
      }
      .flatMap { storages =>
        SdkObservableMeasurement.ofLong(storages, descriptor)
      }
  }

  def registerDoubleObservableMeasurement(
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, Double]] =
    registries.toVector
      .flatTraverse { case (reader, registry) =>
        reader.viewRegistry.findViews(descriptor, scope).flatTraverse {
          registeredView =>
            if (registeredView.view.aggregation != Aggregation.drop) {
              MonadCancelThrow[F].pure(
                Vector.empty[MetricStorage.Asynchronous[F]]
              )
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
        SdkObservableMeasurement.ofDouble(storages, descriptor)
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
      resource: Resource,
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
