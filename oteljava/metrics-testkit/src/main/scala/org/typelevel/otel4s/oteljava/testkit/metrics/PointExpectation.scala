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

  /** Requires the point value to match exactly. */
  def withValue(value: A): PointExpectation[A]

  /** Requires the point attributes to match exactly. */
  def withAttributes(attributes: Attributes): PointExpectation[A]

  /** Requires the point attributes to contain the given attributes. */
  def withAttributesSubset(attributes: Attributes): PointExpectation[A]

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): PointExpectation[A]

  /** Adds a custom point predicate to this expectation. */
  def where(f: JPointData => Boolean, clue: String): PointExpectation[A]

  private[metrics] def matches(point: JPointData): Boolean
}

object PointExpectation {

  /** Creates an expectation that matches any point of type `A`. */
  def any[A]: PointExpectation[A] =
    Impl[A]()

  /** Creates an expectation for a point with the given value. */
  def value[A](value: A): PointExpectation[A] =
    any[A].withValue(value)

  /** Creates an expectation for a summary point using selected summary fields. */
  def summary(
      sum: Option[Double] = None,
      count: Option[Long] = None
  ): PointExpectation[JSummaryPointData] =
    any[JSummaryPointData].where(
      {
        case summary: JSummaryPointData =>
          sum.forall(_ == summary.getSum) &&
          count.forall(_ == summary.getCount)
        case _ =>
          false
      },
      clue = s"summary(sum=$sum, count=$count)"
    )

  /** Creates an expectation for a histogram point using selected histogram fields. */
  def histogram(
      sum: Option[Double] = None,
      count: Option[Long] = None,
      boundaries: Option[List[Double]] = None,
      counts: Option[List[Long]] = None
  ): PointExpectation[JHistogramPointData] =
    any[JHistogramPointData].where(
      {
        case histogram: JHistogramPointData =>
          sum.forall(_ == histogram.getSum) &&
          count.forall(_ == histogram.getCount) &&
          boundaries.forall(_ == histogram.getBoundaries.asScala.toList.map(_.doubleValue())) &&
          counts.forall(_ == histogram.getCounts.asScala.toList.map(_.longValue()))
        case _ =>
          false
      },
      clue = s"histogram(sum=$sum, count=$count, boundaries=$boundaries, counts=$counts)"
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

  private final case class Impl[A](
      expectedValue: Option[A] = None,
      attributeExpectation: AttributeExpectation = AttributeExpectation.Any,
      clue: Option[String] = None,
      predicates: List[(JPointData => Boolean, String)] = Nil
  ) extends PointExpectation[A] {

    def withAttributes(attributes: Attributes): PointExpectation[A] =
      copy(attributeExpectation = AttributeExpectation.Exact(attributes))

    def withAttributesSubset(attributes: Attributes): PointExpectation[A] =
      copy(attributeExpectation = AttributeExpectation.Subset(attributes))

    def clue(text: String): PointExpectation[A] =
      copy(clue = Some(text))

    def withValue(value: A): PointExpectation[A] =
      copy(expectedValue = Some(value))

    def where(f: JPointData => Boolean, clue: String): PointExpectation[A] =
      copy(predicates = predicates :+ (f -> clue))

    def matches(point: JPointData): Boolean =
      expectedValue.forall(matchesValue(_, point)) &&
        attributeExpectation.matches(point.getAttributes.toScala) &&
        predicates.forall { case (predicate, _) => predicate(point) }

    private def matchesValue(expected: A, point: JPointData): Boolean =
      (expected, point) match {
        case (value: Long, long: LongPointData) =>
          value == long.getValue
        case (value: Double, double: io.opentelemetry.sdk.metrics.data.DoublePointData) =>
          value == double.getValue
        case (value: JSummaryPointData, summary: JSummaryPointData) =>
          value == summary
        case (value: JHistogramPointData, histogram: JHistogramPointData) =>
          value == histogram
        case _ =>
          false
      }
  }
}
