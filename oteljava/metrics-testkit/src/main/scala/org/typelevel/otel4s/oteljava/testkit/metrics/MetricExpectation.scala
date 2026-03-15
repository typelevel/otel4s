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
package metrics

import cats.data.NonEmptyList
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData
import io.opentelemetry.sdk.metrics.data.MetricData
import io.opentelemetry.sdk.metrics.data.MetricDataType
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.MeasurementValue.DoubleMeasurementValue
import org.typelevel.otel4s.metrics.MeasurementValue.LongMeasurementValue

import scala.jdk.CollectionConverters._

/** A partial expectation for a single OpenTelemetry Java `MetricData`.
  *
  * `MetricExpectation` is intended for tests where asserting against the full `MetricData` shape would be too verbose.
  * Unspecified properties are ignored. Point matching is expressed through collection-level [[PointSetExpectation]]
  * values, which allows multiple point constraints to accumulate on the same metric expectation.
  *
  * Use the builders in [[MetricExpectation]] to create expectations for the metric kind you want to check:
  *   - [[MetricExpectation.name]] to require only a metric name
  *   - [[MetricExpectation.gauge]] for `LONG_GAUGE` and `DOUBLE_GAUGE`
  *   - [[MetricExpectation.sum]] for `LONG_SUM` and `DOUBLE_SUM`
  *   - [[MetricExpectation.summary]] for summary points
  *   - [[MetricExpectation.histogram]] for histogram points
  *
  * Expectations are matched against collected metrics with [[MetricExpectations.exists]], [[MetricExpectations.find]],
  * or `MetricExpectations.checkAll(...)`.
  */
sealed trait MetricExpectation {
  private[metrics] def expectedName: Option[String]

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the metric description to match exactly. */
  def withDescription(description: String): MetricExpectation

  /** Requires the metric unit to match exactly. */
  def withUnit(unit: String): MetricExpectation

  /** Requires the instrumentation scope name to match exactly. */
  def withScopeName(name: String): MetricExpectation

  /** Requires the instrumentation scope to match the given expectation. */
  def withScope(scope: InstrumentationScopeExpectation): MetricExpectation

  /** Requires the telemetry resource to match the given expectation. */
  def withResource(resource: TelemetryResourceExpectation): MetricExpectation

  /** Attaches a human-readable clue to this expectation. */
  def withClue(text: String): MetricExpectation

  /** Adds a custom predicate over the metric data. */
  def where(f: MetricData => Boolean): MetricExpectation

  /** Adds a custom predicate over the metric data with a clue shown in mismatches. */
  def where(clue: String)(f: MetricData => Boolean): MetricExpectation

  /** Checks the given metric and returns structured mismatches when the expectation does not match. */
  def check(metric: MetricData): Either[NonEmptyList[MetricExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given metric. */
  final def matches(metric: MetricData): Boolean =
    check(metric).isRight
}

object MetricExpectation {

  /** A structured reason explaining why a [[MetricExpectation]] did not match a metric. */
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {

    /** Indicates that the metric name differed from the expected one. */
    sealed trait NameMismatch extends Mismatch {
      def expected: String;
      def actual: String
    }

    /** Indicates that the metric description differed from the expected one. */
    sealed trait DescriptionMismatch extends Mismatch {
      def expected: String;
      def actual: Option[String]
    }

    /** Indicates that the metric unit differed from the expected one. */
    sealed trait UnitMismatch extends Mismatch {
      def expected: String;
      def actual: String
    }

    /** Indicates that the metric type differed from the expected one. */
    sealed trait TypeMismatch extends Mismatch {
      def expected: String;
      def actual: String
    }

    /** Indicates that the instrumentation scope did not satisfy the nested expectation. */
    sealed trait ScopeMismatch extends Mismatch {
      def mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch]
    }

    /** Indicates that the telemetry resource did not satisfy the nested expectation. */
    sealed trait ResourceMismatch extends Mismatch {
      def mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch]
    }

    /** Indicates that a custom metric predicate returned `false`. */
    sealed trait PredicateMismatch extends Mismatch { def clue: Option[String] }

    /** Indicates that the metric points did not satisfy the nested point expectation. */
    sealed trait PointsMismatch extends Mismatch {
      def mismatches: NonEmptyList[PointSetExpectation.Mismatch]
      def clue: Option[String]
    }

    /** Creates a mismatch indicating that the metric name differed from the expected one. */
    def nameMismatch(expected: String, actual: String): NameMismatch = NameMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the metric description differed from the expected one. */
    def descriptionMismatch(expected: String, actual: Option[String]): DescriptionMismatch =
      DescriptionMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the metric unit differed from the expected one. */
    def unitMismatch(expected: String, actual: String): UnitMismatch = UnitMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the metric type differed from the expected one. */
    def typeMismatch(expected: String, actual: String): TypeMismatch = TypeMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the instrumentation scope did not satisfy the nested expectation. */
    def scopeMismatch(mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch]): ScopeMismatch =
      ScopeMismatchImpl(mismatches)

    /** Creates a mismatch indicating that the telemetry resource did not satisfy the nested expectation. */
    def resourceMismatch(mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch]): ResourceMismatch =
      ResourceMismatchImpl(mismatches)

    /** Creates a mismatch indicating that a custom metric predicate returned `false`. */
    def predicateMismatch(clue: Option[String]): PredicateMismatch = PredicateMismatchImpl(clue)

    /** Creates a mismatch indicating that the metric points did not satisfy the nested point expectation. */
    def pointsMismatch(mismatches: NonEmptyList[PointSetExpectation.Mismatch], clue: Option[String]): PointsMismatch =
      PointsMismatchImpl(mismatches, clue)

    private final case class NameMismatchImpl(expected: String, actual: String) extends NameMismatch {
      def message: String = s"name mismatch: expected '$expected', got '$actual'"
    }

    private final case class DescriptionMismatchImpl(expected: String, actual: Option[String])
        extends DescriptionMismatch {
      def message: String =
        s"description mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private final case class UnitMismatchImpl(expected: String, actual: String) extends UnitMismatch {
      def message: String = s"unit mismatch: expected '$expected', got '$actual'"
    }

    private final case class TypeMismatchImpl(expected: String, actual: String) extends TypeMismatch {
      def message: String = s"type mismatch: expected '$expected', got '$actual'"
    }

    private final case class ScopeMismatchImpl(mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch])
        extends ScopeMismatch {
      def message: String =
        s"scope mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private final case class ResourceMismatchImpl(mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch])
        extends ResourceMismatch {
      def message: String =
        s"resource mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private final case class PredicateMismatchImpl(clue: Option[String]) extends PredicateMismatch {
      def message: String =
        s"predicate mismatch${clue.fold("")(value => s": $value")}"
    }

    private final case class PointsMismatchImpl(
        mismatches: NonEmptyList[PointSetExpectation.Mismatch],
        clue: Option[String]
    ) extends PointsMismatch {
      def message: String = {
        val rendered = mismatches.toList.map(_.message).mkString(", ")
        val clueSuffix = clue.fold("")(value => s" [$value]")
        s"points mismatch$clueSuffix: $rendered"
      }
    }
  }

  /** A typed expectation for numeric metrics. */
  sealed trait Numeric[A] extends MetricExpectation {

    /** The `MeasurementValue` used to distinguish long and double metrics at runtime. */
    def valueType: MeasurementValue[A]

    /** Requires at least one point with the given value.
      *
      * If no attributes are provided, this behaves like a value-only check. If attributes are provided, they are
      * matched exactly.
      */
    def withValue(value: A, attributes: Attribute[_]*): Numeric[A]

    /** Requires at least one point with the given value and exact attributes. */
    def withValue(value: A, attributes: Attributes): Numeric[A]

    /** Adds a collection-level expectation over the metric points. */
    def withPoints(expectation: PointSetExpectation[PointExpectation.NumericPointData[A]]): Numeric[A]

    /** Requires the metric to contain all given point expectations. */
    def containsPoints(first: PointExpectation.Numeric[A], rest: PointExpectation.Numeric[A]*): Numeric[A]

    /** Requires the metric points to match the given point expectations exactly. */
    def withExactlyPoints(first: PointExpectation.Numeric[A], rest: PointExpectation.Numeric[A]*): Numeric[A]

    /** Requires the metric to have exactly the given number of points. */
    def withPointCount(count: Int): Numeric[A]

    /** Requires no point to match the given point expectation. */
    def withNoPointsMatching(point: PointExpectation.Numeric[A]): Numeric[A]

    /** Adds a custom predicate over the full numeric point collection. */
    def wherePoints(
        f: List[PointExpectation.NumericPointData[A]] => Boolean
    ): Numeric[A]

    /** Adds a custom predicate over the full numeric point collection with a clue shown in mismatches. */
    def wherePoints(
        clue: String
    )(f: List[PointExpectation.NumericPointData[A]] => Boolean): Numeric[A]

    /** Requires the metric description to match exactly. */
    def withDescription(description: String): Numeric[A]

    /** Requires the metric unit to match exactly. */
    def withUnit(unit: String): Numeric[A]

    /** Requires the instrumentation scope name to match exactly. */
    def withScopeName(name: String): Numeric[A]

    /** Requires the instrumentation scope to match the given expectation. */
    def withScope(scope: InstrumentationScopeExpectation): Numeric[A]

    /** Requires the telemetry resource to match the given expectation. */
    def withResource(resource: TelemetryResourceExpectation): Numeric[A]

    /** Attaches a human-readable clue to this expectation. */
    def withClue(text: String): Numeric[A]

    /** Adds a custom predicate over the metric data. */
    def where(f: MetricData => Boolean): Numeric[A]

    /** Adds a custom predicate over the metric data with a clue shown in mismatches. */
    def where(clue: String)(f: MetricData => Boolean): Numeric[A]
  }

  /** A typed expectation for summary metrics. */
  sealed trait Summary extends MetricExpectation {

    /** Adds a collection-level expectation over the metric points. */
    def withPoints(expectation: PointSetExpectation[JSummaryPointData]): Summary

    /** Requires the metric to contain all given point expectations. */
    def containsPoints(first: PointExpectation.Summary, rest: PointExpectation.Summary*): Summary

    /** Requires the metric points to match the given point expectations exactly. */
    def withExactlyPoints(first: PointExpectation.Summary, rest: PointExpectation.Summary*): Summary

    /** Requires the metric to have exactly the given number of points. */
    def withPointCount(count: Int): Summary

    /** Requires no point to match the given point expectation. */
    def withNoPointsMatching(point: PointExpectation.Summary): Summary

    /** Adds a custom predicate over the full summary point collection. */
    def wherePoints(f: List[JSummaryPointData] => Boolean): Summary

    /** Adds a custom predicate over the full summary point collection with a clue shown in mismatches. */
    def wherePoints(clue: String)(f: List[JSummaryPointData] => Boolean): Summary

    /** Requires the metric description to match exactly. */
    def withDescription(description: String): Summary

    /** Requires the metric unit to match exactly. */
    def withUnit(unit: String): Summary

    /** Requires the instrumentation scope name to match exactly. */
    def withScopeName(name: String): Summary

    /** Requires the instrumentation scope to match the given expectation. */
    def withScope(scope: InstrumentationScopeExpectation): Summary

    /** Requires the telemetry resource to match the given expectation. */
    def withResource(resource: TelemetryResourceExpectation): Summary

    /** Attaches a human-readable clue to this expectation. */
    def withClue(text: String): Summary

    /** Adds a custom predicate over the metric data. */
    def where(f: MetricData => Boolean): Summary

    /** Adds a custom predicate over the metric data with a clue shown in mismatches. */
    def where(clue: String)(f: MetricData => Boolean): Summary
  }

  /** A typed expectation for histogram metrics. */
  sealed trait Histogram extends MetricExpectation {

    /** Adds a collection-level expectation over the metric points. */
    def withPoints(expectation: PointSetExpectation[JHistogramPointData]): Histogram

    /** Requires the metric to contain all given point expectations. */
    def containsPoints(first: PointExpectation.Histogram, rest: PointExpectation.Histogram*): Histogram

    /** Requires the metric points to match the given point expectations exactly. */
    def withExactlyPoints(first: PointExpectation.Histogram, rest: PointExpectation.Histogram*): Histogram

    /** Requires the metric to have exactly the given number of points. */
    def withPointCount(count: Int): Histogram

    /** Requires no point to match the given point expectation. */
    def withNoPointsMatching(point: PointExpectation.Histogram): Histogram

    /** Adds a custom predicate over the full histogram point collection. */
    def wherePoints(f: List[JHistogramPointData] => Boolean): Histogram

    /** Adds a custom predicate over the full histogram point collection with a clue shown in mismatches. */
    def wherePoints(clue: String)(f: List[JHistogramPointData] => Boolean): Histogram

    /** Requires the metric description to match exactly. */
    def withDescription(description: String): Histogram

    /** Requires the metric unit to match exactly. */
    def withUnit(unit: String): Histogram

    /** Requires the instrumentation scope name to match exactly. */
    def withScopeName(name: String): Histogram

    /** Requires the instrumentation scope to match the given expectation. */
    def withScope(scope: InstrumentationScopeExpectation): Histogram

    /** Requires the telemetry resource to match the given expectation. */
    def withResource(resource: TelemetryResourceExpectation): Histogram

    /** Attaches a human-readable clue to this expectation. */
    def withClue(text: String): Histogram

    /** Adds a custom predicate over the metric data. */
    def where(f: MetricData => Boolean): Histogram

    /** Adds a custom predicate over the metric data with a clue shown in mismatches. */
    def where(clue: String)(f: MetricData => Boolean): Histogram
  }

  /** A typed expectation for exponential histogram metrics. */
  sealed trait ExponentialHistogram extends MetricExpectation {

    /** Adds a collection-level expectation over the metric points. */
    def withPoints(expectation: PointSetExpectation[ExponentialHistogramPointData]): ExponentialHistogram

    /** Requires the metric to contain all given point expectations. */
    def containsPoints(
        first: PointExpectation.ExponentialHistogram,
        rest: PointExpectation.ExponentialHistogram*
    ): ExponentialHistogram

    /** Requires the metric points to match the given point expectations exactly. */
    def withExactlyPoints(
        first: PointExpectation.ExponentialHistogram,
        rest: PointExpectation.ExponentialHistogram*
    ): ExponentialHistogram

    /** Requires the metric to have exactly the given number of points. */
    def withPointCount(count: Int): ExponentialHistogram

    /** Requires no point to match the given point expectation. */
    def withNoPointsMatching(point: PointExpectation.ExponentialHistogram): ExponentialHistogram

    /** Adds a custom predicate over the full exponential histogram point collection. */
    def wherePoints(f: List[ExponentialHistogramPointData] => Boolean): ExponentialHistogram

    /** Adds a custom predicate over the full exponential histogram point collection with a clue shown in mismatches. */
    def wherePoints(clue: String)(f: List[ExponentialHistogramPointData] => Boolean): ExponentialHistogram

    /** Requires the metric description to match exactly. */
    def withDescription(description: String): ExponentialHistogram

    /** Requires the metric unit to match exactly. */
    def withUnit(unit: String): ExponentialHistogram

    /** Requires the instrumentation scope name to match exactly. */
    def withScopeName(name: String): ExponentialHistogram

    /** Requires the instrumentation scope to match the given expectation. */
    def withScope(scope: InstrumentationScopeExpectation): ExponentialHistogram

    /** Requires the telemetry resource to match the given expectation. */
    def withResource(resource: TelemetryResourceExpectation): ExponentialHistogram

    /** Attaches a human-readable clue to this expectation. */
    def withClue(text: String): ExponentialHistogram

    /** Adds a custom predicate over the metric data. */
    def where(f: MetricData => Boolean): ExponentialHistogram

    /** Adds a custom predicate over the metric data with a clue shown in mismatches. */
    def where(clue: String)(f: MetricData => Boolean): ExponentialHistogram
  }

  /** Creates an expectation that matches any metric with the given name. */
  def name(name: String): MetricExpectation =
    NameImpl(name = Some(name))

  /** Creates a typed expectation for a gauge metric.
    *
    * The metric kind is selected from `A`:
    *   - `A = Long` matches `LONG_GAUGE`
    *   - `A = Double` matches `DOUBLE_GAUGE`
    */
  def gauge[A: MeasurementValue: NumberComparison](name: String): Numeric[A] =
    NumericImpl(
      name = Some(name),
      kind = NumericKind.Gauge,
      valueType = MeasurementValue[A],
      numberComparison = NumberComparison[A]
    )

  /** Creates a typed expectation for a sum metric.
    *
    * The metric kind is selected from `A`:
    *   - `A = Long` matches `LONG_SUM`
    *   - `A = Double` matches `DOUBLE_SUM`
    */
  def sum[A: MeasurementValue: NumberComparison](name: String): Numeric[A] =
    NumericImpl(
      name = Some(name),
      kind = NumericKind.Sum,
      valueType = MeasurementValue[A],
      numberComparison = NumberComparison[A]
    )

  /** Creates an expectation for a summary metric. */
  def summary(name: String): Summary =
    SummaryImpl(name = Some(name), metricType = MetricDataType.SUMMARY)

  /** Creates an expectation for a histogram metric. */
  def histogram(name: String): Histogram =
    HistogramImpl(name = Some(name), metricType = MetricDataType.HISTOGRAM)

  /** Creates an expectation for an exponential histogram metric. */
  def exponentialHistogram(name: String): ExponentialHistogram =
    ExponentialHistogramImpl(name = Some(name), metricType = MetricDataType.EXPONENTIAL_HISTOGRAM)

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

  private final case class NameImpl(
      name: Option[String] = None,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, Option[String])] = Nil
  ) extends MetricExpectation {
    def expectedName: Option[String] = name
    def withDescription(description: String): MetricExpectation = copy(description = Some(description))
    def withUnit(unit: String): MetricExpectation = copy(unit = Some(unit))
    def withScopeName(name: String): MetricExpectation =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))
    def withScope(scope: InstrumentationScopeExpectation): MetricExpectation = copy(scope = Some(scope))
    def withResource(resource: TelemetryResourceExpectation): MetricExpectation = copy(resource = Some(resource))
    def withClue(text: String): MetricExpectation = copy(clue = Some(text))
    def where(f: MetricData => Boolean): MetricExpectation =
      copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: MetricData => Boolean): MetricExpectation =
      copy(predicates = predicates :+ (f -> Some(clue)))
    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      checkCommon(metric, name, description, unit, scope, resource, predicates)
  }

  private final case class NumericImpl[A](
      name: Option[String],
      kind: NumericKind,
      valueType: MeasurementValue[A],
      numberComparison: NumberComparison[A],
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, Option[String])] = Nil,
      pointConstraints: List[PointSetExpectation[PointExpectation.NumericPointData[A]]] = Nil
  ) extends Numeric[A] {
    def expectedName: Option[String] = name

    def withValue(value: A, attributes: Attribute[_]*): Numeric[A] =
      if (attributes.isEmpty)
        withPoints(PointSetExpectation.exists(PointExpectation.numeric(value)(valueType, numberComparison)))
      else withValue(value, Attributes(attributes *))

    def withValue(value: A, attributes: Attributes): Numeric[A] =
      withPoints(
        PointSetExpectation.exists(
          PointExpectation.numeric(value)(valueType, numberComparison).withAttributesExact(attributes)
        )
      )

    def withPoints(expectation: PointSetExpectation[PointExpectation.NumericPointData[A]]): Numeric[A] =
      copy(pointConstraints = pointConstraints :+ expectation)

    def containsPoints(first: PointExpectation.Numeric[A], rest: PointExpectation.Numeric[A]*): Numeric[A] =
      withPoints(PointSetExpectation.contains(first, rest *))

    def withExactlyPoints(first: PointExpectation.Numeric[A], rest: PointExpectation.Numeric[A]*): Numeric[A] =
      withPoints(PointSetExpectation.exactly(first, rest *))

    def withPointCount(count: Int): Numeric[A] =
      withPoints(PointSetExpectation.count(count))

    def withNoPointsMatching(point: PointExpectation.Numeric[A]): Numeric[A] =
      withPoints(PointSetExpectation.none(point))

    def wherePoints(
        f: List[PointExpectation.NumericPointData[A]] => Boolean
    ): Numeric[A] =
      withPoints(PointSetExpectation.predicate(f))

    def wherePoints(
        clue: String
    )(f: List[PointExpectation.NumericPointData[A]] => Boolean): Numeric[A] =
      withPoints(PointSetExpectation.predicate(clue)(f))

    def withDescription(description: String): Numeric[A] = copy(description = Some(description))
    def withUnit(unit: String): Numeric[A] = copy(unit = Some(unit))
    def withScopeName(name: String): Numeric[A] =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))
    def withScope(scope: InstrumentationScopeExpectation): Numeric[A] = copy(scope = Some(scope))
    def withResource(resource: TelemetryResourceExpectation): Numeric[A] = copy(resource = Some(resource))
    def withClue(text: String): Numeric[A] = copy(clue = Some(text))
    def where(f: MetricData => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: MetricData => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] = {
      val typeResult = checkType(metric, kind.metricTypeFor(valueType))
      val pointsResult =
        if (typeResult.isRight) checkPointConstraints(pointConstraints, numericPoints(metric, valueType))
        else ExpectationChecks.success

      ExpectationChecks.combine(
        typeResult,
        checkCommon(metric, name, description, unit, scope, resource, predicates),
        pointsResult
      )
    }
  }

  private final case class SummaryImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, Option[String])] = Nil,
      pointConstraints: List[PointSetExpectation[JSummaryPointData]] = Nil
  ) extends Summary {
    def expectedName: Option[String] = name
    def withPoints(expectation: PointSetExpectation[JSummaryPointData]): Summary =
      copy(pointConstraints = pointConstraints :+ expectation)
    def containsPoints(first: PointExpectation.Summary, rest: PointExpectation.Summary*): Summary =
      withPoints(PointSetExpectation.contains(first, rest *))
    def withExactlyPoints(first: PointExpectation.Summary, rest: PointExpectation.Summary*): Summary =
      withPoints(PointSetExpectation.exactly(first, rest *))
    def withPointCount(count: Int): Summary = withPoints(PointSetExpectation.count(count))
    def withNoPointsMatching(point: PointExpectation.Summary): Summary = withPoints(PointSetExpectation.none(point))
    def wherePoints(f: List[JSummaryPointData] => Boolean): Summary = withPoints(PointSetExpectation.predicate(f))
    def wherePoints(clue: String)(f: List[JSummaryPointData] => Boolean): Summary =
      withPoints(PointSetExpectation.predicate(clue)(f))
    def withDescription(description: String): Summary = copy(description = Some(description))
    def withUnit(unit: String): Summary = copy(unit = Some(unit))
    def withScopeName(name: String): Summary =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))
    def withScope(scope: InstrumentationScopeExpectation): Summary = copy(scope = Some(scope))
    def withResource(resource: TelemetryResourceExpectation): Summary = copy(resource = Some(resource))
    def withClue(text: String): Summary = copy(clue = Some(text))
    def where(f: MetricData => Boolean): Summary =
      copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: MetricData => Boolean): Summary =
      copy(predicates = predicates :+ (f -> Some(clue)))
    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] = {
      val typeResult = checkType(metric, metricType)
      val pointsResult =
        if (typeResult.isRight) checkPointConstraints(pointConstraints, metric.getSummaryData.getPoints.asScala.toList)
        else ExpectationChecks.success
      ExpectationChecks.combine(
        typeResult,
        checkCommon(metric, name, description, unit, scope, resource, predicates),
        pointsResult
      )
    }
  }

  private final case class HistogramImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, Option[String])] = Nil,
      pointConstraints: List[PointSetExpectation[JHistogramPointData]] = Nil
  ) extends Histogram {
    def expectedName: Option[String] = name
    def withPoints(expectation: PointSetExpectation[JHistogramPointData]): Histogram =
      copy(pointConstraints = pointConstraints :+ expectation)
    def containsPoints(first: PointExpectation.Histogram, rest: PointExpectation.Histogram*): Histogram =
      withPoints(PointSetExpectation.contains(first, rest *))
    def withExactlyPoints(first: PointExpectation.Histogram, rest: PointExpectation.Histogram*): Histogram =
      withPoints(PointSetExpectation.exactly(first, rest *))
    def withPointCount(count: Int): Histogram = withPoints(PointSetExpectation.count(count))
    def withNoPointsMatching(point: PointExpectation.Histogram): Histogram =
      withPoints(PointSetExpectation.none(point))
    def wherePoints(f: List[JHistogramPointData] => Boolean): Histogram = withPoints(PointSetExpectation.predicate(f))
    def wherePoints(clue: String)(f: List[JHistogramPointData] => Boolean): Histogram =
      withPoints(PointSetExpectation.predicate(clue)(f))
    def withDescription(description: String): Histogram = copy(description = Some(description))
    def withUnit(unit: String): Histogram = copy(unit = Some(unit))
    def withScopeName(name: String): Histogram =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))
    def withScope(scope: InstrumentationScopeExpectation): Histogram = copy(scope = Some(scope))
    def withResource(resource: TelemetryResourceExpectation): Histogram = copy(resource = Some(resource))
    def withClue(text: String): Histogram = copy(clue = Some(text))
    def where(f: MetricData => Boolean): Histogram =
      copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: MetricData => Boolean): Histogram =
      copy(predicates = predicates :+ (f -> Some(clue)))
    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] = {
      val typeResult = checkType(metric, metricType)
      val pointsResult =
        if (typeResult.isRight)
          checkPointConstraints(pointConstraints, metric.getHistogramData.getPoints.asScala.toList)
        else ExpectationChecks.success
      ExpectationChecks.combine(
        typeResult,
        checkCommon(metric, name, description, unit, scope, resource, predicates),
        pointsResult
      )
    }
  }

  private final case class ExponentialHistogramImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, Option[String])] = Nil,
      pointConstraints: List[PointSetExpectation[ExponentialHistogramPointData]] = Nil
  ) extends ExponentialHistogram {
    def expectedName: Option[String] = name
    def withPoints(expectation: PointSetExpectation[ExponentialHistogramPointData]): ExponentialHistogram =
      copy(pointConstraints = pointConstraints :+ expectation)
    def containsPoints(
        first: PointExpectation.ExponentialHistogram,
        rest: PointExpectation.ExponentialHistogram*
    ): ExponentialHistogram =
      withPoints(PointSetExpectation.contains(first, rest *))
    def withExactlyPoints(
        first: PointExpectation.ExponentialHistogram,
        rest: PointExpectation.ExponentialHistogram*
    ): ExponentialHistogram =
      withPoints(PointSetExpectation.exactly(first, rest *))
    def withPointCount(count: Int): ExponentialHistogram = withPoints(PointSetExpectation.count(count))
    def withNoPointsMatching(point: PointExpectation.ExponentialHistogram): ExponentialHistogram =
      withPoints(PointSetExpectation.none(point))
    def wherePoints(f: List[ExponentialHistogramPointData] => Boolean): ExponentialHistogram =
      withPoints(PointSetExpectation.predicate(f))
    def wherePoints(clue: String)(f: List[ExponentialHistogramPointData] => Boolean): ExponentialHistogram =
      withPoints(PointSetExpectation.predicate(clue)(f))
    def withDescription(description: String): ExponentialHistogram = copy(description = Some(description))
    def withUnit(unit: String): ExponentialHistogram = copy(unit = Some(unit))
    def withScopeName(name: String): ExponentialHistogram =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))
    def withScope(scope: InstrumentationScopeExpectation): ExponentialHistogram = copy(scope = Some(scope))
    def withResource(resource: TelemetryResourceExpectation): ExponentialHistogram = copy(resource = Some(resource))
    def withClue(text: String): ExponentialHistogram = copy(clue = Some(text))
    def where(f: MetricData => Boolean): ExponentialHistogram =
      copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: MetricData => Boolean): ExponentialHistogram =
      copy(predicates = predicates :+ (f -> Some(clue)))
    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] = {
      val typeResult = checkType(metric, metricType)
      val pointsResult =
        if (typeResult.isRight)
          checkPointConstraints(pointConstraints, metric.getExponentialHistogramData.getPoints.asScala.toList)
        else ExpectationChecks.success
      ExpectationChecks.combine(
        typeResult,
        checkCommon(metric, name, description, unit, scope, resource, predicates),
        pointsResult
      )
    }
  }

  private def checkCommon(
      metric: MetricData,
      name: Option[String],
      description: Option[String],
      unit: Option[String],
      scope: Option[InstrumentationScopeExpectation],
      resource: Option[TelemetryResourceExpectation],
      predicates: List[(MetricData => Boolean, Option[String])]
  ): Either[NonEmptyList[Mismatch], Unit] =
    ExpectationChecks.combine(
      name.fold(ExpectationChecks.success[Mismatch]) { expected =>
        if (expected == metric.getName) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.nameMismatch(expected, metric.getName))
      },
      description.fold(ExpectationChecks.success[Mismatch]) { expected =>
        val actual = Option(metric.getDescription)
        if (actual.contains(expected)) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.descriptionMismatch(expected, actual))
      },
      unit.fold(ExpectationChecks.success[Mismatch]) { expected =>
        if (Option(metric.getUnit).contains(expected)) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.unitMismatch(expected, metric.getUnit))
      },
      scope.fold(ExpectationChecks.success[Mismatch]) { expected =>
        ExpectationChecks.nested(expected.check(metric.getInstrumentationScopeInfo))(Mismatch.scopeMismatch)
      },
      resource.fold(ExpectationChecks.success[Mismatch]) { expected =>
        ExpectationChecks.nested(expected.check(metric.getResource))(Mismatch.resourceMismatch)
      },
      ExpectationChecks.combine(
        predicates.map { case (predicate, clue) =>
          if (predicate(metric)) ExpectationChecks.success[Mismatch]
          else ExpectationChecks.mismatch(Mismatch.predicateMismatch(clue))
        }
      )
    )

  private def checkType(metric: MetricData, expected: MetricDataType): Either[NonEmptyList[Mismatch], Unit] =
    if (metric.getType == expected) ExpectationChecks.success
    else ExpectationChecks.mismatch(Mismatch.typeMismatch(expected.toString, metric.getType.toString))

  private def checkPointConstraints[P](
      expectations: List[PointSetExpectation[P]],
      points: List[P]
  ): Either[NonEmptyList[Mismatch], Unit] =
    ExpectationChecks.combine(
      expectations.map { expectation =>
        ExpectationChecks.nested(expectation.check(points))(mismatches =>
          Mismatch.pointsMismatch(mismatches, expectation.clue)
        )
      }
    )

  private def numericPoints[A](
      metric: MetricData,
      valueType: MeasurementValue[A]
  ): List[PointExpectation.NumericPointData[A]] =
    metric.getType match {
      case MetricDataType.LONG_GAUGE =>
        metric.getLongGaugeData.getPoints.asScala.toList.map(point => numericPoint(valueType, point))
      case MetricDataType.DOUBLE_GAUGE =>
        metric.getDoubleGaugeData.getPoints.asScala.toList.map(point => numericPoint(valueType, point))
      case MetricDataType.LONG_SUM =>
        metric.getLongSumData.getPoints.asScala.toList.map(point => numericPoint(valueType, point))
      case MetricDataType.DOUBLE_SUM =>
        metric.getDoubleSumData.getPoints.asScala.toList.map(point => numericPoint(valueType, point))
      case other =>
        throw new IllegalStateException(s"unexpected metric type for numeric points: $other")
    }

  private def numericPoint[A](
      valueType: MeasurementValue[A],
      point: io.opentelemetry.sdk.metrics.data.PointData
  ): PointExpectation.NumericPointData[A] =
    PointExpectation.toNumericPointData(valueType, point) match {
      case Right(value)     => value
      case Left(mismatches) => throw new IllegalStateException(mismatches.message)
    }
}
