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
import io.opentelemetry.sdk.resources.{Resource => JResource}
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.AttributeConverters._

/** A representation of the `io.opentelemetry.sdk.resources.Resource`.
  */
sealed trait TelemetryResource {

  def attributes: Attributes

  def schemaUrl: Option[String]

  override final lazy val hashCode: Int =
    Hash[TelemetryResource].hash(this)

  override final def toString: String =
    Show[TelemetryResource].show(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: TelemetryResource => Hash[TelemetryResource].eqv(this, other)
      case _                        => false
    }
}

object TelemetryResource {

  def apply(attributes: Attributes): TelemetryResource =
    Impl(attributes, None)

  def apply(
      attributes: Attributes,
      schemaUrl: Option[String]
  ): TelemetryResource =
    Impl(attributes, schemaUrl)

  def apply(resource: JResource): TelemetryResource =
    Impl(
      schemaUrl = Option(resource.getSchemaUrl),
      attributes = resource.getAttributes.toScala
    )

  implicit val telemetryResourceHash: Hash[TelemetryResource] =
    Hash.by(p => (p.schemaUrl, p.attributes))

  implicit val telemetryResourceShow: Show[TelemetryResource] =
    Show.show { r =>
      show"TelemetryResource{attributes=${r.attributes}, schemaUrl=${r.schemaUrl}}"
    }

  private final case class Impl(
      attributes: Attributes,
      schemaUrl: Option[String]
  ) extends TelemetryResource

}
