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

package org.typelevel.otel4s.oteljava.testkit
package metrics.data

import cats.Hash
import cats.Show
import cats.syntax.show._
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

import scala.jdk.CollectionConverters._

/** A representation of the `io.opentelemetry.sdk.metrics.data.MetricData`.
  */
sealed trait Metric {

  def name: String

  def description: Option[String]

  def unit: Option[String]

  def scope: InstrumentationScope

  def resource: TelemetryResource

  def data: MetricData

  override def hashCode(): Int =
    Hash[Metric].hash(this)

  override def toString: String =
    Show[Metric].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Metric =>
        Hash[Metric].eqv(this, other)
      case _ =>
        false
    }

}

object Metric {

  def apply(
      name: String,
      description: Option[String],
      unit: Option[String],
      scope: InstrumentationScope,
      resource: TelemetryResource,
      data: MetricData
  ): Metric =
    Impl(name, description, unit, scope, resource, data)

  def apply(md: JMetricData): Metric = {

    def collectDataPoints[A <: JPointData, B](data: JData[A], f: A => B) =
      data.getPoints.asScala.toList.map(point => PointData[A, B](point, f))

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
            SummaryPointData(_)
          )
        )

      case MetricDataType.HISTOGRAM =>
        MetricData.Histogram(
          collectDataPoints[JHistogramPointData, HistogramPointData](
            md.getHistogramData,
            HistogramPointData(_)
          )
        )

      case MetricDataType.EXPONENTIAL_HISTOGRAM =>
        MetricData.ExponentialHistogram(
          collectDataPoints[JHistogramPointData, HistogramPointData](
            md.getHistogramData,
            HistogramPointData(_)
          )
        )
    }

    Impl(
      name = md.getName,
      description = Option(md.getDescription),
      unit = Option(md.getUnit),
      scope = InstrumentationScope(md.getInstrumentationScopeInfo),
      resource = TelemetryResource(md.getResource),
      data = data
    )
  }

  implicit val metricHash: Hash[Metric] =
    Hash.by(p => (p.name, p.description, p.unit, p.scope, p.resource, p.data))

  implicit val metricShow: Show[Metric] =
    Show.show(p =>
      show"Metric(${p.name}, ${p.description}, ${p.unit}, ${p.scope}, ${p.resource}, ${p.data})"
    )

  final case class Impl(
      name: String,
      description: Option[String],
      unit: Option[String],
      scope: InstrumentationScope,
      resource: TelemetryResource,
      data: MetricData
  ) extends Metric
}
