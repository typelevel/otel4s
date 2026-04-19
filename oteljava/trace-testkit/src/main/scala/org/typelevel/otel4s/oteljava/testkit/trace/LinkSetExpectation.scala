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

package org.typelevel.otel4s.oteljava.testkit.trace

import cats.data.NonEmptyList
import cats.syntax.functor._
import io.opentelemetry.sdk.trace.data.{LinkData => JLinkData}
import org.typelevel.otel4s.oteljava.testkit.CollectionExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.LogicalOperator

/** A partial expectation over a collection of span links. */
sealed trait LinkSetExpectation {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Combines this expectation with another one using logical `and`. */
  def and(other: LinkSetExpectation): LinkSetExpectation =
    LinkSetExpectation.and(this, other)

  /** Combines this expectation with another one using logical `or`. */
  def or(other: LinkSetExpectation): LinkSetExpectation =
    LinkSetExpectation.or(this, other)

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): LinkSetExpectation

  private[trace] def check(links: List[JLinkData]): Either[NonEmptyList[LinkSetExpectation.Mismatch], Unit]
}

object LinkSetExpectation {

  /** A structured reason explaining why a [[LinkSetExpectation]] did not match the collected links. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class LinkCountMismatch(expected: Int, actual: Int) extends Mismatch {
      def message: String = s"link count mismatch: expected $expected, got $actual"
    }

    private[trace] final case class MinimumLinkCountMismatch(expectedAtLeast: Int, actual: Int) extends Mismatch {
      def message: String = s"link count mismatch: expected at least $expectedAtLeast, got $actual"
    }

    private[trace] final case class MaximumLinkCountMismatch(expectedAtMost: Int, actual: Int) extends Mismatch {
      def message: String = s"link count mismatch: expected at most $expectedAtMost, got $actual"
    }

    private[trace] final case class MissingExpectedLink(
        clue: Option[String],
        mismatches: NonEmptyList[LinkExpectation.Mismatch]
    ) extends Mismatch {
      def message: String =
        s"missing expected link${clue.fold("")(v => s" [$v]")}: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class UnexpectedLink(index: Int) extends Mismatch {
      def message: String = s"unexpected link at index $index"
    }

    private[trace] final case class FailingLink(index: Int, mismatches: NonEmptyList[LinkExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"failing link at index $index: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class PredicateFailed(clue: Option[String]) extends Mismatch {
      def message: String =
        s"link set predicate returned false${clue.fold("")(v => s": $v")}"
    }

    private[trace] case object NoLinksCollected extends Mismatch {
      def message: String = "no links were collected"
    }

    private[trace] final case class CompositeMismatch(
        operator: LogicalOperator,
        mismatches: NonEmptyList[Mismatch]
    ) extends Mismatch {
      def message: String =
        s"${operator.render} mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class CluedMismatch(clue: String, mismatches: NonEmptyList[Mismatch]) extends Mismatch {
      def message: String =
        s"link-set mismatch [$clue]: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
  }

  /** Creates an expectation that accepts any link collection. */
  def any: LinkSetExpectation = AnyLink(None)

  /** Requires at least one collected link to match the given expectation. */
  def exists(link: LinkExpectation): LinkSetExpectation = Exists(link, None)

  /** Requires every collected link to match the given expectation. */
  def forall(link: LinkExpectation): LinkSetExpectation = ForAll(link, None)

  /** Requires the collected links to contain all given expectations. */
  def contains(first: LinkExpectation, rest: LinkExpectation*): LinkSetExpectation =
    Contains(NonEmptyList(first, rest.toList), None)

  /** Requires the collected links to match the given expectations exactly. */
  def exactly(first: LinkExpectation, rest: LinkExpectation*): LinkSetExpectation =
    Exactly(NonEmptyList(first, rest.toList), None)

  /** Requires the collected links to have exactly the given size. */
  def count(expected: Int): LinkSetExpectation = Count(expected, None)

  /** Requires the collected links to have at least the given size. */
  def minCount(expectedAtLeast: Int): LinkSetExpectation = MinCount(expectedAtLeast, None)

  /** Requires the collected links to have at most the given size. */
  def maxCount(expectedAtMost: Int): LinkSetExpectation = MaxCount(expectedAtMost, None)

  /** Requires no collected link to match the given expectation. */
  def none(link: LinkExpectation): LinkSetExpectation = NoneOf(link, None)

  /** Adds a custom predicate over the full link collection. */
  def predicate(f: List[JLinkData] => Boolean): LinkSetExpectation = Predicate(f, None)

  /** Adds a custom predicate over the full link collection with a clue shown in mismatches. */
  def predicate(clue: String)(f: List[JLinkData] => Boolean): LinkSetExpectation = Predicate(f, Some(clue))

  /** Combines two link-set expectations using logical `and`. */
  def and(left: LinkSetExpectation, right: LinkSetExpectation): LinkSetExpectation =
    Composite(left, right, LogicalOperator.And, None)

  /** Combines two link-set expectations using logical `or`. */
  def or(left: LinkSetExpectation, right: LinkSetExpectation): LinkSetExpectation =
    Composite(left, right, LogicalOperator.Or, None)

  private final case class AnyLink(clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] = ExpectationChecks.success
  }

  private final case class Exists(link: LinkExpectation, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (links.exists(link.matches)) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MissingExpectedLink(link.clue, closestMismatch(links, link)))
      }
  }

  private final case class ForAll(link: LinkExpectation, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (links.isEmpty) ExpectationChecks.mismatch(Mismatch.NoLinksCollected)
        else
          CollectionExpectationChecks.firstFailingIndex(links, link)(
            (expectation, actual) => expectation.check(actual),
            (index, mismatches) => Mismatch.FailingLink(index, mismatches)
          ) match {
            case Some(mismatch) => ExpectationChecks.mismatch(mismatch)
            case None           => ExpectationChecks.success
          }
      }
  }

  private final case class Contains(expected: NonEmptyList[LinkExpectation], clue: Option[String])
      extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue)(containsCheck(expected, links).void)
  }

  private final case class Exactly(expected: NonEmptyList[LinkExpectation], clue: Option[String])
      extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        containsCheck(expected, links).flatMap { matchedIndices =>
          val unexpected = links.indices.filterNot(matchedIndices.contains).map(Mismatch.UnexpectedLink(_)).toList
          NonEmptyList.fromList(unexpected).toLeft(())
        }
      }
  }

  private final case class Count(expected: Int, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (links.length == expected) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.LinkCountMismatch(expected, links.length))
      }
  }

  private final case class MinCount(expectedAtLeast: Int, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (links.length >= expectedAtLeast) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MinimumLinkCountMismatch(expectedAtLeast, links.length))
      }
  }

  private final case class MaxCount(expectedAtMost: Int, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        if (links.length <= expectedAtMost) ExpectationChecks.success
        else ExpectationChecks.mismatch(Mismatch.MaximumLinkCountMismatch(expectedAtMost, links.length))
      }
  }

  private final case class NoneOf(link: LinkExpectation, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        val firstMatch =
          CollectionExpectationChecks.firstMatchingIndex(links, link)((expectation, actual) =>
            expectation.matches(actual)
          )

        firstMatch match {
          case Some(index) => ExpectationChecks.mismatch[Mismatch](Mismatch.UnexpectedLink(index))
          case None        => ExpectationChecks.success[Mismatch]
        }
      }
  }

  private final case class Predicate(f: List[JLinkData] => Boolean, clue: Option[String]) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue)(Either.cond(f(links), (), NonEmptyList.one(Mismatch.PredicateFailed(clue))))
  }

  private final case class Composite(
      left: LinkSetExpectation,
      right: LinkSetExpectation,
      operator: LogicalOperator,
      clue: Option[String]
  ) extends LinkSetExpectation {
    def clue(text: String): LinkSetExpectation = copy(clue = Some(text))
    def check(links: List[JLinkData]): Either[NonEmptyList[Mismatch], Unit] =
      withClueContext(clue) {
        CollectionExpectationChecks.compositeCheck(operator, left.check(links), right.check(links)) { mismatches =>
          Mismatch.CompositeMismatch(operator, mismatches)
        }
      }
  }

  private def withClueContext(
      clue: Option[String]
  )(result: Either[NonEmptyList[Mismatch], Unit]): Either[NonEmptyList[Mismatch], Unit] =
    CollectionExpectationChecks.withClueContext(clue, result) { (clue, mismatches) =>
      Mismatch.CluedMismatch(clue, mismatches)
    }

  private def closestMismatch(
      links: List[JLinkData],
      expectation: LinkExpectation
  ): NonEmptyList[LinkExpectation.Mismatch] =
    CollectionExpectationChecks.closestMismatch(links, expectation)(
      (expected, actual) => expected.check(actual),
      LinkExpectation.Mismatch.PredicateMismatch(Some("no links available"))
    )

  private def containsCheck(
      expected: NonEmptyList[LinkExpectation],
      links: List[JLinkData]
  ): Either[NonEmptyList[Mismatch], Set[Int]] =
    CollectionExpectationChecks.containsCheck[JLinkData, LinkExpectation, Mismatch, LinkExpectation.Mismatch](
      expected,
      links
    )(
      (expectation, actual) => expectation.matches(actual),
      (link, mismatches) => Mismatch.MissingExpectedLink(link.clue, mismatches),
      closestMismatch
    )
}
