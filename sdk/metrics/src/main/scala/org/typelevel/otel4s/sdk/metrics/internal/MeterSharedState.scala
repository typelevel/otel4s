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
import cats.effect.Ref
import cats.effect.Resource
import cats.effect.std.Console
import cats.effect.std.Mutex
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage
import org.typelevel.otel4s.sdk.metrics.view.View
import org.typelevel.otel4s.sdk.metrics.view.ViewRegistry

import scala.concurrent.duration.FiniteDuration

private[metrics] final class MeterSharedState[
    F[_]: Concurrent: Console: AskContext
] private (
    val meta: InstrumentMeta.Dynamic[F],
    mutex: Mutex[F],
    viewRegistry: ViewRegistry[F],
    reservoirs: Reservoirs[F],
    resource: TelemetryResource,
    val scope: InstrumentationScope,
    startTimestamp: FiniteDuration,
    callbacks: Ref[F, Vector[CallbackRegistration[F]]],
    registries: Map[RegisteredReader[F], MetricStorageRegistry[F]]
) {

  /** Creates a metric storage for the given descriptor of a synchronous instrument.
    *
    * @param descriptor
    *   a descriptor to create a storage for
    *
    * @tparam A
    *   the type of the values to record
    */
  def registerMetricStorage[A: MeasurementValue: Numeric](
      descriptor: InstrumentDescriptor.Synchronous
  ): F[MetricStorage.Synchronous.Writeable[F, A]] = {

    def make(
        reader: RegisteredReader[F],
        registry: MetricStorageRegistry[F],
        aggregation: Aggregation.Synchronous,
        view: Option[View]
    ): F[Vector[MetricStorage.Synchronous[F, A]]] =
      for {
        storage <- MetricStorage.synchronous(
          reader,
          reservoirs,
          view,
          descriptor,
          aggregation
        )
        registered <- registry.register(storage)
        result <- registered match {
          // the storage may already be registered, so we need to reuse it
          case s: MetricStorage.Synchronous[F @unchecked, A @unchecked] =>
            Concurrent[F].pure(Vector(s))

          case other =>
            Console[F]
              .errorln(
                s"MeterSharedState: there is a different storage $other registered for $descriptor. The current instrument will be noop."
              )
              .as(Vector.empty[MetricStorage.Synchronous[F, A]])
        }
      } yield result

    registries.toVector
      .flatTraverse { case (reader, registry) =>
        def defaultAggregation: Aggregation with Aggregation.Synchronous =
          reader.reader.defaultAggregationSelector.forSynchronous(
            descriptor.instrumentType
          )

        viewRegistry
          .findViews(descriptor, scope)
          .flatMap {
            case Some(views) =>
              views.toVector.flatTraverse { view =>
                view.aggregation.getOrElse(defaultAggregation) match {
                  case aggregation: Aggregation.Synchronous =>
                    make(reader, registry, aggregation, Some(view))

                  case _ =>
                    Concurrent[F].pure(
                      Vector.empty[MetricStorage.Synchronous[F, A]]
                    )
                }
              }

            case None =>
              make(reader, registry, defaultAggregation, None)
          }
      }
      .map { storages =>
        MetricStorage.Synchronous.Writeable.of(storages)
      }
  }

  /** Creates an observable measurement for the given descriptor of an asynchronous instrument.
    *
    * @param descriptor
    *   a descriptor to create an observable measurement for
    *
    * @tparam A
    *   the type of the values to record
    */
  def registerObservableMeasurement[A: MeasurementValue: Numeric](
      descriptor: InstrumentDescriptor.Asynchronous
  ): F[SdkObservableMeasurement[F, A]] = {

    def make(
        reader: RegisteredReader[F],
        registry: MetricStorageRegistry[F],
        aggregation: Aggregation.Asynchronous,
        view: Option[View]
    ): F[Vector[MetricStorage.Asynchronous[F, A]]] =
      for {
        storage <- MetricStorage.asynchronous(
          reader,
          view,
          descriptor,
          aggregation
        )
        registered <- registry.register(storage)
        result <- registered match {
          // the storage may already be registered, so we need to reuse it
          case s: MetricStorage.Asynchronous[F @unchecked, A @unchecked] =>
            Concurrent[F].pure(Vector(s))

          case other =>
            Console[F]
              .errorln(
                s"MeterSharedState: there is a different storage $other registered for $descriptor. The current instrument will be noop."
              )
              .as(Vector.empty[MetricStorage.Asynchronous[F, A]])
        }
      } yield result

    registries.toVector
      .flatTraverse { case (reader, registry) =>
        def defaultAggregation: Aggregation with Aggregation.Asynchronous =
          reader.reader.defaultAggregationSelector.forAsynchronous(
            descriptor.instrumentType
          )

        viewRegistry
          .findViews(descriptor, scope)
          .flatMap {
            case Some(views) =>
              views.toVector.flatTraverse { view =>
                view.aggregation.getOrElse(defaultAggregation) match {
                  case aggregation: Aggregation.Asynchronous =>
                    make(reader, registry, aggregation, Some(view))

                  case _ =>
                    Concurrent[F].pure(
                      Vector.empty[MetricStorage.Asynchronous[F, A]]
                    )
                }
              }

            case None =>
              make(reader, registry, defaultAggregation, None)
          }
      }
      .flatMap { storages =>
        SdkObservableMeasurement.create(storages, scope, descriptor)
      }
  }

  /** Collects all metrics.
    *
    * @param reader
    *   the reader to use
    *
    * @param collectTimestamp
    *   the timestamp of the collection
    */
  def collectAll(
      reader: RegisteredReader[F],
      collectTimestamp: FiniteDuration
  ): F[Vector[MetricData]] =
    callbacks.get.flatMap { currentCallbacks =>
      mutex.lock.surround {
        val timeWindow = TimeWindow(startTimestamp, collectTimestamp)

        for {
          _ <- currentCallbacks.traverse_(_.invokeCallback(reader, timeWindow))
          storages <- registries.get(reader).foldMapM(_.storages)
          result <- storages.traverse { storage =>
            storage.collect(resource, scope, timeWindow)
          }
        } yield result.flatten
      }
    }

  /** Registers a callback and removes it from the state upon resource finalization.
    *
    * @param callback
    *   a callback to register
    */
  def withCallback(callback: CallbackRegistration[F]): Resource[F, Unit] =
    Resource
      .make(registerCallback(callback))(_ => removeCallback(callback))
      .void

  private def removeCallback(callback: CallbackRegistration[F]): F[Unit] =
    callbacks.update(_.filter(_ != callback))

  private def registerCallback(callback: CallbackRegistration[F]): F[Unit] =
    callbacks.update(_ :+ callback)

}

private[metrics] object MeterSharedState {

  def create[F[_]: Concurrent: Console: AskContext](
      resource: TelemetryResource,
      scope: InstrumentationScope,
      startTimestamp: FiniteDuration,
      reservoirs: Reservoirs[F],
      viewRegistry: ViewRegistry[F],
      registeredReaders: Vector[RegisteredReader[F]]
  ): F[MeterSharedState[F]] =
    for {
      mutex <- Mutex[F]
      callbacks <- Ref.empty[F, Vector[CallbackRegistration[F]]]
      registries <- registeredReaders.traverse { reader =>
        MetricStorageRegistry.create[F].tupleLeft(reader)
      }
    } yield new MeterSharedState(
      InstrumentMeta.Dynamic.enabled,
      mutex,
      viewRegistry,
      reservoirs,
      resource,
      scope,
      startTimestamp,
      callbacks,
      registries.toMap
    )

}
