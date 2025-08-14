/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.autoconfigure

import cats.Foldable
import cats.effect.IO
import cats.effect.std.Console
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.test.NoopConsole

class MetricExportersAutoConfigureSuite extends CatsEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  // OTLPExporter exists in the separate package, so we use mock here
  private val otlpExporter = customExporter("OTLPExporter")
  private val otlp: AutoConfigure.Named[IO, MetricExporter[IO]] =
    AutoConfigure.Named.const("otlp", otlpExporter)

  test("load from an empty config - load default (otlp)") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    MetricExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (empty string) - load default (otlp)") {
    val props = Map("otel.metrics.exporter" -> "")
    val config = Config.ofProps(props)

    MetricExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("otlp" -> otlpExporter)))
      }
  }

  test("load from the config (none) - load noop") {
    val props = Map("otel.metrics.exporter" -> "none")
    val config = Config.ofProps(props)

    MetricExportersAutoConfigure[IO](Set.empty)
      .configure(config)
      .use { exporters =>
        IO(
          assertEquals(
            exporters.values.map(_.name).toList,
            List("MetricExporter.Noop")
          )
        )
      }
  }

  test("support custom configurers") {
    val props = Map("otel.metrics.exporter" -> "custom")
    val config = Config.ofProps(props)

    val exporter: MetricExporter[IO] = customExporter("CustomExporter")

    val custom: AutoConfigure.Named[IO, MetricExporter[IO]] =
      AutoConfigure.Named.const("custom", exporter)

    MetricExportersAutoConfigure[IO](Set(custom))
      .configure(config)
      .use { exporters =>
        IO(assertEquals(exporters, Map("custom" -> exporter)))
      }
  }

  test("load from the config - 'none' along with others - fail") {
    val props = Map("otel.metrics.exporter" -> "otlp,none")
    val config = Config.ofProps(props)

    MetricExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [MetricExporters].
               |Cause: [otel.metrics.exporter] contains 'none' along with other exporters.
               |Config:
               |1) `otel.metrics.exporter` - otlp,none""".stripMargin)
      )
  }

  test("load from the config - unknown exporter - fail") {
    val props = Map("otel.metrics.exporter" -> "aws-xray")
    val config = Config.ofProps(props)

    MetricExportersAutoConfigure[IO](Set(otlp))
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left("""Cannot autoconfigure [MetricExporters].
               |Cause: Unrecognized value for [otel.metrics.exporter]: aws-xray. Supported options [none, console, otlp].
               |Config:
               |1) `otel.metrics.exporter` - aws-xray""".stripMargin)
      )
  }

  private def customExporter(exporterName: String): MetricExporter.Push[IO] =
    new MetricExporter.Push.Unsealed[IO] {
      def name: String =
        exporterName

      def aggregationTemporalitySelector: AggregationTemporalitySelector =
        AggregationTemporalitySelector.alwaysCumulative

      def defaultAggregationSelector: AggregationSelector =
        AggregationSelector.default

      def defaultCardinalityLimitSelector: CardinalityLimitSelector =
        CardinalityLimitSelector.default

      def exportMetrics[G[_]: Foldable](spans: G[MetricData]): IO[Unit] =
        IO.unit

      def flush: IO[Unit] =
        IO.unit
    }
}
