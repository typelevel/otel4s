/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.functor._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics.BatchCallback
import org.typelevel.otel4s.metrics.ObservableMeasurement

private final class BatchCallbackImpl[F[_]: Async](
    jMeter: JMeter
) extends BatchCallback[F] {

  def apply(
      callback: F[Unit],
      observable: ObservableMeasurement[F, _],
      rest: ObservableMeasurement[F, _]*
  ): Resource[F, Unit] = {
    val all = (observable +: rest).toList.collect {
      case o: ObservableMeasurementImpl[F, _] => o.jObservableMeasurement
    }

    all match {
      case head :: tail =>
        Dispatcher
          .sequential[F]
          .flatMap { dispatcher =>
            Resource.fromAutoCloseable(
              Async[F].delay {
                jMeter.batchCallback(
                  () => dispatcher.unsafeRunSync(callback),
                  head,
                  tail: _*
                )
              }
            )
          }
          .void

      case Nil =>
        Resource.unit
    }
  }

}
