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
import io.opentelemetry.sdk.common.{InstrumentationScopeInfo => JInstrumentationScopeInfo}
import io.opentelemetry.sdk.logs.data.LogRecordData
import io.opentelemetry.sdk.resources.{Resource => JResource}
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.AnyValueConverters._
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.logs.SeverityConversions
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation
import org.typelevel.otel4s.oteljava.testkit.TelemetryResourceExpectation

/** A partial expectation for a single exported OpenTelemetry Java `LogRecordData`. */
sealed trait LogRecordExpectation {
  private[logs] def expectedBody: Option[AnyValue]
  private[logs] def expectedSeverity: Option[Severity]
  private[logs] def expectsTraceCorrelation: Boolean

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the log body to match the exact raw value. */
  def body(body: AnyValue): LogRecordExpectation

  /** Requires the log body to be an exact string message. */
  def message(message: String): LogRecordExpectation

  /** Requires the log severity to match exactly. */
  def severity(severity: Severity): LogRecordExpectation

  /** Requires the log severity text to match exactly. */
  def severityText(text: String): LogRecordExpectation

  /** Requires the log trace id to match exactly. */
  def traceId(traceId: String): LogRecordExpectation

  /** Requires the log span id to match exactly. */
  def spanId(spanId: String): LogRecordExpectation

  /** Requires the log to be untraced. */
  def untraced: LogRecordExpectation

  /** Requires the log attributes to match the given expectation. */
  def attributes(expectation: AttributesExpectation): LogRecordExpectation

  /** Requires the log attributes to match exactly. */
  def attributesExact(attributes: Attributes): LogRecordExpectation

  /** Requires the log attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): LogRecordExpectation

  /** Requires the log attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attributes): LogRecordExpectation

  /** Requires the log attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attribute[_]*): LogRecordExpectation

  /** Requires the log to have no attributes. */
  def attributesEmpty: LogRecordExpectation

  /** Requires the instrumentation scope to match the given expectation. */
  def scope(expectation: InstrumentationScopeExpectation): LogRecordExpectation

  /** Requires the telemetry resource to match the given expectation. */
  def resource(expectation: TelemetryResourceExpectation): LogRecordExpectation

  /** Adds a predicate over the observed timestamp in epoch nanos. */
  def observedTimestampWhere(f: Long => Boolean): LogRecordExpectation

  /** Adds a predicate over the observed timestamp in epoch nanos with a clue shown in mismatches. */
  def observedTimestampWhere(clue: String)(f: Long => Boolean): LogRecordExpectation

  /** Adds a predicate over the timestamp in epoch nanos. */
  def timestampWhere(f: Long => Boolean): LogRecordExpectation

  /** Adds a predicate over the timestamp in epoch nanos with a clue shown in mismatches. */
  def timestampWhere(clue: String)(f: Long => Boolean): LogRecordExpectation

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): LogRecordExpectation

  /** Adds a custom predicate over the whole log record. */
  def where(f: LogRecordData => Boolean): LogRecordExpectation

  /** Adds a custom predicate over the whole log record with a clue shown in mismatches. */
  def where(clue: String)(f: LogRecordData => Boolean): LogRecordExpectation

  /** Checks the given log record and returns structured mismatches when the expectation does not match. */
  def check(record: LogRecordData): Either[NonEmptyList[LogRecordExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given record. */
  final def matches(record: LogRecordData): Boolean =
    check(record).isRight
}

object LogRecordExpectation {

  /** A structured reason explaining why a [[LogRecordExpectation]] did not match a log record. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {
    private[logs] final case class BodyMismatch(expected: AnyValue, actual: Option[AnyValue]) extends Mismatch {
      def message: String =
        s"body mismatch: expected ${render(expected)}, got ${actual.fold("<missing>")(render)}"

      private def render(value: AnyValue): String =
        s"'$value'"
    }

    private[logs] final case class SeverityMismatch(expected: Severity, actual: Option[Severity]) extends Mismatch {
      def message: String =
        s"severity mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private[logs] final case class SeverityTextMismatch(expected: String, actual: Option[String]) extends Mismatch {
      def message: String =
        s"severity text mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private[logs] final case class TraceIdMismatch(expected: String, actual: Option[String]) extends Mismatch {
      def message: String =
        s"trace id mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private[logs] final case class SpanIdMismatch(expected: String, actual: Option[String]) extends Mismatch {
      def message: String =
        s"span id mismatch: expected '$expected', got ${actual.fold("<missing>")(v => s"'$v'")}"
    }

    private[logs] final case class UntracedMismatch(actualTraceId: String, actualSpanId: String) extends Mismatch {
      def message: String =
        s"trace correlation mismatch: expected untraced record, got traceId '$actualTraceId' and spanId '$actualSpanId'"
    }

    private[logs] final case class AttributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"attributes mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[logs] final case class ScopeMismatch(
        mismatches: NonEmptyList[InstrumentationScopeExpectation.Mismatch]
    ) extends Mismatch {
      def message: String =
        s"scope mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[logs] final case class ResourceMismatch(mismatches: NonEmptyList[TelemetryResourceExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"resource mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[logs] final case class ObservedTimestampMismatch(clue: Option[String]) extends Mismatch {
      def message: String =
        s"observed timestamp predicate returned false${clue.fold("")(v => s": $v")}"
    }

    private[logs] final case class TimestampMismatch(clue: Option[String]) extends Mismatch {
      def message: String =
        s"timestamp predicate returned false${clue.fold("")(v => s": $v")}"
    }

    private[logs] final case class PredicateMismatch(clue: Option[String]) extends Mismatch {
      def message: String =
        s"predicate mismatch${clue.fold("")(v => s": $v")}"
    }
  }

  /** Creates an expectation that leaves all log record fields unconstrained. */
  def any: LogRecordExpectation = Impl()

  /** Creates an expectation matching a log record with the given exact raw body. */
  def body(body: AnyValue): LogRecordExpectation = Impl(expectedBodyValue = Some(body))

  /** Creates an expectation matching a log record with the given exact string message. */
  def message(message: String): LogRecordExpectation = body(AnyValue.string(message))

  /** Creates an expectation matching a log record with the given severity. */
  def severity(severity: Severity): LogRecordExpectation = Impl(expectedSeverityValue = Some(severity))

  private final case class Impl(
      expectedBodyValue: Option[AnyValue] = None,
      expectedSeverityValue: Option[Severity] = None,
      expectedSeverityText: Option[String] = None,
      expectedTraceId: Option[String] = None,
      expectedSpanId: Option[String] = None,
      untracedRequired: Boolean = false,
      attributesExpectation: Option[AttributesExpectation] = None,
      scopeExpectation: Option[InstrumentationScopeExpectation] = None,
      resourceExpectation: Option[TelemetryResourceExpectation] = None,
      observedTimestampPredicates: List[(Long => Boolean, Option[String])] = Nil,
      timestampPredicates: List[(Long => Boolean, Option[String])] = Nil,
      clue: Option[String] = None,
      predicates: List[(LogRecordData => Boolean, Option[String])] = Nil
  ) extends LogRecordExpectation {
    private[logs] def expectedBody: Option[AnyValue] = expectedBodyValue
    private[logs] def expectedSeverity: Option[Severity] = expectedSeverityValue
    private[logs] def expectsTraceCorrelation: Boolean =
      expectedTraceId.nonEmpty || expectedSpanId.nonEmpty || untracedRequired

    def body(body: AnyValue): LogRecordExpectation = copy(expectedBodyValue = Some(body))
    def message(message: String): LogRecordExpectation = body(AnyValue.string(message))
    def severity(severity: Severity): LogRecordExpectation = copy(expectedSeverityValue = Some(severity))
    def severityText(text: String): LogRecordExpectation = copy(expectedSeverityText = Some(text))
    def traceId(traceId: String): LogRecordExpectation = copy(expectedTraceId = Some(traceId), untracedRequired = false)
    def spanId(spanId: String): LogRecordExpectation = copy(expectedSpanId = Some(spanId), untracedRequired = false)
    def untraced: LogRecordExpectation = copy(expectedTraceId = None, expectedSpanId = None, untracedRequired = true)
    def attributes(expectation: AttributesExpectation): LogRecordExpectation =
      copy(attributesExpectation = Some(expectation))
    def attributesExact(attributes: Attributes): LogRecordExpectation =
      this.attributes(AttributesExpectation.exact(attributes))
    def attributesExact(attributes: Attribute[_]*): LogRecordExpectation = attributesExact(Attributes(attributes: _*))
    def attributesSubset(attributes: Attributes): LogRecordExpectation =
      this.attributes(AttributesExpectation.subset(attributes))
    def attributesSubset(attributes: Attribute[_]*): LogRecordExpectation =
      attributesSubset(Attributes(attributes: _*))
    def attributesEmpty: LogRecordExpectation = attributesExact(Attributes.empty)
    def scope(expectation: InstrumentationScopeExpectation): LogRecordExpectation =
      copy(scopeExpectation = Some(expectation))
    def resource(expectation: TelemetryResourceExpectation): LogRecordExpectation =
      copy(resourceExpectation = Some(expectation))
    def observedTimestampWhere(f: Long => Boolean): LogRecordExpectation =
      copy(observedTimestampPredicates = observedTimestampPredicates :+ (f -> None))
    def observedTimestampWhere(clue: String)(f: Long => Boolean): LogRecordExpectation =
      copy(observedTimestampPredicates = observedTimestampPredicates :+ (f -> Some(clue)))
    def timestampWhere(f: Long => Boolean): LogRecordExpectation =
      copy(timestampPredicates = timestampPredicates :+ (f -> None))
    def timestampWhere(clue: String)(f: Long => Boolean): LogRecordExpectation =
      copy(timestampPredicates = timestampPredicates :+ (f -> Some(clue)))
    def clue(text: String): LogRecordExpectation = copy(clue = Some(text))
    def where(f: LogRecordData => Boolean): LogRecordExpectation = copy(predicates = predicates :+ (f -> None))
    def where(clue: String)(f: LogRecordData => Boolean): LogRecordExpectation =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(record: LogRecordData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        expectedBodyValue.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = LogRecordExpectation.bodyValue(record)
          if (actual.contains(expected)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.BodyMismatch(expected, actual))
        },
        expectedSeverityValue.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = severityOf(record)
          if (actual.contains(expected)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.SeverityMismatch(expected, actual))
        },
        expectedSeverityText.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = Option(record.getSeverityText)
          if (actual.contains(expected)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.SeverityTextMismatch(expected, actual))
        },
        expectedTraceId.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = validTraceId(record)
          if (actual.contains(expected)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TraceIdMismatch(expected, actual))
        },
        expectedSpanId.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = validSpanId(record)
          if (actual.contains(expected)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.SpanIdMismatch(expected, actual))
        },
        if (untracedRequired) {
          val spanContext = record.getSpanContext
          if (spanContext.isValid)
            ExpectationChecks.mismatch(Mismatch.UntracedMismatch(spanContext.getTraceId, spanContext.getSpanId))
          else
            ExpectationChecks.success[Mismatch]
        } else ExpectationChecks.success[Mismatch],
        attributesExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(record.getAttributes.toScala))(Mismatch.AttributesMismatch(_))
        },
        scopeExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(scopeInfo(record)))(Mismatch.ScopeMismatch(_))
        },
        resourceExpectation.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(resourceInfo(record)))(Mismatch.ResourceMismatch(_))
        },
        ExpectationChecks.combine(observedTimestampPredicates.map { case (predicate, clue) =>
          if (predicate(record.getObservedTimestampEpochNanos)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.ObservedTimestampMismatch(clue))
        }),
        ExpectationChecks.combine(timestampPredicates.map { case (predicate, clue) =>
          if (predicate(record.getTimestampEpochNanos)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TimestampMismatch(clue))
        }),
        ExpectationChecks.combine(predicates.map { case (predicate, clue) =>
          if (predicate(record)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.PredicateMismatch(clue))
        })
      )
  }

  private[logs] def bodyValue(record: LogRecordData): Option[AnyValue] =
    Option(record.getBodyValue).map(_.toScala)

  private[logs] def severityOf(record: LogRecordData): Option[Severity] =
    SeverityConversions.toScala(record.getSeverity)

  private def validTraceId(record: LogRecordData): Option[String] =
    Option.when(record.getSpanContext.isValid)(record.getSpanContext.getTraceId)

  private def validSpanId(record: LogRecordData): Option[String] =
    Option.when(record.getSpanContext.isValid)(record.getSpanContext.getSpanId)

  private def scopeInfo(record: LogRecordData): JInstrumentationScopeInfo =
    record.getInstrumentationScopeInfo

  private def resourceInfo(record: LogRecordData): JResource =
    record.getResource
}
