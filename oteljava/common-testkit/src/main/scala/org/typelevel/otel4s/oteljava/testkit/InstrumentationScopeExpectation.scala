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

  /** Returns `true` if this expectation matches the given instrumentation scope. */
  def matches(scope: JInstrumentationScopeInfo): Boolean
}

object InstrumentationScopeExpectation {

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

    def withAttributesSubset(attributes: Attributes): InstrumentationScopeExpectation =
      withAttributes(AttributesExpectation.subset(attributes))

    def matches(scope: JInstrumentationScopeInfo): Boolean =
      name.forall(_ == scope.getName) &&
        version.forall(_ == Option(scope.getVersion)) &&
        schemaUrl.forall(_ == Option(scope.getSchemaUrl)) &&
        attributes.forall(_.matches(scope.getAttributes.toScala))
  }
}
