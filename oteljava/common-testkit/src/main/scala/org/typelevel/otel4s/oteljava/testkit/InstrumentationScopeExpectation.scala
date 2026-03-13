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
import org.typelevel.otel4s.{Attribute, Attributes}
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

  /** Requires the instrumentation scope attributes to match exactly. */
  def withAttributesExact(attributes: Attribute[_]*): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to contain the given subset. */
  def withAttributesSubset(attributes: Attributes): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to contain the given subset. */
  def withAttributesSubset(attributes: Attribute[_]*): InstrumentationScopeExpectation

  /** Checks the given instrumentation scope and returns structured failures when the expectation does not match. */
  def check(scope: JInstrumentationScopeInfo): Either[NonEmptyList[InstrumentationScopeExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given instrumentation scope. */
  final def matches(scope: JInstrumentationScopeInfo): Boolean =
    check(scope).isRight
}

object InstrumentationScopeExpectation {

  /** A structured reason explaining why an [[InstrumentationScopeExpectation]] did not match an actual scope. */
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {

    /** Indicates that the scope name differed from the expected one. */
    sealed trait NameMismatch extends Mismatch {
      def expected: String
      def actual: String
    }

    /** Indicates that the scope version differed from the expected one. */
    sealed trait VersionMismatch extends Mismatch {
      def expected: Option[String]
      def actual: Option[String]
    }

    /** Indicates that the scope schema URL differed from the expected one. */
    sealed trait SchemaUrlMismatch extends Mismatch {
      def expected: Option[String]
      def actual: Option[String]
    }

    /** Indicates that the scope attributes did not satisfy the nested attributes expectation. */
    sealed trait AttributesMismatch extends Mismatch {
      def mismatches: NonEmptyList[AttributesExpectation.Mismatch]
    }

    /** Creates a mismatch indicating that the scope name differed from the expected one. */
    def nameMismatch(expected: String, actual: String): NameMismatch =
      NameMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the scope version differed from the expected one. */
    def versionMismatch(expected: Option[String], actual: Option[String]): VersionMismatch =
      VersionMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the scope schema URL differed from the expected one. */
    def schemaUrlMismatch(expected: Option[String], actual: Option[String]): SchemaUrlMismatch =
      SchemaUrlMismatchImpl(expected, actual)

    /** Creates a mismatch indicating that the scope attributes did not satisfy the nested attributes expectation. */
    def attributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch]): AttributesMismatch =
      AttributesMismatchImpl(mismatches)

    private final case class NameMismatchImpl(expected: String, actual: String) extends NameMismatch {
      def message: String =
        s"name mismatch: expected '$expected', got '$actual'"
    }

    private final case class VersionMismatchImpl(expected: Option[String], actual: Option[String])
        extends VersionMismatch {
      def message: String = {
        val exp = expected.fold("<missing>")(v => s"'$v'")
        val act = actual.fold("<missing>")(v => s"'$v'")
        s"version mismatch: expected $exp, got $act"
      }
    }

    private final case class SchemaUrlMismatchImpl(expected: Option[String], actual: Option[String])
        extends SchemaUrlMismatch {
      def message: String = {
        val exp = expected.fold("<missing>")(v => s"'$v'")
        val act = actual.fold("<missing>")(v => s"'$v'")
        s"schema URL mismatch: expected $exp, got $act"
      }
    }

    private final case class AttributesMismatchImpl(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends AttributesMismatch {
      def message: String =
        s"attributes mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
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

    def withAttributesExact(attributes: Attribute[_]*): InstrumentationScopeExpectation =
      withAttributesExact(Attributes(attributes *))

    def withAttributesSubset(attributes: Attributes): InstrumentationScopeExpectation =
      withAttributes(AttributesExpectation.subset(attributes))

    def withAttributesSubset(attributes: Attribute[_]*): InstrumentationScopeExpectation =
      withAttributesSubset(Attributes(attributes *))

    def check(scope: JInstrumentationScopeInfo): Either[NonEmptyList[Mismatch], Unit] = {

      def checkName =
        name match {
          case Some(expected) =>
            Either.cond(
              expected == scope.getName,
              (),
              NonEmptyList.one(Mismatch.nameMismatch(expected, scope.getName))
            )

          case None =>
            ExpectationChecks.success
        }

      ExpectationChecks.combine(
        checkName,
        ExpectationChecks.compareOption(version, Option(scope.getVersion))(Mismatch.versionMismatch),
        ExpectationChecks.compareOption(schemaUrl, Option(scope.getSchemaUrl))(Mismatch.schemaUrlMismatch),
        attributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(scope.getAttributes.toScala))(Mismatch.attributesMismatch)
        }
      )
    }
  }

}
