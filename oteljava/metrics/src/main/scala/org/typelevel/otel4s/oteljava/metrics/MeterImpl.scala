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

package org.typelevel.otel4s
package oteljava
package metrics

import cats.effect.kernel.Async
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics._

private[oteljava] class MeterImpl[F[_]: Async](jMeter: JMeter)
    extends Meter[F] {

  def counter[A: MeasurementValue](name: String): Counter.Builder[F, A] =
    CounterBuilderImpl(jMeter, name)

  def histogram[A: MeasurementValue](name: String): Histogram.Builder[F, A] =
    HistogramBuilderImpl(jMeter, name)

  def upDownCounter[A: MeasurementValue](
      name: String
  ): UpDownCounter.Builder[F, A] =
    UpDownCounterBuilderImpl(jMeter, name)

  def gauge[A: MeasurementValue](name: String): Gauge.Builder[F, A] =
    GaugeBuilderImpl(jMeter, name)

}
