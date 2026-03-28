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
import io.opentelemetry.sdk.resources.{Resource => JResource}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A partial expectation for OpenTelemetry Java `Resource`.
  *
  * Unspecified properties are ignored.
  */
sealed trait TelemetryResourceExpectation {

  /** Requires the resource attributes to satisfy the given expectation. */
  def attributes(expectation: AttributesExpectation): TelemetryResourceExpectation

  /** Requires the resource attributes to match exactly. */
  def attributesExact(attributes: Attributes): TelemetryResourceExpectation

  /** Requires the resource attributes to match exactly. */
  def attributesExact(attributes: Attribute[_]*): TelemetryResourceExpectation

  /** Requires the resource attributes to be empty. */
  def attributesEmpty: TelemetryResourceExpectation

  /** Requires the resource attributes to contain the given subset. */
  def attributesSubset(attributes: Attributes): TelemetryResourceExpectation

  /** Requires the resource attributes to contain the given subset. */
  def attributesSubset(attributes: Attribute[_]*): TelemetryResourceExpectation

  /** Requires the resource schema URL to match exactly.
    *
    * Use `Some(schemaUrl)` to require a value or `None` to require that the schema URL is absent.
    */
  def schemaUrl(schemaUrl: Option[String]): TelemetryResourceExpectation

  /** Requires the resource schema URL to match exactly. */
  def schemaUrl(schemaUrl: String): TelemetryResourceExpectation

  /** Checks the given telemetry resource and returns structured failures when the expectation does not match. */
  def check(resource: JResource): Either[NonEmptyList[TelemetryResourceExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given telemetry resource. */
  final def matches(resource: JResource): Boolean =
    check(resource).isRight
}

object TelemetryResourceExpectation {

  /** A structured reason explaining why a [[TelemetryResourceExpectation]] did not match an actual resource. */
  sealed trait Mismatch extends Product with Serializable {

    /** A human-readable description of the mismatch. */
    def message: String
  }

  object Mismatch {
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

  /** Creates an expectation that matches the full telemetry resource exactly. */
  def exact(resource: JResource): TelemetryResourceExpectation =
    Impl(
      attributes = Some(AttributesExpectation.exact(resource.getAttributes.toScala)),
      schemaUrl = Some(Option(resource.getSchemaUrl))
    )

  /** Creates an expectation that matches any telemetry resource. */
  def any: TelemetryResourceExpectation =
    Impl()

  private final case class Impl(
      attributes: Option[AttributesExpectation] = None,
      schemaUrl: Option[Option[String]] = None
  ) extends TelemetryResourceExpectation {

    def attributes(expectation: AttributesExpectation): TelemetryResourceExpectation =
      copy(attributes = Some(expectation))

    def attributesExact(attributes: Attributes): TelemetryResourceExpectation =
      this.attributes(AttributesExpectation.exact(attributes))

    def attributesExact(attributes: Attribute[_]*): TelemetryResourceExpectation =
      attributesExact(Attributes(attributes *))

    def attributesEmpty: TelemetryResourceExpectation =
      attributesExact(Attributes.empty)

    def attributesSubset(attributes: Attributes): TelemetryResourceExpectation =
      this.attributes(AttributesExpectation.subset(attributes))

    def attributesSubset(attributes: Attribute[_]*): TelemetryResourceExpectation =
      attributesSubset(Attributes(attributes *))

    def schemaUrl(schemaUrl: Option[String]): TelemetryResourceExpectation =
      copy(schemaUrl = Some(schemaUrl))

    def schemaUrl(value: String): TelemetryResourceExpectation =
      this.schemaUrl(Some(value))

    def check(resource: JResource): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        attributes.fold(ExpectationChecks.success[Mismatch]) { expected =>
          ExpectationChecks.nested(expected.check(resource.getAttributes.toScala))(Mismatch.AttributesMismatch)
        },
        ExpectationChecks.compareOption(schemaUrl, Option(resource.getSchemaUrl))(Mismatch.SchemaUrlMismatch),
      )
  }

}
