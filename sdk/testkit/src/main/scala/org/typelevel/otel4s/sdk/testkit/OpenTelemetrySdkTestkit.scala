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
import org.typelevel.otel4s.Otel4s
import org.typelevel.otel4s.baggage.BaggageManager
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.context.propagation.ContextPropagators
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.baggage.SdkBaggageManager
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContext
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.logs.SdkLoggerProvider
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.testkit.logs.LogsTestkit
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
    with TracesTestkit.Unsealed[F]
    with LogsTestkit.Unsealed[F] {

  type Ctx = Context

  val baggageManager: BaggageManager[F] = SdkBaggageManager.fromLocal

  override def toString: String =
    "OpenTelemetrySdkTestkit{" +
      s"meterProvider=$meterProvider, " +
      s"tracerProvider=$tracerProvider, " +
      s"loggerProvider=$loggerProvider, " +
      s"propagators=$propagators}"
}

object OpenTelemetrySdkTestkit {

  type Customizer[A] = A => A

  /** Builder for [[OpenTelemetrySdkTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the meter provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addMeterProviderCustomizer(customizer: Customizer[SdkMeterProvider.Builder[F]]): Builder[F]

    /** Adds the tracer provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addTracerProviderCustomizer(customizer: Customizer[SdkTracerProvider.Builder[F]]): Builder[F]

    /** Adds the logger provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addLoggerProviderCustomizer(customizer: Customizer[SdkLoggerProvider.Builder[F]]): Builder[F]

    /** Adds propagators to register on the tracer provider.
      *
      * @param propagators
      *   the propagators to add
      */
    def addTextMapPropagators(propagators: TextMapPropagator[Context]*): Builder[F]

    /** Sets the propagators used by the tracer provider. Any previously added propagators are discarded.
      *
      * @param propagators
      *   the propagators to use
      */
    def withTextMapPropagators(propagators: Iterable[TextMapPropagator[Context]]): Builder[F]

    /** Sets the aggregation temporality selector.
      *
      * @param selector
      *   the selector to use
      */
    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F]

    /** Sets the default aggregation selector. If no views are configured for a metric instrument, an
     *   aggregation provided by the selector will be used
      *
      * @param selector
      *   the selector to use
      */
    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F]

    /** Sets the preferred aggregation for the given instrument type. If no views are configured for a metric instrument, an
     *   aggregation provided by this selector will be used.
      */
    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F]

    /** Creates [[OpenTelemetrySdkTestkit]] using the configuration of this builder. */
    def build: Resource[F, OpenTelemetrySdkTestkit[F]]

  }

  /** Creates a [[Builder]] of [[OpenTelemetrySdkTestkit]] with the default configuration. */
  def builder[F[_]: Async: Parallel: Diagnostic: LocalContextProvider]: Builder[F] =
    BuilderImpl[F]()

  /** Creates an [[OpenTelemetrySdkTestkit]] using [[Builder]]. The instance keeps metrics, logs, and spans in memory.
   *
   * @param customize
   *   a function for customizing the builder
   */
  def inMemory[F[_]: Async: Parallel: Diagnostic: LocalContextProvider](
                                                   customize: Builder[F] => Builder[F] = identity(_)
                                                 ): Resource[F, MetricsTestkit[F]] =
    customize(builder[F]).build

  /** Creates [[OpenTelemetrySdkTestkit]] that keeps spans and metrics in-memory.
    *
    * @param customizeMeterProviderBuilder
    *   the customization of the meter provider builder
    *
    * @param customizeTracerProviderBuilder
    *   the customization of the tracer provider builder
    *
    * @param customizeLoggerProviderBuilder
    *   the customization of the logger provider builder
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
  @deprecated(
    "Use an overloaded alternative of the `OpenTelemetrySdkTestkit.inMemory` or `OpenTelemetrySdkTestkit.builder`",
    "0.15.0"
  )
  def inMemory[F[_]: Async: Parallel: Diagnostic: LocalContextProvider](
      customizeMeterProviderBuilder: Customizer[SdkMeterProvider.Builder[F]] = identity[SdkMeterProvider.Builder[F]](_),
      customizeTracerProviderBuilder: Customizer[SdkTracerProvider.Builder[F]] =
        identity[SdkTracerProvider.Builder[F]](_),
      customizeLoggerProviderBuilder: Customizer[SdkLoggerProvider.Builder[F]] =
        identity[SdkLoggerProvider.Builder[F]](_),
      textMapPropagators: Iterable[TextMapPropagator[Context]] = Nil,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ): Resource[F, OpenTelemetrySdkTestkit[F]] =
    builder[F]
      .addMeterProviderCustomizer(customizeMeterProviderBuilder)
      .addTracerProviderCustomizer(customizeTracerProviderBuilder)
      .addLoggerProviderCustomizer(customizeLoggerProviderBuilder)
      .withTextMapPropagators(textMapPropagators)
      .withAggregationTemporalitySelector(aggregationTemporalitySelector)
      .withDefaultAggregationSelector(defaultAggregationSelector)
      .withDefaultCardinalityLimitSelector(defaultCardinalityLimitSelector)
      .build

  private final class Impl[F[_]: LocalContext](
      metrics: MetricsTestkit[F],
      traces: TracesTestkit[F],
      logs: LogsTestkit[F],
      val propagators: ContextPropagators[Context]
  ) extends OpenTelemetrySdkTestkit[F] {
    def meterProvider: MeterProvider[F] = metrics.meterProvider
    def tracerProvider: TracerProvider[F] = traces.tracerProvider
    def loggerProvider: LoggerProvider[F, Context] = logs.loggerProvider
    def finishedSpans: F[List[SpanData]] = traces.finishedSpans
    def collectMetrics: F[List[MetricData]] = metrics.collectMetrics
    def collectLogs: F[List[LogRecordData]] = logs.collectLogs
  }

  private final case class BuilderImpl[F[_]: Async: Parallel: Diagnostic: LocalContextProvider](
      meterCustomizer: Customizer[SdkMeterProvider.Builder[F]] = identity[SdkMeterProvider.Builder[F]],
      tracerCustomizer: Customizer[SdkTracerProvider.Builder[F]] = identity[SdkTracerProvider.Builder[F]],
      loggerCustomizer: Customizer[SdkLoggerProvider.Builder[F]] = identity[SdkLoggerProvider.Builder[F]],
      textMapPropagators: Vector[TextMapPropagator[Context]] = Vector.empty,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ) extends Builder[F] {

    def addMeterProviderCustomizer(customizer: Customizer[SdkMeterProvider.Builder[F]]): Builder[F] =
      copy(meterCustomizer = meterCustomizer.andThen(customizer))

    def addTracerProviderCustomizer(customizer: Customizer[SdkTracerProvider.Builder[F]]): Builder[F] =
      copy(tracerCustomizer = tracerCustomizer.andThen(customizer))

    def addLoggerProviderCustomizer(customizer: Customizer[SdkLoggerProvider.Builder[F]]): Builder[F] =
      copy(loggerCustomizer = loggerCustomizer.andThen(customizer))

    def addTextMapPropagators(propagators: TextMapPropagator[Context]*): Builder[F] =
      copy(textMapPropagators = textMapPropagators ++ propagators)

    def withTextMapPropagators(propagators: Iterable[TextMapPropagator[Context]]): Builder[F] =
      copy(textMapPropagators = propagators.toVector)

    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F] =
      copy(aggregationTemporalitySelector = selector)

    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F] =
      copy(defaultAggregationSelector = selector)

    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F] =
      copy(defaultCardinalityLimitSelector = selector)

    def build: Resource[F, OpenTelemetrySdkTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        val constLocal: LocalContextProvider[F] = LocalProvider.fromLocal(local)

        val traceContextLookup: TraceContext.Lookup =
          new TraceContext.Lookup {
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

        val metricsBuilder = MetricsTestkit
          .builder[F](implicitly, implicitly, constLocal)
          .addMeterProviderCustomizer(
            meterCustomizer.compose[SdkMeterProvider.Builder[F]](
              _.withTraceContextLookup(traceContextLookup)
            )
          )
          .withAggregationTemporalitySelector(aggregationTemporalitySelector)
          .withDefaultAggregationSelector(defaultAggregationSelector)
          .withDefaultCardinalityLimitSelector(defaultCardinalityLimitSelector)

        val tracesBuilder = TracesTestkit
          .builder[F](Async[F], Parallel[F], Diagnostic[F], constLocal)
          .addTracerProviderCustomizer(
            tracerCustomizer.compose[SdkTracerProvider.Builder[F]](
              _.addTextMapPropagators(textMapPropagators: _*)
            )
          )

        val logsBuilder = LogsTestkit
          .builder[F](Async[F], Parallel[F], Diagnostic[F], constLocal)
          .addLoggerProviderCustomizer(
            loggerCustomizer.compose[SdkLoggerProvider.Builder[F]](
              _.withTraceContextLookup(traceContextLookup)
            )
          )

        for {
          metrics <- metricsBuilder.build
          traces <- tracesBuilder.build
          logs <- logsBuilder.build
        } yield new Impl[F](
          metrics,
          traces,
          logs,
          ContextPropagators.of(textMapPropagators: _*)
        )
      }
  }

}
