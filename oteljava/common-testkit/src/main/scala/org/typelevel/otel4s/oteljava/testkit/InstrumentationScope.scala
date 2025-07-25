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

import cats.Hash
import cats.Show
import cats.syntax.show._
import io.opentelemetry.sdk.common.{InstrumentationScopeInfo => JInstrumentationScopeInfo}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A representation of the `io.opentelemetry.sdk.common.InstrumentationScopeInfo`.
  */
sealed trait InstrumentationScope {

  def name: String

  def version: Option[String]

  def schemaUrl: Option[String]

  def attributes: Attributes

  override final def hashCode(): Int =
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

  def apply(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ): InstrumentationScope =
    Impl(name, version, schemaUrl, attributes)

  def apply(scope: JInstrumentationScopeInfo): InstrumentationScope =
    Impl(
      name = scope.getName,
      version = Option(scope.getVersion),
      schemaUrl = Option(scope.getSchemaUrl),
      attributes = scope.getAttributes.toScala
    )

  implicit val instrumentationScopeHash: Hash[InstrumentationScope] =
    Hash.by { scope =>
      (scope.name, scope.version, scope.schemaUrl, scope.attributes)
    }

  implicit val instrumentationScopeShow: Show[InstrumentationScope] =
    Show.show { scope =>
      show"InstrumentationScope{name=${scope.name}, version=${scope.version}, schemaUrl=${scope.schemaUrl}, attributes=${scope.attributes}}"
    }

  private final case class Impl(
      name: String,
      version: Option[String],
      schemaUrl: Option[String],
      attributes: Attributes
  ) extends InstrumentationScope

}
