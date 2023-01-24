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

package org.typelevel.otel4s.java.metrics

import cats.effect.kernel.Sync
import io.opentelemetry.api.{OpenTelemetry => JOpenTelemetry}
import org.typelevel.otel4s.metrics.MeterProvider

trait Metrics[F[_]] {
  def meterProvider: MeterProvider[F]
}

object Metrics {

  def forSync[F[_]: Sync](jOtel: JOpenTelemetry): Metrics[F] =
    new Metrics[F] {
      val meterProvider: MeterProvider[F] = new MeterProviderImpl[F](jOtel)
    }

}
