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
package metrics

import cats.effect.kernel.Sync
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics.SdkMeterProvider
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder
import io.opentelemetry.sdk.metrics.data.{Data => JData}
import io.opentelemetry.sdk.metrics.data.{
  HistogramPointData => JHistogramPointData
}
import io.opentelemetry.sdk.metrics.data.{MetricData => JMetricData}
import io.opentelemetry.sdk.metrics.data.{PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader

import scala.jdk.CollectionConverters._

trait MetricsSdk[F[_]] {
  def sdk: OpenTelemetrySdk
  def metrics: F[List[Metric]]
}

object MetricsSdk {

  def inMemory[F[_]: Sync](
      customize: SdkMeterProviderBuilder => SdkMeterProviderBuilder = identity
  ): MetricsSdk[F] = {
    val metricReader = InMemoryMetricReader.create()

    val meterProviderBuilder = SdkMeterProvider
      .builder()
      .registerMetricReader(metricReader)

    val meterProvider = customize(meterProviderBuilder).build()

    val openTelemetrySdk = OpenTelemetrySdk
      .builder()
      .setMeterProvider(meterProvider)
      .build()

    fromSdk(openTelemetrySdk, metricReader)
  }

  private[testkit] def fromSdk[F[_]: Sync](
      openTelemetrySdk: OpenTelemetrySdk,
      metricReader: InMemoryMetricReader
  ): MetricsSdk[F] =
    new MetricsSdk[F] {
      val sdk: OpenTelemetrySdk = openTelemetrySdk

      def metrics: F[List[Metric]] =
        Sync[F].delay {
          metricReader.collectAllMetrics().asScala.toList.map(makeMetric)
        }
    }

  private def makeMetric(md: JMetricData): Metric = {

    def summaryPoint(data: JSummaryPointData): SummaryPointData =
      new SummaryPointData(
        sum = data.getSum,
        count = data.getCount,
        values = data.getValues.asScala.toList.map(v =>
          new QuantileData(v.getQuantile, v.getValue)
        )
      )

    def histogramPoint(data: JHistogramPointData): HistogramPointData =
      new HistogramPointData(
        sum = data.getSum,
        count = data.getCount,
        boundaries = data.getBoundaries.asScala.toList.map(_.doubleValue()),
        counts = data.getCounts.asScala.toList.map(_.longValue())
      )

    def pointData[A <: JPointData, B](point: A, f: A => B) =
      new PointData(
        point.getStartEpochNanos,
        point.getEpochNanos,
        TestkitConversion.fromJAttributes(point.getAttributes),
        f(point)
      )

    def collectDataPoints[A <: JPointData, B](data: JData[A], f: A => B) =
      data.getPoints.asScala.toList
        .map(point => pointData[A, B](point, f))

    val data = md.getType match {
      case MetricDataType.LONG_GAUGE =>
        MetricData.LongGauge(
          collectDataPoints[LongPointData, Long](
            md.getLongGaugeData,
            _.getValue
          )
        )

      case MetricDataType.DOUBLE_GAUGE =>
        MetricData.DoubleGauge(
          collectDataPoints[DoublePointData, Double](
            md.getDoubleGaugeData,
            _.getValue
          )
        )

      case MetricDataType.LONG_SUM =>
        MetricData.LongSum(
          collectDataPoints[LongPointData, Long](
            md.getLongSumData,
            _.getValue
          )
        )

      case MetricDataType.DOUBLE_SUM =>
        MetricData.DoubleSum(
          collectDataPoints[DoublePointData, Double](
            md.getDoubleSumData,
            _.getValue
          )
        )

      case MetricDataType.SUMMARY =>
        MetricData.Summary(
          collectDataPoints[JSummaryPointData, SummaryPointData](
            md.getSummaryData,
            summaryPoint
          )
        )

      case MetricDataType.HISTOGRAM =>
        MetricData.Histogram(
          collectDataPoints[JHistogramPointData, HistogramPointData](
            md.getHistogramData,
            histogramPoint
          )
        )

      case MetricDataType.EXPONENTIAL_HISTOGRAM =>
        MetricData.ExponentialHistogram(
          collectDataPoints[JHistogramPointData, HistogramPointData](
            md.getHistogramData,
            histogramPoint
          )
        )
    }

    val scope = md.getInstrumentationScopeInfo
    val resource = md.getResource

    new Metric(
      name = md.getName,
      description = Option(md.getDescription),
      unit = Option(md.getUnit),
      scope = new InstrumentationScope(
        name = scope.getName,
        version = Option(scope.getVersion),
        schemaUrl = Option(scope.getSchemaUrl)
      ),
      resource = new InstrumentationResource(
        schemaUrl = Option(resource.getSchemaUrl),
        attributes = TestkitConversion.fromJAttributes(resource.getAttributes)
      ),
      data = data
    )
  }

}
