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

import cats.effect.kernel.Sync
import io.opentelemetry.api.metrics.{
  ObservableMeasurement => JObservableMeasurement
}
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.oteljava.AttributeConverters._

private[metrics] sealed abstract class ObservableMeasurementImpl[F[_], A](
    val jObservableMeasurement: JObservableMeasurement
) extends ObservableMeasurement[F, A] {}

private object ObservableMeasurementImpl {

  final class LongObservableMeasurement[F[_]: Sync, A](
      cast: A => Long,
      jObservableMeasurement: ObservableLongMeasurement
  ) extends ObservableMeasurementImpl[F, A](jObservableMeasurement) {

    def record(value: A, attributes: Attributes): F[Unit] =
      Sync[F].delay(
        jObservableMeasurement.record(
          cast(value),
          attributes.toJava
        )
      )
  }

  final class DoubleObservableMeasurement[F[_]: Sync, A](
      cast: A => Double,
      jObservableMeasurement: ObservableDoubleMeasurement
  ) extends ObservableMeasurementImpl[F, A](jObservableMeasurement) {

    def record(value: A, attributes: Attributes): F[Unit] =
      Sync[F].delay(
        jObservableMeasurement.record(
          cast(value),
          attributes.toJava
        )
      )
  }

}
