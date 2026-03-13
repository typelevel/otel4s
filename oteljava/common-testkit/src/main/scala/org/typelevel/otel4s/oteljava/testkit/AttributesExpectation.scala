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
  sealed trait Mismatch

  object Mismatch {

    /** Indicates that an expected attribute was missing. */
    final case class MissingAttribute(
        key: String,
        expected: String
    ) extends Mismatch

    /** Indicates that an attribute was present unexpectedly. */
    final case class UnexpectedAttribute(
        key: String,
        actual: String
    ) extends Mismatch

    /** Indicates that an attribute key was present, but its value differed from the expected one. */
    final case class AttributeValueMismatch(
        key: String,
        expected: String,
        actual: String
    ) extends Mismatch

    /** Indicates that a custom predicate expectation returned `false`. */
    case object PredicateFailed extends Mismatch
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
    Predicate(f)

  private final case class Exact(expected: Attributes) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] = {
      val missingOrMismatched = expected.iterator.map { attribute =>
        attributes.get(attribute.key) match {
          case Some(actual) if actual == attribute =>
            ExpectationChecks.success
          case Some(actual) =>
            ExpectationChecks.failure(
              Mismatch.AttributeValueMismatch(attribute.key.name, attribute.value.toString, actual.value.toString)
            )
          case None =>
            ExpectationChecks.failure(
              Mismatch.MissingAttribute(attribute.key.name, attribute.value.toString)
            )
        }
      }

      val unexpected = attributes.iterator.collect {
        case attribute if expected.get(attribute.key).isEmpty =>
          ExpectationChecks.failure(
            Mismatch.UnexpectedAttribute(attribute.key.name, attribute.value.toString)
          )
      }

      ExpectationChecks.combine((missingOrMismatched ++ unexpected).toList)
    }
  }

  private final case class Subset(expected: Attributes) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(expected.iterator.map { attribute =>
        attributes.get(attribute.key) match {
          case Some(actual) if actual == attribute =>
            ExpectationChecks.success
          case Some(actual) =>
            ExpectationChecks.failure(
              Mismatch.AttributeValueMismatch(attribute.key.name, attribute.value.toString, actual.value.toString)
            )
          case None =>
            ExpectationChecks.failure(
              Mismatch.MissingAttribute(attribute.key.name, attribute.value.toString)
            )
        }
      }.toList)
  }

  private final case class Predicate(f: Attributes => Boolean) extends AttributesExpectation {
    def check(attributes: Attributes): Either[NonEmptyList[Mismatch], Unit] =
      if (f(attributes)) ExpectationChecks.success
      else ExpectationChecks.failure(Mismatch.PredicateFailed)
  }

  private object ExpectationChecks {
    def success: Either[NonEmptyList[Mismatch], Unit] =
      org.typelevel.otel4s.oteljava.testkit.ExpectationChecks.success

    def failure(
        failure: Mismatch
    ): Either[NonEmptyList[Mismatch], Unit] =
      org.typelevel.otel4s.oteljava.testkit.ExpectationChecks.failure(failure)

    def combine(
        results: Iterable[Either[NonEmptyList[Mismatch], Unit]]
    ): Either[NonEmptyList[Mismatch], Unit] =
      org.typelevel.otel4s.oteljava.testkit.ExpectationChecks.combine(results)
  }
}
