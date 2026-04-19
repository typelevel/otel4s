/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.logs

import cats.data.NonEmptyList
import io.opentelemetry.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.oteljava.testkit.FlatExpectationMatching

/** Result of matching a [[LogRecordExpectation]] against a list of collected log records. */
sealed trait LogRecordMismatch {
  def expectation: LogRecordExpectation
  def clue: Option[String]
  def message: String
}

object LogRecordMismatch {

  sealed trait NotFound extends LogRecordMismatch {
    def availableRecordSummaries: List[String]
  }

  sealed trait ClosestMismatch extends LogRecordMismatch {
    def record: LogRecordData
    def mismatches: NonEmptyList[LogRecordExpectation.Mismatch]
  }

  sealed trait DistinctMatchUnavailable extends LogRecordMismatch {
    def candidateRecordSummaries: List[String]
  }

  def notFound(expectation: LogRecordExpectation, availableRecordSummaries: List[String]): NotFound =
    NotFoundImpl(expectation, availableRecordSummaries)

  def closestMismatch(
      expectation: LogRecordExpectation,
      record: LogRecordData,
      mismatches: NonEmptyList[LogRecordExpectation.Mismatch]
  ): ClosestMismatch =
    ClosestMismatchImpl(expectation, record, mismatches)

  def distinctMatchUnavailable(
      expectation: LogRecordExpectation,
      candidateRecordSummaries: List[String]
  ): DistinctMatchUnavailable =
    DistinctMatchUnavailableImpl(expectation, candidateRecordSummaries)

  private final case class NotFoundImpl(expectation: LogRecordExpectation, availableRecordSummaries: List[String])
      extends NotFound {
    def clue: Option[String] = expectation.clue
    def message: String =
      s"${clue.fold("")(c => s"[$c] ")}no log record matched the expectation; available records: [${availableRecordSummaries.mkString(", ")}]"
  }

  private final case class ClosestMismatchImpl(
      expectation: LogRecordExpectation,
      record: LogRecordData,
      mismatches: NonEmptyList[LogRecordExpectation.Mismatch]
  ) extends ClosestMismatch {
    def clue: Option[String] = expectation.clue
    def message: String = {
      val rendered = mismatches.toList.map(_.message).mkString("\n  - ", "\n  - ", "")
      s"${clue.fold("")(c => s"[$c] ")}closest log record ${LogRecordExpectations.renderRecord(record)} mismatched:$rendered"
    }
  }

  private final case class DistinctMatchUnavailableImpl(
      expectation: LogRecordExpectation,
      candidateRecordSummaries: List[String]
  ) extends DistinctMatchUnavailable {
    def clue: Option[String] = expectation.clue
    def message: String =
      s"${clue.fold("")(c => s"[$c] ")}no distinct log record remained for the expectation; matched records: [${candidateRecordSummaries.mkString(", ")}]"
  }
}

/** Helpers for matching collected log records against [[LogRecordExpectation]] values. */
object LogRecordExpectations {

  /** Returns `true` if at least one collected log record matches the expectation. */
  def exists(records: List[LogRecordData], expectation: LogRecordExpectation): Boolean =
    FlatExpectationMatching.exists(records, expectation)(_.matches(_))

  /** Returns the first collected log record matching the expectation, if any. */
  def find(records: List[LogRecordData], expectation: LogRecordExpectation): Option[LogRecordData] =
    FlatExpectationMatching.find(records, expectation)(_.matches(_))

  /** Returns a mismatch if no collected log record matches the expectation. */
  def check(records: List[LogRecordData], expectation: LogRecordExpectation): Option[LogRecordMismatch] =
    FlatExpectationMatching.check(records, expectation)(_.matches(_), bestMismatch)

  /** Checks that every expectation matched at least one collected log record. */
  def checkAll(
      records: List[LogRecordData],
      expectations: LogRecordExpectation*
  ): Either[NonEmptyList[LogRecordMismatch], Unit] =
    checkAll(records, expectations.toList)

  /** Checks that every expectation matched at least one collected log record. */
  def checkAll(
      records: List[LogRecordData],
      expectations: List[LogRecordExpectation]
  ): Either[NonEmptyList[LogRecordMismatch], Unit] =
    FlatExpectationMatching.checkAll(records, expectations)(_.matches(_), bestMismatch)

  /** Checks that every expectation matched a different collected log record. */
  def checkAllDistinct(
      records: List[LogRecordData],
      expectations: LogRecordExpectation*
  ): Either[NonEmptyList[LogRecordMismatch], Unit] =
    checkAllDistinct(records, expectations.toList)

  /** Checks that every expectation matched a different collected log record. */
  def checkAllDistinct(
      records: List[LogRecordData],
      expectations: List[LogRecordExpectation]
  ): Either[NonEmptyList[LogRecordMismatch], Unit] =
    FlatExpectationMatching.checkAllDistinct(records, expectations)(
      _.matches(_),
      bestMismatch,
      LogRecordMismatch.distinctMatchUnavailable,
      renderRecord
    )

  /** Returns mismatches for all expectations that did not match any collected log record. */
  def missing(records: List[LogRecordData], expectations: List[LogRecordExpectation]): List[LogRecordMismatch] =
    FlatExpectationMatching.missing(records, expectations)(_.matches(_), bestMismatch)

  /** Returns mismatches for all expectations that could not be matched to distinct collected log records. */
  def missingDistinct(
      records: List[LogRecordData],
      expectations: List[LogRecordExpectation]
  ): List[LogRecordMismatch] =
    FlatExpectationMatching.missingDistinct(records, expectations)(
      _.matches(_),
      bestMismatch,
      LogRecordMismatch.distinctMatchUnavailable,
      renderRecord
    )

  /** Returns `true` if every expectation matched at least one collected log record. */
  def allMatch(records: List[LogRecordData], expectations: List[LogRecordExpectation]): Boolean =
    FlatExpectationMatching.allMatch(records, expectations)(_.matches(_), bestMismatch)

  /** Returns `true` if every expectation matched a different collected log record. */
  def allMatchDistinct(records: List[LogRecordData], expectations: List[LogRecordExpectation]): Boolean =
    FlatExpectationMatching.allMatchDistinct(records, expectations)(
      _.matches(_),
      bestMismatch,
      LogRecordMismatch.distinctMatchUnavailable,
      renderRecord
    )

  /** Formats mismatches into a multi-line human-readable failure message. */
  def format(mismatches: NonEmptyList[LogRecordMismatch]): String =
    FlatExpectationMatching.format("Log record expectations", mismatches)(_.message)

  private[logs] def renderRecord(record: LogRecordData): String = {
    val severity = LogRecordExpectation.severityOf(record).fold("UNSPECIFIED")(_.toString)
    val body = LogRecordExpectation.bodyValue(record).fold("<missing>")(v => s"'$v'")
    s"[$severity $body]"
  }

  private def bestMismatch(records: List[LogRecordData], expectation: LogRecordExpectation): LogRecordMismatch = {
    records
      .flatMap(record => expectation.check(record).left.toOption.map(record -> _))
      .sortBy { case (_, mismatches) =>
        val traceCorrelationMismatch = mismatches.exists {
          case _: LogRecordExpectation.Mismatch.TraceIdMismatch  => true
          case _: LogRecordExpectation.Mismatch.SpanIdMismatch   => true
          case _: LogRecordExpectation.Mismatch.UntracedMismatch => true
          case _                                                 => false
        }
        val severityOrBodyMismatch = mismatches.exists {
          case _: LogRecordExpectation.Mismatch.BodyMismatch         => true
          case _: LogRecordExpectation.Mismatch.SeverityMismatch     => true
          case _: LogRecordExpectation.Mismatch.SeverityTextMismatch =>
            true
          case _ => false
        }
        (traceCorrelationMismatch, severityOrBodyMismatch, mismatches.length)
      }
      .headOption
      .map { case (record, mismatches) => LogRecordMismatch.closestMismatch(expectation, record, mismatches) }
      .getOrElse(LogRecordMismatch.notFound(expectation, records.map(renderRecord)))
  }
}
