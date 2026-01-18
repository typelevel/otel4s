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
package semconv
package experimental.attributes

// DO NOT EDIT, this is an Auto-generated file from buildscripts/templates/registry/otel4s/attributes/SemanticAttributes.scala.j2
object GeoExperimentalAttributes {

  /** Two-letter code representing continent’s name.
    */
  val GeoContinentCode: AttributeKey[String] =
    AttributeKey("geo.continent.code")

  /** Two-letter ISO Country Code (<a href="https://wikipedia.org/wiki/ISO_3166-1#Codes">ISO 3166-1 alpha2</a>).
    */
  val GeoCountryIsoCode: AttributeKey[String] =
    AttributeKey("geo.country.iso_code")

  /** Locality name. Represents the name of a city, town, village, or similar populated place.
    */
  val GeoLocalityName: AttributeKey[String] =
    AttributeKey("geo.locality.name")

  /** Latitude of the geo location in <a href="https://wikipedia.org/wiki/World_Geodetic_System#WGS84">WGS84</a>.
    */
  val GeoLocationLat: AttributeKey[Double] =
    AttributeKey("geo.location.lat")

  /** Longitude of the geo location in <a href="https://wikipedia.org/wiki/World_Geodetic_System#WGS84">WGS84</a>.
    */
  val GeoLocationLon: AttributeKey[Double] =
    AttributeKey("geo.location.lon")

  /** Postal code associated with the location. Values appropriate for this field may also be known as a postcode or ZIP
    * code and will vary widely from country to country.
    */
  val GeoPostalCode: AttributeKey[String] =
    AttributeKey("geo.postal_code")

  /** Region ISO code (<a href="https://wikipedia.org/wiki/ISO_3166-2">ISO 3166-2</a>).
    */
  val GeoRegionIsoCode: AttributeKey[String] =
    AttributeKey("geo.region.iso_code")

  /** Values for [[GeoContinentCode]].
    */
  abstract class GeoContinentCodeValue(val value: String)
  object GeoContinentCodeValue {
    implicit val attributeFromGeoContinentCodeValue: Attribute.From[GeoContinentCodeValue, String] = _.value

    /** Africa
      */
    case object Af extends GeoContinentCodeValue("AF")

    /** Antarctica
      */
    case object An extends GeoContinentCodeValue("AN")

    /** Asia
      */
    case object As extends GeoContinentCodeValue("AS")

    /** Europe
      */
    case object Eu extends GeoContinentCodeValue("EU")

    /** North America
      */
    case object Na extends GeoContinentCodeValue("NA")

    /** Oceania
      */
    case object Oc extends GeoContinentCodeValue("OC")

    /** South America
      */
    case object Sa extends GeoContinentCodeValue("SA")
  }

}
