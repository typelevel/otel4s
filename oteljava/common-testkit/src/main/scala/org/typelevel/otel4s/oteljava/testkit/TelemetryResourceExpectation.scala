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

import io.opentelemetry.sdk.resources.{Resource => JResource}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A partial expectation for OpenTelemetry Java [[JResource]].
  *
  * Unspecified properties are ignored.
  */
sealed trait TelemetryResourceExpectation {

  /** Requires the resource attributes to satisfy the given expectation. */
  def withAttributes(expectation: AttributesExpectation): TelemetryResourceExpectation

  /** Requires the resource attributes to match exactly. */
  def withAttributesExact(attributes: Attributes): TelemetryResourceExpectation

  /** Requires the resource attributes to contain the given subset. */
  def withAttributesSubset(attributes: Attributes): TelemetryResourceExpectation

  /** Requires the resource schema URL to match exactly.
    *
    * Use `Some(schemaUrl)` to require a value or `None` to require that the schema URL is absent.
    */
  def withSchemaUrl(schemaUrl: Option[String]): TelemetryResourceExpectation

  /** Requires the resource schema URL to match exactly. */
  def withSchemaUrl(schemaUrl: String): TelemetryResourceExpectation

  /** Returns `true` if this expectation matches the given telemetry resource. */
  def matches(resource: JResource): Boolean
}

object TelemetryResourceExpectation {

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

    def withAttributes(expectation: AttributesExpectation): TelemetryResourceExpectation =
      copy(attributes = Some(expectation))

    def withAttributesExact(attributes: Attributes): TelemetryResourceExpectation =
      withAttributes(AttributesExpectation.exact(attributes))

    def withAttributesSubset(attributes: Attributes): TelemetryResourceExpectation =
      withAttributes(AttributesExpectation.subset(attributes))

    def withSchemaUrl(schemaUrl: Option[String]): TelemetryResourceExpectation =
      copy(schemaUrl = Some(schemaUrl))

    def withSchemaUrl(schemaUrl: String): TelemetryResourceExpectation =
      withSchemaUrl(Some(schemaUrl))

    def matches(resource: JResource): Boolean =
      attributes.forall(_.matches(resource.getAttributes.toScala)) &&
        schemaUrl.forall(_ == Option(resource.getSchemaUrl))
  }
}
