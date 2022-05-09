package com.example

import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import com.rossabaker.otel4s.oteljava.OtelJava

import cats.effect.IO
import cats.effect.IOApp

object Poc extends IOApp.Simple {
  def run = for {
    _ <- IO(sys.props("otel.traces.exporter") = "none")
    _ <- IO(sys.props("otel.metrics.exporter") = "logging")
    _ <- IO(sys.props("otel.logs.exporter") = "none")
    otel4j <- IO(AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk)
    otel4s = OtelJava.forSync[IO](otel4j)
    meter <- otel4s.meterProvider.get("poc")
    counter <- meter.counter("test").create
    _ <- counter.add(1)
    _ <- counter.add(2)
    provider = otel4j.getSdkMeterProvider
    _ <- IO.println(provider.forceFlush())
  } yield ()
}
