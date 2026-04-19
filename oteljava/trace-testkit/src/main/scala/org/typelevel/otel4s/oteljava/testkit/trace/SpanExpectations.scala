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
import io.opentelemetry.sdk.trace.data.SpanData
import org.typelevel.otel4s.oteljava.testkit.FlatExpectationMatching

/** Result of matching a [[SpanExpectation]] against a list of collected spans. */
sealed trait SpanMismatch {

  /** The expectation that did not match any collected span. */
  def expectation: SpanExpectation

  /** A human-readable clue copied from [[expectation]], if one was provided. */
  def clue: Option[String]

  /** A human-readable description of the mismatch. */
  def message: String
}

object SpanMismatch {

  /** Indicates that no collected span matched the given expectation. */
  sealed trait NotFound extends SpanMismatch {
    def availableSpanNames: List[String]
  }

  /** Indicates that a candidate span was found, but it still did not satisfy the full expectation. */
  sealed trait ClosestMismatch extends SpanMismatch {
    def span: SpanData
    def mismatches: NonEmptyList[SpanExpectation.Mismatch]
  }

  /** Indicates that an expectation matched collected spans, but none were available as a distinct match. */
  sealed trait DistinctMatchUnavailable extends SpanMismatch {
    def candidateSpanNames: List[String]
  }

  def notFound(expectation: SpanExpectation, availableSpanNames: List[String]): NotFound =
    NotFoundImpl(expectation, availableSpanNames)

  def closestMismatch(
      expectation: SpanExpectation,
      span: SpanData,
      mismatches: NonEmptyList[SpanExpectation.Mismatch]
  ): ClosestMismatch =
    ClosestMismatchImpl(expectation, span, mismatches)

  def distinctMatchUnavailable(
      expectation: SpanExpectation,
      candidateSpanNames: List[String]
  ): DistinctMatchUnavailable =
    DistinctMatchUnavailableImpl(expectation, candidateSpanNames)

  private final case class NotFoundImpl(expectation: SpanExpectation, availableSpanNames: List[String])
      extends NotFound {
    def clue: Option[String] = expectation.clue
    def message: String =
      s"${clue.fold("")(c => s"[$c] ")}no span matched the expectation; available spans: [${availableSpanNames.mkString(", ")}]"
  }

  private final case class ClosestMismatchImpl(
      expectation: SpanExpectation,
      span: SpanData,
      mismatches: NonEmptyList[SpanExpectation.Mismatch]
  ) extends ClosestMismatch {
    def clue: Option[String] = expectation.clue
    def message: String = {
      val rendered = mismatches.toList.map(_.message).mkString("\n  - ", "\n  - ", "")
      s"${clue.fold("")(c => s"[$c] ")}closest span '${span.getName}' mismatched:$rendered"
    }
  }

  private final case class DistinctMatchUnavailableImpl(expectation: SpanExpectation, candidateSpanNames: List[String])
      extends DistinctMatchUnavailable {
    def clue: Option[String] = expectation.clue
    def message: String =
      s"${clue.fold("")(c => s"[$c] ")}no distinct span remained for the expectation; matched spans: [${candidateSpanNames.mkString(", ")}]"
  }
}

/** Helpers for matching collected spans against [[SpanExpectation]] values. */
object SpanExpectations {

  /** Returns `true` if at least one collected span matches the expectation. */
  def exists(spans: List[SpanData], expectation: SpanExpectation): Boolean =
    FlatExpectationMatching.exists(spans, expectation)(_.matches(_))

  /** Returns the first collected span matching the expectation, if any. */
  def find(spans: List[SpanData], expectation: SpanExpectation): Option[SpanData] =
    FlatExpectationMatching.find(spans, expectation)(_.matches(_))

  /** Returns a mismatch if no collected span matches the expectation. */
  def check(spans: List[SpanData], expectation: SpanExpectation): Option[SpanMismatch] =
    FlatExpectationMatching.check(spans, expectation)(_.matches(_), bestMismatch)

  /** Checks that every expectation matched at least one collected span. */
  def checkAll(spans: List[SpanData], expectations: SpanExpectation*): Either[NonEmptyList[SpanMismatch], Unit] =
    checkAll(spans, expectations.toList)

  /** Checks that every expectation matched at least one collected span. */
  def checkAll(spans: List[SpanData], expectations: List[SpanExpectation]): Either[NonEmptyList[SpanMismatch], Unit] =
    FlatExpectationMatching.checkAll(spans, expectations)(_.matches(_), bestMismatch)

  /** Checks that every expectation matched a different collected span. */
  def checkAllDistinct(
      spans: List[SpanData],
      expectations: SpanExpectation*
  ): Either[NonEmptyList[SpanMismatch], Unit] =
    checkAllDistinct(spans, expectations.toList)

  /** Checks that every expectation matched a different collected span. */
  def checkAllDistinct(
      spans: List[SpanData],
      expectations: List[SpanExpectation]
  ): Either[NonEmptyList[SpanMismatch], Unit] =
    FlatExpectationMatching.checkAllDistinct(spans, expectations)(
      _.matches(_),
      bestMismatch,
      SpanMismatch.distinctMatchUnavailable,
      _.getName
    )

  /** Returns mismatches for all expectations that did not match any collected span. */
  def missing(spans: List[SpanData], expectations: List[SpanExpectation]): List[SpanMismatch] =
    FlatExpectationMatching.missing(spans, expectations)(_.matches(_), bestMismatch)

  /** Returns mismatches for all expectations that could not be matched to distinct collected spans. */
  def missingDistinct(spans: List[SpanData], expectations: List[SpanExpectation]): List[SpanMismatch] =
    FlatExpectationMatching.missingDistinct(spans, expectations)(
      _.matches(_),
      bestMismatch,
      SpanMismatch.distinctMatchUnavailable,
      _.getName
    )

  /** Returns `true` if every expectation matched at least one collected span. */
  def allMatch(spans: List[SpanData], expectations: List[SpanExpectation]): Boolean =
    FlatExpectationMatching.allMatch(spans, expectations)(_.matches(_), bestMismatch)

  /** Returns `true` if every expectation matched a different collected span. */
  def allMatchDistinct(spans: List[SpanData], expectations: List[SpanExpectation]): Boolean =
    FlatExpectationMatching.allMatchDistinct(spans, expectations)(
      _.matches(_),
      bestMismatch,
      SpanMismatch.distinctMatchUnavailable,
      _.getName
    )

  /** Formats mismatches into a multi-line human-readable failure message. */
  def format(mismatches: NonEmptyList[SpanMismatch]): String =
    FlatExpectationMatching.format("Span expectations", mismatches)(_.message)

  private def bestMismatch(spans: List[SpanData], expectation: SpanExpectation): SpanMismatch = {
    val candidates = expectation.expectedName match {
      case Some(name) => spans.filter(_.getName == name)
      case None       => spans
    }

    candidates
      .flatMap(span => expectation.check(span).left.toOption.map(span -> _))
      .sortBy { case (_, mismatches) =>
        val kindMismatch = mismatches.exists {
          case _: SpanExpectation.Mismatch.KindMismatch => true
          case _                                        => false
        }
        (kindMismatch, mismatches.length)
      }
      .headOption
      .map { case (span, mismatches) => SpanMismatch.closestMismatch(expectation, span, mismatches) }
      .getOrElse(SpanMismatch.notFound(expectation, spans.map(_.getName)))
  }
}
