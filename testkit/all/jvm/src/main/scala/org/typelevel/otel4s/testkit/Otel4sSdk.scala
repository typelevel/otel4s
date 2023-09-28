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

package org.typelevel.otel4s.testkit

import cats.effect.Sync
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor
import org.typelevel.otel4s.testkit.metrics.Metric
import org.typelevel.otel4s.testkit.metrics.MetricsSdk
import org.typelevel.otel4s.testkit.trace.SpanNode
import org.typelevel.otel4s.testkit.trace.TracerSdk

import scala.jdk.CollectionConverters._

trait Otel4sSdk[F[_]] extends MetricsSdk[F] with TracerSdk[F]

object Otel4sSdk {

  def inMemory[F[_]: Sync](
      customizeMeter: SdkMeterProviderBuilder => SdkMeterProviderBuilder =
        identity,
      customizeTracer: SdkTracerProviderBuilder => SdkTracerProviderBuilder =
        identity,
      additionalPropagators: Seq[JTextMapPropagator] = Nil
  ): Otel4sSdk[F] = {
    val metricReader = InMemoryMetricReader.create()
    val spanExporter = InMemorySpanExporter.create()

    val meterProviderBuilder = SdkMeterProvider
      .builder()
      .registerMetricReader(metricReader)

    val tracerProviderBuilder = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(spanExporter))

    val meterProvider = customizeMeter(meterProviderBuilder).build()
    val tracerProvider = customizeTracer(tracerProviderBuilder).build()
    val textMapPropagators =
      (W3CTraceContextPropagator.getInstance() +: additionalPropagators).asJava

    val openTelemetrySdk = OpenTelemetrySdk
      .builder()
      .setMeterProvider(meterProvider)
      .setTracerProvider(tracerProvider)
      .setPropagators(
        JContextPropagators.create(
          JTextMapPropagator.composite(textMapPropagators)
        )
      )
      .build()

    val metricsSdk = MetricsSdk.fromSdk(openTelemetrySdk, metricReader)
    val tracerSdk = TracerSdk.fromSdk(openTelemetrySdk, spanExporter)

    new Otel4sSdk[F] {
      def sdk: OpenTelemetrySdk = openTelemetrySdk
      def metrics: F[List[Metric]] = metricsSdk.metrics
      def finishedSpanData: F[List[SpanData]] = tracerSdk.finishedSpanData
      def finishedSpanNodes: F[List[SpanNode]] = tracerSdk.finishedSpanNodes
    }
  }
}
