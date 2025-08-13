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

package org.typelevel.otel4s.sdk.testkit

import cats.Parallel
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.baggage.SdkBaggageManager
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.sdk.testkit.trace.TracesTestkit
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.TracerProvider

sealed abstract class OpenTelemetrySdkTestkit[F[_]] private (implicit
    val localContext: LocalContext[F]
) extends Otel4s.Unsealed[F]
    with MetricsTestkit.Unsealed[F]
    with TracesTestkit.Unsealed[F] {

  type Ctx = Context

  val baggageManager: BaggageManager[F] = SdkBaggageManager.fromLocal

  override def toString: String =
    s"OpenTelemetrySdkTestkit{meterProvider=$meterProvider, tracerProvider=$tracerProvider, propagators=$propagators}"
}

object OpenTelemetrySdkTestkit {

  type Customizer[A] = A => A

  /** Creates [[OpenTelemetrySdkTestkit]] that keeps spans and metrics in-memory.
    *
    * @param customizeMeterProviderBuilder
    *   the customization of the meter provider builder
    *
    * @param customizeTracerProviderBuilder
    *   the customization of the tracer provider builder
    *
    * @param textMapPropagators
    *   the propagators to use
    *
    * @param aggregationTemporalitySelector
    *   the preferred aggregation for the given instrument type
    *
    * @param defaultAggregationSelector
    *   the preferred aggregation for the given instrument type. If no views are configured for a metric instrument, an
    *   aggregation provided by the selector will be used
    *
    * @param defaultCardinalityLimitSelector
    *   the preferred cardinality limit for the given instrument type. If no views are configured for a metric
    *   instrument, a limit provided by the selector will be used
    */
  def inMemory[F[_]: Async: Parallel: Console: LocalContextProvider](
      customizeMeterProviderBuilder: Customizer[SdkMeterProvider.Builder[F]] = identity[SdkMeterProvider.Builder[F]],
      customizeTracerProviderBuilder: Customizer[SdkTracerProvider.Builder[F]] = identity[SdkTracerProvider.Builder[F]],
      textMapPropagators: Iterable[TextMapPropagator[Context]] = Nil,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ): Resource[F, OpenTelemetrySdkTestkit[F]] =
    Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
      val traceContextLookup: TraceContext.Lookup =
        new TraceContext.Lookup.Unsealed {
          def get(context: Context): Option[TraceContext] =
            context
              .get(SdkContextKeys.SpanContextKey)
              .filter(_.isValid)
              .map { ctx =>
                TraceContext(
                  ctx.traceId,
                  ctx.spanId,
                  ctx.isSampled
                )
              }
        }

      for {
        metrics <- MetricsTestkit.create(
          customizeMeterProviderBuilder.compose[SdkMeterProvider.Builder[F]](
            _.withTraceContextLookup(traceContextLookup)
          ),
          aggregationTemporalitySelector,
          defaultAggregationSelector,
          defaultCardinalityLimitSelector
        )
        traces <- TracesTestkit.inMemory(
          customizeTracerProviderBuilder.compose[SdkTracerProvider.Builder[F]](
            _.addTextMapPropagators(textMapPropagators.toSeq: _*)
          )
        )(
          Async[F],
          Parallel[F],
          Console[F],
          LocalProvider.fromLocal(local)
        )
      } yield new Impl[F](
        metrics,
        traces,
        ContextPropagators.of(textMapPropagators.toSeq: _*)
      )
    }

  private final class Impl[F[_]: LocalContext](
      metrics: MetricsTestkit[F],
      traces: TracesTestkit[F],
      val propagators: ContextPropagators[Context]
  ) extends OpenTelemetrySdkTestkit[F] {
    def meterProvider: MeterProvider[F] = metrics.meterProvider
    def tracerProvider: TracerProvider[F] = traces.tracerProvider
    def finishedSpans: F[List[SpanData]] = traces.finishedSpans
    def collectMetrics: F[List[MetricData]] = metrics.collectMetrics
  }

}
