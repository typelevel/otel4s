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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.Clock
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import org.typelevel.otel4s.metrics.BatchCallback
import org.typelevel.otel4s.metrics.Counter
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.metrics.ObservableCounter
import org.typelevel.otel4s.metrics.ObservableGauge
import org.typelevel.otel4s.metrics.ObservableUpDownCounter
import org.typelevel.otel4s.metrics.UpDownCounter
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.metrics.data.MetricData

import scala.concurrent.duration.FiniteDuration

private class SdkMeter[F[_]: MonadCancelThrow: Clock: Console: AskContext](
    sharedState: MeterSharedState[F]
) extends Meter[F] {

  def counter[A: MeasurementValue](
      name: String
  ): Counter.Builder[F, A] =
    if (isValidName(name))
      SdkCounter.Builder(name, sharedState)
    else
      NoopInstrumentBuilder.counter(name)

  def histogram[A: MeasurementValue](
      name: String
  ): Histogram.Builder[F, A] =
    if (isValidName(name))
      SdkHistogram.Builder(name, sharedState)
    else
      NoopInstrumentBuilder.histogram(name)

  def upDownCounter[A: MeasurementValue](
      name: String
  ): UpDownCounter.Builder[F, A] =
    if (isValidName(name))
      SdkUpDownCounter.Builder(name, sharedState)
    else
      NoopInstrumentBuilder.upDownCounter(name)

  def observableGauge[A: MeasurementValue](
      name: String
  ): ObservableGauge.Builder[F, A] =
    if (isValidName(name))
      SdkObservableGauge.Builder(name, sharedState)
    else
      NoopInstrumentBuilder.observableGauge(name)

  def observableCounter[A: MeasurementValue](
      name: String
  ): ObservableCounter.Builder[F, A] =
    /*if (isValidName(name))
      SdkLongCounter.Builder[F](name, storage)
    else*/
    NoopInstrumentBuilder.observableCounter(name)

  def observableUpDownCounter[A: MeasurementValue](
      name: String
  ): ObservableUpDownCounter.Builder[F, A] =
    /* if (isValidName(name))
      SdkLongCounter.Builder[F](name, storage)
    else*/
    NoopInstrumentBuilder.observableUpDownCounter(name)

  def collectAll(
      reader: RegisteredReader[F],
      collectTimestamp: FiniteDuration
  ): F[Vector[MetricData]] =
    sharedState.collectAll(reader, collectTimestamp)

  val batchCallback: BatchCallback[F] =
    new SdkBatchCallback[F](sharedState)

  private def isValidName(name: String): Boolean =
    name != null && SdkMeter.InstrumentNamePattern.matches(name)
}

object SdkMeter {

  private val InstrumentNamePattern =
    "([A-Za-z]){1}([A-Za-z0-9\\_\\-\\./]){0,254}".r

}
