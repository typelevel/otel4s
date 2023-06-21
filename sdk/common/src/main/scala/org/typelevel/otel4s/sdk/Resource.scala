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

import cats.Show
import cats.implicits.catsSyntaxSemigroup
import cats.implicits.showInterpolator
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.BuildInfo
import org.typelevel.otel4s.semconv.resource.attributes.ResourceAttributes._

/** [[Resource]] serves as a representation of a resource that captures
  * essential identifying information regarding the entities associated with
  * reported signals, such as statistics or traces.
  * @param attributes
  *   \- a collection of [[Attribute]]s
  * @param schemaUrl
  *   \- an optional schema URL
  */
final case class Resource(
    attributes: Attributes,
    schemaUrl: Option[String]
) {

  /** Merges [[Resource]] into another [[Resource]]. If the same attribute
    * exists in both resources, the attribute in the other [[Resource]] will be
    * used.
    * @param other
    *   \- the other [[Resource]] to merge into.
    * @return
    *   a new [[Resource]] with the merged attributes.
    */
  def mergeInto(other: Resource): Resource = {
    if (other == Resource.Empty) this
    else {
      val mergedAttributes = other.attributes |+| attributes
      val mergedSchemaUrl = (other.schemaUrl, schemaUrl) match {
        case (Some(otherUrl), Some(url)) =>
          if (otherUrl == url) Some(url) else None
        case (otherUrl, url) =>
          otherUrl.orElse(url)
      }
      Resource(mergedAttributes, mergedSchemaUrl)
    }
  }
}

object Resource {

  def apply(attributes: Attributes): Resource =
    Resource(attributes, None)

  /** Returns an empty [[Resource]]. It is strongly recommended to start with
    * [[Resource.Default]] instead of this method to include SDK required
    * attributes.
    *
    * @return
    *   an empty <pre>Resource</pre>.
    */
  val Empty: Resource = Resource(Attributes.Empty)

  private val TelemetrySdk: Resource = Resource(
    Attributes(
      Attribute(TelemetrySdkName, "otel4s"),
      Attribute(TelemetrySdkLanguage, TelemetrySdkLanguageValue.Scala.value),
      Attribute(TelemetrySdkVersion, BuildInfo.version)
    )
  )

  private val Mandatory: Resource = Resource(
    Attributes(
      Attribute(ServiceName, "unknown_service:scala")
    )
  )

  /** Returns the default [[Resource]]. This resource contains the default
    * attributes provided by the SDK.
    *
    * @return
    *   a <pre>Resource</pre>.
    */
  val Default: Resource = TelemetrySdk.mergeInto(Mandatory)

  implicit val showResource: Show[Resource] =
    r => show"Resource(${r.attributes}, ${r.schemaUrl})"

}
