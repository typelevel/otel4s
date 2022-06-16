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
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.data.{
  HistogramPointData,
  MetricDataType,
  SummaryPointData,
  MetricData => JMetricData
}
import io.opentelemetry.sdk.metrics.{SdkMeterProvider, SdkMeterProviderBuilder}
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader

import scala.jdk.CollectionConverters._

trait Sdk[F[_]] {
  def sdk: OpenTelemetrySdk
  def metrics: F[List[Metric]]
}

object Sdk {

  def create[F[_]: Sync](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): Sdk[F] = {
    val metricReader = InMemoryMetricReader.create()

    val meterProviderBuilder = SdkMeterProvider
      .builder()
      .registerMetricReader(metricReader)

    val meterProvider = customize(meterProviderBuilder).build()

    val openTelemetrySdk = OpenTelemetrySdk
      .builder()
      .setMeterProvider(meterProvider)
      .build()

    new Sdk[F] {
      val sdk: OpenTelemetrySdk = openTelemetrySdk

      def metrics: F[List[Metric]] =
        Sync[F].delay {
          metricReader.collectAllMetrics().asScala.toList.map(makeMetric)
        }
    }
  }

  private def makeMetric(md: JMetricData): Metric = {
    def summaryPoint(data: SummaryPointData): MetricData.SummaryPoint =
      MetricData.SummaryPoint(
        sum = data.getSum,
        count = data.getCount,
        values = data.getValues.asScala.toList.map(v =>
          MetricData.QuantileData(v.getQuantile, v.getValue)
        )
      )

    def histogramPoint(data: HistogramPointData): MetricData.HistogramPoint =
      MetricData.HistogramPoint(
        sum = data.getSum,
        count = data.getCount,
        boundaries = data.getBoundaries.asScala.toList.map(Double.unbox),
        counts = data.getCounts.asScala.toList.map(Long.unbox)
      )

    val data = md.getType match {
      case MetricDataType.LONG_GAUGE =>
        MetricData.LongGauge(
          md.getLongGaugeData.getPoints.asScala.toList.map(_.getValue)
        )

      case MetricDataType.DOUBLE_GAUGE =>
        MetricData.DoubleGauge(
          md.getDoubleGaugeData.getPoints.asScala.toList.map(_.getValue)
        )

      case MetricDataType.LONG_SUM =>
        MetricData.LongSum(
          md.getLongSumData.getPoints.asScala.toList.map(_.getValue)
        )

      case MetricDataType.DOUBLE_SUM =>
        MetricData.DoubleSum(
          md.getDoubleSumData.getPoints.asScala.toList.map(_.getValue)
        )

      case MetricDataType.SUMMARY =>
        MetricData.Summary(
          md.getSummaryData.getPoints.asScala.toList.map(summaryPoint)
        )

      case MetricDataType.HISTOGRAM =>
        MetricData.Histogram(
          md.getHistogramData.getPoints.asScala.toList.map(histogramPoint)
        )

      case MetricDataType.EXPONENTIAL_HISTOGRAM =>
        MetricData.ExponentialHistogram(
          md.getHistogramData.getPoints.asScala.toList.map(histogramPoint)
        )
    }

    Metric(md.getName, Option(md.getDescription), Option(md.getUnit), data)
  }

}
