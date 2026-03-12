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

import io.opentelemetry.sdk.metrics.data.{ExponentialHistogramPointData, MetricData, MetricDataType}
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.MeasurementValue.{DoubleMeasurementValue, LongMeasurementValue}

import scala.jdk.CollectionConverters._

/** A partial expectation for a single OpenTelemetry Java [[MetricData]].
  *
  * `MetricExpectation` is intended for tests where asserting against the full `MetricData` shape would be too verbose.
  * An expectation matches a metric if all configured predicates succeed. Unspecified properties are ignored.
  *
  * Use the builders in [[MetricExpectation]] to create expectations for the metric kind you want to check:
  *   - [[MetricExpectation.name]] to require only a metric name
  *   - [[MetricExpectation.gauge]] for `LONG_GAUGE` and `DOUBLE_GAUGE`
  *   - [[MetricExpectation.sum]] for `LONG_SUM` and `DOUBLE_SUM`
  *   - [[MetricExpectation.summary]] for summary points
  *   - [[MetricExpectation.histogram]] for histogram points
  *
  * Expectations are matched against collected metrics with [[MetricExpectations.exists]], [[MetricExpectations.find]],
  * or [[MetricExpectations.missing]].
  */
sealed trait MetricExpectation {
  type Value

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the metric description to match exactly. */
  def withDescription(description: String): MetricExpectation

  /** Requires the metric unit to match exactly. */
  def withUnit(unit: String): MetricExpectation

  /** Requires the instrumentation scope name to match exactly. */
  def withScopeName(name: String): MetricExpectation

  /** Attaches a human-readable clue to this expectation. */
  def withClue(text: String): MetricExpectation

  /** Returns `true` if this expectation matches the given metric. */
  def matches(metric: MetricData): Boolean
}

object MetricExpectation {

  /** A typed expectation for numeric metrics.
    *
    * The value type is driven by [[MeasurementValue]]. For example:
    *
    * {{{
    * MetricExpectation.gauge[Long]("queue.size").withValue(10L)
    * MetricExpectation.sum[Double]("request.duration")
    * }}}
    */
  sealed trait Numeric[A] extends MetricExpectation {
    type Value = A

    /** The `MeasurementValue` used to distinguish long and double metrics at runtime. */
    def valueType: MeasurementValue[A]

    /** Requires at least one point with the given value. */
    def withValue(value: A): Numeric[A]

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation[A]): Numeric[A]

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation[A]): Numeric[A]

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation[A]): Numeric[A]
  }

  /** A typed expectation for metrics whose points are not simple numeric values, such as summaries and histograms. */
  sealed trait Points[A] extends MetricExpectation {
    type Value = A

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation[A]): Points[A]

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation[A]): Points[A]

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation[A]): Points[A]
  }

  /** Creates an expectation that matches any metric with the given name. */
  def name(name: String): MetricExpectation =
    BaseImpl[Nothing](name = Some(name))

  /** Creates a typed expectation for a gauge metric.
    *
    * The metric kind is selected from `A`:
    *   - `A = Long` matches `LONG_GAUGE`
    *   - `A = Double` matches `DOUBLE_GAUGE`
    */
  def gauge[A: MeasurementValue](name: String): Numeric[A] =
    NumericImpl(
      name = Some(name),
      kind = NumericKind.Gauge,
      valueType = MeasurementValue[A]
    )

  /** Creates a typed expectation for a sum metric.
    *
    * The metric kind is selected from `A`:
    *   - `A = Long` matches `LONG_SUM`
    *   - `A = Double` matches `DOUBLE_SUM`
    */
  def sum[A: MeasurementValue](name: String): Numeric[A] =
    NumericImpl(
      name = Some(name),
      kind = NumericKind.Sum,
      valueType = MeasurementValue[A]
    )

  /** Creates an expectation for a summary metric. */
  def summary(name: String): Points[JSummaryPointData] =
    PointMetricImpl(
      name = Some(name),
      metricType = MetricDataType.SUMMARY
    )

  /** Creates an expectation for a histogram metric. */
  def histogram(name: String): Points[JHistogramPointData] =
    PointMetricImpl(
      name = Some(name),
      metricType = MetricDataType.HISTOGRAM
    )

  /** Creates an expectation for an exponential histogram metric. */
  def exponentialHistogram(name: String): Points[ExponentialHistogramPointData] =
    PointMetricImpl(
      name = Some(name),
      metricType = MetricDataType.EXPONENTIAL_HISTOGRAM
    )

  private sealed trait PointMatch[-A] {
    def matches(points: List[JPointData]): Boolean
  }

  private object PointMatch {
    case object Ignore extends PointMatch[_root_.scala.Any] {
      def matches(points: List[JPointData]): Boolean = true
    }

    final case class Any[A](expectation: PointExpectation[A]) extends PointMatch[A] {
      def matches(points: List[JPointData]): Boolean =
        points.exists(expectation.matches)
    }

    final case class All[A](expectation: PointExpectation[A]) extends PointMatch[A] {
      def matches(points: List[JPointData]): Boolean =
        points.nonEmpty && points.forall(expectation.matches)
    }
  }

  private sealed trait NumericKind {
    def metricTypeFor[A](valueType: MeasurementValue[A]): MetricDataType
  }

  private object NumericKind {
    case object Gauge extends NumericKind {
      def metricTypeFor[A](valueType: MeasurementValue[A]): MetricDataType =
        valueType match {
          case _: LongMeasurementValue[_]   => MetricDataType.LONG_GAUGE
          case _: DoubleMeasurementValue[_] => MetricDataType.DOUBLE_GAUGE
        }
    }

    case object Sum extends NumericKind {
      def metricTypeFor[A](valueType: MeasurementValue[A]): MetricDataType =
        valueType match {
          case _: LongMeasurementValue[_]   => MetricDataType.LONG_SUM
          case _: DoubleMeasurementValue[_] => MetricDataType.DOUBLE_SUM
        }
    }
  }

  private sealed trait CommonImpl[A] extends MetricExpectation {
    type Value = A

    def name: Option[String]
    def description: Option[String]
    def unit: Option[String]
    def scopeName: Option[String]
    def clue: Option[String]
    def predicates: List[(MetricData => Boolean, String)]

    protected def copyCommon(
        name: Option[String] = name,
        description: Option[String] = description,
        unit: Option[String] = this.unit,
        scopeName: Option[String] = this.scopeName,
        clue: Option[String] = this.clue,
        predicates: List[(MetricData => Boolean, String)] = this.predicates
    ): MetricExpectation

    def withDescription(description: String): MetricExpectation =
      copyCommon(description = Some(description))

    def withUnit(unit: String): MetricExpectation =
      copyCommon(unit = Some(unit))

    def withScopeName(name: String): MetricExpectation =
      copyCommon(scopeName = Some(name))

    def withClue(text: String): MetricExpectation =
      copyCommon(clue = Some(text))

    protected final def matchesCommon(metric: MetricData): Boolean =
      name.forall(_ == metric.getName) &&
        description.forall(Option(metric.getDescription).contains) &&
        unit.forall(Option(metric.getUnit).contains) &&
        scopeName.forall(_ == metric.getInstrumentationScopeInfo.getName) &&
        predicates.forall { case (predicate, _) => predicate(metric) }
  }

  private final case class BaseImpl[A](
      name: Option[String] = None,
      description: Option[String] = None,
      unit: Option[String] = None,
      scopeName: Option[String] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil
  ) extends CommonImpl[A] {

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scopeName: Option[String],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, description, unit, scopeName, clue, predicates)

    def matches(metric: MetricData): Boolean =
      matchesCommon(metric)
  }

  private final case class NumericImpl[A](
      name: Option[String],
      kind: NumericKind,
      valueType: MeasurementValue[A],
      description: Option[String] = None,
      unit: Option[String] = None,
      scopeName: Option[String] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[A] = PointMatch.Ignore
  ) extends Numeric[A]
      with CommonImpl[A] {
    override type Value = A

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scopeName: Option[String],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(
        name = name,
        description = description,
        unit = unit,
        scopeName = scopeName,
        clue = clue,
        predicates = predicates
      )

    def withValue(value: A): Numeric[A] =
      withAnyPoint(PointExpectation.value(value))

    def withPoint(point: PointExpectation[A]): Numeric[A] =
      withAnyPoint(point)

    def withAnyPoint(point: PointExpectation[A]): Numeric[A] =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation[A]): Numeric[A] =
      copy(pointMatch = PointMatch.All(point))

    override def withDescription(description: String): Numeric[A] =
      copy(description = Some(description))

    override def withUnit(unit: String): Numeric[A] =
      copy(unit = Some(unit))

    override def withScopeName(name: String): Numeric[A] =
      copy(scopeName = Some(name))

    override def withClue(text: String): Numeric[A] =
      copy(clue = Some(text))

    def matches(metric: MetricData): Boolean =
      metric.getType == kind.metricTypeFor(valueType) &&
        matchesCommon(metric) &&
        pointMatch.matches(points(metric))
  }

  private final case class PointMetricImpl[A <: JPointData](
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scopeName: Option[String] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[A] = PointMatch.Ignore
  ) extends Points[A]
      with CommonImpl[A] {
    override type Value = A

    override def withDescription(description: String): Points[A] =
      copy(description = Some(description))

    override def withUnit(unit: String): Points[A] =
      copy(unit = Some(unit))

    override def withScopeName(name: String): Points[A] =
      copy(scopeName = Some(name))

    override def withClue(text: String): Points[A] =
      copy(clue = Some(text))

    def withPoint(point: PointExpectation[A]): Points[A] =
      copy(pointMatch = PointMatch.Any(point))

    def withAnyPoint(point: PointExpectation[A]): Points[A] =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation[A]): Points[A] =
      copy(pointMatch = PointMatch.All(point))

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scopeName: Option[String],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, metricType, description, unit, scopeName, clue, predicates, pointMatch)

    def matches(metric: MetricData): Boolean =
      metric.getType == metricType &&
        matchesCommon(metric) &&
        pointMatch.matches(points(metric))
  }

  private def points(metric: MetricData): List[JPointData] =
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
