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

import cats.data.NonEmptyList
import io.opentelemetry.sdk.metrics.data.MetricData
import org.typelevel.otel4s.oteljava.testkit.FlatExpectationMatching

/** Result of matching a [[MetricExpectation]] against a list of collected metrics. */
sealed trait MetricMismatch {

  /** The expectation that did not match any collected metric. */
  def expectation: MetricExpectation

  /** A human-readable clue copied from [[expectation]], if one was provided.
    *
    * Clues are intended to explain the business meaning of the expectation, so mismatch output is easier to interpret.
    */
  def clue: Option[String]

  /** A human-readable description of the mismatch. */
  def message: String
}

object MetricMismatch {

  /** Indicates that no collected metric matched the given expectation. */
  sealed trait NotFound extends MetricMismatch {
    def availableMetricNames: List[String]
  }

  /** Indicates that a candidate metric was found, but it still did not satisfy the full expectation. */
  sealed trait ClosestMismatch extends MetricMismatch {
    def metric: MetricData
    def mismatches: NonEmptyList[MetricExpectation.Mismatch]
  }

  /** Indicates that an expectation matched collected metrics, but none were available as a distinct match. */
  sealed trait DistinctMatchUnavailable extends MetricMismatch {
    def candidateMetricNames: List[String]
  }

  /** Creates a mismatch indicating that no collected metric matched the given expectation. */
  def notFound(expectation: MetricExpectation, availableMetricNames: List[String]): NotFound =
    NotFoundImpl(expectation, availableMetricNames)

  /** Creates a mismatch indicating that a candidate metric was found but did not satisfy the full expectation. */
  def closestMismatch(
      expectation: MetricExpectation,
      metric: MetricData,
      mismatches: NonEmptyList[MetricExpectation.Mismatch]
  ): ClosestMismatch =
    ClosestMismatchImpl(expectation, metric, mismatches)

  /** Creates a mismatch indicating that only already-consumed metric candidates remained for a distinct match. */
  def distinctMatchUnavailable(
      expectation: MetricExpectation,
      candidateMetricNames: List[String]
  ): DistinctMatchUnavailable =
    DistinctMatchUnavailableImpl(expectation, candidateMetricNames)

  private final case class NotFoundImpl(
      expectation: MetricExpectation,
      availableMetricNames: List[String]
  ) extends NotFound {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val available = availableMetricNames.mkString(", ")
      s"${prefix}no metric matched the expectation; available metrics: [$available]"
    }
  }

  private final case class ClosestMismatchImpl(
      expectation: MetricExpectation,
      metric: MetricData,
      mismatches: NonEmptyList[MetricExpectation.Mismatch]
  ) extends ClosestMismatch {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val rendered = mismatches.toList.map(_.message).mkString("\n  - ", "\n  - ", "")
      s"${prefix}closest metric '${metric.getName}' mismatched:$rendered"
    }
  }

  private final case class DistinctMatchUnavailableImpl(
      expectation: MetricExpectation,
      candidateMetricNames: List[String]
  ) extends DistinctMatchUnavailable {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val candidates = candidateMetricNames.mkString(", ")
      s"${prefix}no distinct metric remained for the expectation; matched metrics: [$candidates]"
    }
  }
}

/** Helpers for matching collected metrics against [[MetricExpectation]] values.
  *
  * Typical usage:
  *
  * {{{
  * val expected = List(
  *   MetricExpectation.name("service.requests"),
  *   MetricExpectation.sum[Long]("service.counter").value(1L)
  * )
  *
  * MetricExpectations.checkAll(metrics, expected) match {
  *   case Right(_) =>
  *     ()
  *   case Left(mismatches) =>
  *     fail(MetricExpectations.format(mismatches))
  * }
  * }}}
  */
object MetricExpectations {

  /** Returns `true` if at least one collected metric matches the expectation. */
  def exists(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Boolean =
    FlatExpectationMatching.exists(metrics, expectation)(_.matches(_))

  /** Returns the first collected metric matching the expectation, if any. */
  def find(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricData] =
    FlatExpectationMatching.find(metrics, expectation)(_.matches(_))

  /** Returns a mismatch if no collected metric matches the expectation. */
  def check(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricMismatch] =
    FlatExpectationMatching.check(metrics, expectation)(_.matches(_), bestMismatch _)

  /** Checks that every expectation matched at least one collected metric.
    *
    * Returns `Right(())` when all expectations matched. Otherwise returns a non-empty list of mismatches describing the
    * unmatched expectations.
    */
  def checkAll(
      metrics: List[MetricData],
      expectations: MetricExpectation*
  ): Either[NonEmptyList[MetricMismatch], Unit] =
    checkAll(metrics, expectations.toList)

  /** Checks that every expectation matched at least one collected metric.
    *
    * Returns `Right(())` when all expectations matched. Otherwise returns a non-empty list of mismatches describing the
    * unmatched expectations.
    */
  def checkAll(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Either[NonEmptyList[MetricMismatch], Unit] =
    FlatExpectationMatching.checkAll(metrics, expectations)(_.matches(_), bestMismatch _)

  /** Checks that every expectation matched a different collected metric.
    *
    * Returns `Right(())` when all expectations matched distinct collected metrics. Otherwise returns a non-empty list
    * of mismatches describing the unmatched expectations.
    */
  def checkAllDistinct(
      metrics: List[MetricData],
      expectations: MetricExpectation*
  ): Either[NonEmptyList[MetricMismatch], Unit] =
    checkAllDistinct(metrics, expectations.toList)

  /** Checks that every expectation matched a different collected metric.
    *
    * Returns `Right(())` when all expectations matched distinct collected metrics. Otherwise returns a non-empty list
    * of mismatches describing the unmatched expectations.
    */
  def checkAllDistinct(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Either[NonEmptyList[MetricMismatch], Unit] =
    FlatExpectationMatching.checkAllDistinct(metrics, expectations)(
      _.matches(_),
      bestMismatch _,
      MetricMismatch.distinctMatchUnavailable _,
      _.getName
    )

  /** Returns mismatches for all expectations that did not match any collected metric. */
  def missing(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): List[MetricMismatch] =
    FlatExpectationMatching.missing(metrics, expectations)(_.matches(_), bestMismatch _)

  /** Returns mismatches for all expectations that could not be matched to distinct collected metrics. */
  def missingDistinct(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): List[MetricMismatch] =
    FlatExpectationMatching.missingDistinct(metrics, expectations)(
      _.matches(_),
      bestMismatch _,
      MetricMismatch.distinctMatchUnavailable _,
      _.getName
    )

  /** Returns `true` if every expectation matched at least one collected metric. */
  def allMatch(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Boolean =
    FlatExpectationMatching.allMatch(metrics, expectations)(_.matches(_), bestMismatch _)

  /** Returns `true` if every expectation matched a different collected metric. */
  def allMatchDistinct(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Boolean =
    FlatExpectationMatching.allMatchDistinct(metrics, expectations)(
      _.matches(_),
      bestMismatch _,
      MetricMismatch.distinctMatchUnavailable _,
      _.getName
    )

  /** Formats mismatches into a multi-line human-readable failure message. */
  def format(
      mismatches: NonEmptyList[MetricMismatch]
  ): String =
    FlatExpectationMatching.format("Metric expectations", mismatches)(_.message)

  private def bestMismatch(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): MetricMismatch = {
    val candidates = expectation.expectedName match {
      case Some(name) =>
        metrics.filter(_.getName == name)
      case None =>
        metrics
    }

    candidates
      .flatMap { metric =>
        expectation.check(metric).left.toOption.map(metric -> _)
      }
      .sortBy { case (_, mismatches) =>
        (
          mismatches.exists {
            case MetricExpectation.Mismatch.TypeMismatch(_, _) => true
            case _                                             => false
          },
          mismatches.length
        )
      }
      .headOption
      .map { case (metric, mismatches) =>
        MetricMismatch.closestMismatch(expectation, metric, mismatches)
      }
      .getOrElse(MetricMismatch.notFound(expectation, metrics.map(_.getName)))
  }
}
