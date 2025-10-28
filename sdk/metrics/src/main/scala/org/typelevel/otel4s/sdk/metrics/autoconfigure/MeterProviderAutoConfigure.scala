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

import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.autoconfigure.MeterProviderAutoConfigure.Customizer
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

/** Autoconfigures [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
  *
  * @see
  *   [[MetricReadersAutoConfigure]]
  *
  * @see
  *   [[MetricExportersAutoConfigure]]
  *
  * @see
  *   [[ExemplarFilterAutoConfigure]]
  *
  * @param resource
  *   the resource to use
  *
  * @param traceContextLookup
  *   used by the exemplar reservoir to extract tracing information from the context
  *
  * @param customizer
  *   the function to customize the builder
  *
  * @param exporterConfigurers
  *   the extra exporter configurers
  */
private final class MeterProviderAutoConfigure[
    F[_]: Temporal: Random: Console: Diagnostic: AskContext
](
    resource: TelemetryResource,
    traceContextLookup: TraceContext.Lookup,
    customizer: Customizer[SdkMeterProvider.Builder[F]],
    exporterConfigurers: Set[AutoConfigure.Named[F, MetricExporter[F]]]
) extends AutoConfigure.WithHint[F, MeterProvider[F]](
      "MeterProvider",
      Set.empty
    ) {

  protected def fromConfig(config: Config): Resource[F, MeterProvider[F]] = {
    val exemplarFilterAutoConfigure =
      ExemplarFilterAutoConfigure[F](traceContextLookup)

    val exportersAutoConfigure =
      MetricExportersAutoConfigure[F](exporterConfigurers)

    def readersAutoConfigure(exporters: Set[MetricExporter[F]]) =
      MetricReadersAutoConfigure[F](exporters)

    for {
      exemplarFilter <- exemplarFilterAutoConfigure.configure(config)
      exporters <- exportersAutoConfigure.configure(config)
      readers <- readersAutoConfigure(exporters.values.toSet).configure(config)

      meterProviderBuilder = {
        val builder = SdkMeterProvider
          .builder[F]
          .withResource(resource)
          .withExemplarFilter(exemplarFilter)
          .withTraceContextLookup(traceContextLookup)

        readers.foldLeft(builder)(_.registerMetricReader(_))
      }

      provider <- Resource.eval(customizer(meterProviderBuilder, config).build)
    } yield provider
  }

}

private[sdk] object MeterProviderAutoConfigure {

  type Customizer[A] = (A, Config) => A

  /** Autoconfigures [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    *
    * @see
    *   [[MetricReadersAutoConfigure]]
    *
    * @see
    *   [[MetricExportersAutoConfigure]]
    *
    * @see
    *   [[ExemplarFilterAutoConfigure]]
    *
    * @param resource
    *   the resource to use
    *
    * @param traceContextLookup
    *   used by the exemplar reservoir to extract tracing information from the context
    *
    * @param meterProviderBuilderCustomizer
    *   the function to customize the builder
    *
    * @param exporterConfigurers
    *   the extra exporter configurers
    */
  def apply[F[_]: Temporal: Random: Console: Diagnostic: AskContext](
      resource: TelemetryResource,
      traceContextLookup: TraceContext.Lookup,
      meterProviderBuilderCustomizer: Customizer[SdkMeterProvider.Builder[F]],
      exporterConfigurers: Set[AutoConfigure.Named[F, MetricExporter[F]]]
  ): AutoConfigure[F, MeterProvider[F]] =
    new MeterProviderAutoConfigure[F](
      resource,
      traceContextLookup,
      meterProviderBuilderCustomizer,
      exporterConfigurers
    )

}
