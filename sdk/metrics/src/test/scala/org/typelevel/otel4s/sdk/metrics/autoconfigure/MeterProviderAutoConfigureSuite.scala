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
import cats.effect.Resource
import cats.effect.std.Random
import cats.mtl.Ask
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer

class MeterProviderAutoConfigureSuite extends CatsEffectSuite {
  import MeterProviderAutoConfigure.Customizer

  test("noop exporter - load noop provider") {
    val config = Config.ofProps(Map("otel.metrics.exporter" -> "none"))
    val expected = "MeterProvider.Noop"

    configure(config) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("load default") {
    val config = Config.ofProps(Map("otel.metrics.exporter" -> "console"))

    val expected =
      s"SdkMeterProvider{resource=${TelemetryResource.empty}, " +
        "metricReaders=[PeriodicMetricReader{exporter=ConsoleMetricExporter, interval=1 minute, timeout=30 seconds}], " +
        "metricProducers=[], views=[]}"

    configure(config) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("customize meter provider") {
    val config = Config.ofProps(Map("otel.metrics.exporter" -> "console"))
    val name = "CustomMetricProducer"

    val producer = new MetricProducer[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
      override def toString: String = name
    }

    val expected =
      s"SdkMeterProvider{resource=${TelemetryResource.empty}, " +
        "metricReaders=[PeriodicMetricReader{exporter=ConsoleMetricExporter, interval=1 minute, timeout=30 seconds}], " +
        "metricProducers=[CustomMetricProducer], views=[]}"

    configure(
      config,
      customizer = (b, _) => b.registerMetricProducer(producer)
    ) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("use given resource") {
    val config = Config.ofProps(Map("otel.metrics.exporter" -> "console"))
    val resource = TelemetryResource.default

    val expected =
      s"SdkMeterProvider{resource=$resource, " +
        "metricReaders=[PeriodicMetricReader{exporter=ConsoleMetricExporter, interval=1 minute, timeout=30 seconds}], " +
        "metricProducers=[], views=[]}"

    configure(config, resource = resource) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  test("use given exporter configurers") {
    val config = Config.ofProps(
      Map("otel.metrics.exporter" -> "custom-1,custom-2")
    )

    val exporter1: MetricExporter[IO] = customExporter("CustomExporter1")
    val exporter2: MetricExporter[IO] = customExporter("CustomExporter2")

    val configurers: Set[AutoConfigure.Named[IO, MetricExporter[IO]]] = Set(
      AutoConfigure.Named.const("custom-1", exporter1),
      AutoConfigure.Named.const("custom-2", exporter2)
    )

    val expected =
      s"SdkMeterProvider{resource=${TelemetryResource.empty}, " +
        "metricReaders=[" +
        "PeriodicMetricReader{exporter=CustomExporter1, interval=1 minute, timeout=30 seconds}, " +
        "PeriodicMetricReader{exporter=CustomExporter2, interval=1 minute, timeout=30 seconds}" +
        "], metricProducers=[], views=[]}"

    configure(config, exporterConfigurers = configurers) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  private def configure[A](
      config: Config,
      resource: TelemetryResource = TelemetryResource.empty,
      customizer: Customizer[SdkMeterProvider.Builder[IO]] = (a, _) => a,
      exporterConfigurers: Set[AutoConfigure.Named[IO, MetricExporter[IO]]] = Set.empty
  )(f: MeterProvider[IO] => IO[A]): IO[A] =
    Resource.eval(Random.scalaUtilRandom[IO]).use { implicit random =>
      implicit val askContext: AskContext[IO] = Ask.const(Context.root)

      val autoConfigure = MeterProviderAutoConfigure[IO](
        resource,
        TraceContextLookup.noop,
        customizer,
        exporterConfigurers
      )

      autoConfigure.configure(config).use(f)
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

}
