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

package org.typelevel.otel4s
package sdk
package common

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.scalaccompat.annotation.threadUnsafe3

/** Holds information about instrumentation scope.
  *
  * Instrumentation scope is a logical unit of the application code with which emitted telemetry is associated. The most
  * common approach is to use the instrumentation library as the scope, however other scopes are also common, e.g. a
  * module, a package, or a class may be chosen as the instrumentation scope.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/glossary/#instrumentation-scope Instrumentation Scope spec]]
  */
sealed trait InstrumentationScope {

  /** Returns the name of the instrumentation scope.
    */
  def name: String

  /** Returns the version of the instrumentation scope, or None if not available.
    */
  def version: Option[String]

  /** Returns the URL of the schema used by this instrumentation scope, or None if not available.
    */
  def schemaUrl: Option[String]

  /** Returns the attributes of this instrumentation scope.
    */
  def attributes: Attributes

  @threadUnsafe3
  override final lazy val hashCode: Int =
    Hash[InstrumentationScope].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: InstrumentationScope =>
        Hash[InstrumentationScope].eqv(this, other)
      case _ =>
        false
    }

  override final def toString: String =
    Show[InstrumentationScope].show(this)
}

object InstrumentationScope {

  /** A builder of [[InstrumentationScope]].
    */
  sealed trait Builder {

    /** Assigns the version to the scope.
      *
      * @param version
      *   the version to assign
      */
    def withVersion(version: String): Builder

    /** Assigns the schema URL to the scope.
      *
      * @param schemaUrl
      *   the schema URL to assign
      */
    def withSchemaUrl(schemaUrl: String): Builder

    /** Assigns the attributes to the scope.
      *
      * @note
      *   if called multiple times, only the last specified attributes will be used.
      *
      * @param attributes
      *   the attributes to assign
      */
    def withAttributes(attributes: Attributes): Builder

    /** Creates an [[InstrumentationScope]] with the configuration of this builder.
      */
    def build: InstrumentationScope
  }

  private val Empty: InstrumentationScope =
    apply("", None, None, Attributes.empty)

  /** An empty [[InstrumentationScope]] */
  def empty: InstrumentationScope = Empty

  /** Creates a [[Builder]] of [[InstrumentationScope]].
    *
    * @param name
    *   the name of the instrumentation scope
    */
  def builder(name: String): Builder =
    ScopeImpl(name, None, None, Attributes.empty)

  /** Creates an [[InstrumentationScope]].
    *
    * @param name
    *   the name of the instrumentation scope
    *
    * @param version
    *   the version of the instrumentation scope if the scope has a version (e.g. a library version)
    *
    * @param schemaUrl
    *   the Schema URL that should be recorded in the emitted telemetry
    *
    * @param attributes
    *   the instrumentation scope attributes to associate with emitted telemetry
    */
  def apply(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ): InstrumentationScope =
    ScopeImpl(name, version, schemaUrl, attributes)

  implicit val showInstrumentationScope: Show[InstrumentationScope] =
    Show.show { scope =>
      show"InstrumentationScope{name=${scope.name}, version=${scope.version}, schemaUrl=${scope.schemaUrl}, attributes=${scope.attributes}}"
    }

  implicit val hashInstrumentationScope: Hash[InstrumentationScope] =
    Hash.by { scope =>
      (scope.name, scope.version, scope.schemaUrl, scope.attributes)
    }

  private final case class ScopeImpl(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ) extends InstrumentationScope
      with Builder {
    def withVersion(version: String): Builder =
      copy(version = Some(version))

    def withSchemaUrl(schemaUrl: String): Builder =
      copy(schemaUrl = Some(schemaUrl))

    def withAttributes(attributes: Attributes): Builder =
      copy(attributes = attributes)

    def build: InstrumentationScope =
      this
  }

}
