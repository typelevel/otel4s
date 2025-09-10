/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs.autoconfigure

import cats.effect.IO
import cats.syntax.all._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

class BatchLogRecordProcessorAutoConfigureSuite extends CatsEffectSuite {

  private val exporter = LogRecordExporter.noop[IO]

  private val default =
    "BatchLogRecordProcessor{" +
      "exporter=LogRecordExporter.Noop, " +
      "scheduleDelay=5 seconds, " +
      "exporterTimeout=30 seconds, " +
      "maxQueueSize=2048, " +
      "maxExportBatchSize=512}"

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    BatchLogRecordProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, default))
      }
  }

  test("load from the config (empty string) - load default") {
    val props = Map(
      "otel.blrp.schedule.delay" -> "",
      "otel.blrp.max.queue.size" -> "",
      "otel.blrp.max.export.batch.size" -> "",
      "otel.blrp.export.timeout" -> "",
    )
    val config = Config.ofProps(props)

    BatchLogRecordProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, default))
      }
  }

  test("load from the config - use given value") {
    val props = Map(
      "otel.blrp.schedule.delay" -> "1s",
      "otel.blrp.max.queue.size" -> "42",
      "otel.blrp.max.export.batch.size" -> "128",
      "otel.blrp.export.timeout" -> "120 millis",
    )
    val config = Config.ofProps(props)

    val expected =
      "BatchLogRecordProcessor{" +
        "exporter=LogRecordExporter.Noop, " +
        "scheduleDelay=1 second, " +
        "exporterTimeout=120 milliseconds, " +
        "maxQueueSize=42, " +
        "maxExportBatchSize=128}"

    BatchLogRecordProcessorAutoConfigure[IO](exporter)
      .configure(config)
      .use { processor =>
        IO(assertEquals(processor.name, expected))
      }
  }

  test("invalid config values - fail") {
    val inputs = List(
      "otel.blrp.schedule.delay" -> "some delay",
      "otel.blrp.max.queue.size" -> "-128",
      "otel.blrp.max.export.batch.size" -> "not int",
      "otel.blrp.export.timeout" -> "non-parsable",
    )

    val errors = Map(
      "otel.blrp.schedule.delay" -> "Invalid value for property otel.blrp.schedule.delay=some delay. Must be [FiniteDuration]",
      "otel.blrp.max.queue.size" -> "Dropping queue capacity must be positive, was: -128",
      "otel.blrp.max.export.batch.size" -> "Invalid value for property otel.blrp.max.export.batch.size=not int. Must be [Int]",
      "otel.blrp.export.timeout" -> "Invalid value for property otel.blrp.export.timeout=non-parsable. Must be [FiniteDuration]",
    )

    inputs.traverse { case (key, value) =>
      val config = Config.ofProps(Map(key -> value))
      val error = errors(key)

      val delay = if (key == "otel.blrp.schedule.delay") value else "N/A"
      val queueSize = if (key == "otel.blrp.max.queue.size") value else "N/A"
      val batchSize =
        if (key == "otel.blrp.max.export.batch.size") value else "N/A"
      val timeout = if (key == "otel.blrp.export.timeout") value else "N/A"

      BatchLogRecordProcessorAutoConfigure[IO](exporter)
        .configure(config)
        .evalMap(IO.println)
        .use_
        .attempt
        .map(_.leftMap(_.getMessage))
        .assertEquals(
          Left(
            s"""Cannot autoconfigure [BatchLogRecordProcessor].
               |Cause: $error.
               |Config:
               |1) `otel.blrp.export.timeout` - $timeout
               |2) `otel.blrp.max.export.batch.size` - $batchSize
               |3) `otel.blrp.max.queue.size` - $queueSize
               |4) `otel.blrp.schedule.delay` - $delay""".stripMargin
          )
        )
    }

  }
}
