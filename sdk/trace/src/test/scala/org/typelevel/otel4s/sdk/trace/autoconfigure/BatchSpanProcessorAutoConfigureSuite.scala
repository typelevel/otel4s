/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.traverse._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

class BatchSpanProcessorAutoConfigureSuite extends CatsEffectSuite {

  private val exporter = SpanExporter.noop[IO]

  private val default =
    "BatchSpanProcessor{" +
      "exporter=SpanExporter.Noop, " +
      "scheduleDelay=5 seconds, " +
      "exporterTimeout=30 seconds, " +
      "maxQueueSize=2048, " +
      "maxExportBatchSize=512}"

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    BatchSpanProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, default))
      }
  }

  test("load from the config (empty string) - load default") {
    val props = Map(
      "otel.bsp.schedule.delay" -> "",
      "otel.bsp.max.queue.size" -> "",
      "otel.bsp.max.export.batch.size" -> "",
      "otel.bsp.export.timeout" -> "",
    )
    val config = Config.ofProps(props)

    BatchSpanProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, default))
      }
  }

  test("load from the config - use given value") {
    val props = Map(
      "otel.bsp.schedule.delay" -> "1s",
      "otel.bsp.max.queue.size" -> "42",
      "otel.bsp.max.export.batch.size" -> "128",
      "otel.bsp.export.timeout" -> "120 millis",
    )
    val config = Config.ofProps(props)

    val expected =
      "BatchSpanProcessor{" +
        "exporter=SpanExporter.Noop, " +
        "scheduleDelay=1 second, " +
        "exporterTimeout=120 milliseconds, " +
        "maxQueueSize=42, " +
        "maxExportBatchSize=128}"

    BatchSpanProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, expected))
      }
  }

  test("invalid config values - fail") {
    val inputs = List(
      "otel.bsp.schedule.delay" -> "some delay",
      "otel.bsp.max.queue.size" -> "-128",
      "otel.bsp.max.export.batch.size" -> "not int",
      "otel.bsp.export.timeout" -> "non-parsable",
    )

    val errors = Map(
      "otel.bsp.schedule.delay" -> "Invalid value for property otel.bsp.schedule.delay=some delay. Must be [FiniteDuration]",
      "otel.bsp.max.queue.size" -> "Dropping queue capacity must be positive, was: -128",
      "otel.bsp.max.export.batch.size" -> "Invalid value for property otel.bsp.max.export.batch.size=not int. Must be [Int]",
      "otel.bsp.export.timeout" -> "Invalid value for property otel.bsp.export.timeout=non-parsable. Must be [FiniteDuration]",
    )

    inputs.traverse { case (key, value) =>
      val config = Config.ofProps(Map(key -> value))
      val error = errors(key)

      val delay = if (key == "otel.bsp.schedule.delay") value else "N/A"
      val queueSize = if (key == "otel.bsp.max.queue.size") value else "N/A"
      val batchSize =
        if (key == "otel.bsp.max.export.batch.size") value else "N/A"
      val timeout = if (key == "otel.bsp.export.timeout") value else "N/A"

      BatchSpanProcessorAutoConfigure[IO](exporter)
        .configure(config)
        .evalMap(IO.println)
        .use_
        .attempt
        .map(_.leftMap(_.getMessage))
        .assertEquals(
          Left(
            s"""Cannot autoconfigure [BatchSpanProcessor].
              |Cause: $error.
              |Config:
              |1) `otel.bsp.export.timeout` - $timeout
              |2) `otel.bsp.max.export.batch.size` - $batchSize
              |3) `otel.bsp.max.queue.size` - $queueSize
              |4) `otel.bsp.schedule.delay` - $delay""".stripMargin
          )
        )
    }

  }

}
