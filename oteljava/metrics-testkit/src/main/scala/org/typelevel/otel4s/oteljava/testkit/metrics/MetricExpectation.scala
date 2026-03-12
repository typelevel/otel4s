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

import io.opentelemetry.sdk.metrics.data.{MetricData, MetricDataType}

import scala.jdk.CollectionConverters._

sealed trait MetricExpectation {

  def withDescription(description: String): MetricExpectation

  def withUnit(unit: String): MetricExpectation

  def withScopeName(name: String): MetricExpectation

  def withPoint(point: PointExpectation[_]): MetricExpectation

  def withAnyPoint(point: PointExpectation[_]): MetricExpectation

  def withAllPoints(point: PointExpectation[_]): MetricExpectation

  def withValue(value: Long): MetricExpectation

  def withValue(value: Double): MetricExpectation

  def where(f: MetricData => Boolean, clue: String): MetricExpectation

  def matches(metric: MetricData): Boolean

}

object MetricExpectation {

  def any: MetricExpectation =
    Impl()

  def name(name: String): MetricExpectation =
    any.where(_.getName == name, s"name == $name")

  def longGauge(name: String): MetricExpectation =
    byKind(name, MetricKind.LongGauge)

  def doubleGauge(name: String): MetricExpectation =
    byKind(name, MetricKind.DoubleGauge)

  def longSum(name: String): MetricExpectation =
    byKind(name, MetricKind.LongSum)

  def doubleSum(name: String): MetricExpectation =
    byKind(name, MetricKind.DoubleSum)

  def summary(name: String): MetricExpectation =
    byKind(name, MetricKind.Summary)

  def histogram(name: String): MetricExpectation =
    byKind(name, MetricKind.Histogram)

  def exponentialHistogram(name: String): MetricExpectation =
    byKind(name, MetricKind.ExponentialHistogram)

  private def byKind(name: String, kind: MetricKind): MetricExpectation =
    Impl(
      name = Some(name),
      kind = Some(kind)
    )

  private sealed trait PointMatch {
    def matches(metric: MetricData): Boolean
  }

  private object PointMatch {
    case object Ignore extends PointMatch {
      def matches(metric: MetricData): Boolean = true
    }

    final case class Any(expectation: PointExpectation[_]) extends PointMatch {
      def matches(metric: MetricData): Boolean =
        points(metric).exists(expectation.matches)
    }

    final case class All(expectation: PointExpectation[_]) extends PointMatch {
      def matches(metric: MetricData): Boolean = {
        val values = points(metric)
        values.nonEmpty && values.forall(expectation.matches)
      }
    }

    private def points(
        metric: MetricData
    ): List[io.opentelemetry.sdk.metrics.data.PointData] =
      metric.getType match {
        case MetricDataType.LONG_GAUGE =>
          metric.getLongGaugeData.getPoints.asScala.toList
        case MetricDataType.DOUBLE_GAUGE =>
          metric.getDoubleGaugeData.getPoints.asScala.toList
        case MetricDataType.LONG_SUM =>
          metric.getLongSumData.getPoints.asScala.toList
        case MetricDataType.DOUBLE_SUM =>
          metric.getDoubleSumData.getPoints.asScala.toList
        case MetricDataType.SUMMARY =>
          metric.getSummaryData.getPoints.asScala.toList
        case MetricDataType.HISTOGRAM =>
          metric.getHistogramData.getPoints.asScala.toList
        case MetricDataType.EXPONENTIAL_HISTOGRAM =>
          metric.getExponentialHistogramData.getPoints.asScala.toList
      }
  }

  private sealed trait MetricKind {
    def matches(data: MetricData): Boolean
  }

  private object MetricKind {
    case object LongGauge extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.LONG_GAUGE
    }
    case object DoubleGauge extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.DOUBLE_GAUGE
    }
    case object LongSum extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.LONG_SUM
    }
    case object DoubleSum extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.DOUBLE_SUM
    }
    case object Summary extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.SUMMARY
    }
    case object Histogram extends MetricKind {
      def matches(data: MetricData): Boolean = data.getType == MetricDataType.HISTOGRAM
    }
    case object ExponentialHistogram extends MetricKind {
      def matches(data: MetricData): Boolean =
        data.getType == MetricDataType.EXPONENTIAL_HISTOGRAM
    }
  }

  private final case class Impl(
      name: Option[String] = None,
      kind: Option[MetricKind] = None,
      description: Option[String] = None,
      unit: Option[String] = None,
      scopeName: Option[String] = None,
      pointMatch: PointMatch = PointMatch.Ignore,
      predicates: List[(MetricData => Boolean, String)] = Nil
  ) extends MetricExpectation {

    def withDescription(description: String): MetricExpectation =
      copy(description = Some(description))

    def withUnit(unit: String): MetricExpectation =
      copy(unit = Some(unit))

    def withScopeName(name: String): MetricExpectation =
      copy(scopeName = Some(name))

    def withPoint(point: PointExpectation[_]): MetricExpectation =
      withAnyPoint(point)

    def withAnyPoint(point: PointExpectation[_]): MetricExpectation =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation[_]): MetricExpectation =
      copy(pointMatch = PointMatch.All(point))

    def withValue(value: Long): MetricExpectation =
      withAnyPoint(PointExpectation.long(value))

    def withValue(value: Double): MetricExpectation =
      withAnyPoint(PointExpectation.double(value))

    def where(f: MetricData => Boolean, clue: String): MetricExpectation =
      copy(predicates = predicates :+ (f -> clue))

    def matches(metric: MetricData): Boolean =
      name.forall(_ == metric.getName) &&
        kind.forall(_.matches(metric)) &&
        description.forall(Option(metric.getDescription).contains) &&
        unit.forall(Option(metric.getUnit).contains) &&
        scopeName.forall(_ == metric.getInstrumentationScopeInfo.getName) &&
        pointMatch.matches(metric) &&
        predicates.forall { case (predicate, _) => predicate(metric) }
  }
}
