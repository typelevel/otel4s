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
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A partial expectation for OpenTelemetry Java `InstrumentationScopeInfo`.
  *
  * Unspecified properties are ignored. Use the fluent methods on this trait to constrain only the parts of the
  * instrumentation scope that matter for a given test.
  */
sealed trait InstrumentationScopeExpectation {

  /** Requires the instrumentation scope name to match exactly. */
  def name(name: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope version to match exactly.
    *
    * Use `Some(version)` to require a value or `None` to require that the version is absent.
    */
  def version(version: Option[String]): InstrumentationScopeExpectation

  /** Requires the instrumentation scope version to match exactly. */
  def version(version: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope schema URL to match exactly.
    *
    * Use `Some(schemaUrl)` to require a value or `None` to require that the schema URL is absent.
    */
  def schemaUrl(schemaUrl: Option[String]): InstrumentationScopeExpectation

  /** Requires the instrumentation scope schema URL to match exactly. */
  def schemaUrl(schemaUrl: String): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to satisfy the given expectation. */
  def attributes(expectation: AttributesExpectation): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to match exactly. */
  def attributesExact(attributes: Attributes): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to be empty. */
  def attributesEmpty: InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to contain the given subset. */
  def attributesSubset(attributes: Attributes): InstrumentationScopeExpectation

  /** Requires the instrumentation scope attributes to contain the given subset. */
  def attributesSubset(attributes: Attribute[_]*): InstrumentationScopeExpectation

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
    private[testkit] final case class NameMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"name mismatch: expected '$expected', got '$actual'"
    }

    private[testkit] final case class VersionMismatch(expected: Option[String], actual: Option[String])
        extends Mismatch {
      def message: String = {
        val exp = expected.fold("<missing>")(v => s"'$v'")
        val act = actual.fold("<missing>")(v => s"'$v'")
        s"version mismatch: expected $exp, got $act"
      }
    }

    private[testkit] final case class SchemaUrlMismatch(expected: Option[String], actual: Option[String])
        extends Mismatch {
      def message: String = {
        val exp = expected.fold("<missing>")(v => s"'$v'")
        val act = actual.fold("<missing>")(v => s"'$v'")
        s"schema URL mismatch: expected $exp, got $act"
      }
    }

    private[testkit] final case class AttributesMismatch(mismatches: NonEmptyList[AttributesExpectation.Mismatch])
        extends Mismatch {
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

    def name(name: String): InstrumentationScopeExpectation =
      copy(name = Some(name))

    def version(version: Option[String]): InstrumentationScopeExpectation =
      copy(version = Some(version))

    def version(value: String): InstrumentationScopeExpectation =
      this.version(Some(value))

    def schemaUrl(schemaUrl: Option[String]): InstrumentationScopeExpectation =
      copy(schemaUrl = Some(schemaUrl))

    def schemaUrl(value: String): InstrumentationScopeExpectation =
      this.schemaUrl(Some(value))

    def attributes(expectation: AttributesExpectation): InstrumentationScopeExpectation =
      copy(attributes = Some(expectation))

    def attributesExact(attributes: Attributes): InstrumentationScopeExpectation =
      this.attributes(AttributesExpectation.exact(attributes))

    def attributesExact(attributes: Attribute[_]*): InstrumentationScopeExpectation =
      attributesExact(Attributes(attributes *))

    def attributesEmpty: InstrumentationScopeExpectation =
      attributesExact(Attributes.empty)

    def attributesSubset(attributes: Attributes): InstrumentationScopeExpectation =
      this.attributes(AttributesExpectation.subset(attributes))

    def attributesSubset(attributes: Attribute[_]*): InstrumentationScopeExpectation =
      attributesSubset(Attributes(attributes *))

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
        checkName,
        ExpectationChecks.compareOption(version, Option(scope.getVersion))(Mismatch.VersionMismatch),
        ExpectationChecks.compareOption(schemaUrl, Option(scope.getSchemaUrl))(Mismatch.SchemaUrlMismatch),
        attributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(scope.getAttributes.toScala))(Mismatch.AttributesMismatch)
        }
      )
    }
  }

}
