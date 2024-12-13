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

package org.typelevel.otel4s.sdk.metrics

import cats.Foldable
import cats.effect.IO
import cats.effect.std.Console
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector
import org.typelevel.otel4s.sdk.metrics.view.View
import org.typelevel.otel4s.sdk.test.NoopConsole

class SdkMetricsSuite extends CatsEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  private val DefaultMetrics =
    metricsToString(TelemetryResource.default)

  private val NoopMetrics =
    "SdkMetrics{meterProvider=MeterProvider.Noop}"

  test("withConfig - use the given config") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.metrics.exporter" -> "console"
      )
    )

    SdkMetrics
      .autoConfigured[IO](_.withConfig(config))
      .use(metrics => IO(assertEquals(metrics.toString, DefaultMetrics)))
  }

  test("load noop instance when 'otel.sdk.disabled=true'") {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    SdkMetrics
      .autoConfigured[IO](_.withConfig(config))
      .use(metrics => IO(assertEquals(metrics.toString, NoopMetrics)))
  }

  test(
    "withConfig - ignore 'addPropertiesCustomizer' and 'addPropertiesLoader'"
  ) {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    SdkMetrics
      .autoConfigured[IO](
        _.withConfig(config)
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
      )
      .use(metrics => IO(assertEquals(metrics.toString, NoopMetrics)))
  }

  // the latter loader should prevail
  test("addPropertiesLoader - use the loaded properties") {
    SdkMetrics
      .autoConfigured[IO](
        _.addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
          .addPropertiesLoader(IO.delay(Map("otel.sdk.disabled" -> "true")))
      )
      .use(metrics => IO(assertEquals(metrics.toString, NoopMetrics)))
  }

  // the latter customizer should prevail
  test("addPropertiesCustomizer - customize properties") {
    SdkMetrics
      .autoConfigured[IO](
        _.addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "true"))
      )
      .use(metrics => IO(assertEquals(metrics.toString, NoopMetrics)))
  }

  test("addMeterProviderCustomizer - customize meter provider") {
    val config = Config.ofProps(Map("otel.metrics.exporter" -> "console"))

    val selector = InstrumentSelector.builder.withInstrumentName("*").build
    val view = View.builder.build
    val resource = TelemetryResource.default

    SdkMetrics
      .autoConfigured[IO](
        _.withConfig(config)
          .addMeterProviderCustomizer((t, _) => t.registerView(selector, view))
          .addMeterProviderCustomizer((t, _) => t.withResource(resource))
      )
      .use { metrics =>
        IO(
          assertEquals(
            metrics.toString,
            metricsToString(
              resource,
              "RegisteredView{selector=InstrumentSelector{instrumentName=*}, view=View{}}"
            )
          )
        )
      }
  }

  test("addResourceCustomizer - customize a resource") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.metrics.exporter" -> "console"
      )
    )

    val default = TelemetryResource.default
    val withAttributes =
      TelemetryResource(Attributes(Attribute("key", "value")))
    val withSchema = TelemetryResource(Attributes.empty, Some("schema"))
    val result = default.mergeUnsafe(withAttributes).mergeUnsafe(withSchema)

    SdkMetrics
      .autoConfigured[IO](
        _.withConfig(config)
          .addResourceCustomizer((r, _) => r.mergeUnsafe(default))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withAttributes))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withSchema))
      )
      .use { metrics =>
        IO(
          assertEquals(metrics.toString, metricsToString(result))
        )
      }
  }

  test("addExporterConfigurer - support external configurers") {
    val config = Config.ofProps(
      Map(
        "otel.otel4s.resource.detectors.enabled" -> "none",
        "otel.metrics.exporter" -> "custom-1,custom-2"
      )
    )

    val exporter1: MetricExporter[IO] = customExporter("CustomExporter1")
    val exporter2: MetricExporter[IO] = customExporter("CustomExporter2")

    SdkMetrics
      .autoConfigured[IO](
        _.withConfig(config)
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-1", exporter1)
          )
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-2", exporter2)
          )
      )
      .use { metrics =>
        IO(
          assertEquals(
            metrics.toString,
            metricsToString(
              exporters = List("CustomExporter1", "CustomExporter2")
            )
          )
        )
      }
  }

  private def customExporter(exporterName: String): MetricExporter.Push[IO] =
    new MetricExporter.Push[IO] {
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

  private def metricsToString(
      resource: TelemetryResource = TelemetryResource.default,
      view: String = "",
      exporters: List[String] = List("ConsoleMetricExporter")
  ) =
    "SdkMetrics{meterProvider=" +
      s"SdkMeterProvider{resource=$resource, " +
      s"metricReaders=[${exporters.map(e => s"PeriodicMetricReader{exporter=$e, interval=1 minute, timeout=30 seconds}").mkString(", ")}], " +
      s"metricProducers=[], views=[$view]}}"
}
