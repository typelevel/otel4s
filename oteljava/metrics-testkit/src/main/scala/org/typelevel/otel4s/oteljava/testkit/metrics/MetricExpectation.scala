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
import io.opentelemetry.sdk.metrics.data.{ExponentialHistogramPointData, MetricData, MetricDataType}
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import org.typelevel.otel4s.{Attribute, Attributes}
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
  * or [[MetricExpectations.checkAll]].
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
    sealed trait NameMismatch extends Mismatch { def expected: String; def actual: String }

    /** Indicates that the metric description differed from the expected one. */
    sealed trait DescriptionMismatch extends Mismatch { def expected: String; def actual: Option[String] }

    /** Indicates that the metric unit differed from the expected one. */
    sealed trait UnitMismatch extends Mismatch { def expected: String; def actual: String }

    /** Indicates that the metric type differed from the expected one. */
    sealed trait TypeMismatch extends Mismatch { def expected: String; def actual: String }

    /** Indicates that the instrumentation scope did not satisfy the nested expectation. */
    sealed trait ScopeMismatch extends Mismatch {
      def mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch]
    }

    /** Indicates that the telemetry resource did not satisfy the nested expectation. */
    sealed trait ResourceMismatch extends Mismatch {
      def mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch]
    }

    /** Indicates that a custom metric predicate returned `false`. */
    sealed trait PredicateMismatch extends Mismatch { def clue: String }

    /** Indicates that the metric points did not satisfy the nested point expectation. */
    sealed trait PointsMismatch extends Mismatch {
      def mode: String
      def mismatches: NonEmptyList[PointExpectation.Mismatch]
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
    def predicateMismatch(clue: String): PredicateMismatch = PredicateMismatchImpl(clue)

    /** Creates a mismatch indicating that the metric points did not satisfy the nested point expectation. */
    def pointsMismatch(
        mode: String,
        mismatches: NonEmptyList[PointExpectation.Mismatch],
        clue: Option[String]
    ): PointsMismatch =
      PointsMismatchImpl(mode, mismatches, clue)

    private final case class NameMismatchImpl(expected: String, actual: String) extends NameMismatch {
      def message: String =
        s"name mismatch: expected '$expected', got '$actual'"
    }

    private final case class DescriptionMismatchImpl(expected: String, actual: Option[String])
        extends DescriptionMismatch {
      def message: String =
        s"description mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private final case class UnitMismatchImpl(expected: String, actual: String) extends UnitMismatch {
      def message: String =
        s"unit mismatch: expected '$expected', got '$actual'"
    }

    private final case class TypeMismatchImpl(expected: String, actual: String) extends TypeMismatch {
      def message: String =
        s"type mismatch: expected '$expected', got '$actual'"
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

    private final case class PredicateMismatchImpl(clue: String) extends PredicateMismatch {
      def message: String =
        s"predicate mismatch: $clue"
    }

    private final case class PointsMismatchImpl(
        mode: String,
        mismatches: NonEmptyList[PointExpectation.Mismatch],
        clue: Option[String]
    ) extends PointsMismatch {
      def message: String = {
        val rendered = mismatches.toList.map(_.message).mkString(", ")
        val clueSuffix = clue.fold("")(value => s" [$value]")
        s"points mismatch ($mode$clueSuffix): $rendered"
      }
    }
  }

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

    /** The `MeasurementValue` used to distinguish long and double metrics at runtime. */
    def valueType: MeasurementValue[A]

    /** Requires at least one point with the given value.
      *
      * If no attributes are provided, this behaves like a value-only check. If attributes are provided, they are
      * matched exactly.
      */
    def withValue(value: A, attributes: Attribute[_]*): Numeric[A]

    /** Requires at least one point with the given value and exact attributes.
      *
      * This is equivalent to calling:
      * {{{
      * withAnyPoint(PointExpectation.numeric(value).withAttributesExact(attributes))
      * }}}
      */
    def withValue(value: A, attributes: Attributes): Numeric[A]

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation.Numeric[A]): Numeric[A]

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation.Numeric[A]): Numeric[A]

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation.Numeric[A]): Numeric[A]
  }

  /** A typed expectation for summary metrics. */
  sealed trait Summary extends MetricExpectation {

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation.Summary): Summary

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation.Summary): Summary

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation.Summary): Summary
  }

  /** A typed expectation for histogram metrics. */
  sealed trait Histogram extends MetricExpectation {

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation.Histogram): Histogram

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation.Histogram): Histogram

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation.Histogram): Histogram
  }

  /** A typed expectation for exponential histogram metrics. */
  sealed trait ExponentialHistogram extends MetricExpectation {

    /** Alias for [[withAnyPoint]]. */
    def withPoint(point: PointExpectation.ExponentialHistogram): ExponentialHistogram

    /** Requires at least one point matching the given expectation. */
    def withAnyPoint(point: PointExpectation.ExponentialHistogram): ExponentialHistogram

    /** Requires all points to match the given expectation. */
    def withAllPoints(point: PointExpectation.ExponentialHistogram): ExponentialHistogram
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
    SummaryImpl(
      name = Some(name),
      metricType = MetricDataType.SUMMARY
    )

  /** Creates an expectation for a histogram metric. */
  def histogram(name: String): Histogram =
    HistogramImpl(
      name = Some(name),
      metricType = MetricDataType.HISTOGRAM
    )

  /** Creates an expectation for an exponential histogram metric. */
  def exponentialHistogram(name: String): ExponentialHistogram =
    ExponentialHistogramImpl(
      name = Some(name),
      metricType = MetricDataType.EXPONENTIAL_HISTOGRAM
    )

  private sealed trait PointMatch[-A] {
    def check(points: List[JPointData]): Either[NonEmptyList[Mismatch], Unit]
  }

  private object PointMatch {
    case object Ignore extends PointMatch[_root_.scala.Any] {
      def check(points: List[JPointData]): Either[NonEmptyList[Mismatch], Unit] =
        ExpectationChecks.success
    }

    final case class Any[A](expectation: PointExpectation) extends PointMatch[A] {
      def check(points: List[JPointData]): Either[NonEmptyList[Mismatch], Unit] =
        if (points.exists(expectation.matches)) ExpectationChecks.success
        else {
          val mismatches = closestPointMismatch(points, expectation)

          ExpectationChecks.mismatch(Mismatch.pointsMismatch("any", mismatches, expectation.clue))
        }
    }

    final case class All[A](expectation: PointExpectation) extends PointMatch[A] {
      def check(points: List[JPointData]): Either[NonEmptyList[Mismatch], Unit] =
        if (points.isEmpty) {
          ExpectationChecks.mismatch(
            Mismatch.pointsMismatch(
              "all",
              NonEmptyList.one(
                PointExpectation.Mismatch.predicateMismatch("no points were collected")
              ),
              expectation.clue
            )
          )
        } else {
          points.collectFirst(Function.unlift(point => expectation.check(point).left.toOption)) match {
            case None =>
              ExpectationChecks.success
            case Some(_) =>
              ExpectationChecks.mismatch(
                Mismatch.pointsMismatch("all", closestPointMismatch(points, expectation), expectation.clue)
              )
          }
        }
    }

    private def closestPointMismatch[A](
        points: List[JPointData],
        expectation: PointExpectation
    ): NonEmptyList[PointExpectation.Mismatch] =
      points
        .flatMap(point => expectation.check(point).left.toOption)
        .sortBy(_.length)
        .headOption
        .getOrElse(
          NonEmptyList.one(
            PointExpectation.Mismatch.predicateMismatch("no points were collected")
          )
        )
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
    def name: Option[String]
    def description: Option[String]
    def unit: Option[String]
    def scope: Option[InstrumentationScopeExpectation]
    def resource: Option[TelemetryResourceExpectation]
    def clue: Option[String]
    def predicates: List[(MetricData => Boolean, String)]

    final def expectedName: Option[String] = name

    protected def copyCommon(
        name: Option[String] = name,
        description: Option[String] = description,
        unit: Option[String] = this.unit,
        scope: Option[InstrumentationScopeExpectation] = this.scope,
        resource: Option[TelemetryResourceExpectation] = this.resource,
        clue: Option[String] = this.clue,
        predicates: List[(MetricData => Boolean, String)] = this.predicates
    ): MetricExpectation

    def withDescription(description: String): MetricExpectation =
      copyCommon(description = Some(description))

    def withUnit(unit: String): MetricExpectation =
      copyCommon(unit = Some(unit))

    def withScopeName(name: String): MetricExpectation =
      copyCommon(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))

    def withScope(scope: InstrumentationScopeExpectation): MetricExpectation =
      copyCommon(scope = Some(scope))

    def withResource(resource: TelemetryResourceExpectation): MetricExpectation =
      copyCommon(resource = Some(resource))

    def withClue(text: String): MetricExpectation =
      copyCommon(clue = Some(text))

    protected final def checkCommon(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        List(
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
          }
        ) ++ predicates.map { case (predicate, clue) =>
          if (predicate(metric)) ExpectationChecks.success[Mismatch]
          else ExpectationChecks.mismatch(Mismatch.predicateMismatch(clue))
        }
      )
  }

  private final case class BaseImpl[A](
      name: Option[String] = None,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil
  ) extends CommonImpl[A] {

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scope: Option[InstrumentationScopeExpectation],
        resource: Option[TelemetryResourceExpectation],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, description, unit, scope, resource, clue, predicates)

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      checkCommon(metric)
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
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[A] = PointMatch.Ignore
  ) extends Numeric[A]
      with CommonImpl[A] {
    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scope: Option[InstrumentationScopeExpectation],
        resource: Option[TelemetryResourceExpectation],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(
        name = name,
        description = description,
        unit = unit,
        scope = scope,
        resource = resource,
        clue = clue,
        predicates = predicates
      )

    def withValue(value: A, attributes: Attribute[_]*): Numeric[A] =
      if (attributes.isEmpty) withAnyPoint(PointExpectation.numeric(value)(numberComparison))
      else withValue(value, Attributes(attributes *))

    def withValue(value: A, attributes: Attributes): Numeric[A] =
      withAnyPoint(
        PointExpectation
          .numeric(value)(numberComparison)
          .withAttributesExact(attributes)
      )

    def withPoint(point: PointExpectation.Numeric[A]): Numeric[A] =
      withAnyPoint(point)

    def withAnyPoint(point: PointExpectation.Numeric[A]): Numeric[A] =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation.Numeric[A]): Numeric[A] =
      copy(pointMatch = PointMatch.All(point))

    override def withDescription(description: String): Numeric[A] =
      copy(description = Some(description))

    override def withUnit(unit: String): Numeric[A] =
      copy(unit = Some(unit))

    override def withScopeName(name: String): Numeric[A] =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))

    override def withScope(scope: InstrumentationScopeExpectation): Numeric[A] =
      copy(scope = Some(scope))

    override def withResource(resource: TelemetryResourceExpectation): Numeric[A] =
      copy(resource = Some(resource))

    override def withClue(text: String): Numeric[A] =
      copy(clue = Some(text))

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        checkType(metric, kind.metricTypeFor(valueType)),
        checkCommon(metric),
        pointMatch.check(points(metric))
      )
  }

  private final case class SummaryImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[JSummaryPointData] = PointMatch.Ignore
  ) extends Summary
      with CommonImpl[JSummaryPointData] {
    override def withDescription(description: String): Summary =
      copy(description = Some(description))

    override def withUnit(unit: String): Summary =
      copy(unit = Some(unit))

    override def withScopeName(name: String): Summary =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))

    override def withScope(scope: InstrumentationScopeExpectation): Summary =
      copy(scope = Some(scope))

    override def withResource(resource: TelemetryResourceExpectation): Summary =
      copy(resource = Some(resource))

    override def withClue(text: String): Summary =
      copy(clue = Some(text))

    def withPoint(point: PointExpectation.Summary): Summary =
      copy(pointMatch = PointMatch.Any(point))

    def withAnyPoint(point: PointExpectation.Summary): Summary =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation.Summary): Summary =
      copy(pointMatch = PointMatch.All(point))

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scope: Option[InstrumentationScopeExpectation],
        resource: Option[TelemetryResourceExpectation],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, metricType, description, unit, scope, resource, clue, predicates, pointMatch)

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        checkType(metric, metricType),
        checkCommon(metric),
        pointMatch.check(points(metric))
      )
  }

  private final case class HistogramImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[JHistogramPointData] = PointMatch.Ignore
  ) extends Histogram
      with CommonImpl[JHistogramPointData] {
    override def withDescription(description: String): Histogram =
      copy(description = Some(description))

    override def withUnit(unit: String): Histogram =
      copy(unit = Some(unit))

    override def withScopeName(name: String): Histogram =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))

    override def withScope(scope: InstrumentationScopeExpectation): Histogram =
      copy(scope = Some(scope))

    override def withResource(resource: TelemetryResourceExpectation): Histogram =
      copy(resource = Some(resource))

    override def withClue(text: String): Histogram =
      copy(clue = Some(text))

    def withPoint(point: PointExpectation.Histogram): Histogram =
      copy(pointMatch = PointMatch.Any(point))

    def withAnyPoint(point: PointExpectation.Histogram): Histogram =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation.Histogram): Histogram =
      copy(pointMatch = PointMatch.All(point))

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scope: Option[InstrumentationScopeExpectation],
        resource: Option[TelemetryResourceExpectation],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, metricType, description, unit, scope, resource, clue, predicates, pointMatch)

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        checkType(metric, metricType),
        checkCommon(metric),
        pointMatch.check(points(metric))
      )
  }

  private final case class ExponentialHistogramImpl(
      name: Option[String],
      metricType: MetricDataType,
      description: Option[String] = None,
      unit: Option[String] = None,
      scope: Option[InstrumentationScopeExpectation] = None,
      resource: Option[TelemetryResourceExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(MetricData => Boolean, String)] = Nil,
      pointMatch: PointMatch[ExponentialHistogramPointData] = PointMatch.Ignore
  ) extends ExponentialHistogram
      with CommonImpl[ExponentialHistogramPointData] {
    override def withDescription(description: String): ExponentialHistogram =
      copy(description = Some(description))

    override def withUnit(unit: String): ExponentialHistogram =
      copy(unit = Some(unit))

    override def withScopeName(name: String): ExponentialHistogram =
      copy(scope = Some(scope.fold(InstrumentationScopeExpectation.name(name))(_.withName(name))))

    override def withScope(scope: InstrumentationScopeExpectation): ExponentialHistogram =
      copy(scope = Some(scope))

    override def withResource(resource: TelemetryResourceExpectation): ExponentialHistogram =
      copy(resource = Some(resource))

    override def withClue(text: String): ExponentialHistogram =
      copy(clue = Some(text))

    def withPoint(point: PointExpectation.ExponentialHistogram): ExponentialHistogram =
      copy(pointMatch = PointMatch.Any(point))

    def withAnyPoint(point: PointExpectation.ExponentialHistogram): ExponentialHistogram =
      copy(pointMatch = PointMatch.Any(point))

    def withAllPoints(point: PointExpectation.ExponentialHistogram): ExponentialHistogram =
      copy(pointMatch = PointMatch.All(point))

    protected def copyCommon(
        name: Option[String],
        description: Option[String],
        unit: Option[String],
        scope: Option[InstrumentationScopeExpectation],
        resource: Option[TelemetryResourceExpectation],
        clue: Option[String],
        predicates: List[(MetricData => Boolean, String)]
    ): MetricExpectation =
      copy(name, metricType, description, unit, scope, resource, clue, predicates, pointMatch)

    def check(metric: MetricData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        checkType(metric, metricType),
        checkCommon(metric),
        pointMatch.check(points(metric))
      )
  }

  private def checkType(metric: MetricData, expected: MetricDataType): Either[NonEmptyList[Mismatch], Unit] =
    if (metric.getType == expected) ExpectationChecks.success
    else ExpectationChecks.mismatch(Mismatch.typeMismatch(expected.toString, metric.getType.toString))

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
