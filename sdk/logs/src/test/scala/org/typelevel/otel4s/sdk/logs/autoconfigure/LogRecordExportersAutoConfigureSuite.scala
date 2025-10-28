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
import cats.syntax.all._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

class LogRecordExportersAutoConfigureSuite extends CatsEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  // OTLPExporter exists in the separate package, so we use mock here
  private val otlpExporter = customExporter("OTLPExporter")
  private val otlp: AutoConfigure.Named[IO, LogRecordExporter[IO]] =
    AutoConfigure.Named.const("otlp", otlpExporter)

  test("load from an empty config - load default (otlp)") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    LogRecordExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (empty string) - load default (otlp)") {
    val props = Map("otel.logs.exporter" -> "")
    val config = Config.ofProps(props)

    LogRecordExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (none) - load noop") {
    val props = Map("otel.logs.exporter" -> "none")
    val config = Config.ofProps(props)

    LogRecordExportersAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { exporters =>
        IO(
          assertEquals(
            exporters.values.map(_.name).toList,
            List("LogRecordExporter.Noop")
          )
        )
      }
  }

  test("support custom configurers") {
    val props = Map("otel.logs.exporter" -> "custom")
    val config = Config.ofProps(props)

    val exporter: LogRecordExporter[IO] = customExporter("CustomExporter")

    val custom: AutoConfigure.Named[IO, LogRecordExporter[IO]] =
      AutoConfigure.Named.const("custom", exporter)

    LogRecordExportersAutoConfigure[IO](Set(custom))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("custom" -> exporter)))
      }
  }

  test("load from the config - 'none' along with others - fail") {
    val props = Map("otel.logs.exporter" -> "otlp,none")
    val config = Config.ofProps(props)

    LogRecordExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [LogRecordExporters].
               |Cause: [otel.logs.exporter] contains 'none' along with other exporters.
               |Config:
               |1) `otel.logs.exporter` - otlp,none""".stripMargin)
      )
  }

  test("load from the config - unknown exporter - fail") {
    val props = Map("otel.logs.exporter" -> "aws-xray")
    val config = Config.ofProps(props)

    LogRecordExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [LogRecordExporters].
               |Cause: Unrecognized value for [otel.logs.exporter]: aws-xray. Supported options [none, console, otlp].
               |Config:
               |1) `otel.logs.exporter` - aws-xray""".stripMargin)
      )
  }

  private def customExporter(exporterName: String): LogRecordExporter[IO] =
    new LogRecordExporter.Unsealed[IO] {
      def name: String = exporterName
      def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): IO[Unit] = ???
      def flush: IO[Unit] = IO.unit
    }

}
