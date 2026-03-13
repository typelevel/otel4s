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
      metrics: List[MetricData]
  ) extends MetricMismatch {
    def clue: Option[String] = expectation.clue

    def message: String = {
      val prefix = clue.fold("")(c => s"[$c] ")
      val available = metrics.map(_.getName).mkString(", ")
      s"${prefix}no metric matched the expectation; available metrics: [$available]"
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
    Option.unless(exists(metrics, expectation)) {
      MetricMismatch.NotFound(expectation, metrics)
    }

  /** Returns mismatches for all expectations that did not match any collected metric. */
  def missing(
      metrics: List[MetricData],
      expectations: List[MetricExpectation]
  ): List[MetricMismatch] =
    expectations.flatMap { expectation =>
      Option.unless(metrics.exists(expectation.matches)) {
        MetricMismatch.NotFound(expectation, metrics)
      }
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
}
