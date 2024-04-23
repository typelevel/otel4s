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
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage

private[metrics] final class SdkObservableMeasurement[
    F[_]: Monad: Console,
    A: MeasurementValue
] private (
    stateRef: Ref[F, SdkObservableMeasurement.State[F]],
    val scope: InstrumentationScope,
    val descriptor: InstrumentDescriptor,
    val storages: Vector[MetricStorage.Asynchronous[F, A]]
) extends ObservableMeasurement[F, A] {
  import SdkObservableMeasurement._

  private val isValid: A => Boolean =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(_) =>
        Function.const(true)
      case MeasurementValue.DoubleMeasurementValue(cast) =>
        v => !cast(v).isNaN
    }

  /** Sets an active reader and resets the state upon resource finalization.
    *
    * @param reader
    *   the reader to use
    *
    * @param timeWindow
    *   the time window of the measurement
    */
  def withActiveReader(
      reader: RegisteredReader[F],
      timeWindow: TimeWindow
  ): Resource[F, Unit] =
    Resource.make(stateRef.set(State.WithReader(reader, timeWindow))) { _ =>
      stateRef.set(State.Empty())
    }

  def record(value: A, attributes: Attributes): F[Unit] =
    stateRef.get
      .flatMap {
        case State.Empty() =>
          Console[F].errorln(
            "SdkObservableMeasurement: " +
              s"trying to record a measurement for an instrument [${descriptor.name}] while the active reader is unset. " +
              "Dropping the measurement."
          )

        case State.WithReader(reader, timeWindow) =>
          val measurement =
            AsynchronousMeasurement(timeWindow, attributes, value)

          storages
            .filter(_.reader == reader)
            .traverse_(storage => storage.record(measurement))
      }
      .whenA(isValid(value))

}

private[metrics] object SdkObservableMeasurement {

  private sealed trait State[F[_]]
  private object State {
    final case class Empty[F[_]]() extends State[F]

    final case class WithReader[F[_]](
        reader: RegisteredReader[F],
        timeWindow: TimeWindow
    ) extends State[F]
  }

  def create[F[_]: Concurrent: Console, A: MeasurementValue](
      storages: Vector[MetricStorage.Asynchronous[F, A]],
      scope: InstrumentationScope,
      descriptor: InstrumentDescriptor.Asynchronous
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
