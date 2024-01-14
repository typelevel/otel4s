package org.typelevel.otel4s.sdk.metrics

import cats.effect.Clock
import cats.effect.MonadCancelThrow
import cats.effect.std.Console
import org.typelevel.otel4s.metrics.Counter
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.metrics.ObservableCounter
import org.typelevel.otel4s.metrics.ObservableGauge
import org.typelevel.otel4s.metrics.ObservableInstrumentBuilder
import org.typelevel.otel4s.metrics.ObservableUpDownCounter
import org.typelevel.otel4s.metrics.SyncInstrumentBuilder
import org.typelevel.otel4s.metrics.UpDownCounter
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.metrics.data.MetricData

import scala.concurrent.duration.FiniteDuration

private class SdkMeter[F[_]: MonadCancelThrow: Clock: Console: AskContext](
    sharedState: MeterSharedState[F]
) extends Meter[F] {

  def counter(name: String): SyncInstrumentBuilder[F, Counter[F, Long]] =
    if (isValidName(name))
      SdkLongCounter.Builder[F](name, sharedState)
    else
      NoopInstrumentBuilder.sync(name, Counter.noop)

  def histogram(name: String): SyncInstrumentBuilder[F, Histogram[F, Double]] =
    if (isValidName(name))
      SdkDoubleHistogram.Builder[F](name, sharedState)
    else
      NoopInstrumentBuilder.sync(name, Histogram.noop)

  def upDownCounter(
      name: String
  ): SyncInstrumentBuilder[F, UpDownCounter[F, Long]] =
    if (isValidName(name))
      SdkLongUpDownCounter.Builder[F](name, sharedState)
    else
      NoopInstrumentBuilder.sync(name, UpDownCounter.noop)

  def observableGauge(
      name: String
  ): ObservableInstrumentBuilder[F, Double, ObservableGauge] =
    if (isValidName(name))
      SdkDoubleGauge.Builder[F](name, sharedState)
    else
      NoopInstrumentBuilder.observable(name, new ObservableGauge {})

  def observableCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableCounter] =
    /*if (isValidName(name))
      SdkLongCounter.Builder[F](name, storage)
    else*/
    NoopInstrumentBuilder.observable(name, new ObservableCounter {})

  def observableUpDownCounter(
      name: String
  ): ObservableInstrumentBuilder[F, Long, ObservableUpDownCounter] =
    /* if (isValidName(name))
      SdkLongCounter.Builder[F](name, storage)
    else*/
    NoopInstrumentBuilder.observable(name, new ObservableUpDownCounter {})

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
