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
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.test.NoopConsole

class MetricReadersAutoConfigureSuite extends CatsEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  test("empty exporters - load nothing") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    MetricReadersAutoConfigure[IO](Set.empty)
      .configure(config)
      .use(readers => IO(assertEquals(readers, Vector.empty)))
  }

  test("load from an empty config - push exporter - use periodic reader") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    MetricReadersAutoConfigure[IO](Set(pushExporter("CustomExporter")))
      .configure(config)
      .use { readers =>
        IO(
          assertEquals(
            readers.map(_.toString),
            Vector(
              "PeriodicMetricReader{exporter=CustomExporter, interval=1 minute, timeout=30 seconds}"
            )
          )
        )
      }
  }

  test("load from the config - push exporter - use config intervals") {
    val props = Map(
      "otel.metric.export.interval" -> "10 seconds",
      "otel.metric.export.timeout" -> "5 seconds"
    )

    val config = Config.ofProps(props)

    MetricReadersAutoConfigure[IO](Set(pushExporter("CustomExporter")))
      .configure(config)
      .use { readers =>
        IO(
          assertEquals(
            readers.map(_.toString),
            Vector(
              "PeriodicMetricReader{exporter=CustomExporter, interval=10 seconds, timeout=5 seconds}"
            )
          )
        )
      }
  }

  private def pushExporter(exporterName: String): MetricExporter.Push[IO] =
    new MetricExporter.Push.Unsealed[IO] {
      def name: String = exporterName

      def aggregationTemporalitySelector: AggregationTemporalitySelector =
        AggregationTemporalitySelector.alwaysCumulative

      def defaultAggregationSelector: AggregationSelector =
        AggregationSelector.default

      def defaultCardinalityLimitSelector: CardinalityLimitSelector =
        CardinalityLimitSelector.default

      def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): IO[Unit] =
        IO.unit

      def flush: IO[Unit] =
        IO.unit
    }

}
