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
import cats.syntax.show._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

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
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {
    private[testkit] final case class MissingAttribute(attribute: Attribute[_]) extends Mismatch {
      def message: String =
        show"missing attribute $attribute"
    }

    private[testkit] final case class UnexpectedAttribute(attribute: Attribute[_]) extends Mismatch {
      def message: String =
        show"unexpected attribute $attribute"
    }

    private[testkit] final case class AttributeValueMismatch(expected: Attribute[_], actual: Attribute[_])
        extends Mismatch {
      def message: String =
        show"attribute mismatch for '${expected.key.name}': expected $expected, got $actual"
    }

    private[testkit] final case class PredicateFailed(clue: Option[String]) extends Mismatch {
      def message: String =
        s"attributes predicate returned false${clue.fold("")(value => s": $value")}"
    }
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
  def where(f: Attributes => Boolean): AttributesExpectation =
    Predicate(f, None)

  /** Creates an expectation from a custom predicate with an optional clue used in mismatch messages. */
  def where(clue: String)(f: Attributes => Boolean): AttributesExpectation =
    Predicate(f, Some(clue))

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
