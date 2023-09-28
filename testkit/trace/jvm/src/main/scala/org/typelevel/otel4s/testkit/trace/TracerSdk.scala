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

package org.typelevel.otel4s
package testkit
package trace

import cats.effect.Sync
import cats.syntax.functor._
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator
import io.opentelemetry.context.propagation.{
  ContextPropagators => JContextPropagators
}
import io.opentelemetry.context.propagation.{
  TextMapPropagator => JTextMapPropagator
}
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder
import io.opentelemetry.sdk.trace.data.SpanData
import io.opentelemetry.sdk.trace.`export`.SimpleSpanProcessor

import scala.jdk.CollectionConverters.*

trait TracerSdk[F[_]] {
  def sdk: OpenTelemetrySdk
  def finishedSpanData: F[List[SpanData]]
  def finishedSpanNodes: F[List[SpanNode]]
}

object TracerSdk {

  def inMemory[F[_]: Sync](
      customize: SdkTracerProviderBuilder => SdkTracerProviderBuilder =
        identity,
      additionalPropagators: Seq[JTextMapPropagator] = Nil
  ): TracerSdk[F] = {
    val exporter = InMemorySpanExporter.create()

    val builder = SdkTracerProvider
      .builder()
      .addSpanProcessor(SimpleSpanProcessor.create(exporter))

    val tracerProvider: SdkTracerProvider =
      customize(builder).build()

    val textMapPropagators =
      (W3CTraceContextPropagator.getInstance() +: additionalPropagators).asJava

    val openTelemetrySdk = OpenTelemetrySdk
      .builder()
      .setTracerProvider(tracerProvider)
      .setPropagators(
        JContextPropagators.create(
          JTextMapPropagator.composite(textMapPropagators)
        )
      )
      .build()

    fromSdk(openTelemetrySdk, exporter)
  }

  private[testkit] def fromSdk[F[_]: Sync](
      openTelemetrySdk: OpenTelemetrySdk,
      exporter: InMemorySpanExporter
  ): TracerSdk[F] =
    new TracerSdk[F] {
      val sdk: OpenTelemetrySdk = openTelemetrySdk

      def finishedSpanData: F[List[SpanData]] =
        Sync[F].delay(exporter.getFinishedSpanItems.asScala.toList)

      def finishedSpanNodes: F[List[SpanNode]] =
        finishedSpanData.map(SpanNode.fromSpans)
    }

}
