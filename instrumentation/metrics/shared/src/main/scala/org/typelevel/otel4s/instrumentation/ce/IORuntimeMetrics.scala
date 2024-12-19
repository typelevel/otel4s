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

package org.typelevel.otel4s.instrumentation.ce

import cats.effect.Resource
import cats.effect.Sync
import cats.effect.unsafe.metrics.CpuStarvationMetrics
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.Meter

object IORuntimeMetrics extends IORuntimeMetricsPlatform {

  protected object Const {
    val MeterNamespace = "cats.effect.runtime"
  }

  /** Registers the CPU starvation:
    *   - `cats.effect.runtime.cpu.starvation.count`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.current`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.max`
    *
    * @param attributes
    *   the attributes to attach to the metrics
    */
  protected def cpuStarvationMetrics[F[_]: Sync: Meter](
      metrics: CpuStarvationMetrics,
      attributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.cpu.starvation"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableCounter[Long](s"$prefix.count")
        .withDescription("The number of CPU starvation events.")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.clock.drift.current")
        .withDescription("The current CPU drift in milliseconds.")
        .withUnit("ms")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.clock.drift.max")
        .withDescription("The max CPU drift in milliseconds.")
        .withUnit("ms")
        .createObserver,
    ) { (count, driftCurrent, driftMax) =>
      for {
        snapshot <- Sync[F].delay(
          (
            metrics.starvationCount(),
            metrics.clockDriftCurrent(),
            metrics.clockDriftMax()
          )
        )
        _ <- count.record(snapshot._1, attributes)
        _ <- driftCurrent.record(snapshot._2.toMillis, attributes)
        _ <- driftMax.record(snapshot._3.toMillis, attributes)
      } yield ()
    }
  }

}
