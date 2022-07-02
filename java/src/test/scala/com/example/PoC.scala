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
import cats.syntax.foldable._
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.java.OtelJava

import scala.jdk.CollectionConverters._

object Poc extends IOApp.Simple {

  def run: IO[Unit] = for {
    jMetricReader <- IO(InMemoryMetricReader.create())
    otel4j <- IO {
      val meterProvider = SdkMeterProvider
        .builder()
        .registerMetricReader(jMetricReader)
        .build()

      val sdk = OpenTelemetrySdk
        .builder()
        .setMeterProvider(meterProvider)
        .build()

      sdk
    }
    otel4s = OtelJava.forSync[IO](otel4j)
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
    _ <- jMetricReader.collectAllMetrics().asScala.toList.traverse_(IO.println)
  } yield ()
}
