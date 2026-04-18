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
import cats.syntax.functor._
import io.opentelemetry.sdk.metrics.data.{ExponentialHistogramPointData => JExponentialHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}
import org.typelevel.otel4s.oteljava.testkit.CollectionExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.LogicalOperator
import org.typelevel.otel4s.oteljava.testkit.MaximumMatching

/** A partial expectation over a collection of metric points.
  *
  * `PointSetExpectation` is the collection-level layer that sits between individual point expectations and full metric
  * expectations. It can express existential, universal, cardinality, and exact-set constraints over a metric's points.
  */
sealed trait PointSetExpectation[P] {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Combines this expectation with another one using logical conjunction. */
  def and(other: PointSetExpectation[P]): PointSetExpectation[P] =
    PointSetExpectation.and(this, other)

  /** Combines this expectation with another one using logical disjunction. */
  def or(other: PointSetExpectation[P]): PointSetExpectation[P] =
    PointSetExpectation.or(this, other)

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): PointSetExpectation[P]

  private[metrics] def check(points: List[P]): Either[NonEmptyList[PointSetExpectation.Mismatch], Unit]
}

object PointSetExpectation {

  /** A structured reason explaining why a [[PointSetExpectation]] did not match a collection of points. */
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {
    private[metrics] final case class PointCountMismatch(expected: Int, actual: Int) extends Mismatch {
      def message: String =
        s"point count mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class MinimumPointCountMismatch(expectedAtLeast: Int, actual: Int) extends Mismatch {
      def message: String =
        s"point count mismatch: expected at least $expectedAtLeast, got $actual"
    }

    private[metrics] final case class MaximumPointCountMismatch(expectedAtMost: Int, actual: Int) extends Mismatch {
      def message: String =
        s"point count mismatch: expected at most $expectedAtMost, got $actual"
    }

    private[metrics] final case class MatchedPointCountMismatch(expected: Int, actual: Int) extends Mismatch {
      def message: String =
        s"matched point count mismatch: expected $expected, got $actual"
    }

    private[metrics] final case class MissingExpectedPoint(
        clue: Option[String],
        mismatches: NonEmptyList[PointExpectation.Mismatch]
    ) extends Mismatch {
      def message: String = {
        val prefix = clue.fold("")(value => s" [$value]")
        s"missing expected point$prefix: ${mismatches.toList.map(_.message).mkString(", ")}"
      }
    }

    private[metrics] final case class UnexpectedPoint(index: Int) extends Mismatch {
      def message: String =
        s"unexpected point at index $index"
    }

    private[metrics] final case class FailingPoint(index: Int, mismatches: NonEmptyList[PointExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"failing point at index $index: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[metrics] final case class PredicateFailed(clue: Option[String]) extends Mismatch {
      def message: String =
        s"point set predicate returned false${clue.fold("")(value => s": $value")}"
    }

    private[metrics] case object NoPointsCollected extends Mismatch {
      def message: String =
        "no points were collected"
    }

    private[metrics] final case class CompositeMismatch(
        operator: LogicalOperator,
        mismatches: NonEmptyList[Mismatch]
    ) extends Mismatch {
      def message: String =
        s"${operator.render} mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[metrics] final case class CluedMismatch(
        clue: String,
        mismatches: NonEmptyList[Mismatch]
    ) extends Mismatch {
      def message: String =
        s"point-set mismatch [$clue]: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
  }

  /** Type class used to evaluate a single point expectation against a collected point of the corresponding type. */
  trait SinglePointChecker[E] {
    type P
    def clue(expectation: E): Option[String]
    def check(expectation: E, point: P): Either[NonEmptyList[PointExpectation.Mismatch], Unit]
  }

  object SinglePointChecker {

    /** A version of [[SinglePointChecker]] with the collected point type fixed to `P0`. */
    type Aux[E, P0] = SinglePointChecker[E] { type P = P0 }

    implicit def numeric[A]: Aux[PointExpectation.Numeric[A], PointExpectation.NumericPointData[A]] =
      new SinglePointChecker[PointExpectation.Numeric[A]] {
        type P = PointExpectation.NumericPointData[A]

        def clue(expectation: PointExpectation.Numeric[A]): Option[String] =
          expectation.clue

        def check(
            expectation: PointExpectation.Numeric[A],
            point: PointExpectation.NumericPointData[A]
        ): Either[NonEmptyList[PointExpectation.Mismatch], Unit] =
          expectation.check(point.underlying)
      }

    implicit val summary: Aux[PointExpectation.Summary, JSummaryPointData] =
      new SinglePointChecker[PointExpectation.Summary] {
        type P = JSummaryPointData
        def clue(expectation: PointExpectation.Summary): Option[String] = expectation.clue
        def check(
            expectation: PointExpectation.Summary,
            point: JSummaryPointData
        ): Either[NonEmptyList[PointExpectation.Mismatch], Unit] =
          expectation.check(point)
      }

    implicit val histogram: Aux[PointExpectation.Histogram, JHistogramPointData] =
      new SinglePointChecker[PointExpectation.Histogram] {
        type P = JHistogramPointData
        def clue(expectation: PointExpectation.Histogram): Option[String] = expectation.clue
        def check(
            expectation: PointExpectation.Histogram,
            point: JHistogramPointData
        ): Either[NonEmptyList[PointExpectation.Mismatch], Unit] =
          expectation.check(point)
      }

    implicit val exponentialHistogram: Aux[PointExpectation.ExponentialHistogram, JExponentialHistogramPointData] =
      new SinglePointChecker[PointExpectation.ExponentialHistogram] {
        type P = JExponentialHistogramPointData
        def clue(expectation: PointExpectation.ExponentialHistogram): Option[String] = expectation.clue
        def check(
            expectation: PointExpectation.ExponentialHistogram,
            point: JExponentialHistogramPointData
        ): Either[NonEmptyList[PointExpectation.Mismatch], Unit] =
          expectation.check(point)
      }
  }

  /** Creates an expectation that matches any point collection. */
  def any[P]: PointSetExpectation[P] =
    AnyImpl(None)

  /** Creates an expectation that requires at least one collected point to match the given point expectation. */
  def exists[E](point: E)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    ExistsImpl(point, checker, None)

  /** Creates an expectation that requires every collected point to match the given point expectation. */
  def forall[E](point: E)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    ForAllImpl(point, checker, None)

  /** Creates an expectation that requires the point collection to contain distinct matches for all given expectations.
    */
  def contains[E](first: E, rest: E*)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    ContainsImpl(NonEmptyList(first, rest.toList), checker, None)

  /** Creates an expectation that requires the point collection to match the given point expectations exactly, with no
    * extra points.
    */
  def exactly[E](first: E, rest: E*)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    ExactlyImpl(NonEmptyList(first, rest.toList), checker, None)

  /** Creates an expectation that requires the point collection to have exactly the given size. */
  def count[P](expected: Int): PointSetExpectation[P] =
    CountImpl(expected, None)

  /** Creates an expectation that requires the point collection to have at least the given size. */
  def minCount[P](expectedAtLeast: Int): PointSetExpectation[P] =
    MinCountImpl(expectedAtLeast, None)

  /** Creates an expectation that requires the point collection to have at most the given size. */
  def maxCount[P](expectedAtMost: Int): PointSetExpectation[P] =
    MaxCountImpl(expectedAtMost, None)

  /** Creates an expectation that requires exactly the given number of collected points to match the point expectation.
    */
  def countWhere[E](point: E, expected: Int)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    CountWhereImpl(point, expected, checker, None)

  /** Creates an expectation that requires no collected point to match the given point expectation. */
  def none[E](point: E)(implicit checker: SinglePointChecker[E]): PointSetExpectation[checker.P] =
    NoneOfImpl(point, checker, None)

  /** Creates an expectation from a custom predicate over the entire point collection. */
  def predicate[P](f: List[P] => Boolean): PointSetExpectation[P] =
    PredicateImpl(f, None)

  /** Creates an expectation from a custom predicate over the entire point collection with an explanatory clue. */
  def predicate[P](clue: String)(f: List[P] => Boolean): PointSetExpectation[P] =
    PredicateImpl(f, Some(clue))

  /** Combines two point-set expectations using logical conjunction. */
  def and[P](left: PointSetExpectation[P], right: PointSetExpectation[P]): PointSetExpectation[P] =
    CompositeImpl(left, right, LogicalOperator.And, None)

  /** Combines two point-set expectations using logical disjunction. */
  def or[P](left: PointSetExpectation[P], right: PointSetExpectation[P]): PointSetExpectation[P] =
    CompositeImpl(left, right, LogicalOperator.Or, None)

  private final case class AnyImpl[P](clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] = ExpectationChecks.success
  }

  private final case class ExistsImpl[E, P](
      point: E,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (points.exists(point => checker.check(this.point, point).isRight)) ExpectationChecks.success
        else {
          val mismatch = Mismatch.MissingExpectedPoint(
            checker.clue(point),
            closestMismatch(points, point, checker)
          )
          ExpectationChecks.mismatch(mismatch)
        }
      }
  }

  private final case class ForAllImpl[E, P](
      point: E,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (points.isEmpty) {
          ExpectationChecks.mismatch(Mismatch.NoPointsCollected)
        } else {
          val firstMismatch = CollectionExpectationChecks.firstFailingIndex(points, point)(
            (expectation, actual) => checker.check(expectation, actual),
            (index, mismatches) => Mismatch.FailingPoint(index, mismatches)
          )

          firstMismatch match {
            case Some(mismatch) => ExpectationChecks.mismatch(mismatch)
            case None           => ExpectationChecks.success
          }
        }
      }
  }

  private final case class ContainsImpl[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue)(containsCheck(expected, checker, points).void)
  }

  private final case class ExactlyImpl[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        containsCheck(expected, checker, points).flatMap { matchedIndices =>
          val unexpected = points.indices.filterNot(matchedIndices.contains).map(Mismatch.UnexpectedPoint(_)).toList
          NonEmptyList.fromList(unexpected).toLeft(())
        }
      }
  }

  private final case class CountImpl[P](expected: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (points.length == expected) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.PointCountMismatch(expected, points.length))
      }
  }

  private final case class MinCountImpl[P](expectedAtLeast: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (points.length >= expectedAtLeast) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MinimumPointCountMismatch(expectedAtLeast, points.length))
      }
  }

  private final case class MaxCountImpl[P](expectedAtMost: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (points.length <= expectedAtMost) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MaximumPointCountMismatch(expectedAtMost, points.length))
      }
  }

  private final case class CountWhereImpl[E, P](
      point: E,
      expected: Int,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        val actual = points.count(point => checker.check(this.point, point).isRight)
        if (actual == expected) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MatchedPointCountMismatch(expected, actual))
      }
  }

  private final case class NoneOfImpl[E, P](
      point: E,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        val firstMatch = CollectionExpectationChecks.firstMatchingIndex(points, point)((expectation, actual) =>
          checker.check(expectation, actual).isRight
        )

        firstMatch match {
          case None        => ExpectationChecks.success
          case Some(index) => ExpectationChecks.mismatch(Mismatch.UnexpectedPoint(index))
        }
      }
  }

  private final case class PredicateImpl[P](
      f: List[P] => Boolean,
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue)(Either.cond(f(points), (), NonEmptyList.one(Mismatch.PredicateFailed(clue))))
  }

  private final case class CompositeImpl[P](
      left: PointSetExpectation[P],
      right: PointSetExpectation[P],
      operator: LogicalOperator,
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        CollectionExpectationChecks.compositeCheck(operator, left.check(points), right.check(points)) { mismatches =>
          Mismatch.CompositeMismatch(operator, mismatches)
        }
      }
  }

  private def withClueContext(clue: Option[String])(
      result: Either[NonEmptyList[Mismatch], Unit]
  ): Either[NonEmptyList[Mismatch], Unit] =
    CollectionExpectationChecks.withClueContext(clue, result) { (clue, mismatches) =>
      Mismatch.CluedMismatch(clue, mismatches)
    }

  private def closestMismatch[E, P](
      points: List[P],
      expectation: E,
      checker: SinglePointChecker.Aux[E, P]
  ): NonEmptyList[PointExpectation.Mismatch] =
    CollectionExpectationChecks.closestMismatch(points, expectation)(
      (expected, actual) => checker.check(expected, actual),
      PointExpectation.Mismatch.PredicateMismatch("no points were collected")
    )

  private def containsCheck[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      points: List[P]
  ): Either[NonEmptyList[Mismatch], Set[Int]] = {
    val indexedPoints = points.toVector
    val candidates = expected.toList.map { expectation =>
      indexedPoints.indices.filter(index => checker.check(expectation, indexedPoints(index)).isRight).toList
    }
    val matching = MaximumMatching.find(candidates.toVector)

    if (matching.isComplete) Right(matching.matchedCandidateIndices)
    else {
      val missing = expected.toList.zip(candidates).collect { case (expectation, Nil) =>
        Mismatch.MissingExpectedPoint(checker.clue(expectation), closestMismatch(points, expectation, checker))
      }

      Left(
        NonEmptyList
          .fromList(missing)
          .getOrElse(
            NonEmptyList.one(Mismatch.MatchedPointCountMismatch(expected.length, matching.size))
          )
      )
    }
  }

}
