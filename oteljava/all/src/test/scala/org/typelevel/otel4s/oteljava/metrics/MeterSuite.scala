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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.IO
import cats.effect.Resource
import cats.mtl.Local
import io.opentelemetry.api.trace.Span
import io.opentelemetry.api.trace.SpanContext
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import io.opentelemetry.context.{Context => JContext}
import io.opentelemetry.sdk.common.Clock
import io.opentelemetry.sdk.metrics.data.AggregationTemporality
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.ExemplarData
import io.opentelemetry.sdk.metrics.data.HistogramPointData
import io.opentelemetry.sdk.metrics.data.LongExemplarData
import io.opentelemetry.sdk.metrics.data.LongPointData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import io.opentelemetry.sdk.metrics.data.PointData
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.BaseMeterSuite
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.Context
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class MeterSuite extends BaseMeterSuite {

  type Ctx = Context

  protected def tracedContext(traceId: String, spanId: String): Context = {
    val spanContext = SpanContext.create(
      traceId,
      spanId,
      TraceFlags.getSampled,
      TraceState.getDefault
    )

    Context.wrap(
      Span.wrap(spanContext).storeInContext(JContext.root())
    )
  }

  protected def makeSdk: Resource[IO, BaseMeterSuite.Sdk[Ctx]] =
    Resource
      .eval(LocalProvider[IO, Context].local)
      .flatMap { implicit localContext =>
        val clock = new Clock {
          def now(): Long = 0L
          def nanoTime(): Long = 0L
        }

        MetricsTestkit
          .create[IO](_.setClock(clock))
          .map { metrics =>
            new BaseMeterSuite.Sdk[Ctx] {
              def provider: MeterProvider[IO] =
                metrics.meterProvider

              def collectMetrics: IO[List[BaseMeterSuite.MetricData]] =
                metrics.collectMetrics[MetricData].map(_.map(toMetricData))

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
        case AggregationTemporality.DELTA =>
          BaseMeterSuite.AggregationTemporality.Delta
        case AggregationTemporality.CUMULATIVE =>
          BaseMeterSuite.AggregationTemporality.Cumulative
      }

    def toExemplar(exemplar: ExemplarData) = {
      val value = exemplar match {
        case long: LongExemplarData     => Left(long.getValue)
        case double: DoubleExemplarData => Right(double.getValue)
        case other                      => sys.error(s"unknown exemplar $other")
      }

      // OtelJava exemplar reservoirs always use Clock.getDefault
      // this makes testing difficult, so we always use 0L

      BaseMeterSuite.Exemplar(
        exemplar.getFilteredAttributes.toScala,
        Duration.Zero, // exemplar.getEpochNanos.nanos,
        Option(exemplar.getSpanContext.getTraceId),
        Option(exemplar.getSpanContext.getSpanId),
        value
      )
    }

    def toNumberPoint(number: PointData) = {
      val value = number match {
        case long: LongPointData     => Left(long.getValue)
        case double: DoublePointData => Right(double.getValue)
        case other                   => sys.error(s"unknown point data $other")
      }

      BaseMeterSuite.PointData.NumberPoint(
        number.getStartEpochNanos.nanos,
        number.getEpochNanos.nanos,
        number.getAttributes.toScala,
        value,
        number.getExemplars.asScala.toVector.map(toExemplar)
      )
    }

    def toHistogramPoint(histogram: HistogramPointData) =
      BaseMeterSuite.PointData.Histogram(
        histogram.getStartEpochNanos.nanos,
        histogram.getEpochNanos.nanos,
        histogram.getAttributes.toScala,
        Option.when(histogram.getCount > 0L)(histogram.getSum),
        Option.when(histogram.hasMin)(histogram.getMin),
        Option.when(histogram.hasMax)(histogram.getMax),
        Option.when(histogram.getCount > 0L)(histogram.getCount),
        BucketBoundaries(
          histogram.getBoundaries.asScala.toVector.map(Double.unbox)
        ),
        histogram.getCounts.asScala.toVector.map(Long.unbox),
        histogram.getExemplars.asScala.toVector.map(toExemplar)
      )

    val data = md.getType match {
      case MetricDataType.LONG_GAUGE =>
        BaseMeterSuite.MetricPoints.Gauge(
          md.getLongGaugeData.getPoints.asScala.toVector.map(toNumberPoint)
        )

      case MetricDataType.DOUBLE_GAUGE =>
        BaseMeterSuite.MetricPoints.Gauge(
          md.getDoubleGaugeData.getPoints.asScala.toVector.map(toNumberPoint)
        )

      case MetricDataType.LONG_SUM =>
        val sum = md.getLongSumData
        BaseMeterSuite.MetricPoints.Sum(
          sum.getPoints.asScala.toVector.map(toNumberPoint),
          sum.isMonotonic,
          temporality(sum.getAggregationTemporality)
        )

      case MetricDataType.DOUBLE_SUM =>
        val sum = md.getDoubleSumData
        BaseMeterSuite.MetricPoints.Sum(
          sum.getPoints.asScala.toVector.map(toNumberPoint),
          sum.isMonotonic,
          temporality(sum.getAggregationTemporality)
        )

      case MetricDataType.HISTOGRAM =>
        val histogram = md.getHistogramData

        BaseMeterSuite.MetricPoints.Histogram(
          histogram.getPoints.asScala.toVector.map(toHistogramPoint),
          temporality(histogram.getAggregationTemporality)
        )

      case other =>
        sys.error(s"unsupported metric data type $other")
    }

    BaseMeterSuite.MetricData(
      md.getName,
      Option(md.getDescription).filter(_.nonEmpty),
      Option(md.getUnit).filter(_.nonEmpty),
      data
    )
  }

}
