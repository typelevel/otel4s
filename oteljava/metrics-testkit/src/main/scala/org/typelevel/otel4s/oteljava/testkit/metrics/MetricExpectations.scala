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
import org.typelevel.otel4s.oteljava.testkit.{InstrumentationScopeExpectation, TelemetryResourceExpectation}

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
  final case class NotFound(
      expectation: MetricExpectation,
      availableMetricNames: List[String]
  ) extends MetricMismatch {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val available = availableMetricNames.mkString(", ")
      s"${prefix}no metric matched the expectation; available metrics: [$available]"
    }
  }

  /** Indicates that a candidate metric was found, but it still did not satisfy the full expectation. */
  final case class ClosestMismatch(
      expectation: MetricExpectation,
      metric: MetricData,
      mismatches: NonEmptyList[MetricExpectation.Mismatch]
  ) extends MetricMismatch {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val rendered = mismatches.toList.map(renderMetricMismatch).mkString("\n  - ", "\n  - ", "")
      s"${prefix}closest metric '${metric.getName}' mismatched:$rendered"
    }
  }

  private def renderMetricMismatch(mismatch: MetricExpectation.Mismatch): String =
    mismatch match {
      case MetricExpectation.Mismatch.NameMismatch(expected, actual) =>
        s"name mismatch: expected '$expected', got '$actual'"
      case MetricExpectation.Mismatch.DescriptionMismatch(expected, actual) =>
        s"description mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
      case MetricExpectation.Mismatch.UnitMismatch(expected, actual) =>
        s"unit mismatch: expected '$expected', got '$actual'"
      case MetricExpectation.Mismatch.TypeMismatch(expected, actual) =>
        s"type mismatch: expected '$expected', got '$actual'"
      case MetricExpectation.Mismatch.ScopeMismatch(mismatches) =>
        s"scope mismatch: ${mismatches.toList.map(InstrumentationScopeExpectation.formatMismatch).mkString(", ")}"
      case MetricExpectation.Mismatch.ResourceMismatch(mismatches) =>
        s"resource mismatch: ${mismatches.toList.map(TelemetryResourceExpectation.formatMismatch).mkString(", ")}"
      case MetricExpectation.Mismatch.PredicateMismatch(clue) =>
        s"predicate mismatch: $clue"
      case MetricExpectation.Mismatch.PointsMismatch(mode, mismatches, clue) =>
        val rendered = mismatches.toList.map(PointExpectation.formatMismatch).mkString(", ")
        val clueSuffix = clue.fold("")(value => s" [$value]")
        s"points mismatch ($mode$clueSuffix): $rendered"
    }
}

/** Helpers for matching collected metrics against [[MetricExpectation]] values.
  *
  * Typical usage:
  *
  * {{{
  * val expected = List(
  *   MetricExpectation.name("service.requests"),
  *   MetricExpectation.sum[Long]("service.counter").withValue(1L)
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
    find(metrics, expectation).nonEmpty

  /** Returns the first collected metric matching the expectation, if any. */
  def find(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricData] =
    metrics.find(expectation.matches)

  /** Returns a mismatch if no collected metric matches the expectation. */
  def check(
      metrics: List[MetricData],
      expectation: MetricExpectation
  ): Option[MetricMismatch] =
    if (exists(metrics, expectation)) None
    else Some(bestMismatch(metrics, expectation))

  /** Returns mismatches for all expectations that did not match any collected metric. */
  def missing(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): List[MetricMismatch] =
    expectations.flatMap { expectation =>
      check(metrics, expectation)
    }

  /** Checks that every expectation matched at least one collected metric.
    *
    * Returns `Right(())` when all expectations matched. Otherwise returns a non-empty list of mismatches describing the
    * unmatched expectations.
    */
  def checkAll(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Either[NonEmptyList[MetricMismatch], Unit] =
    NonEmptyList.fromList(missing(metrics, expectations)).toLeft(())

  /** Formats mismatches into a multi-line human-readable failure message. */
  def format(
      mismatches: NonEmptyList[MetricMismatch]
  ): String =
    mismatches.toList.zipWithIndex
      .map { case (mismatch, index) => s"${index + 1}. ${mismatch.message}" }
      .mkString("Metric expectations failed:\n", "\n", "")

  /** Returns `true` if every expectation matched at least one collected metric. */
  def allMatch(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): Boolean =
    checkAll(metrics, expectations).isRight

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
      .sortBy { case (_, mismatches) => mismatches.length }
      .headOption
      .map { case (metric, mismatches) =>
        MetricMismatch.ClosestMismatch(expectation, metric, mismatches)
      }
      .getOrElse(MetricMismatch.NotFound(expectation, metrics.map(_.getName)))
  }
}
