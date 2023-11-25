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

import cats.Hash
import cats.Show
import cats.syntax.all._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.Resource.ResourceInitializationError
import org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes._

/** [[Resource]] serves as a representation of a resource that captures
  * essential identifying information regarding the entities associated with
  * reported signals, such as statistics or traces.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/resource/sdk]]
  */
sealed trait Resource {

  /** An [[Attributes]] associated with the resource.
    */
  def attributes: Attributes

  /** An optional schema URL associated with the resource.
    */
  def schemaUrl: Option[String]

  /** Merges [[Resource]] into another [[Resource]].
    *
    * Schema URL merge outcomes:
    *   - if `this` resource's schema URL is empty then the `other` resource's
    *     schema URL will be selected
    *
    *   - if `other` resource's schema URL is empty then `this` resource's
    *     schema URL will be selected
    *
    *   - if `this` and `other` resources have the same non-empty schema URL
    *     then this schema URL will be selected
    *
    *   - if `this` and `other` resources have different non-empty schema URLs
    *     then the result will be a merge error
    *
    * @note
    *   if the same attribute exists in both resources, the attribute from the
    *   `other` [[Resource]] will be retained.
    *
    * @param other
    *   the other [[Resource]] to merge with
    *
    * @return
    *   a new [[Resource]] with the merged attributes
    */
  def merge(other: Resource): Either[ResourceInitializationError, Resource]

  /** Unsafe version of [[merge]] which throws an exception if the merge fails.
    *
    * @param other
    *   the other [[Resource]] to merge with
    */
  def mergeUnsafe(other: Resource): Resource

  override final def hashCode(): Int =
    Hash[Resource].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: Resource => Hash[Resource].eqv(this, other)
      case _               => false
    }

  override final def toString: String =
    Show[Resource].show(this)

}

object Resource {
  private val Empty: Resource =
    Resource(Attributes.empty, None)

  private val Default: Resource = {
    val telemetrySdk = Attributes(
      Attribute(TelemetrySdkName, "otel4s"),
      Attribute(TelemetrySdkLanguage, TelemetrySdkLanguageValue.Scala.value),
      Attribute(TelemetrySdkVersion, BuildInfo.version)
    )

    val mandatory = Attributes(
      Attribute(ServiceName, "unknown_service:scala")
    )

    Resource(telemetrySdk |+| mandatory, None)
  }

  sealed abstract class ResourceInitializationError extends Throwable
  object ResourceInitializationError {
    case object SchemaUrlConflict extends ResourceInitializationError
  }

  /** Creates a [[Resource]] with the given `attributes`. The `schemaUrl` will
    * be `None.`
    *
    * @param attributes
    *   the attributes to associate with the resource
    */
  def apply(attributes: Attributes): Resource =
    Impl(attributes, None)

  /** Creates a [[Resource]] with the given `attributes` and `schemaUrl`.
    *
    * @param attributes
    *   the attributes to associate with the resource
    *
    * @param schemaUrl
    *   schema URL to associate with the result
    */
  def apply(attributes: Attributes, schemaUrl: Option[String]): Resource =
    Impl(attributes, schemaUrl)

  /** Returns an empty [[Resource]].
    *
    * It is strongly recommended to start with [[Resource.default]] instead of
    * this method to include SDK required attributes.
    */
  def empty: Resource = Empty

  /** Returns the default [[Resource]]. This resource contains the default
    * attributes provided by the SDK.
    */
  def default: Resource = Default

  implicit val showResource: Show[Resource] =
    r => show"Resource{attributes=${r.attributes}, schemaUrl=${r.schemaUrl}}"

  implicit val hashResource: Hash[Resource] =
    Hash.by(r => (r.attributes, r.schemaUrl))

  private final case class Impl(
      attributes: Attributes,
      schemaUrl: Option[String]
  ) extends Resource {

    def merge(other: Resource): Either[ResourceInitializationError, Resource] =
      if (other == Resource.Empty) Right(this)
      else if (this == Resource.Empty) Right(other)
      else {
        (other.schemaUrl, schemaUrl) match {
          case (Some(otherUrl), Some(url)) if otherUrl != url =>
            Left(ResourceInitializationError.SchemaUrlConflict)

          case (otherUrl, url) =>
            Right(Impl(attributes |+| other.attributes, otherUrl.orElse(url)))
        }
      }

    def mergeUnsafe(other: Resource): Resource =
      merge(other).fold(throw _, identity)

  }

}
