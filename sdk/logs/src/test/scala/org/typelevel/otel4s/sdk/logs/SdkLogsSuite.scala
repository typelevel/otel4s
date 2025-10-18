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

package org.typelevel.otel4s.sdk.logs

import cats.Foldable
import cats.effect.IO
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

class SdkLogsSuite extends CatsEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  private val NoopLogs = "SdkLogs{loggerProvider=LoggerProvider.Noop}"

  test("withConfig - use the given config") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.logs.exporter" -> "none"
      )
    )

    SdkLogs
      .autoConfigured[IO](_.withConfig(config))
      .use(logs => IO(assertEquals(logs.toString, NoopLogs)))
  }

  test("load noop instance when 'otel.sdk.disabled=true'") {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    SdkLogs
      .autoConfigured[IO](_.withConfig(config))
      .use(logs => IO(assertEquals(logs.toString, NoopLogs)))
  }

  test("withConfig - ignore 'addPropertiesCustomizer' and 'addPropertiesLoader'") {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    SdkLogs
      .autoConfigured[IO](
        _.withConfig(config)
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
      )
      .use(logs => IO(assertEquals(logs.toString, NoopLogs)))
  }

  // the latter loader should prevail
  test("addPropertiesLoader - use the loaded properties") {
    SdkLogs
      .autoConfigured[IO](
        _.addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
          .addPropertiesLoader(IO.delay(Map("otel.sdk.disabled" -> "true")))
      )
      .use(logs => IO(assertEquals(logs.toString, NoopLogs)))
  }

  // the latter customizer should prevail
  test("addPropertiesCustomizer - customize properties") {
    SdkLogs
      .autoConfigured[IO](
        _.addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "true"))
      )
      .use(logs => IO(assertEquals(logs.toString, NoopLogs)))
  }

  test("addLoggerProviderCustomizer - customize logger provider") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.logs.exporter" -> "console"
      )
    )

    val resource = TelemetryResource.default
    val limits = LogRecordLimits.builder
      .withMaxNumberOfAttributes(64)
      .withMaxAttributeValueLength(128)
      .build

    val expected =
      "SdkLogs{" +
        s"loggerProvider=SdkLoggerProvider{resource=$resource, logRecordLimits=$limits, " +
        "logRecordProcessor=SimpleLogRecordProcessor{exporter=ConsoleLogRecordExporter}}}"

    SdkLogs
      .autoConfigured[IO](
        _.withConfig(config)
          .addLoggerProviderCustomizer((b, _) => b.withLogRecordLimits(limits))
          .addLoggerProviderCustomizer((b, _) => b.withResource(resource))
      )
      .use(logs => IO(assertEquals(logs.toString, expected)))
  }

  test("addResourceCustomizer - customize a resource") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.logs.exporter" -> "console"
      )
    )

    val default = TelemetryResource.default
    val withAttributes = TelemetryResource(Attributes(Attribute("key", "value")))
    val withSchema = TelemetryResource(Attributes.empty, Some("schema"))
    val result = default.mergeUnsafe(withAttributes).mergeUnsafe(withSchema)

    val expected =
      "SdkLogs{" +
        s"loggerProvider=SdkLoggerProvider{resource=$result, logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=SimpleLogRecordProcessor{exporter=ConsoleLogRecordExporter}}}"

    SdkLogs
      .autoConfigured[IO](
        _.withConfig(config)
          .addResourceCustomizer((r, _) => r.mergeUnsafe(default))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withAttributes))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withSchema))
      )
      .use(logs => IO(assertEquals(logs.toString, expected)))
  }

  test("addExporterConfigurer - support external configurers") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.logs.exporter" -> "custom-1,custom-2"
      )
    )

    def customExporter(exporterName: String): LogRecordExporter[IO] =
      new LogRecordExporter.Unsealed[IO] {
        def name: String = exporterName
        def exportLogRecords[G[_]: Foldable](logs: G[org.typelevel.otel4s.sdk.logs.data.LogRecordData]): IO[Unit] =
          IO.unit
        def flush: IO[Unit] = IO.unit
      }

    val exporter1: LogRecordExporter[IO] = customExporter("CustomExporter1")
    val exporter2: LogRecordExporter[IO] = customExporter("CustomExporter2")

    val expected =
      "SdkLogs{" +
        s"loggerProvider=SdkLoggerProvider{resource=${TelemetryResource.default}, logRecordLimits=${LogRecordLimits.default}, " +
        "logRecordProcessor=BatchLogRecordProcessor{" +
        s"exporter=LogRecordExporter.Multi($exporter1, $exporter2), scheduleDelay=5 seconds, " +
        "exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}}}"

    SdkLogs
      .autoConfigured[IO](
        _.withConfig(config)
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-1", exporter1)
          )
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-2", exporter2)
          )
      )
      .use(logs => IO(assertEquals(logs.toString, expected)))
  }
}
