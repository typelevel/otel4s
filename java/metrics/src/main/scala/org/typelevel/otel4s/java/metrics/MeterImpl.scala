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
package java
package metrics

import cats.effect.kernel.Async
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics._

private[java] class MeterImpl[F[_]: Async](jMeter: JMeter) extends Meter[F] {
  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
    new CounterBuilderImpl(jMeter, name)

  def histogram(
      name: String
  ): SyncInstrumentBuilder[F, Histogram[F, Double]] =
    new HistogramBuilderImpl(jMeter, name)

  def upDownCounter(
      name: String
  ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]] =
    new UpDownCounterBuilderImpl(jMeter, name)

  def observableGauge(
      name: String
  ): ObservableInstrumentBuilder[F, Double, ObservableGauge] =
    new ObservableGaugeBuilderImpl(jMeter, name)

  def observableUpDownCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter] =
    new ObservableUpDownCounterBuilderImpl(jMeter, name)

  def observableCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableCounter] =
    new ObservableCounterBuilderImpl(jMeter, name)
}
