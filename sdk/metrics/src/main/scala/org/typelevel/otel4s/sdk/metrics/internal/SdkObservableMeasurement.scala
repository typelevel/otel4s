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
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

import scala.concurrent.duration.FiniteDuration

private[metrics] final class SdkObservableMeasurement[
    F[_]: Monad,
    A: MeasurementValue
](
    stateRef: Ref[F, SdkObservableMeasurement.State[F]],
    val scope: InstrumentationScope,
    val descriptor: InstrumentDescriptor,
    val storages: Vector[MetricStorage.Asynchronous[F]]
) extends ObservableMeasurement[F, A] {
  import SdkObservableMeasurement._

  def setActiveReader(
      reader: RegisteredReader[F],
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[Unit] =
    stateRef.set(State.WithReader(reader, startTimestamp, collectTimestamp))

  def unsetActiveReader: F[Unit] =
    stateRef.set(State.Empty())

  def record(value: A, attributes: Attributes): F[Unit] = {
    stateRef.get.flatMap {
      case State.Empty() =>
        Monad[F].unit // todo: log warning

      case State.WithReader(reader, start, collect) =>
        val attrs = Attributes.fromSpecific(attributes)
        val measurement = Measurement.of(start, collect, attrs, value)

        storages.traverse_ { storage =>
          storage.record(measurement).whenA(storage.reader == reader)
        }
    }
  }

}

object SdkObservableMeasurement {

  sealed trait State[F[_]]
  object State {
    final case class Empty[F[_]]() extends State[F]

    final case class WithReader[F[_]](
        reader: RegisteredReader[F],
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration
    ) extends State[F]
  }

  def create[F[_]: Concurrent, A: MeasurementValue](
      storages: Vector[MetricStorage.Asynchronous[F]],
      scope: InstrumentationScope,
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, A]] =
    for {
      state <- Ref.of[F, State[F]](State.Empty())
    } yield new SdkObservableMeasurement[F, A](
      state,
      scope,
      descriptor,
      storages
    )

}
