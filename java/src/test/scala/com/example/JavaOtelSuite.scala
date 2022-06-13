package com.example

import cats.effect.{IO, Sync}
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.metrics._
import io.opentelemetry.sdk.metrics.data.{
  HistogramPointData,
  MetricDataType,
  SummaryPointData,
  MetricData => JMetricData
}
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader
import munit.CatsEffectSuite
import org.typelevel.otel4s.java.OtelJava
import org.typelevel.otel4s.{Attribute, AttributeKey, Otel4s}

import scala.jdk.CollectionConverters._

class JavaOtelSuite extends CatsEffectSuite {
  import JavaOtelSuite._

  test("Counter record a proper data") {
    val expected = Metric(
      "counter",
      Some("description"),
      Some("unit"),
      MetricData.LongSum(List(1))
    )

    for {
      sdk <- IO.delay(makeSdk)
      meter <- sdk.otel4s.meterProvider.get("java.otel.suite")

      counter <- meter
        .counter("counter")
        .withUnit("unit")
        .withDescription("description")
        .create

      _ <- counter.add(
        1L,
        Attribute(AttributeKey.string("string-attribute"), "value")
      )

      metrics <- sdk.metrics
    } yield assertEquals(metrics, List(expected))
  }

  private def makeSdk: OtelSdk[IO] = {
    val metricReader = InMemoryMetricReader.create()

    val meterProvider = SdkMeterProvider
      .builder()
      .registerMetricReader(metricReader)
      .registerView(
        InstrumentSelector.builder().setType(InstrumentType.HISTOGRAM).build(),
        View
          .builder()
          .setAggregation(
            Aggregation
              .explicitBucketHistogram(HistogramBuckets.map(Double.box).asJava)
          )
          .build()
      )
      .build()

    val sdk = OpenTelemetrySdk
      .builder()
      .setMeterProvider(meterProvider)
      .build()

    new OtelSdk[IO](sdk, metricReader)
  }

  private val HistogramBuckets: List[Double] =
    List(0.01, 1, 100)

}

object JavaOtelSuite {

  private class OtelSdk[F[_]: Sync](
      val sdk: OpenTelemetrySdk,
      val metricReader: InMemoryMetricReader
  ) {

    val otel4s: Otel4s[F] = OtelJava.forSync[F](sdk)

    def metrics: F[List[Metric]] =
      Sync[F].delay {
        metricReader
          .collectAllMetrics()
          .asScala
          .toList
          .map(md => makeMetric(md))
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

  final case class Metric(
      name: String,
      description: Option[String],
      unit: Option[String],
      data: MetricData
  )

  sealed trait MetricData extends Product with Serializable

  object MetricData {
    final case class QuantileData(quantile: Double, value: Double)

    final case class SummaryPoint(
        sum: Double,
        count: Long,
        values: List[QuantileData]
    )

    final case class HistogramPoint(
        sum: Double,
        count: Long,
        boundaries: List[Double],
        counts: List[Long]
    )

    final case class LongGauge(points: List[Long]) extends MetricData
    final case class DoubleGauge(points: List[Double]) extends MetricData
    final case class LongSum(points: List[Long]) extends MetricData
    final case class DoubleSum(points: List[Double]) extends MetricData
    final case class Summary(points: List[SummaryPoint]) extends MetricData
    final case class Histogram(point: List[HistogramPoint]) extends MetricData
    final case class ExponentialHistogram(points: List[HistogramPoint])
        extends MetricData
  }

}
