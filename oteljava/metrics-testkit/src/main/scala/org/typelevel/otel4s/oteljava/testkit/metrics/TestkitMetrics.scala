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
import cats.syntax.functor._
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.metrics.MeterProviderImpl
import org.typelevel.otel4s.oteljava.testkit.metrics.data.Metric

import scala.jdk.CollectionConverters._

trait TestkitMetrics[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]

  /** The collected raw metrics (OpenTelemetry Java models).
    *
    * @note
    *   metrics are recollected on each invocation.
    */
  def rawMetrics: F[List[MetricData]]

  /** The collected metrics converted into Scala models.
    *
    * @note
    *   metrics are recollected on each invocation.
    */
  def metrics: F[List[Metric]]

}

object TestkitMetrics {

  /** Creates [[TestkitMetrics]] that keeps metrics in-memory.
    *
    * @param customize
    *   the customization of the builder
    */
  def inMemory[F[_]: Async](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): Resource[F, TestkitMetrics[F]] = {

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
  ) extends TestkitMetrics[F] {

    def rawMetrics: F[List[MetricData]] =
      Async[F].delay {
        metricReader.collectAllMetrics().asScala.toList
      }

    def metrics: F[List[Metric]] =
      rawMetrics.map(_.map(Metric(_)))
  }

}
