/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.sdk

import cats.Foldable
import cats.effect.IO
import cats.effect.std.Console
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.NoopConsole
import org.typelevel.otel4s.sdk.trace.SpanLimits
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CBaggagePropagator
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CTraceContextPropagator
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

class OpenTelemetrySdkSuite extends CatsEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]

  private val DefaultSdk =
    sdkToString(
      TelemetryResource.empty,
      Sampler.parentBased(Sampler.AlwaysOn)
    )

  private val NoopSdk =
    "OpenTelemetrySdk.AutoConfigured{sdk=" +
      "OpenTelemetrySdk{" +
      "meterProvider=MeterProvider.Noop, " +
      "tracerProvider=TracerProvider.Noop, " +
      s"propagators=ContextPropagators.Noop}, resource=${TelemetryResource.empty}}"

  test("withConfig - use the given config") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))

    OpenTelemetrySdk
      .autoConfigured[IO](_.withConfig(config))
      .use(traces => IO(assertEquals(traces.toString, DefaultSdk)))
  }

  test("load noop instance when 'otel.sdk.disabled=true'") {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    OpenTelemetrySdk
      .autoConfigured[IO](_.withConfig(config))
      .use(traces => IO(assertEquals(traces.toString, NoopSdk)))
  }

  test(
    "withConfig - ignore 'addPropertiesCustomizer' and 'addPropertiesLoader'"
  ) {
    val config = Config.ofProps(Map("otel.sdk.disabled" -> "true"))

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config)
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
      )
      .use(traces => IO(assertEquals(traces.toString, NoopSdk)))
  }

  // the latter loader should prevail
  test("addPropertiesLoader - use the loaded properties") {
    OpenTelemetrySdk
      .autoConfigured[IO](
        _.addPropertiesLoader(IO.pure(Map("otel.sdk.disabled" -> "false")))
          .addPropertiesLoader(IO.delay(Map("otel.sdk.disabled" -> "true")))
      )
      .use(traces => IO(assertEquals(traces.toString, NoopSdk)))
  }

  // the latter customizer should prevail
  test("addPropertiesCustomizer - customize properties") {
    OpenTelemetrySdk
      .autoConfigured[IO](
        _.addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "false"))
          .addPropertiesCustomizer(_ => Map("otel.sdk.disabled" -> "true"))
      )
      .use(traces => IO(assertEquals(traces.toString, NoopSdk)))
  }

  test("addTracerProviderCustomizer - customize tracer provider") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))

    val sampler = Sampler.AlwaysOff

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config).addTracerProviderCustomizer((t, _) =>
          t.withSampler(sampler)
        )
      )
      .use { traces =>
        IO(assertEquals(traces.toString, sdkToString(sampler = sampler)))
      }
  }

  test("addResourceCustomizer - customize a resource") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))

    val default = TelemetryResource.default
    val withAttributes =
      TelemetryResource(Attributes(Attribute("key", "value")))
    val withSchema = TelemetryResource(Attributes.empty, Some("schema"))
    val result = default.mergeUnsafe(withAttributes).mergeUnsafe(withSchema)

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config)
          .addResourceCustomizer((r, _) => r.mergeUnsafe(default))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withAttributes))
          .addResourceCustomizer((r, _) => r.mergeUnsafe(withSchema))
      )
      .use { traces =>
        IO(
          assertEquals(traces.toString, sdkToString(result))
        )
      }
  }

  test("addExporterConfigurer - support external configurers") {
    val config = Config.ofProps(
      Map("otel.traces.exporter" -> "custom-1,custom-2")
    )

    def customExporter(exporterName: String): SpanExporter[IO] =
      new SpanExporter[IO] {
        def name: String = exporterName
        def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] = IO.unit
        def flush: IO[Unit] = IO.unit
      }

    val exporter1: SpanExporter[IO] = customExporter("CustomExporter1")
    val exporter2: SpanExporter[IO] = customExporter("CustomExporter2")

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config)
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-1", exporter1)
          )
          .addExporterConfigurer(
            AutoConfigure.Named.const("custom-2", exporter2)
          )
      )
      .use { traces =>
        IO(
          assertEquals(
            traces.toString,
            sdkToString(
              exporter = "SpanExporter.Multi(CustomExporter1, CustomExporter2)"
            )
          )
        )
      }
  }

  test("addSamplerConfigurer - support external configurers") {
    val config = Config.ofProps(
      Map(
        "otel.traces.exporter" -> "none",
        "otel.traces.sampler" -> "custom-sampler",
      )
    )

    val sampler: Sampler = new Sampler {
      def shouldSample(
          parentContext: Option[SpanContext],
          traceId: ByteVector,
          name: String,
          spanKind: SpanKind,
          attributes: Attributes,
          parentLinks: Vector[LinkData]
      ): SamplingResult =
        SamplingResult.Drop

      def description: String = "CustomSampler"
    }

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config).addSamplerConfigurer(
          AutoConfigure.Named.const("custom-sampler", sampler)
        )
      )
      .use { traces =>
        IO(assertEquals(traces.toString, sdkToString(sampler = sampler)))
      }
  }

  test("addTextMapPropagatorConfigurer - support external configurers") {
    val config = Config.ofProps(
      Map(
        "otel.traces.exporter" -> "none",
        "otel.propagators" -> "tracecontext,custom-1,custom-2,baggage",
      )
    )

    def customPropagator(name: String): TextMapPropagator[Context] =
      new TextMapPropagator[Context] {
        def fields: Iterable[String] = Nil
        def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = ???
        def inject[A: TextMapUpdater](ctx: Context, carrier: A): A = ???
        override def toString: String = name
      }

    val propagator1 = customPropagator("CustomPropagator1")
    val propagator2 = customPropagator("CustomPropagator2")

    val expected = ContextPropagators.of(
      W3CTraceContextPropagator.default,
      propagator1,
      propagator2,
      W3CBaggagePropagator.default
    )

    OpenTelemetrySdk
      .autoConfigured[IO](
        _.withConfig(config)
          .addTextMapPropagatorConfigurer(
            AutoConfigure.Named.const("custom-1", propagator1)
          )
          .addTextMapPropagatorConfigurer(
            AutoConfigure.Named.const("custom-2", propagator2)
          )
      )
      .use { traces =>
        IO(
          assertEquals(traces.toString, sdkToString(propagators = expected))
        )
      }
  }

  private def sdkToString(
      resource: TelemetryResource = TelemetryResource.empty,
      sampler: Sampler = Sampler.parentBased(Sampler.AlwaysOn),
      propagators: ContextPropagators[Context] = ContextPropagators.of(
        W3CTraceContextPropagator.default,
        W3CBaggagePropagator.default
      ),
      exporter: String = "SpanExporter.Noop"
  ) =
    "OpenTelemetrySdk.AutoConfigured{sdk=" +
      "OpenTelemetrySdk{meterProvider=MeterProvider.Noop, " +
      "tracerProvider=" +
      s"SdkTracerProvider{resource=$resource, spanLimits=${SpanLimits.Default}, sampler=$sampler, " +
      "spanProcessor=SpanProcessor.Multi(" +
      s"BatchSpanProcessor{exporter=$exporter, scheduleDelay=5 seconds, exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}, " +
      "SpanStorage)}, " +
      s"propagators=$propagators}, resource=$resource}"

}
