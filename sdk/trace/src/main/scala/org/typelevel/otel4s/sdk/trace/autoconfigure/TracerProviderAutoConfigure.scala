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

import cats.Parallel
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.foldable._
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.autoconfigure.TracerProviderAutoConfigure.Customizer
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.trace.TracerProvider

private final class TracerProviderAutoConfigure[
    F[_]: Temporal: Parallel: Random: Console: LocalContext
](
    resource: TelemetryResource,
    contextPropagators: ContextPropagators[Context],
    customizer: Customizer[SdkTracerProvider.Builder[F]],
    samplerConfigurers: Set[AutoConfigure.Named[F, Sampler[F]]],
    exporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]]
) extends AutoConfigure.WithHint[F, TracerProvider[F]](
      "TracerProvider",
      Set.empty
    ) {

  protected def fromConfig(config: Config): Resource[F, TracerProvider[F]] = {
    val samplerAutoConfigure =
      SamplerAutoConfigure[F](samplerConfigurers)

    val exporterAutoConfigure =
      SpanExportersAutoConfigure[F](exporterConfigurers)

    for {
      sampler <- samplerAutoConfigure.configure(config)
      exporters <- exporterAutoConfigure.configure(config)
      processors <- configureProcessors(config, exporters)
      spanLimits <- SpanLimitsAutoConfigure[F].configure(config)

      tracerProviderBuilder = {
        val builder = SdkTracerProvider
          .builder[F]
          .withResource(resource)
          .withSpanLimits(spanLimits)
          .withSampler(sampler)
          .addTextMapPropagators(contextPropagators.textMapPropagator)

        processors.foldLeft(builder)(_.addSpanProcessor(_))
      }

      tracerProvider <- Resource.eval(
        customizer(tracerProviderBuilder, config).build
      )
    } yield tracerProvider
  }

  private def configureProcessors(
      config: Config,
      exporters: Map[String, SpanExporter[F]]
  ): Resource[F, List[SpanProcessor[F]]] = {
    val consoleExporter = SpanExportersAutoConfigure.Const.ConsoleExporter

    val console = exporters.get(consoleExporter) match {
      case Some(console) => List(SimpleSpanProcessor(console))
      case None          => Nil
    }

    val others = exporters.removed(consoleExporter)
    if (others.nonEmpty) {
      val exporter = others.values.toList.combineAll
      BatchSpanProcessorAutoConfigure[F](exporter)
        .configure(config)
        .map(processor => console :+ processor)
    } else {
      Resource.pure(console)
    }
  }
}

private[sdk] object TracerProviderAutoConfigure {

  type Customizer[A] = (A, Config) => A

  /** Autoconfigures [[TracerProvider]].
    *
    * @see
    *   [[SpanExportersAutoConfigure]]
    *
    * @see
    *   [[SamplerAutoConfigure]]
    *
    * @see
    *   [[BatchSpanProcessorAutoConfigure]]
    *
    * @param resource
    *   the resource to use
    *
    * @param contextPropagators
    *   the context propagators to use
    *
    * @param tracerProviderBuilderCustomizer
    *   the function to customize the builder
    *
    * @param samplerConfigurers
    *   the extra sampler configurers
    *
    * @param exporterConfigurers
    *   the extra exporter configurers
    */
  def apply[F[_]: Temporal: Parallel: Random: Console: LocalContext](
      resource: TelemetryResource,
      contextPropagators: ContextPropagators[Context],
      tracerProviderBuilderCustomizer: Customizer[SdkTracerProvider.Builder[F]],
      samplerConfigurers: Set[AutoConfigure.Named[F, Sampler[F]]],
      exporterConfigurers: Set[AutoConfigure.Named[F, SpanExporter[F]]]
  ): AutoConfigure[F, TracerProvider[F]] =
    new TracerProviderAutoConfigure[F](
      resource,
      contextPropagators,
      tracerProviderBuilderCustomizer,
      samplerConfigurers,
      exporterConfigurers
    )

}
