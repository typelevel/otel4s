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

    /** Indicates that the total number of collected points differed from the expected size. */
    sealed trait PointCountMismatch extends Mismatch {
      def expected: Int
      def actual: Int
    }

    /** Indicates that fewer points were collected than required. */
    sealed trait MinimumPointCountMismatch extends Mismatch {
      def expectedAtLeast: Int
      def actual: Int
    }

    /** Indicates that more points were collected than allowed. */
    sealed trait MaximumPointCountMismatch extends Mismatch {
      def expectedAtMost: Int
      def actual: Int
    }

    /** Indicates that the number of points matching a nested point expectation differed from the expected count. */
    sealed trait MatchedPointCountMismatch extends Mismatch {
      def expected: Int
      def actual: Int
    }

    /** Indicates that an expected point could not be matched. */
    sealed trait MissingExpectedPoint extends Mismatch {
      def clue: Option[String]
      def mismatches: NonEmptyList[PointExpectation.Mismatch]
    }

    /** Indicates that a point was present but not allowed by the expectation. */
    sealed trait UnexpectedPoint extends Mismatch {
      def index: Int
    }

    /** Indicates that a specific collected point failed a universal point constraint. */
    sealed trait FailingPoint extends Mismatch {
      def index: Int
      def mismatches: NonEmptyList[PointExpectation.Mismatch]
    }

    /** Indicates that a custom point-set predicate returned `false`. */
    sealed trait PredicateFailed extends Mismatch {
      def clue: Option[String]
    }

    /** Indicates that a non-empty point set was required but no points were collected. */
    sealed trait NoPointsCollected extends Mismatch

    /** Indicates that a composed expectation failed after combining nested mismatches with a logical operator. */
    sealed trait CompositeMismatch extends Mismatch {
      def operator: LogicalOperator
      def mismatches: NonEmptyList[Mismatch]
    }

    /** Indicates that a point-set expectation with an explicit clue failed. */
    sealed trait CluedMismatch extends Mismatch {
      def clue: String
      def mismatches: NonEmptyList[Mismatch]
    }

    /** Creates a mismatch indicating that the total point count differed from the expected size. */
    def pointCountMismatch(expected: Int, actual: Int): PointCountMismatch =
      PointCountMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that fewer points were collected than required. */
    def minimumPointCountMismatch(expectedAtLeast: Int, actual: Int): MinimumPointCountMismatch =
      MinimumPointCountMismatchImpl(expectedAtLeast, actual)

    /** Creates a mismatch indicating that more points were collected than allowed. */
    def maximumPointCountMismatch(expectedAtMost: Int, actual: Int): MaximumPointCountMismatch =
      MaximumPointCountMismatchImpl(expectedAtMost, actual)

    /** Creates a mismatch indicating that the number of matching points differed from the expected count. */
    def matchedPointCountMismatch(expected: Int, actual: Int): MatchedPointCountMismatch =
      MatchedPointCountMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that an expected point could not be matched. */
    def missingExpectedPoint(
        clue: Option[String],
        mismatches: NonEmptyList[PointExpectation.Mismatch]
    ): MissingExpectedPoint =
      MissingExpectedPointImpl(clue, mismatches)

    /** Creates a mismatch indicating that an unexpected point was present at the given index. */
    def unexpectedPoint(index: Int): UnexpectedPoint =
      UnexpectedPointImpl(index)

    /** Creates a mismatch indicating that the point at the given index failed a universal point constraint. */
    def failingPoint(index: Int, mismatches: NonEmptyList[PointExpectation.Mismatch]): FailingPoint =
      FailingPointImpl(index, mismatches)

    /** Creates a mismatch indicating that a custom point-set predicate returned `false`. */
    def predicateFailed(clue: Option[String]): PredicateFailed =
      PredicateFailedImpl(clue)

    /** Creates a mismatch indicating that no points were collected. */
    def noPointsCollected: NoPointsCollected =
      NoPointsCollectedImpl

    /** Creates a mismatch indicating that a composed point-set expectation failed. */
    def compositeMismatch(
        operator: LogicalOperator,
        mismatches: NonEmptyList[Mismatch]
    ): CompositeMismatch =
      CompositeMismatchImpl(operator, mismatches)

    /** Creates a mismatch indicating that a point-set expectation with a clue failed. */
    def cluedMismatch(clue: String, mismatches: NonEmptyList[Mismatch]): CluedMismatch =
      CluedMismatchImpl(clue, mismatches)

    private final case class PointCountMismatchImpl(expected: Int, actual: Int) extends PointCountMismatch {
      def message: String =
        s"point count mismatch: expected $expected, got $actual"
    }

    private final case class MinimumPointCountMismatchImpl(expectedAtLeast: Int, actual: Int)
        extends MinimumPointCountMismatch {
      def message: String =
        s"point count mismatch: expected at least $expectedAtLeast, got $actual"
    }

    private final case class MaximumPointCountMismatchImpl(expectedAtMost: Int, actual: Int)
        extends MaximumPointCountMismatch {
      def message: String =
        s"point count mismatch: expected at most $expectedAtMost, got $actual"
    }

    private final case class MatchedPointCountMismatchImpl(expected: Int, actual: Int)
        extends MatchedPointCountMismatch {
      def message: String =
        s"matched point count mismatch: expected $expected, got $actual"
    }

    private final case class MissingExpectedPointImpl(
        clue: Option[String],
        mismatches: NonEmptyList[PointExpectation.Mismatch]
    ) extends MissingExpectedPoint {
      def message: String = {
        val prefix = clue.fold("")(value => s" [$value]")
        s"missing expected point$prefix: ${mismatches.toList.map(_.message).mkString(", ")}"
      }
    }

    private final case class UnexpectedPointImpl(index: Int) extends UnexpectedPoint {
      def message: String =
        s"unexpected point at index $index"
    }

    private final case class FailingPointImpl(index: Int, mismatches: NonEmptyList[PointExpectation.Mismatch])
        extends FailingPoint {
      def message: String =
        s"failing point at index $index: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private final case class PredicateFailedImpl(clue: Option[String]) extends PredicateFailed {
      def message: String =
        s"point set predicate returned false${clue.fold("")(value => s": $value")}"
    }

    private case object NoPointsCollectedImpl extends NoPointsCollected {
      def message: String =
        "no points were collected"
    }

    private final case class CompositeMismatchImpl(
        operator: LogicalOperator,
        mismatches: NonEmptyList[Mismatch]
    ) extends CompositeMismatch {
      def message: String =
        s"${operator.render} mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private final case class CluedMismatchImpl(
        clue: String,
        mismatches: NonEmptyList[Mismatch]
    ) extends CluedMismatch {
      def message: String =
        s"point-set mismatch [$clue]: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
  }

  /** Logical operator used when combining point-set expectations. */
  sealed trait LogicalOperator extends Product with Serializable {
    def render: String
  }

  object LogicalOperator {

    /** Logical conjunction. */
    case object And extends LogicalOperator {
      val render: String = "and"
    }

    /** Logical disjunction. */
    case object Or extends LogicalOperator {
      val render: String = "or"
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
    AndImpl(left, right, None)

  /** Combines two point-set expectations using logical disjunction. */
  def or[P](left: PointSetExpectation[P], right: PointSetExpectation[P]): PointSetExpectation[P] =
    OrImpl(left, right, None)

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
      withClueContext(
        clue,
        if (points.exists(point => checker.check(this.point, point).isRight)) ExpectationChecks.success
        else {
          val mismatch = Mismatch.missingExpectedPoint(
            checker.clue(point),
            closestMismatch(points, point, checker)
          )
          ExpectationChecks.mismatch(mismatch)
        }
      )
  }

  private final case class ForAllImpl[E, P](
      point: E,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        if (points.isEmpty) ExpectationChecks.mismatch(Mismatch.noPointsCollected)
        else {
          points.zipWithIndex.collectFirst(Function.unlift { case (point, index) =>
            checker.check(this.point, point).left.toOption.map(Mismatch.failingPoint(index, _))
          }) match {
            case Some(mismatch) => ExpectationChecks.mismatch(mismatch)
            case None           => ExpectationChecks.success
          }
        }
      )
  }

  private final case class ContainsImpl[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue, containsCheck(expected, checker, points).void)
  }

  private final case class ExactlyImpl[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        containsCheck(expected, checker, points).flatMap { matchedIndices =>
          val unexpected = points.indices.filterNot(matchedIndices.contains).map(Mismatch.unexpectedPoint).toList
          NonEmptyList.fromList(unexpected).toLeft(())
        }
      )
  }

  private final case class CountImpl[P](expected: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        if (points.length == expected) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.pointCountMismatch(expected, points.length))
      )
  }

  private final case class MinCountImpl[P](expectedAtLeast: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        if (points.length >= expectedAtLeast) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.minimumPointCountMismatch(expectedAtLeast, points.length))
      )
  }

  private final case class MaxCountImpl[P](expectedAtMost: Int, clue: Option[String]) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        if (points.length <= expectedAtMost) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.maximumPointCountMismatch(expectedAtMost, points.length))
      )
  }

  private final case class CountWhereImpl[E, P](
      point: E,
      expected: Int,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue, {
          val actual = points.count(point => checker.check(this.point, point).isRight)
          if (actual == expected) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.matchedPointCountMismatch(expected, actual))
        }
      )
  }

  private final case class NoneOfImpl[E, P](
      point: E,
      checker: SinglePointChecker.Aux[E, P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        points.zipWithIndex.collectFirst(Function.unlift { case (point, index) =>
          checker.check(this.point, point).toOption.map(_ => Mismatch.unexpectedPoint(index))
        }) match {
          case Some(mismatch) => ExpectationChecks.mismatch(mismatch)
          case None           => ExpectationChecks.success
        }
      )
  }

  private final case class PredicateImpl[P](
      f: List[P] => Boolean,
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue, Either.cond(f(points), (), NonEmptyList.one(Mismatch.predicateFailed(clue))))
  }

  private final case class AndImpl[P](
      left: PointSetExpectation[P],
      right: PointSetExpectation[P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        (left.check(points), right.check(points)) match {
          case (Right(_), Right(_)) => Right(())
          case (Left(l), Right(_))  => Left(l)
          case (Right(_), Left(r))  => Left(r)
          case (Left(l), Left(r))   =>
            Left(NonEmptyList.one(Mismatch.compositeMismatch(LogicalOperator.And, l.concatNel(r))))
        }
      )
  }

  private final case class OrImpl[P](
      left: PointSetExpectation[P],
      right: PointSetExpectation[P],
      clue: Option[String]
  ) extends PointSetExpectation[P] {
    def clue(text: String): PointSetExpectation[P] = copy(clue = Some(text))
    def check(points: List[P]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(
        clue,
        (left.check(points), right.check(points)) match {
          case (Right(_), _) | (_, Right(_)) => Right(())
          case (Left(l), Left(r))            =>
            Left(NonEmptyList.one(Mismatch.compositeMismatch(LogicalOperator.Or, l.concatNel(r))))
        }
      )
  }

  private def withClueContext[P](
      clue: Option[String],
      result: Either[NonEmptyList[Mismatch], Unit]
  ): Either[NonEmptyList[Mismatch], Unit] =
    clue match {
      case Some(value) =>
        result.left.map(mismatches => NonEmptyList.one(Mismatch.cluedMismatch(value, mismatches)))
      case None =>
        result
    }

  private def closestMismatch[E, P](
      points: List[P],
      expectation: E,
      checker: SinglePointChecker.Aux[E, P]
  ): NonEmptyList[PointExpectation.Mismatch] =
    points
      .flatMap(point => checker.check(expectation, point).left.toOption)
      .sortBy(_.length)
      .headOption
      .getOrElse(NonEmptyList.one(PointExpectation.Mismatch.predicateMismatch("no points were collected")))

  private def containsCheck[E, P](
      expected: NonEmptyList[E],
      checker: SinglePointChecker.Aux[E, P],
      points: List[P]
  ): Either[NonEmptyList[Mismatch], Set[Int]] = {
    val indexedPoints = points.toVector
    val candidates = expected.toList.map { expectation =>
      indexedPoints.indices.filter(index => checker.check(expectation, indexedPoints(index)).isRight).toList
    }
    val matching = maximumMatching(candidates.toVector)

    if (matching.isComplete) Right(matching.matchedIndices)
    else {
      val missing = expected.toList.zip(candidates).collect { case (expectation, Nil) =>
        Mismatch.missingExpectedPoint(
          checker.clue(expectation),
          closestMismatch(points, expectation, checker)
        )
      }

      Left(
        NonEmptyList
          .fromList(missing)
          .getOrElse(
            NonEmptyList.one(
              Mismatch.matchedPointCountMismatch(expected.length, matching.size)
            )
          )
      )
    }
  }

  private final case class MatchingResult(
      isComplete: Boolean,
      matchedIndices: Set[Int],
      size: Int
  )

  private def maximumMatching(
      candidates: Vector[List[Int]],
  ): MatchingResult = {
    type Matching = Map[Int, Int] // pointIndex -> expectationIndex

    val orderedCandidates = candidates.zipWithIndex.sortBy(_._1.length)

    def augment(
        expectationIndex: Int,
        seen: Set[Int],
        matching: Matching
    ): Option[Matching] =
      orderedCandidates(expectationIndex)._1.foldLeft(Option.empty[Matching]) {
        case (result @ Some(_), _) =>
          result
        case (None, pointIndex) if seen(pointIndex) =>
          None
        case (None, pointIndex) =>
          matching.get(pointIndex) match {
            case None =>
              Some(matching.updated(pointIndex, expectationIndex))
            case Some(otherExpectationIndex) =>
              augment(otherExpectationIndex, seen + pointIndex, matching)
                .map(_.updated(pointIndex, expectationIndex))
          }
      }

    val finalMatching =
      orderedCandidates.indices.foldLeft(Map.empty[Int, Int]) { case (matching, expectationIndex) =>
        augment(expectationIndex, Set.empty, matching).getOrElse(matching)
      }

    MatchingResult(
      isComplete = finalMatching.size == candidates.length,
      matchedIndices = finalMatching.keySet,
      size = finalMatching.size
    )
  }

}
