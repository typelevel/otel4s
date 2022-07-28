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

package com.example

import cats.effect.IO
import cats.effect.IOApp
import io.opentelemetry.sdk.autoconfigure.AutoConfiguredOpenTelemetrySdk
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.java.OtelJava

object Poc extends IOApp.Simple {
  def run: IO[Unit] = for {
    _ <- IO(sys.props("otel.traces.exporter") = "logging")
    _ <- IO(sys.props("otel.metrics.exporter") = "logging")
    _ <- IO(sys.props("otel.logs.exporter") = "none")
    otel4j <- IO(
      AutoConfiguredOpenTelemetrySdk.initialize().getOpenTelemetrySdk
    )
    otel4s <- OtelJava.forSync[IO](otel4j)
    meter <- otel4s.meterProvider.get("poc")
    counter <- meter.counter("test").create
    _ <- counter.add(1)
    dog = AttributeKey.string("dog")
    fish = AttributeKey.stringList("fish")
    numbers = AttributeKey.longList("numbers")
    _ <- counter.add(
      2,
      Attribute(dog, "barking"),
      Attribute(fish, List("one", "two", "red", "blue")),
      Attribute(numbers, List(1L, 2L, 3L, 4L))
    )
    tracer <- otel4s.traceProvider.tracer("my-tracer").get
    _ <- tracer.span("my-span").use { _ =>
      tracer.span("span-2").use { _ =>
        IO.println("my-span")
      }
    }
    provider = otel4j.getSdkMeterProvider
    _ <- IO.println(provider.forceFlush())
    _ <- IO.delay(otel4j.getSdkMeterProvider.close())
    _ <- IO.delay(otel4j.getSdkTracerProvider.close())
  } yield ()
}
