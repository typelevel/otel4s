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

import io.opentelemetry.sdk.metrics.data.{ExponentialHistogramPointData => JExponentialHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{LongPointData, PointData => JPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

import scala.jdk.CollectionConverters._

/** A partial expectation for a single OpenTelemetry Java point.
  *
  * `PointExpectation` is used together with [[MetricExpectation.Numeric]] and [[MetricExpectation.Points]] to express
  * which metric points should be present. Unspecified properties are ignored.
  *
  * Attribute matching is done with `otel4s` [[Attributes]]:
  *   - [[withAttributes]] requires exact equality
  *   - [[withAttributesSubset]] requires the expected attributes to be contained in the point
  */
sealed trait PointExpectation[A] {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the point attributes to match exactly. */
  def withAttributes(attributes: Attributes): PointExpectation[A]

  /** Requires the point attributes to contain the given attributes. */
  def withAttributesSubset(attributes: Attributes): PointExpectation[A]

  /** Attaches a human-readable clue to this expectation. */
  def withClue(text: String): PointExpectation[A]

  /** Adds a custom point predicate to this expectation. */
  def where(f: JPointData => Boolean, clue: String): PointExpectation[A]

  private[metrics] def matches(point: JPointData): Boolean
}

object PointExpectation {

  /** A point expectation for numeric points. */
  sealed trait Numeric[A] extends PointExpectation[A] {
    /** Requires the point value to match exactly. */
    def withValue(value: A): Numeric[A]

    override def withAttributes(attributes: Attributes): Numeric[A]

    override def withAttributesSubset(attributes: Attributes): Numeric[A]

    override def withClue(text: String): Numeric[A]

    override def where(f: JPointData => Boolean, clue: String): Numeric[A]
  }

  /** A point expectation for summary points. */
  sealed trait Summary extends PointExpectation[JSummaryPointData] {
    def withCount(count: Long): Summary

    def withSum(sum: Double): Summary

    override def withAttributes(attributes: Attributes): Summary

    override def withAttributesSubset(attributes: Attributes): Summary

    override def withClue(text: String): Summary

    override def where(f: JPointData => Boolean, clue: String): Summary
  }

  /** A point expectation for histogram points. */
  sealed trait Histogram extends PointExpectation[JHistogramPointData] {
    def withCount(count: Long): Histogram

    def withSum(sum: Double): Histogram

    def withBoundaries(boundaries: List[Double]): Histogram

    def withCounts(counts: List[Long]): Histogram

    override def withAttributes(attributes: Attributes): Histogram

    override def withAttributesSubset(attributes: Attributes): Histogram

    override def withClue(text: String): Histogram

    override def where(f: JPointData => Boolean, clue: String): Histogram
  }

  /** A point expectation for exponential histogram points. */
  sealed trait ExponentialHistogram extends PointExpectation[JExponentialHistogramPointData] {
    def withScale(scale: Int): ExponentialHistogram

    def withCount(count: Long): ExponentialHistogram

    def withSum(sum: Double): ExponentialHistogram

    def withZeroCount(zeroCount: Long): ExponentialHistogram

    override def withAttributes(attributes: Attributes): ExponentialHistogram

    override def withAttributesSubset(attributes: Attributes): ExponentialHistogram

    override def withClue(text: String): ExponentialHistogram

    override def where(f: JPointData => Boolean, clue: String): ExponentialHistogram
  }

  /** Creates an expectation that matches any numeric point of type `A`. */
  def any[A]: Numeric[A] =
    NumericImpl[A]()

  /** Creates an expectation for a numeric point with the given value. */
  def value[A](value: A): Numeric[A] =
    NumericImpl[A](expectedValue = Some(value))

  /** Creates an expectation for a summary point. */
  def summary(
      sum: Option[Double] = None,
      count: Option[Long] = None
  ): Summary =
    SummaryImpl(
      expectedSum = sum,
      expectedCount = count
    )

  /** Creates an expectation for a histogram point. */
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

  /** Creates an expectation for an exponential histogram point. */
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

  private sealed trait AttributeExpectation {
    def matches(attributes: Attributes): Boolean
  }

  private object AttributeExpectation {
    case object Any extends AttributeExpectation {
      def matches(attributes: Attributes): Boolean = true
    }

    final case class Exact(expected: Attributes) extends AttributeExpectation {
      def matches(attributes: Attributes): Boolean =
        attributes == expected
    }

    final case class Subset(expected: Attributes) extends AttributeExpectation {
      def matches(attributes: Attributes): Boolean =
        expected.iterator.forall { attribute =>
          attributes.get(attribute.key).contains(attribute)
        }
    }
  }

  private sealed trait CommonImpl[A] extends PointExpectation[A] {
    def attributeExpectation: AttributeExpectation
    def clue: Option[String]
    def predicates: List[(JPointData => Boolean, String)]

    protected def copyCommon(
        attributeExpectation: AttributeExpectation = this.attributeExpectation,
        clue: Option[String] = this.clue,
        predicates: List[(JPointData => Boolean, String)] = this.predicates
    ): PointExpectation[A]

    def withAttributes(attributes: Attributes): PointExpectation[A] =
      copyCommon(attributeExpectation = AttributeExpectation.Exact(attributes))

    def withAttributesSubset(attributes: Attributes): PointExpectation[A] =
      copyCommon(attributeExpectation = AttributeExpectation.Subset(attributes))

    def withClue(text: String): PointExpectation[A] =
      copyCommon(clue = Some(text))

    def where(f: JPointData => Boolean, clue: String): PointExpectation[A] =
      copyCommon(predicates = predicates :+ (f -> clue))

    protected final def matchesCommon(point: JPointData): Boolean =
      attributeExpectation.matches(point.getAttributes.toScala) &&
        predicates.forall { case (predicate, _) => predicate(point) }
  }

  private final case class NumericImpl[A](
      expectedValue: Option[A] = None,
      attributeExpectation: AttributeExpectation = AttributeExpectation.Any,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, String)] = Nil
  ) extends Numeric[A]
      with CommonImpl[A] {

    protected def copyCommon(
        attributeExpectation: AttributeExpectation,
        clue: Option[String],
        predicates: List[(JPointData => Boolean, String)]
    ): Numeric[A] =
      copy(expectedValue, attributeExpectation, clue, predicates)

    def withValue(value: A): Numeric[A] =
      copy(expectedValue = Some(value))

    override def withAttributes(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = AttributeExpectation.Exact(attributes))

    override def withAttributesSubset(attributes: Attributes): Numeric[A] =
      copy(attributeExpectation = AttributeExpectation.Subset(attributes))

    override def withClue(text: String): Numeric[A] =
      copy(clue = Some(text))

    override def where(f: JPointData => Boolean, clue: String): Numeric[A] =
      copy(predicates = predicates :+ (f -> clue))

    def matches(point: JPointData): Boolean =
      expectedValue.forall(matchesValue(_, point)) &&
        matchesCommon(point)

    private def matchesValue(expected: A, point: JPointData): Boolean =
      (expected, point) match {
        case (value: Long, long: LongPointData) =>
          value == long.getValue
        case (value: Double, double: io.opentelemetry.sdk.metrics.data.DoublePointData) =>
          value == double.getValue
        case _ =>
          false
      }
  }

  private final case class SummaryImpl(
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      attributeExpectation: AttributeExpectation = AttributeExpectation.Any,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, String)] = Nil
  ) extends Summary
      with CommonImpl[JSummaryPointData] {

    protected def copyCommon(
        attributeExpectation: AttributeExpectation,
        clue: Option[String],
        predicates: List[(JPointData => Boolean, String)]
    ): Summary =
      copy(expectedSum, expectedCount, attributeExpectation, clue, predicates)

    def withCount(count: Long): Summary =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): Summary =
      copy(expectedSum = Some(sum))

    override def withAttributes(attributes: Attributes): Summary =
      copy(attributeExpectation = AttributeExpectation.Exact(attributes))

    override def withAttributesSubset(attributes: Attributes): Summary =
      copy(attributeExpectation = AttributeExpectation.Subset(attributes))

    override def withClue(text: String): Summary =
      copy(clue = Some(text))

    override def where(f: JPointData => Boolean, clue: String): Summary =
      copy(predicates = predicates :+ (f -> clue))

    def matches(point: JPointData): Boolean =
      point match {
        case summary: JSummaryPointData =>
          expectedSum.forall(_ == summary.getSum) &&
          expectedCount.forall(_ == summary.getCount) &&
          matchesCommon(summary)
        case _ =>
          false
      }
  }

  private final case class HistogramImpl(
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedBoundaries: Option[List[Double]] = None,
      expectedCounts: Option[List[Long]] = None,
      attributeExpectation: AttributeExpectation = AttributeExpectation.Any,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, String)] = Nil
  ) extends Histogram
      with CommonImpl[JHistogramPointData] {

    protected def copyCommon(
        attributeExpectation: AttributeExpectation,
        clue: Option[String],
        predicates: List[(JPointData => Boolean, String)]
    ): Histogram =
      copy(
        expectedSum,
        expectedCount,
        expectedBoundaries,
        expectedCounts,
        attributeExpectation,
        clue,
        predicates
      )

    def withCount(count: Long): Histogram =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): Histogram =
      copy(expectedSum = Some(sum))

    def withBoundaries(boundaries: List[Double]): Histogram =
      copy(expectedBoundaries = Some(boundaries))

    def withCounts(counts: List[Long]): Histogram =
      copy(expectedCounts = Some(counts))

    override def withAttributes(attributes: Attributes): Histogram =
      copy(attributeExpectation = AttributeExpectation.Exact(attributes))

    override def withAttributesSubset(attributes: Attributes): Histogram =
      copy(attributeExpectation = AttributeExpectation.Subset(attributes))

    override def withClue(text: String): Histogram =
      copy(clue = Some(text))

    override def where(f: JPointData => Boolean, clue: String): Histogram =
      copy(predicates = predicates :+ (f -> clue))

    def matches(point: JPointData): Boolean =
      point match {
        case histogram: JHistogramPointData =>
          expectedSum.forall(_ == histogram.getSum) &&
          expectedCount.forall(_ == histogram.getCount) &&
          expectedBoundaries.forall(_ == histogram.getBoundaries.asScala.toList.map(_.doubleValue())) &&
          expectedCounts.forall(_ == histogram.getCounts.asScala.toList.map(_.longValue())) &&
          matchesCommon(histogram)
        case _ =>
          false
      }
  }

  private final case class ExponentialHistogramImpl(
      expectedScale: Option[Int] = None,
      expectedSum: Option[Double] = None,
      expectedCount: Option[Long] = None,
      expectedZeroCount: Option[Long] = None,
      attributeExpectation: AttributeExpectation = AttributeExpectation.Any,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, String)] = Nil
  ) extends ExponentialHistogram
      with CommonImpl[JExponentialHistogramPointData] {

    protected def copyCommon(
        attributeExpectation: AttributeExpectation,
        clue: Option[String],
        predicates: List[(JPointData => Boolean, String)]
    ): ExponentialHistogram =
      copy(
        expectedScale,
        expectedSum,
        expectedCount,
        expectedZeroCount,
        attributeExpectation,
        clue,
        predicates
      )

    def withScale(scale: Int): ExponentialHistogram =
      copy(expectedScale = Some(scale))

    def withCount(count: Long): ExponentialHistogram =
      copy(expectedCount = Some(count))

    def withSum(sum: Double): ExponentialHistogram =
      copy(expectedSum = Some(sum))

    def withZeroCount(zeroCount: Long): ExponentialHistogram =
      copy(expectedZeroCount = Some(zeroCount))

    override def withAttributes(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = AttributeExpectation.Exact(attributes))

    override def withAttributesSubset(attributes: Attributes): ExponentialHistogram =
      copy(attributeExpectation = AttributeExpectation.Subset(attributes))

    override def withClue(text: String): ExponentialHistogram =
      copy(clue = Some(text))

    override def where(f: JPointData => Boolean, clue: String): ExponentialHistogram =
      copy(predicates = predicates :+ (f -> clue))

    def matches(point: JPointData): Boolean =
      point match {
        case histogram: JExponentialHistogramPointData =>
          expectedScale.forall(_ == histogram.getScale) &&
          expectedSum.forall(_ == histogram.getSum) &&
          expectedCount.forall(_ == histogram.getCount) &&
          expectedZeroCount.forall(_ == histogram.getZeroCount) &&
          matchesCommon(histogram)
        case _ =>
          false
      }
  }
}
