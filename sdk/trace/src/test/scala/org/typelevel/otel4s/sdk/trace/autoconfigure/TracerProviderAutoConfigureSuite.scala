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
import cats.effect.Resource
import cats.effect.std.Random
import munit.CatsEffectSuite
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CBaggagePropagator
import org.typelevel.otel4s.sdk.trace.context.propagation.W3CTraceContextPropagator
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.TracerProvider
import scodec.bits.ByteVector

class TracerProviderAutoConfigureSuite extends CatsEffectSuite {
  import TracerProviderAutoConfigure.Customizer

  private val DefaultProvider =
    providerToString(
      TelemetryResource.empty,
      Sampler.parentBased(Sampler.AlwaysOn)
    )

  test("load default") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))

    configure(config) { provider =>
      IO(assertEquals(provider.toString, DefaultProvider))
    }
  }

  test("customize tracer provider") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))
    val sampler = Sampler.AlwaysOff

    configure(config, customizer = (b, _) => b.withSampler(sampler)) { p =>
      IO(assertEquals(p.toString, providerToString(sampler = sampler)))
    }
  }

  test("use given resource") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))
    val resource = TelemetryResource.default

    configure(config, resource = resource) { provider =>
      IO(assertEquals(provider.toString, providerToString(resource = resource)))
    }
  }

  test("use given exporter configurers") {
    val config = Config.ofProps(
      Map("otel.traces.exporter" -> "custom-1,custom-2")
    )

    val exporter1: SpanExporter[IO] = customExporter("CustomExporter1")
    val exporter2: SpanExporter[IO] = customExporter("CustomExporter2")

    val configurers: Set[AutoConfigure.Named[IO, SpanExporter[IO]]] = Set(
      AutoConfigure.Named.const("custom-1", exporter1),
      AutoConfigure.Named.const("custom-2", exporter2)
    )

    configure(config, exporterConfigurers = configurers) { provider =>
      IO(
        assertEquals(
          provider.toString,
          providerToString(
            exporter = "SpanExporter.Multi(CustomExporter1, CustomExporter2)"
          )
        )
      )
    }
  }

  test("use given sampler configurers") {
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

    val configurers: Set[AutoConfigure.Named[IO, Sampler]] = Set(
      AutoConfigure.Named.const("custom-sampler", sampler)
    )

    configure(config, samplerConfigurers = configurers) { provider =>
      IO(assertEquals(provider.toString, providerToString(sampler = sampler)))
    }
  }

  test("use given context propagator") {
    val config = Config.ofProps(Map("otel.traces.exporter" -> "none"))

    def customPropagator(name: String): TextMapPropagator[Context] =
      new TextMapPropagator[Context] {
        def fields: Iterable[String] = Nil
        def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = ???
        def inject[A: TextMapUpdater](ctx: Context, carrier: A): A = ???
        override def toString: String = name
      }

    val propagator1 = customPropagator("CustomPropagator1")
    val propagator2 = customPropagator("CustomPropagator2")

    val propagators = ContextPropagators.of(
      W3CTraceContextPropagator.default,
      propagator1,
      propagator2,
      W3CBaggagePropagator.default
    )

    configure(config, propagators = propagators) { provider =>
      IO(assertEquals(provider.toString, providerToString()))
    }
  }

  test("console exporter should use a dedicated SimpleSpanProcessor") {
    val config = Config.ofProps(
      Map(
        "otel.traces.exporter" -> "console,custom",
        "otel.traces.sampler" -> "always_off"
      )
    )

    val custom: SpanExporter[IO] = customExporter("CustomExporter")

    val configurers: Set[AutoConfigure.Named[IO, SpanExporter[IO]]] = Set(
      AutoConfigure.Named.const("custom", custom)
    )

    val expected =
      "SdkTracerProvider{" +
        s"resource=${TelemetryResource.empty}, " +
        s"sampler=${Sampler.AlwaysOff}, " +
        "spanProcessor=SpanProcessor.Multi(" +
        "SimpleSpanProcessor{exporter=ConsoleSpanExporter, exportOnlySampled=true}, " +
        "BatchSpanProcessor{exporter=CustomExporter, scheduleDelay=5 seconds, exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}, " +
        "SpanStorage)}"

    configure(config, exporterConfigurers = configurers) { provider =>
      IO(assertEquals(provider.toString, expected))
    }
  }

  private def configure[A](
      config: Config,
      resource: TelemetryResource = TelemetryResource.empty,
      propagators: ContextPropagators[Context] = ContextPropagators.noop,
      customizer: Customizer[SdkTracerProvider.Builder[IO]] = (a, _) => a,
      samplerConfigurers: Set[AutoConfigure.Named[IO, Sampler]] = Set.empty,
      exporterConfigurers: Set[AutoConfigure.Named[IO, SpanExporter[IO]]] =
        Set.empty
  )(f: TracerProvider[IO] => IO[A]): IO[A] =
    Resource.eval(LocalProvider[IO, Context].local).use { implicit local =>
      Resource.eval(Random.scalaUtilRandom[IO]).use { implicit random =>
        val autoConfigure = TracerProviderAutoConfigure[IO](
          resource,
          propagators,
          customizer,
          samplerConfigurers,
          exporterConfigurers
        )

        autoConfigure.configure(config).use(f)
      }
    }

  private def customExporter(exporterName: String): SpanExporter[IO] =
    new SpanExporter[IO] {
      def name: String = exporterName
      def exportSpans[G[_]: Foldable](spans: G[SpanData]): IO[Unit] = IO.unit
      def flush: IO[Unit] = IO.unit
    }

  private def providerToString(
      resource: TelemetryResource = TelemetryResource.empty,
      sampler: Sampler = Sampler.parentBased(Sampler.AlwaysOn),
      exporter: String = "SpanExporter.Noop"
  ) =
    "SdkTracerProvider{" +
      s"resource=$resource, " +
      s"sampler=$sampler, " +
      "spanProcessor=SpanProcessor.Multi(" +
      s"BatchSpanProcessor{exporter=$exporter, scheduleDelay=5 seconds, exporterTimeout=30 seconds, maxQueueSize=2048, maxExportBatchSize=512}, " +
      "SpanStorage)}"

}
