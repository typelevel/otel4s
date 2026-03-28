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
import io.opentelemetry.sdk.metrics.data.{PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import io.opentelemetry.sdk.metrics.data.DoublePointData
import io.opentelemetry.sdk.metrics.data.LongPointData
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.MeasurementValue.DoubleMeasurementValue
import org.typelevel.otel4s.metrics.MeasurementValue.LongMeasurementValue
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation

import scala.jdk.CollectionConverters._

/** A partial expectation for a single OpenTelemetry Java point.
  *
  * `PointExpectation` is used together with metric expectations to express which individual points should be present.
  * Unspecified properties are ignored.
  */
sealed trait PointExpectation {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the point attributes to satisfy the given expectation. */
  def attributes(expectation: AttributesExpectation): PointExpectation

  /** Requires the point attributes to match exactly. */
  def attributesExact(attributes: Attributes): PointExpectation

  /** Requires the point attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): PointExpectation

  /** Requires the point attributes to be empty. */
  def attributesEmpty: PointExpectation

  /** Requires the point attributes to contain the given attributes. */
  def attributesSubset(attributes: Attributes): PointExpectation

  /** Requires the point attributes to contain the given attributes. */
  def attributesSubset(attributes: Attribute[_]*): PointExpectation

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): PointExpectation

  /** Checks the given point and returns structured mismatches when the expectation does not match. */
  def check(point: JPointData): Either[NonEmptyList[PointExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given point. */
  final def matches(point: JPointData): Boolean =
    check(point).isRight
}

object PointExpectation {

  /** A typed view over OpenTelemetry numeric point data.
    *
    * This avoids exposing raw `JPointData` in the public numeric predicate API while still giving access to the
    * underlying OpenTelemetry point when needed.
    */
  sealed trait NumericPointData[A] {
    def value: A
    def attributes: Attributes
    def underlying: JPointData
  }

  /** A structured reason explaining why a [[PointExpectation]] did not match a point. */
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {
    private[metrics] final case class TypeMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"type mismatch: expected '$expected', got '$actual'"
    }

    private[metrics] final case class ValueMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"value mismatch: expected '$expected', got '$actual'"
    }

    private[metrics] final case class CountMismatch(expected: Long, actual: Long) extends Mismatch {
      def message: String =
        s"count mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class SumMismatch(expected: Double, actual: Double) extends Mismatch {
      def message: String =
        s"sum mismatch: expected ${NumberComparison[Double].render(expected)}, got ${NumberComparison[Double].render(actual)}"
    }

    private[metrics] final case class BoundariesMismatch(expected: BucketBoundaries, actual: BucketBoundaries)
        extends Mismatch {
      def message: String =
        s"boundaries mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class CountsMismatch(expected: List[Long], actual: List[Long]) extends Mismatch {
      def message: String =
        s"counts mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class ScaleMismatch(expected: Int, actual: Int) extends Mismatch {
      def message: String =
        s"scale mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class ZeroCountMismatch(expected: Long, actual: Long) extends Mismatch {
      def message: String =
        s"zero count mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class AttributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"attributes mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[metrics] final case class PredicateMismatch(clue: String) extends Mismatch {
      def message: String =
        s"predicate mismatch: $clue"
    }
  }

  /** A point expectation for numeric points. */
  sealed trait Numeric[A] extends PointExpectation {

    /** Requires the point value to match exactly. */
    def value(value: A): Numeric[A]

    /** Adds a custom predicate over the numeric point data. */
    def where(f: NumericPointData[A] => Boolean): Numeric[A]

    /** Adds a custom predicate over the numeric point data with a clue shown in mismatches. */
    def where(clue: String)(f: NumericPointData[A] => Boolean): Numeric[A]
    def attributes(expectation: AttributesExpectation): Numeric[A]
    def attributesExact(attributes: Attributes): Numeric[A]
    def attributesExact(attributes: Attribute[_]*): Numeric[A]
    def attributesEmpty: Numeric[A]
    def attributesSubset(attributes: Attributes): Numeric[A]
    def attributesSubset(attributes: Attribute[_]*): Numeric[A]
    def clue(text: String): Numeric[A]
  }

  /** A point expectation for summary points. */
  sealed trait Summary extends PointExpectation {

    /** Requires the summary point count to match exactly. */
    def count(count: Long): Summary

    /** Requires the summary point sum to match exactly. */
    def sum(sum: Double): Summary

    /** Adds a custom predicate over summary point data. */
    def where(f: JSummaryPointData => Boolean): Summary

    /** Adds a custom predicate over summary point data with a clue shown in mismatches. */
    def where(clue: String)(f: JSummaryPointData => Boolean): Summary
    def attributes(expectation: AttributesExpectation): Summary
    def attributesExact(attributes: Attributes): Summary
    def attributesExact(attributes: Attribute[_]*): Summary
    def attributesEmpty: Summary
    def attributesSubset(attributes: Attributes): Summary
    def attributesSubset(attributes: Attribute[_]*): Summary
    def clue(text: String): Summary
  }

  /** A point expectation for histogram points. */
  sealed trait Histogram extends PointExpectation {

    /** Requires the histogram point count to match exactly. */
    def count(count: Long): Histogram

    /** Requires the histogram point sum to match exactly. */
    def sum(sum: Double): Histogram

    /** Requires the histogram bucket boundaries to match exactly. */
    def boundaries(boundaries: BucketBoundaries): Histogram

    /** Requires the histogram bucket counts to match exactly. */
    def counts(counts: List[Long]): Histogram

    /** Requires the histogram bucket counts to match exactly. */
    def counts(counts: Long*): Histogram

    /** Adds a custom predicate over histogram point data. */
    def where(f: JHistogramPointData => Boolean): Histogram

    /** Adds a custom predicate over histogram point data with a clue shown in mismatches. */
    def where(clue: String)(f: JHistogramPointData => Boolean): Histogram
    def attributes(expectation: AttributesExpectation): Histogram
    def attributesExact(attributes: Attributes): Histogram
    def attributesExact(attributes: Attribute[_]*): Histogram
    def attributesEmpty: Histogram
    def attributesSubset(attributes: Attributes): Histogram
    def attributesSubset(attributes: Attribute[_]*): Histogram
    def clue(text: String): Histogram
  }

  /** A point expectation for exponential histogram points. */
  sealed trait ExponentialHistogram extends PointExpectation {

    /** Requires the exponential histogram scale to match exactly. */
    def scale(scale: Int): ExponentialHistogram

    /** Requires the exponential histogram count to match exactly. */
    def count(count: Long): ExponentialHistogram

    /** Requires the exponential histogram sum to match exactly. */
    def sum(sum: Double): ExponentialHistogram

    /** Requires the exponential histogram zero count to match exactly. */
    def zeroCount(zeroCount: Long): ExponentialHistogram

    /** Adds a custom predicate over exponential histogram point data. */
    def where(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram

    /** Adds a custom predicate over exponential histogram point data with a clue shown in mismatches. */
    def where(clue: String)(f: JExponentialHistogramPointData => Boolean): ExponentialHistogram
    def attributes(expectation: AttributesExpectation): ExponentialHistogram
    def attributesExact(attributes: Attributes): ExponentialHistogram
    def attributesExact(attributes: Attribute[_]*): ExponentialHistogram
    def attributesEmpty: ExponentialHistogram
    def attributesSubset(attributes: Attributes): ExponentialHistogram
    def attributesSubset(attributes: Attribute[_]*): ExponentialHistogram
    def clue(text: String): ExponentialHistogram
  }

  /** Creates an expectation for a numeric point with the given value. */
  def numeric[A: MeasurementValue: NumberComparison](value: A): Numeric[A] =
    NumericImpl(
      expectedValue = Some(value),
      valueType = MeasurementValue[A],
      numberComparison = NumberComparison[A]
    )

  /** Creates an expectation for a summary point. */
  def summary(implicit cmp: NumberComparison[Double]): Summary =
    SummaryImpl(doubleComparison = cmp)

  /** Creates an expectation for a summary point with exact sum and count expectations. */
  def summary(sum: Double, count: Long)(implicit cmp: NumberComparison[Double]): Summary =
    SummaryImpl(doubleComparison = cmp, expectedSum = Some(sum), expectedCount = Some(count))

  /** Creates an expectation for a histogram point. */
  def histogram(implicit cmp: NumberComparison[Double]): Histogram =
    HistogramImpl(doubleComparison = cmp)

  /** Creates an expectation for a histogram point with exact sum, count, boundaries, and counts expectations. */
  def histogram(
      sum: Double,
      count: Long,
      boundaries: BucketBoundaries,
      counts: List[Long]
  )(implicit cmp: NumberComparison[Double]): Histogram =
    HistogramImpl(
      doubleComparison = cmp,
      expectedSum = Some(sum),
      expectedCount = Some(count),
      expectedBoundaries = Some(boundaries),
      expectedCounts = Some(counts)
    )

  /** Creates an expectation for an exponential histogram point. */
  def exponentialHistogram(implicit cmp: NumberComparison[Double]): ExponentialHistogram =
    ExponentialHistogramImpl(doubleComparison = cmp)

  /** Creates an expectation for an exponential histogram point with exact scale, sum, count, and zero count. */
  def exponentialHistogram(
      scale: Int,
      sum: Double,
      count: Long,
      zeroCount: Long
  )(implicit cmp: NumberComparison[Double]): ExponentialHistogram =
    ExponentialHistogramImpl(
      doubleComparison = cmp,
      expectedScale = Some(scale),
      expectedSum = Some(sum),
      expectedCount = Some(count),
      expectedZeroCount = Some(zeroCount)
    )

  private final case class NumericImpl[A](
      expectedValue: Option[A] = None,
      valueType: MeasurementValue[A],
      numberComparison: NumberComparison[A],
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(NumericPointData[A] => Boolean, Option[String])] = Nil
  ) extends Numeric[A] {
    def value(value: A): Numeric[A] =
      copy(expectedValue = Some(value))

    def attributes(expectation: AttributesExpectation): Numeric[A] =
      copy(attributeExpectation = Some(expectation))

    def attributesExact(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))

    def attributesExact(attributes: Attribute[_]*): Numeric[A] =
      attributesExact(Attributes(attributes *))

    def attributesEmpty: Numeric[A] =
      attributesExact(Attributes.empty)

    def attributesSubset(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))

    def attributesSubset(attributes: Attribute[_]*): Numeric[A] =
      attributesSubset(Attributes(attributes *))

    def clue(text: String): Numeric[A] =
      copy(clue = Some(text))

    def where(f: NumericPointData[A] => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: NumericPointData[A] => Boolean): Numeric[A] =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      toNumericPointData(valueType, point) match {
        case Right(numericPoint) =>
          ExpectationChecks.combine(
            expectedValue.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (numberComparison.equal(expected, numericPoint.value)) ExpectationChecks.success
              else {
                val expectedValue = numberComparison.render(expected)
                val actualValue = numberComparison.render(numericPoint.value)
                ExpectationChecks.mismatch(Mismatch.ValueMismatch(expectedValue, actualValue))
              }
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(expected.check(numericPoint.attributes))(Mismatch.AttributesMismatch)
            },
            ExpectationChecks.combine(
              predicates.map { case (predicate, clue) =>
                if (predicate(numericPoint)) ExpectationChecks.success
                else
                  ExpectationChecks.mismatch(
                    Mismatch.PredicateMismatch(clue.getOrElse("point predicate returned false"))
                  )
              }
            )
          )
        case Left(mismatch) =>
          ExpectationChecks.mismatch(mismatch)
      }
  }

  private final case class SummaryImpl(
      doubleComparison: NumberComparison[Double],
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JSummaryPointData => Boolean, Option[String])] = Nil
  ) extends Summary {
    def count(count: Long): Summary = copy(expectedCount = Some(count))
    def sum(sum: Double): Summary = copy(expectedSum = Some(sum))
    def attributes(expectation: AttributesExpectation): Summary = copy(attributeExpectation = Some(expectation))
    def attributesExact(attributes: Attributes): Summary =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))
    def attributesExact(attributes: Attribute[_]*): Summary = attributesExact(Attributes(attributes *))
    def attributesEmpty: Summary = attributesExact(Attributes.empty)
    def attributesSubset(attributes: Attributes): Summary =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))
    def attributesSubset(attributes: Attribute[_]*): Summary = attributesSubset(Attributes(attributes *))
    def clue(text: String): Summary = copy(clue = Some(text))
    def where(f: JSummaryPointData => Boolean): Summary = copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: JSummaryPointData => Boolean): Summary =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      point match {
        case summary: JSummaryPointData =>
          ExpectationChecks.combine(
            expectedSum.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (doubleComparison.equal(expected, summary.getSum)) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.SumMismatch(expected, summary.getSum))
            },
            expectedCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == summary.getCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountMismatch(expected, summary.getCount))
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(expected.check(summary.getAttributes.toScala))(Mismatch.AttributesMismatch)
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
      doubleComparison: NumberComparison[Double],
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedBoundaries: Option[BucketBoundaries] = None,
      expectedCounts: Option[List[Long]] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JHistogramPointData => Boolean, Option[String])] = Nil
  ) extends Histogram {
    def count(count: Long): Histogram = copy(expectedCount = Some(count))
    def sum(sum: Double): Histogram = copy(expectedSum = Some(sum))
    def boundaries(boundaries: BucketBoundaries): Histogram = copy(expectedBoundaries = Some(boundaries))
    def counts(expected: List[Long]): Histogram = copy(expectedCounts = Some(expected))
    def counts(expected: Long*): Histogram = counts(expected.toList)
    def attributes(expectation: AttributesExpectation): Histogram = copy(attributeExpectation = Some(expectation))
    def attributesExact(attributes: Attributes): Histogram =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))
    def attributesExact(attributes: Attribute[_]*): Histogram = attributesExact(Attributes(attributes *))
    def attributesEmpty: Histogram = attributesExact(Attributes.empty)
    def attributesSubset(attributes: Attributes): Histogram =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))
    def attributesSubset(attributes: Attribute[_]*): Histogram = attributesSubset(Attributes(attributes *))
    def clue(text: String): Histogram = copy(clue = Some(text))
    def where(f: JHistogramPointData => Boolean): Histogram = copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: JHistogramPointData => Boolean): Histogram =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(point: JPointData): Either[NonEmptyList[Mismatch], Unit] =
      point match {
        case histogram: JHistogramPointData =>
          ExpectationChecks.combine(
            expectedSum.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (doubleComparison.equal(expected, histogram.getSum)) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.SumMismatch(expected, histogram.getSum))
            },
            expectedCount.fold(ExpectationChecks.success[Mismatch]) { expected =>
              if (expected == histogram.getCount) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountMismatch(expected, histogram.getCount))
            },
            expectedBoundaries.fold(ExpectationChecks.success[Mismatch]) { expected =>
              val actual = BucketBoundaries(histogram.getBoundaries.asScala.toVector.map(_.doubleValue()))
              if (
                expected.boundaries.length == actual.boundaries.length &&
                expected.boundaries.zip(actual.boundaries).forall { case (expectedValue, actualValue) =>
                  doubleComparison.equal(expectedValue, actualValue)
                }
              ) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.BoundariesMismatch(expected, actual))
            },
            expectedCounts.fold(ExpectationChecks.success[Mismatch]) { expected =>
              val actual = histogram.getCounts.asScala.toList.map(_.longValue())
              if (expected == actual) ExpectationChecks.success
              else ExpectationChecks.mismatch(Mismatch.CountsMismatch(expected, actual))
            },
            attributeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
              ExpectationChecks.nested(expected.check(histogram.getAttributes.toScala))(Mismatch.AttributesMismatch)
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
      doubleComparison: NumberComparison[Double],
      expectedScale: Option[Int] = None,
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedZeroCount: Option[Long] = None,
      attributeExpectation: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JExponentialHistogramPointData => Boolean, Option[String])] = Nil
  ) extends ExponentialHistogram {
    def scale(scale: Int): ExponentialHistogram = copy(expectedScale = Some(scale))
    def count(count: Long): ExponentialHistogram = copy(expectedCount = Some(count))
    def sum(sum: Double): ExponentialHistogram = copy(expectedSum = Some(sum))
    def zeroCount(zeroCount: Long): ExponentialHistogram = copy(expectedZeroCount = Some(zeroCount))
    def attributes(expectation: AttributesExpectation): ExponentialHistogram =
      copy(attributeExpectation = Some(expectation))
    def attributesExact(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = Some(AttributesExpectation.exact(attributes)))
    def attributesExact(attributes: Attribute[_]*): ExponentialHistogram =
      attributesExact(Attributes(attributes *))
    def attributesEmpty: ExponentialHistogram = attributesExact(Attributes.empty)
    def attributesSubset(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = Some(AttributesExpectation.subset(attributes)))
    def attributesSubset(attributes: Attribute[_]*): ExponentialHistogram =
      attributesSubset(Attributes(attributes *))
    def clue(text: String): ExponentialHistogram = copy(clue = Some(text))
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
              if (doubleComparison.equal(expected, histogram.getSum)) ExpectationChecks.success
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
              ExpectationChecks.nested(expected.check(histogram.getAttributes.toScala))(Mismatch.AttributesMismatch)
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

  private[metrics] def toNumericPointData[A](
      valueType: MeasurementValue[A],
      point: JPointData
  ): Either[Mismatch, NumericPointData[A]] =
    valueType match {
      case _: LongMeasurementValue[_] =>
        point match {
          case long: LongPointData =>
            Right(LongNumericPointData(long).asInstanceOf[NumericPointData[A]])
          case other =>
            Left(Mismatch.TypeMismatch("LongPointData", other.getClass.getSimpleName))
        }
      case _: DoubleMeasurementValue[_] =>
        point match {
          case double: DoublePointData =>
            Right(DoubleNumericPointData(double).asInstanceOf[NumericPointData[A]])
          case other =>
            Left(Mismatch.TypeMismatch("DoublePointData", other.getClass.getSimpleName))
        }
    }

  private[metrics] final case class LongNumericPointData(underlying: LongPointData) extends NumericPointData[Long] {
    def value: Long = underlying.getValue
    def attributes: Attributes = underlying.getAttributes.toScala
  }

  private[metrics] final case class DoubleNumericPointData(
      underlying: DoublePointData
  ) extends NumericPointData[Double] {
    def value: Double = underlying.getValue
    def attributes: Attributes = underlying.getAttributes.toScala
  }
}
