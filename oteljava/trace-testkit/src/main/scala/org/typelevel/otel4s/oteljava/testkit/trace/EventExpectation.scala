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
import io.opentelemetry.sdk.trace.data.{EventData => JEventData}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.testkit.AttributesExpectation
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks

import scala.concurrent.duration._
import scala.concurrent.duration.FiniteDuration

/** A partial expectation for a single OpenTelemetry Java `EventData`. */
sealed trait EventExpectation {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the event name to match exactly. */
  def name(name: String): EventExpectation

  /** Requires the event timestamp to match exactly. */
  def timestamp(timestamp: FiniteDuration): EventExpectation

  /** Requires the event attributes to match the given expectation. */
  def attributes(expectation: AttributesExpectation): EventExpectation

  /** Requires the event attributes to match exactly. */
  def attributesExact(attributes: Attributes): EventExpectation

  /** Requires the event attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): EventExpectation

  /** Requires the event attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attributes): EventExpectation

  /** Requires the event attributes to contain at least the given subset. */
  def attributesSubset(attributes: Attribute[_]*): EventExpectation

  /** Requires the event to have no attributes. */
  def attributesEmpty: EventExpectation

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): EventExpectation

  /** Adds a custom predicate over the event data. */
  def where(f: JEventData => Boolean): EventExpectation

  /** Adds a custom predicate over the event data with a clue shown in mismatches. */
  def where(clue: String)(f: JEventData => Boolean): EventExpectation

  /** Checks the given event and returns structured mismatches when the expectation does not match. */
  def check(event: JEventData): Either[NonEmptyList[EventExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given event. */
  final def matches(event: JEventData): Boolean =
    check(event).isRight
}

object EventExpectation {

  /** A structured reason explaining why an [[EventExpectation]] did not match an event. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class NameMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"name mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class TimestampMismatch(expected: FiniteDuration, actual: FiniteDuration)
        extends Mismatch {
      def message: String =
        s"timestamp mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class AttributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"attributes mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class PredicateMismatch(clue: Option[String]) extends Mismatch {
      def message: String =
        s"event predicate returned false${clue.fold("")(v => s": $v")}"
    }
  }

  /** Creates an expectation that leaves all event fields unconstrained. */
  def any: EventExpectation =
    Impl()

  /** Creates an expectation matching an event with the given name. */
  def name(name: String): EventExpectation =
    Impl(expectedName = Some(name))

  private final case class Impl(
      expectedName: Option[String] = None,
      expectedTimestamp: Option[FiniteDuration] = None,
      expectedAttributes: Option[AttributesExpectation] = None,
      clue: Option[String] = None,
      predicates: List[(JEventData => Boolean, Option[String])] = Nil
  ) extends EventExpectation {

    def name(name: String): EventExpectation =
      copy(expectedName = Some(name))

    def timestamp(timestamp: FiniteDuration): EventExpectation =
      copy(expectedTimestamp = Some(timestamp))

    def attributes(expectation: AttributesExpectation): EventExpectation =
      copy(expectedAttributes = Some(expectation))

    def attributesExact(attributes: Attributes): EventExpectation =
      copy(expectedAttributes = Some(AttributesExpectation.exact(attributes)))

    def attributesExact(attributes: Attribute[_]*): EventExpectation =
      attributesExact(Attributes(attributes: _*))

    def attributesSubset(attributes: Attributes): EventExpectation =
      copy(expectedAttributes = Some(AttributesExpectation.subset(attributes)))

    def attributesSubset(attributes: Attribute[_]*): EventExpectation =
      attributesSubset(Attributes(attributes: _*))

    def attributesEmpty: EventExpectation =
      attributesExact(Attributes.empty)

    def clue(text: String): EventExpectation =
      copy(clue = Some(text))

    def where(f: JEventData => Boolean): EventExpectation =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: JEventData => Boolean): EventExpectation =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(event: JEventData): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        expectedName.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == event.getName) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.NameMismatch(expected, event.getName))
        },
        expectedTimestamp.fold(ExpectationChecks.success[Mismatch]) { expected =>
          val actual = event.getEpochNanos.nanos
          if (expected == actual) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TimestampMismatch(expected, actual))
        },
        expectedAttributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(event.getAttributes.toScala))(Mismatch.AttributesMismatch(_))
        },
        ExpectationChecks.combine(predicates.map { case (predicate, clue) =>
          if (predicate(event)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.PredicateMismatch(clue))
        })
      )
  }
}
