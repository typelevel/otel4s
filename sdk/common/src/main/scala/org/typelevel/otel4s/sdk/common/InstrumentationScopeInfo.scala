/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk
package common

import cats.Hash
import cats.Show
import cats.syntax.show._

/** Holds information about instrumentation scope.
  *
  * Instrumentation scope is a logical unit of the application code with which
  * emitted telemetry is associated. The most common approach is to use the
  * instrumentation library as the scope, however other scopes are also common,
  * e.g. a module, a package, or a class may be chosen as the instrumentation
  * scope.
  */
sealed trait InstrumentationScopeInfo {

  /** Returns the name of the instrumentation scope.
    */
  def name: String

  /** Returns the version of the instrumentation scope, or None if not
    * available.
    */
  def version: Option[String]

  /** Returns the URL of the schema used by this instrumentation scope, or None
    * if not available.
    */
  def schemaUrl: Option[String]

  /** Returns the attributes of this instrumentation scope.
    */
  def attributes: Attributes

  override final def hashCode(): Int =
    Hash[InstrumentationScopeInfo].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: InstrumentationScopeInfo =>
        Hash[InstrumentationScopeInfo].eqv(this, other)
      case _ =>
        false
    }

  override final def toString: String =
    Show[InstrumentationScopeInfo].show(this)
}

object InstrumentationScopeInfo {

  /** A builder for [[InstrumentationScopeInfo]].
    */
  sealed trait Builder {

    /** Sets the version.
      *
      * @param version
      *   the version to set
      */
    def setVersion(version: String): Builder

    /** Sets the schema URL.
      *
      * @param schemaUrl
      *   the schema URL to set
      */
    def setSchemaUrl(schemaUrl: String): Builder

    /** Sets the attributes.
      *
      * @param attributes
      *   the attributes to set
      */
    def setAttributes(attributes: Attributes): Builder

    /** Creates an [[InstrumentationScopeInfo]] with the configuration of this
      * builder.
      */
    def build: InstrumentationScopeInfo
  }

  val Empty: InstrumentationScopeInfo =
    create("", None, None, Attributes.Empty)

  def builder(name: String): Builder =
    BuilderImpl(name, None, None, Attributes.Empty)

  def create(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ): InstrumentationScopeInfo =
    ScopeInfoImpl(name, version, schemaUrl, attributes)

  implicit val showInstrumentationScopeInfo: Show[InstrumentationScopeInfo] =
    Show.show { scope =>
      show"InstrumentationScopeInfo{name=${scope.name}, version=${scope.version}, schemaUrl=${scope.schemaUrl}, attributes=${scope.attributes}}"
    }

  implicit val hashInstrumentationScopeInfo: Hash[InstrumentationScopeInfo] =
    Hash.by { scope =>
      (scope.name, scope.version, scope.schemaUrl, scope.attributes)
    }

  private final case class ScopeInfoImpl(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ) extends InstrumentationScopeInfo

  private final case class BuilderImpl(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ) extends Builder {
    def setVersion(version: String): Builder =
      copy(version = Some(version))

    def setSchemaUrl(schemaUrl: String): Builder =
      copy(schemaUrl = Some(schemaUrl))

    def setAttributes(attributes: Attributes): Builder =
      copy(attributes = attributes)

    def build: InstrumentationScopeInfo =
      ScopeInfoImpl(name, version, schemaUrl, attributes)
  }
}
