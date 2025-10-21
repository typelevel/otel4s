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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.IO
import cats.effect.Resource
import cats.effect.SyncIO
import cats.effect.testkit.TestControl
import cats.mtl.Local
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.BaseMeterSuite
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.testkit.metrics.MetricsTestkit
import scodec.bits.ByteVector

class SdkMeterSuite extends BaseMeterSuite {

  type Ctx = Context

  private val traceContextKey = Context.Key
    .unique[SyncIO, TraceContext]("trace-context")
    .unsafeRunSync()

  protected def tracedContext(traceId: String, spanId: String): Context = {
    val spanContext = TraceContext(
      traceId = ByteVector.fromValidHex(traceId),
      spanId = ByteVector.fromValidHex(spanId),
      sampled = true
    )

    Context.root.updated(traceContextKey, spanContext)
  }

  override protected def transform[A](io: IO[A]): IO[A] =
    TestControl.executeEmbed(io)

  protected def makeSdk: Resource[IO, BaseMeterSuite.Sdk[Ctx]] =
    Resource
      .eval(LocalProvider[IO, Context].local)
      .flatMap { implicit localContext =>
        implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

        MetricsTestkit
          .create[IO](
            _.withTraceContextLookup(_.get(traceContextKey)),
            AggregationTemporalitySelector.alwaysCumulative,
            AggregationSelector.default,
            CardinalityLimitSelector.default
          )
          .map { metrics =>
            new BaseMeterSuite.Sdk[Ctx] {
              def provider: MeterProvider[IO] =
                metrics.meterProvider

              def collectMetrics: IO[List[BaseMeterSuite.MetricData]] =
                metrics.collectMetrics.map(_.map(toMetricData))

              def local: Local[IO, Context] =
                localContext
            }
          }
      }

  private def toMetricData(md: MetricData): BaseMeterSuite.MetricData = {
    def temporality(
        aggregationTemporality: AggregationTemporality
    ): BaseMeterSuite.AggregationTemporality =
      aggregationTemporality match {
        case AggregationTemporality.Delta =>
          BaseMeterSuite.AggregationTemporality.Delta
        case AggregationTemporality.Cumulative =>
          BaseMeterSuite.AggregationTemporality.Cumulative
      }

    def toExemplar(exemplar: ExemplarData) = {
      val value = exemplar match {
        case long: ExemplarData.LongExemplar     => Left(long.value)
        case double: ExemplarData.DoubleExemplar => Right(double.value)
      }

      BaseMeterSuite.Exemplar(
        exemplar.filteredAttributes,
        exemplar.timestamp,
        exemplar.traceContext.map(_.traceId.toHex),
        exemplar.traceContext.map(_.spanId.toHex),
        value
      )
    }

    def toNumberPoint(number: PointData.NumberPoint) = {
      val value = number match {
        case long: PointData.LongNumber     => Left(long.value)
        case double: PointData.DoubleNumber => Right(double.value)
      }

      BaseMeterSuite.PointData.NumberPoint(
        number.timeWindow.start,
        number.timeWindow.end,
        number.attributes,
        value,
        number.exemplars.map(toExemplar)
      )
    }

    def toHistogramPoint(histogram: PointData.Histogram) =
      BaseMeterSuite.PointData.Histogram(
        histogram.timeWindow.start,
        histogram.timeWindow.end,
        histogram.attributes,
        histogram.stats.map(_.sum),
        histogram.stats.map(_.min),
        histogram.stats.map(_.max),
        histogram.stats.map(_.count),
        histogram.boundaries,
        histogram.counts,
        histogram.exemplars.map(toExemplar)
      )

    val data = md.data match {
      case sum: MetricPoints.Sum =>
        BaseMeterSuite.MetricPoints.Sum(
          sum.points.toVector.map(toNumberPoint),
          sum.monotonic,
          temporality(sum.aggregationTemporality)
        )

      case gauge: MetricPoints.Gauge =>
        BaseMeterSuite.MetricPoints.Gauge(
          gauge.points.toVector.map(toNumberPoint)
        )

      case histogram: MetricPoints.Histogram =>
        BaseMeterSuite.MetricPoints.Histogram(
          histogram.points.toVector.map(toHistogramPoint),
          temporality(histogram.aggregationTemporality)
        )
    }

    BaseMeterSuite.MetricData(
      md.name,
      md.description,
      md.unit,
      data
    )
  }

}
