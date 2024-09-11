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

package org.typelevel.otel4s.oteljava.testkit.metrics

import cats.effect.Async
import cats.effect.Resource
import cats.mtl.Ask
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.context.AskContext
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.metrics.MeterProviderImpl

import scala.jdk.CollectionConverters._

trait MetricsTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]

  /** Collects and returns metrics.
    *
    * @example
    *   {{{
    * import io.opentelemetry.sdk.metrics.data.MetricData
    * import org.typelevel.otel4s.oteljava.testkit.metrics.data.Metric
    *
    * MetricTestkit[F].collectMetrics[MetricData] // OpenTelemetry Java models
    * MetricTestkit[F].collectMetrics[Metric] // Otel4s testkit models
    *   }}}
    *
    * @note
    *   metrics are recollected on each invocation.
    */
  def collectMetrics[A: FromMetricData]: F[List[A]]

}

object MetricsTestkit {

  /** Creates [[MetricsTestkit]] that keeps metrics in-memory.
    *
    * @note
    *   the implementation does not record exemplars. Use `OtelJavaTestkit` if you need to record exemplars.
    *
    * @param customize
    *   the customization of the builder
    */
  def inMemory[F[_]: Async](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): Resource[F, MetricsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)
    create[F](customize)
  }

  private[oteljava] def create[F[_]: Async: AskContext](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder
  ): Resource[F, MetricsTestkit[F]] = {

    def createMetricReader =
      Async[F].delay(InMemoryMetricReader.create())

    def createMetricProvider(metricReader: InMemoryMetricReader) =
      Async[F].delay {
        val builder = SdkMeterProvider
          .builder()
          .registerMetricReader(metricReader)

        customize(builder).build()
      }

    for {
      metricReader <- Resource.fromAutoCloseable(createMetricReader)
      provider <- Resource.fromAutoCloseable(createMetricProvider(metricReader))
    } yield new Impl(
      new MeterProviderImpl(provider),
      metricReader
    )
  }

  private final class Impl[F[_]: Async](
      val meterProvider: MeterProvider[F],
      metricReader: InMemoryMetricReader
  ) extends MetricsTestkit[F] {

    def collectMetrics[A: FromMetricData]: F[List[A]] =
      Async[F].delay {
        val metrics = metricReader.collectAllMetrics().asScala.toList
        metrics.map(FromMetricData[A].from)
      }
  }

}
