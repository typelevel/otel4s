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
import io.opentelemetry.sdk.metrics.data.{ExponentialHistogramPointData => JExponentialHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{LongPointData, PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation

import scala.jdk.CollectionConverters._

/** A partial expectation for a single OpenTelemetry Java point.
  *
  * `PointExpectation` is used together with [[MetricExpectation.Numeric]], [[MetricExpectation.Summary]],
  * [[MetricExpectation.Histogram]], and [[MetricExpectation.ExponentialHistogram]] to express which metric points
  * should be present. Unspecified properties are ignored.
  *
  * Attribute matching is done with [[AttributesExpectation]]:
  *   - [[withAttributes]] accepts a custom attribute expectation
  *   - [[withAttributesExact]] requires exact equality
  *   - [[withAttributesSubset]] requires the expected attributes to be contained in the point
  *
  * Use [[check]] when you need structured mismatch diagnostics and [[matches]] when you only need a boolean result.
  */
sealed trait PointExpectation {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the point attributes to satisfy the given expectation. */
  def withAttributes(expectation: AttributesExpectation): PointExpectation

  /** Requires the point attributes to match exactly. */
  def withAttributesExact(attributes: Attributes): PointExpectation

  /** Requires the point attributes to contain the given attributes. */
  def withAttributesSubset(attributes: Attributes): PointExpectation

  /** Attaches a human-readable clue to this expectation. */
  def withClue(text: String): PointExpectation

  /** Checks the given point and returns structured mismatches when the expectation does not match. */
  def check(point: JPointData): Either[NonEmptyList[PointExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given point. */
  final def matches(point: JPointData): Boolean =
    check(point).isRight
}

object PointExpectation {

  /** A structured reason explaining why a [[PointExpectation]] did not match a point. */
  sealed trait Mismatch extends Product with Serializable

  object Mismatch {

    /** Indicates that the actual point type differed from the expected one. */
    final case class TypeMismatch(expected: String, actual: String) extends Mismatch

    /** Indicates that the numeric point value differed from the expected one. */
    final case class ValueMismatch(expected: String, actual: String) extends Mismatch

    /** Indicates that the point count differed from the expected one. */
    final case class CountMismatch(expected: Long, actual: Long) extends Mismatch

    /** Indicates that the point sum differed from the expected one. */
    final case class SumMismatch(expected: Double, actual: Double) extends Mismatch

    /** Indicates that histogram boundaries differed from the expected ones. */
    final case class BoundariesMismatch(expected: List[Double], actual: List[Double]) extends Mismatch

    /** Indicates that histogram bucket counts differed from the expected ones. */
    final case class CountsMismatch(expected: List[Long], actual: List[Long]) extends Mismatch

    /** Indicates that the exponential histogram scale differed from the expected one. */
    final case class ScaleMismatch(expected: Int, actual: Int) extends Mismatch

    /** Indicates that the exponential histogram zero count differed from the expected one. */
    final case class ZeroCountMismatch(expected: Long, actual: Long) extends Mismatch

    /** Indicates that the point attributes did not satisfy the nested attributes expectation. */
    final case class AttributesMismatch(
        mismatches: NonEmptyList[AttributesExpectation.Mismatch]
    ) extends Mismatch

    /** Indicates that a custom point predicate returned `false`. */
    final case class PredicateMismatch(clue: String) extends Mismatch
  }

  /** A point expectation for numeric points. */
  sealed trait Numeric[A] extends PointExpectation {

    /** Requires the point value to match exactly. */
    def withValue(value: A): Numeric[A]

    /** Adds a custom predicate over the numeric point data. */
    def where(f: JPointData => Boolean): Numeric[A]

    /** Adds a custom predicate over the numeric point data with a clue shown in mismatches. */
    def where(clue: String)(f: JPointData => Boolean): Numeric[A]

    def withAttributes(expectation: AttributesExpectation): Numeric[A]

    def withAttributesExact(attributes: Attributes): Numeric[A]

    def withAttributesSubset(attributes: Attributes): Numeric[A]

    def withClue(text: String): Numeric[A]

  }

  /** A point expectation for summary points. */
  sealed trait Summary extends PointExpectation {

    /** Requires the summary point count to match exactly. */
    def withCount(count: Long): Summary

    /** Requires the summary point sum to match exactly. */
    def withSum(sum: Double): Summary

    /** Adds a custom predicate over summary point data. */
    def where(f: JSummaryPointData => Boolean): Summary

    /** Adds a custom predicate over summary point data with a clue shown in mismatches. */
    def where(clue: String)(f: JSummaryPointData => Boolean): Summary

    def withAttributes(expectation: AttributesExpectation): Summary

    def withAttributesExact(attributes: Attributes): Summary

    def withAttributesSubset(attributes: Attributes): Summary

    def withClue(text: String): Summary

  }

  /** A point expectation for histogram points. */
  sealed trait Histogram extends PointExpectation {

    /** Requires the histogram point count to match exactly. */
    def withCount(count: Long): Histogram

    /** Requires the histogram point sum to match exactly. */
    def withSum(sum: Double): Histogram

    /** Requires the histogram bucket boundaries to match exactly. */
    def withBoundaries(boundaries: List[Double]): Histogram

    /** Requires the histogram bucket counts to match exactly. */
    def withCounts(counts: List[Long]): Histogram

    /** Adds a custom predicate over histogram point data. */
    def where(f: JHistogramPointData => Boolean): Histogram

    /** Adds a custom predicate over histogram point data with a clue shown in mismatches. */
    def where(clue: String)(f: JHistogramPointData => Boolean): Histogram

    def withAttributes(expectation: AttributesExpectation): Histogram

    def withAttributesExact(attributes: Attributes): Histogram

    def withAttributesSubset(attributes: Attributes): Histogram

    def withClue(text: String): Histogram

  }

  /** A point expectation for exponential histogram points. */
  sealed trait ExponentialHistogram extends PointExpectation {

    /** Requires the exponential histogram scale to match exactly. */
    def withScale(scale: Int): ExponentialHistogram

    /** Requires the exponential histogram count to match exactly. */
    def withCount(count: Long): ExponentialHistogram

    /** Requires the exponential histogram sum to match exactly. */
    def withSum(sum: Double): ExponentialHistogram

    /** Requires the exponential histogram zero count to match exactly. */
    def withZeroCount(zeroCount: Long): ExponentialHistogram

    /** Adds a custom predicate over exponential histogram point data. */
    def where(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram

    /** Adds a custom predicate over exponential histogram point data with a clue shown in mismatches. */
    def where(clue: String)(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram

    def withAttributes(expectation: AttributesExpectation): ExponentialHistogram

    def withAttributesExact(attributes: Attributes): ExponentialHistogram

    def withAttributesSubset(attributes: Attributes): ExponentialHistogram

    def withClue(text: String): ExponentialHistogram

  }

  /** Creates an expectation that matches any numeric point of type `A`. */
  def any[A]: Numeric[A] =
    NumericImpl[A]()

  /** Creates an expectation for a numeric point with the given value. */
  def value[A](value: A): Numeric[A] =
    NumericImpl[A](expectedValue = Some(value))

  /** Creates an expectation for a summary point.
    *
    * The optional parameters are shorthand for applying the corresponding fluent methods.
    */
  def summary(
      sum: Option[Double] = None,
      count: Option[Long] = None
  ): Summary =
    SummaryImpl(
      expectedSum = sum,
      expectedCount = count
    )

  /** Creates an expectation for a histogram point.
    *
    * The optional parameters are shorthand for applying the corresponding fluent methods.
    */
  def histogram(
      sum: Option[Double] = None,
      count: Option[Long] = None,
      boundaries: Option[List[Double]] = None,
      counts: Option[List[Long]] = None
  ): Histogram =
    HistogramImpl(
      expectedSum = sum,
      expectedCount = count,
      expectedBoundaries = boundaries,
      expectedCounts = counts
    )

  /** Creates an expectation for an exponential histogram point.
    *
    * The optional parameters are shorthand for applying the corresponding fluent methods.
    */
  def exponentialHistogram(
      scale: Option[Int] = None,
      sum: Option[Double] = None,
      count: Option[Long] = None,
      zeroCount: Option[Long] = None
  ): ExponentialHistogram =
    ExponentialHistogramImpl(
      expectedScale = scale,
      expectedSum = sum,
      expectedCount = count,
      expectedZeroCount = zeroCount
    )

  /** Formats a mismatch into a human-readable message. */
  def formatMismatch(mismatch: Mismatch): String =
    mismatch match {
      case Mismatch.TypeMismatch(expected, actual) =>
        s"type mismatch: expected '$expected', got '$actual'"
      case Mismatch.ValueMismatch(expected, actual) =>
        s"value mismatch: expected '$expected', got '$actual'"
      case Mismatch.CountMismatch(expected, actual) =>
        s"count mismatch: expected $expected, got $actual"
      case Mismatch.SumMismatch(expected, actual) =>
        s"sum mismatch: expected $expected, got $actual"
      case Mismatch.BoundariesMismatch(expected, actual) =>
        s"boundaries mismatch: expected $expected, got $actual"
      case Mismatch.CountsMismatch(expected, actual) =>
        s"counts mismatch: expected $expected, got $actual"
      case Mismatch.ScaleMismatch(expected, actual) =>
        s"scale mismatch: expected $expected, got $actual"
      case Mismatch.ZeroCountMismatch(expected, actual) =>
        s"zero count mismatch: expected $expected, got $actual"
      case Mismatch.AttributesMismatch(mismatches) =>
        s"attributes mismatch: ${mismatches.toList.map(AttributesExpectation.formatMismatch).mkString(", ")}"
      case Mismatch.PredicateMismatch(clue) =>
        s"predicate mismatch: $clue"
    }

  private final case class NumericImpl[A](
      expectedValue: Option[A] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, Option[String])] = Nil
  ) extends Numeric[A] {

    def withValue(value: A): Numeric[A] =
      copy(expectedValue = Some(value))

    def withAttributes(expectation: AttributesExpectation): Numeric[A] =
      copy(attributeExpectation = Some(expectation))

    def withAttributesExact(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))

    def withAttributesSubset(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))

    def withClue(text: String): Numeric[A] =
      copy(clue = Some(text))

    def where(f: JPointData => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: JPointData => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        expectedValue.fold(ExpectationChecks.success[Mismatch]) { expected =>
          checkValue(expected, point)
        },
        attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(point.getAttributes.toScala))(Mismatch.AttributesMismatch.apply)
        },
        ExpectationChecks.combine(
          predicates.map { case (predicate, clue) =>
            if (predicate(point)) ExpectationChecks.success
            else
              ExpectationChecks.mismatch(Mismatch.PredicateMismatch(clue.getOrElse("point predicate returned false")))
          }
        )
      )

    private def checkValue(expected: A, point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      (expected, point) match {
        case (value: Long, long: LongPointData) =>
          if (value == long.getValue) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.ValueMismatch(value.toString, long.getValue.toString))
        case (value: Double, double: io.opentelemetry.sdk.metrics.data.DoublePointData) =>
          if (value == double.getValue) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.ValueMismatch(value.toString, double.getValue.toString))
        case _ =>
          ExpectationChecks.mismatch(
            Mismatch.TypeMismatch(
              expected.getClass.getSimpleName,
              point.getClass.getSimpleName
            )
          )
      }
  }

  private final case class SummaryImpl(
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JSummaryPointData => Boolean, Option[String])] = Nil
  ) extends Summary {

    def withCount(count: Long): Summary =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): Summary =
      copy(expectedSum = Some(sum))

    def withAttributes(expectation: AttributesExpectation): Summary =
      copy(attributeExpectation = Some(expectation))

    def withAttributesExact(attributes: Attributes): Summary =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))

    def withAttributesSubset(attributes: Attributes): Summary =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))

    def withClue(text: String): Summary =
      copy(clue = Some(text))

    def where(f: JSummaryPointData => Boolean): Summary =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: JSummaryPointData => Boolean): Summary =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      point match {
        case summary: JSummaryPointData =>
          ExpectationChecks.combine(
            expectedSum.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == summary.getSum) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.SumMismatch(expected, summary.getSum))
            },
            expectedCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == summary.getCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountMismatch(expected, summary.getCount))
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(expected.check(summary.getAttributes.toScala))(Mismatch.AttributesMismatch.apply)
            },
            ExpectationChecks.combine(
              predicates.map { case (predicate, clue) =>
                if (predicate(summary)) ExpectationChecks.success
                else
                  ExpectationChecks.mismatch(
                    Mismatch.PredicateMismatch(clue.getOrElse("point predicate returned false"))
                  )
              }
            )
          )
        case other =>
          ExpectationChecks.mismatch(Mismatch.TypeMismatch("SummaryPointData", other.getClass.getSimpleName))
      }
  }

  private final case class HistogramImpl(
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedBoundaries: Option[List[Double]] = None,
      expectedCounts: Option[List[Long]] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JHistogramPointData => Boolean, Option[String])] = Nil
  ) extends Histogram {

    def withCount(count: Long): Histogram =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): Histogram =
      copy(expectedSum = Some(sum))

    def withBoundaries(boundaries: List[Double]): Histogram =
      copy(expectedBoundaries = Some(boundaries))

    def withCounts(counts: List[Long]): Histogram =
      copy(expectedCounts = Some(counts))

    def withAttributes(expectation: AttributesExpectation): Histogram =
      copy(attributeExpectation = Some(expectation))

    def withAttributesExact(attributes: Attributes): Histogram =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))

    def withAttributesSubset(attributes: Attributes): Histogram =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))

    def withClue(text: String): Histogram =
      copy(clue = Some(text))

    def where(f: JHistogramPointData => Boolean): Histogram =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: JHistogramPointData => Boolean): Histogram =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      point match {
        case histogram: JHistogramPointData =>
          ExpectationChecks.combine(
            expectedSum.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getSum) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.SumMismatch(expected, histogram.getSum))
            },
            expectedCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountMismatch(expected, histogram.getCount))
            },
            expectedBoundaries.fold(ExpectationChecks.success[Mismatch]) { expected =>
              val actual = histogram.getBoundaries.asScala.toList.map(_.doubleValue())
              if (expected == actual) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.BoundariesMismatch(expected, actual))
            },
            expectedCounts.fold(ExpectationChecks.success[Mismatch]) { expected =>
              val actual = histogram.getCounts.asScala.toList.map(_.longValue())
              if (expected == actual) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountsMismatch(expected, actual))
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(
                expected.check(histogram.getAttributes.toScala)
              )(Mismatch.AttributesMismatch.apply)
            },
            ExpectationChecks.combine(
              predicates.map { case (predicate, clue) =>
                if (predicate(histogram)) ExpectationChecks.success
                else
                  ExpectationChecks.mismatch(
                    Mismatch.PredicateMismatch(clue.getOrElse("point predicate returned false"))
                  )
              }
            )
          )
        case other =>
          ExpectationChecks.mismatch(Mismatch.TypeMismatch("HistogramPointData", other.getClass.getSimpleName))
      }
  }

  private final case class ExponentialHistogramImpl(
      expectedScale: Option[Int] = None,
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedZeroCount: Option[Long] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JExponentialHistogramPointData => Boolean, Option[String])] = Nil
  ) extends ExponentialHistogram {

    def withScale(scale: Int): ExponentialHistogram =
      copy(expectedScale = Some(scale))

    def withCount(count: Long): ExponentialHistogram =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): ExponentialHistogram =
      copy(expectedSum = Some(sum))

    def withZeroCount(zeroCount: Long): ExponentialHistogram =
      copy(expectedZeroCount = Some(zeroCount))

    def withAttributes(expectation: AttributesExpectation): ExponentialHistogram =
      copy(attributeExpectation = Some(expectation))

    def withAttributesExact(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))

    def withAttributesSubset(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))

    def withClue(text: String): ExponentialHistogram =
      copy(clue = Some(text))

    def where(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      point match {
        case histogram: JExponentialHistogramPointData =>
          ExpectationChecks.combine(
            expectedScale.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getScale) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.ScaleMismatch(expected, histogram.getScale))
            },
            expectedSum.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getSum) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.SumMismatch(expected, histogram.getSum))
            },
            expectedCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountMismatch(expected, histogram.getCount))
            },
            expectedZeroCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getZeroCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.ZeroCountMismatch(expected, histogram.getZeroCount))
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(
                expected.check(histogram.getAttributes.toScala)
              )(Mismatch.AttributesMismatch.apply)
            },
            ExpectationChecks.combine(
              predicates.map { case (predicate, clue) =>
                if (predicate(histogram)) ExpectationChecks.success
                else
                  ExpectationChecks.mismatch(
                    Mismatch.PredicateMismatch(clue.getOrElse("point predicate returned false"))
                  )
              }
            )
          )
        case other =>
          ExpectationChecks.mismatch(
            Mismatch.TypeMismatch("ExponentialHistogramPointData", other.getClass.getSimpleName)
          )
      }
  }

}
