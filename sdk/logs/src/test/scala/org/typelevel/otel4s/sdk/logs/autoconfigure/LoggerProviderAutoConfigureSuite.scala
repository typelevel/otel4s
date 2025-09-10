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

import cats.Foldable
import cats.effect.IO
import cats.mtl.Ask
import munit.CatsEffectSuite
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.logs.LogRecordLimits
import org.typelevel.otel4s.sdk.logs.SdkLoggerProvider
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

class LoggerProviderAutoConfigureSuite extends CatsEffectSuite {
  import LoggerProviderAutoConfigure.Customizer

  test("noop exporter - load noop provider") {
    val config = Config.ofProps(Map("otel.logs.exporter" -> "none"))
    val expected = "LoggerProvider.Noop"

    configure(config) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("load default") {
    val config = Config.ofProps(Map("otel.logs.exporter" -> "console"))

    val expected =
      "SdkLoggerProvider{" +
        s"resource=${TelemetryResource.empty}, " +
        s"logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=SimpleLogRecordProcessor{exporter=ConsoleLogRecordExporter}}"

    configure(config) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("use given resource") {
    val config = Config.ofProps(Map("otel.logs.exporter" -> "console"))
    val resource = TelemetryResource.default

    val expected =
      "SdkLoggerProvider{" +
        s"resource=$resource, " +
        s"logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=SimpleLogRecordProcessor{exporter=ConsoleLogRecordExporter}}"

    configure(config, resource = resource) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("use given exporter configurers") {
    val config = Config.ofProps(
      Map("otel.logs.exporter" -> "custom-1,custom-2")
    )

    val exporter1: LogRecordExporter[IO] = customExporter("CustomExporter1")
    val exporter2: LogRecordExporter[IO] = customExporter("CustomExporter2")

    val configurers: Set[AutoConfigure.Named[IO, LogRecordExporter[IO]]] = Set(
      AutoConfigure.Named.const("custom-1", exporter1),
      AutoConfigure.Named.const("custom-2", exporter2)
    )

    val expected =
      "SdkLoggerProvider{" +
        s"resource=${TelemetryResource.empty}, " +
        s"logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=BatchLogRecordProcessor{" +
        s"exporter=LogRecordExporter.Multi($exporter1, $exporter2), scheduleDelay=5 seconds, " +
        "exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}" +
        "}"

    configure(config, exporterConfigurers = configurers) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("console exporter should use a dedicated SimpleLogRecordProcessor") {
    val config = Config.ofProps(
      Map(
        "otel.logs.exporter" -> "console,custom",
      )
    )

    val custom: LogRecordExporter[IO] = customExporter("CustomExporter")

    val configurers: Set[AutoConfigure.Named[IO, LogRecordExporter[IO]]] = Set(
      AutoConfigure.Named.const("custom", custom)
    )

    val expected =
      "SdkLoggerProvider{" +
        s"resource=${TelemetryResource.empty}, " +
        s"logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=LogRecordProcessor.Multi(" +
        "SimpleLogRecordProcessor{exporter=ConsoleLogRecordExporter}, " +
        "BatchLogRecordProcessor{exporter=CustomExporter, scheduleDelay=5 seconds, exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512})}"

    configure(config, exporterConfigurers = configurers) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  private def configure[A](
      config: Config,
      resource: TelemetryResource = TelemetryResource.empty,
      customizer: Customizer[SdkLoggerProvider.Builder[IO]] = (a, _) => a,
      exporterConfigurers: Set[AutoConfigure.Named[IO, LogRecordExporter[IO]]] = Set.empty
  )(f: LoggerProvider[IO, Context] => IO[A]): IO[A] = {
    implicit val askContext: AskContext[IO] = Ask.const(Context.root)

    val autoConfigure = LoggerProviderAutoConfigure[IO](
      resource,
      TraceContext.Lookup.noop,
      customizer,
      exporterConfigurers
    )

    autoConfigure.configure(config).use(f)
  }

  private def customExporter(exporterName: String): LogRecordExporter[IO] =
    new LogRecordExporter.Unsealed[IO] {
      def name: String = exporterName
      def exportLogRecords[G[_]: Foldable](spans: G[LogRecordData]): IO[Unit] = IO.unit
      def flush: IO[Unit] = IO.unit
    }

}
