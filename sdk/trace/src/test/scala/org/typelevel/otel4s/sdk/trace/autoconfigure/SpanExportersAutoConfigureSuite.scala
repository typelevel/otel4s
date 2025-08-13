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

import cats.Foldable
import cats.effect.IO
import cats.effect.std.Console
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.test.NoopConsole
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

class SpanExportersAutoConfigureSuite extends CatsEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  // OTLPExporter exists in the separate package, so we use mock here
  private val otlpExporter = customExporter("OTLPExporter")
  private val otlp: AutoConfigure.Named[IO, SpanExporter[IO]] =
    AutoConfigure.Named.const("otlp", otlpExporter)

  test("load from an empty config - load default (otlp)") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    SpanExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (empty string) - load default (otlp)") {
    val props = Map("otel.traces.exporter" -> "")
    val config = Config.ofProps(props)

    SpanExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (none) - load noop") {
    val props = Map("otel.traces.exporter" -> "none")
    val config = Config.ofProps(props)

    SpanExportersAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { exporters =>
        IO(
          assertEquals(
            exporters.values.map(_.name).toList,
            List("SpanExporter.Noop")
          )
        )
      }
  }

  test("support custom configurers") {
    val props = Map("otel.traces.exporter" -> "custom")
    val config = Config.ofProps(props)

    val exporter: SpanExporter[IO] = customExporter("CustomExporter")

    val custom: AutoConfigure.Named[IO, SpanExporter[IO]] =
      AutoConfigure.Named.const("custom", exporter)

    SpanExportersAutoConfigure[IO](Set(custom))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("custom" -> exporter)))
      }
  }

  test("load from the config - 'none' along with others - fail") {
    val props = Map("otel.traces.exporter" -> "otlp,none")
    val config = Config.ofProps(props)

    SpanExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [SpanExporters].
               |Cause: [otel.traces.exporter] contains 'none' along with other exporters.
               |Config:
               |1) `otel.traces.exporter` - otlp,none""".stripMargin)
      )
  }

  test("load from the config - unknown exporter - fail") {
    val props = Map("otel.traces.exporter" -> "aws-xray")
    val config = Config.ofProps(props)

    SpanExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [SpanExporters].
            |Cause: Unrecognized value for [otel.traces.exporter]: aws-xray. Supported options [none, console, otlp].
            |Config:
            |1) `otel.traces.exporter` - aws-xray""".stripMargin)
      )
  }

  private def customExporter(exporterName: String): SpanExporter[IO] =
    new SpanExporter.Unsealed[IO] {
      def name: String = exporterName
      def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] = IO.unit
      def flush: IO[Unit] = IO.unit
    }
}
