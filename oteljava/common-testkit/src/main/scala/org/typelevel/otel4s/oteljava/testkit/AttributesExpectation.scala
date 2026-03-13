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

import cats.data.NonEmptyList
import org.typelevel.otel4s.{Attribute, Attributes}

/** A partial expectation for [[Attributes]].
  *
  * Use [[AttributesExpectation.exact]] to require the full attribute set to match or [[AttributesExpectation.subset]]
  * to require only a subset.
  */
sealed trait AttributesExpectation {

  /** Checks the given attributes and returns structured failures when the expectation does not match. */
  def check(attributes: Attributes): Either[NonEmptyList[AttributesExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given attributes. */
  final def matches(attributes: Attributes): Boolean =
    check(attributes).isRight
}

object AttributesExpectation {

  /** A structured reason explaining why an [[AttributesExpectation]] did not match actual attributes. */
  sealed trait Mismatch extends Product with Serializable

  object Mismatch {

    /** Indicates that an expected attribute was missing. */
    final case class MissingAttribute(
        attribute: Attribute[_]
    ) extends Mismatch

    /** Indicates that an attribute was present unexpectedly. */
    final case class UnexpectedAttribute(
        attribute: Attribute[_]
    ) extends Mismatch

    /** Indicates that an attribute key was present, but its value differed from the expected one. */
    final case class AttributeValueMismatch(
        expected: Attribute[_],
        actual: Attribute[_]
    ) extends Mismatch

    /** Indicates that a custom predicate expectation returned `false`. */
    final case class PredicateFailed(clue: Option[String]) extends Mismatch
  }

  /** Creates an expectation that matches only when all attributes are equal. */
  def exact(attributes: Attributes): AttributesExpectation =
    Exact(attributes)

  /** Creates an expectation that matches when all expected attributes are present in the actual set. */
  def subset(attributes: Attributes): AttributesExpectation =
    Subset(attributes)

  /** Creates an expectation that matches only an empty attribute set. */
  def empty: AttributesExpectation =
    exact(Attributes.empty)

  /** Creates an expectation from a custom predicate. */
  def predicate(f: Attributes => Boolean): AttributesExpectation =
    Predicate(f, None)

  /** Creates an expectation from a custom predicate with an optional clue used in mismatch messages. */
  def predicate(clue: String)(f: Attributes => Boolean): AttributesExpectation =
    Predicate(f, Some(clue))

  /** Formats a mismatch into a human-readable message. */
  def formatMismatch(mismatch: Mismatch): String = {
    def formatAttribute(attribute: Attribute[_]): String =
      s"'${attribute.key.name}'='${attribute.value}'"

    mismatch match {
      case Mismatch.MissingAttribute(attribute) =>
        s"missing attribute ${formatAttribute(attribute)}"
      case Mismatch.UnexpectedAttribute(attribute) =>
        s"unexpected attribute ${formatAttribute(attribute)}"
      case Mismatch.AttributeValueMismatch(expected, actual) =>
        s"attribute mismatch for '${expected.key.name}': expected ${formatAttribute(expected)}, got ${formatAttribute(actual)}"
      case Mismatch.PredicateFailed(clue) =>
        clue.fold("attributes predicate returned false")(value => s"attributes predicate returned false: $value")
    }
  }

  private final case class Exact(expected: Attributes) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] = {
      val missingOrMismatched = expected.map { attribute =>
        attributes.get(attribute.key) match {
          case Some(actual) if actual == attribute =>
            ExpectationChecks.success
          case Some(actual) =>
            ExpectationChecks.mismatch(Mismatch.AttributeValueMismatch(attribute, actual))
          case None =>
            ExpectationChecks.mismatch(Mismatch.MissingAttribute(attribute))
        }
      }

      val unexpected = attributes.collect {
        case attribute if expected.get(attribute.key).isEmpty =>
          Left(NonEmptyList.one(Mismatch.UnexpectedAttribute(attribute)))
      }

      ExpectationChecks.combine((missingOrMismatched ++ unexpected).toList)
    }
  }

  private final case class Subset(expected: Attributes) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(expected.map { attribute =>
        attributes.get(attribute.key) match {
          case Some(actual) if actual == attribute =>
            ExpectationChecks.success
          case Some(actual) =>
            ExpectationChecks.mismatch(Mismatch.AttributeValueMismatch(attribute, actual))
          case None =>
            ExpectationChecks.mismatch(Mismatch.MissingAttribute(attribute))
        }
      }.toList)
  }

  private final case class Predicate(
      f: Attributes => Boolean,
      clue: Option[String]
  ) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] =
      Either.cond(f(attributes), (), NonEmptyList.one(Mismatch.PredicateFailed(clue)))
  }

}
