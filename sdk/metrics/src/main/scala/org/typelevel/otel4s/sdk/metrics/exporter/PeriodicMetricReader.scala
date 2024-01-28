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

package org.typelevel.otel4s.sdk.metrics.exporter

import cats.effect.Concurrent
import cats.effect.Fiber
import cats.effect.Resource
import cats.effect.Spawn
import cats.effect.Temporal
import cats.effect.std.AtomicCell
import cats.effect.std.Console
import cats.effect.syntax.spawn._
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

import scala.concurrent.duration.FiniteDuration

class PeriodicMetricReader[F[_]: Temporal: Console](
    stateRef: AtomicCell[F, PeriodicMetricReader.State[F]],
    exporter: MetricExporter[F],
    interval: FiniteDuration
) extends MetricReader[F] {
  import PeriodicMetricReader._

  def defaultAggregationSelector: DefaultAggregationSelector =
    new DefaultAggregationSelector {
      def get(instrumentType: InstrumentType): Aggregation =
        Aggregation.default
    }

  def aggregationTemporality(
      instrumentType: InstrumentType
  ): AggregationTemporality =
    exporter.aggregationTemporality(instrumentType)

  def register(registration: CollectionRegistration[F]): F[Unit] =
    stateRef.evalUpdate {
      case State.Idle() =>
        process(registration).start.map(fiber => State.Running(fiber))

      case state: State.Running[F] =>
        Spawn[F].pure(state)
    }

  private def process(registration: CollectionRegistration[F]): F[Unit] = {
    val doExport =
      for {
        data <- registration.collectAllMetrics
        _ <- exporter.exportMetrics(data).whenA(data.nonEmpty)
      } yield ()

    val step = doExport.handleErrorWith { e =>
      Console[F].error(
        s"PeriodicMetricExporter: the export has failed: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
      )
    }

    step.delayBy(interval).foreverM
  }

  def forceFlush: F[Unit] = ???
}

object PeriodicMetricReader {

  private sealed trait State[F[_]]
  private object State {
    final case class Idle[F[_]]() extends State[F]

    final case class Running[F[_]](
        process: Fiber[F, Throwable, Unit]
    ) extends State[F]
  }

  def create[F[_]: Temporal: Console](
      exporter: MetricExporter[F],
      interval: FiniteDuration
  ): Resource[F, PeriodicMetricReader[F]] =
    for {
      ref <- Resource.eval(AtomicCell[F].of[State[F]](State.Idle()))
      _ <- Resource.make(Concurrent[F].unit)(_ =>
        ref.get.flatMap {
          case State.Idle()           => Concurrent[F].unit
          case State.Running(process) => process.cancel
        }
      )
    } yield new PeriodicMetricReader(ref, exporter, interval)

}
