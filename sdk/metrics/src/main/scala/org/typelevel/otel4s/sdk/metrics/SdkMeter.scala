package org.typelevel.otel4s.sdk.metrics

import cats.effect.Clock
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import org.typelevel.otel4s.metrics.{
  Counter,
  Histogram,
  MeasurementValue,
  Meter,
  ObservableCounter,
  ObservableGauge,
  ObservableUpDownCounter,
  UpDownCounter
}
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

  private def isValidName(name: String): Boolean =
    name != null && SdkMeter.InstrumentNamePattern.matches(name)
}

object SdkMeter {

  private val InstrumentNamePattern =
    "([A-Za-z]){1}([A-Za-z0-9\\_\\-\\./]){0,254}".r

}
