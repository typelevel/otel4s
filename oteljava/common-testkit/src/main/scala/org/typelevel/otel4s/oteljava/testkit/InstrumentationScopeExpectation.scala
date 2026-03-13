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
import io.opentelemetry.sdk.common.{InstrumentationScopeInfo => JInstrumentationScopeInfo}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A partial expectation for OpenTelemetry Java [[JInstrumentationScopeInfo]].
  *
  * Unspecified properties are ignored. Use the fluent methods on this trait to constrain only the parts of the
  * instrumentation scope that matter for a given test.
  */
sealed trait InstrumentationScopeExpectation {

  /** Requires the instrumentation scope name to match exactly. */
  def withName(name: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope version to match exactly.
    *
    * Use `Some(version)` to require a value or `None` to require that the version is absent.
    */
  def withVersion(version: Option[String]): InstrumentationScopeExpectation

  /** Requires the instrumentation scope version to match exactly. */
  def withVersion(version: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope schema URL to match exactly.
    *
    * Use `Some(schemaUrl)` to require a value or `None` to require that the schema URL is absent.
    */
  def withSchemaUrl(schemaUrl: Option[String]): InstrumentationScopeExpectation

  /** Requires the instrumentation scope schema URL to match exactly. */
  def withSchemaUrl(schemaUrl: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to satisfy the given expectation. */
  def withAttributes(expectation: AttributesExpectation): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to match exactly. */
  def withAttributesExact(attributes: Attributes): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to contain the given subset. */
  def withAttributesSubset(attributes: Attributes): InstrumentationScopeExpectation

  /** Checks the given instrumentation scope and returns structured failures when the expectation does not match. */
  def check(scope: JInstrumentationScopeInfo): Either[NonEmptyList[InstrumentationScopeExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given instrumentation scope. */
  final def matches(scope: JInstrumentationScopeInfo): Boolean =
    check(scope).isRight
}

object InstrumentationScopeExpectation {

  /** A structured reason explaining why an [[InstrumentationScopeExpectation]] did not match an actual scope. */
  sealed trait Mismatch extends Product with Serializable

  object Mismatch {

    /** Indicates that the scope name differed from the expected one. */
    final case class NameMismatch(
        expected: String,
        actual: String
    ) extends Mismatch

    /** Indicates that the scope version differed from the expected one. */
    final case class VersionMismatch(
        expected: Option[String],
        actual: Option[String]
    ) extends Mismatch

    /** Indicates that the scope schema URL differed from the expected one. */
    final case class SchemaUrlMismatch(
        expected: Option[String],
        actual: Option[String]
    ) extends Mismatch

    /** Indicates that the scope attributes did not satisfy the nested attributes expectation. */
    final case class AttributesMismatch(
        failures: NonEmptyList[AttributesExpectation.Mismatch]
    ) extends Mismatch
  }

  /** Creates an expectation that matches any instrumentation scope with the given name. */
  def name(name: String): InstrumentationScopeExpectation =
    Impl(name = Some(name))

  /** Creates an expectation that matches the full instrumentation scope exactly. */
  def exact(scope: JInstrumentationScopeInfo): InstrumentationScopeExpectation =
    Impl(
      name = Some(scope.getName),
      version = Some(Option(scope.getVersion)),
      schemaUrl = Some(Option(scope.getSchemaUrl)),
      attributes = Some(AttributesExpectation.exact(scope.getAttributes.toScala))
    )

  /** Formats a mismatch into a human-readable message. */
  def formatMismatch(mismatch: Mismatch): String = {
    def formatOption(value: Option[String]): String =
      value.fold("<missing>")(v => s"'$v'")

    mismatch match {
      case Mismatch.NameMismatch(expected, actual) =>
        s"name mismatch: expected '$expected', got '$actual'"
      case Mismatch.VersionMismatch(expected, actual) =>
        s"version mismatch: expected ${formatOption(expected)}, got ${formatOption(actual)}"
      case Mismatch.SchemaUrlMismatch(expected, actual) =>
        s"schema URL mismatch: expected ${formatOption(expected)}, got ${formatOption(actual)}"
      case Mismatch.AttributesMismatch(mismatches) =>
        s"attributes mismatch: ${mismatches.toList.map(AttributesExpectation.formatMismatch).mkString(", ")}"
    }
  }

  private final case class Impl(
      name: Option[String] = None,
      version: Option[Option[String]] = None,
      schemaUrl: Option[Option[String]] = None,
      attributes: Option[AttributesExpectation] = None
  ) extends InstrumentationScopeExpectation {

    def withName(name: String): InstrumentationScopeExpectation =
      copy(name = Some(name))

    def withVersion(version: Option[String]): InstrumentationScopeExpectation =
      copy(version = Some(version))

    def withVersion(version: String): InstrumentationScopeExpectation =
      withVersion(Some(version))

    def withSchemaUrl(schemaUrl: Option[String]): InstrumentationScopeExpectation =
      copy(schemaUrl = Some(schemaUrl))

    def withSchemaUrl(schemaUrl: String): InstrumentationScopeExpectation =
      withSchemaUrl(Some(schemaUrl))

    def withAttributes(expectation: AttributesExpectation): InstrumentationScopeExpectation =
      copy(attributes = Some(expectation))

    def withAttributesExact(attributes: Attributes): InstrumentationScopeExpectation =
      withAttributes(AttributesExpectation.exact(attributes))

    def withAttributesSubset(attributes: Attributes): InstrumentationScopeExpectation =
      withAttributes(AttributesExpectation.subset(attributes))

    def check(scope: JInstrumentationScopeInfo): Either[NonEmptyList[Mismatch], Unit] = {

      def checkName =
        name match {
          case Some(expected) =>
            Either.cond(
              expected == scope.getName,
              (),
              NonEmptyList.one(Mismatch.NameMismatch(expected, scope.getName))
            )

          case None =>
            ExpectationChecks.success
        }

      ExpectationChecks.combine(
        List(
          checkName,
          ExpectationChecks.compareOption(version, Option(scope.getVersion))(Mismatch.VersionMismatch),
          ExpectationChecks.compareOption(schemaUrl, Option(scope.getSchemaUrl))(Mismatch.SchemaUrlMismatch),
          attributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
            ExpectationChecks.nested(expected.check(scope.getAttributes.toScala))(Mismatch.AttributesMismatch.apply)
          }
        )
      )
    }
  }

}
