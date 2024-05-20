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

package org.typelevel.otel4s.sdk.metrics
package view

import cats.Hash
import cats.Show

/** Instrument selection criteria for applying views registered via meter
  * provider.
  *
  * The predicate is built by the logical conjunction of the present properties.
  */
sealed trait InstrumentSelector {

  /** The required [[InstrumentType type]] of an instrument.
    *
    * `None` means all instruments match.
    */
  def instrumentType: Option[InstrumentType]

  /** The required name of an instrument.
    *
    * Instrument name may contain the wildcard characters `*` and `?` with the
    * following matching criteria:
    *   - `*` - matches 0 or more instances of any character
    *   - `?` - matches exactly one instance of any character
    *
    * `None` means all instruments match.
    */
  def instrumentName: Option[String]

  /** The required unit of an instrument.
    *
    * `None` means all instruments match.
    */
  def instrumentUnit: Option[String]

  /** The required name of a meter.
    *
    * `None` means all meters match.
    */
  def meterName: Option[String]

  /** The required version of a meter.
    *
    * `None` means all meters match.
    */
  def meterVersion: Option[String]

  /** The required schema URL of a meter.
    *
    * `None` means all meters match.
    */
  def meterSchemaUrl: Option[String]

  override final def toString: String =
    Show[InstrumentSelector].show(this)

  override final def hashCode(): Int =
    Hash[InstrumentSelector].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: InstrumentSelector =>
        Hash[InstrumentSelector].eqv(this, other)
      case _ =>
        false
    }
}

object InstrumentSelector {

  /** A builder of an [[InstrumentSelector]].
    */
  sealed trait Builder {

    /** Adds the given `tpe` as a predicate for an instrument.
      *
      * @param tpe
      *   the instrument type to filter by
      */
    def withInstrumentType(tpe: InstrumentType): Builder

    /** Adds the given `name` as a predicate for an instrument.
      *
      * Instrument name may contain the wildcard characters `*` and `?` with the
      * following matching criteria:
      *   - `*` - matches 0 or more instances of any character
      *   - `?` - matches exactly one instance of any character
      *
      * @note
      *   empty string is ignored
      *
      * @param name
      *   the instrument name to filter by
      */
    def withInstrumentName(name: String): Builder

    /** Adds the given `unit` as a predicate for an instrument.
      *
      * @note
      *   empty string is ignored
      *
      * @param unit
      *   the instrument type to filter by
      */
    def withInstrumentUnit(unit: String): Builder

    /** Adds the given `name` as a predicate for a meter.
      *
      * @note
      *   empty string is ignored
      *
      * @param name
      *   the meter name to filter by
      */
    def withMeterName(name: String): Builder

    /** Adds the given `version` as a predicate for a meter.
      *
      * @note
      *   empty string is ignored
      *
      * @param version
      *   the instrument type to filter by
      */
    def withMeterVersion(version: String): Builder

    /** Adds the given `schemaUrl` as a predicate for a meter.
      *
      * @note
      *   empty string is ignored
      *
      * @param schemaUrl
      *   the instrument type to filter by
      */
    def withMeterSchemaUrl(schemaUrl: String): Builder

    /** Creates an [[InstrumentSelector]] with the configuration of this
      * builder.
      */
    def build: InstrumentSelector
  }

  /** Returns an empty [[Builder]] of an [[InstrumentSelector]].
    */
  def builder: Builder =
    BuilderImpl()

  implicit val instrumentSelectorHash: Hash[InstrumentSelector] =
    Hash.by { selector =>
      (
        selector.instrumentType,
        selector.instrumentName,
        selector.instrumentUnit,
        selector.meterName,
        selector.meterVersion,
        selector.meterSchemaUrl
      )
    }

  implicit val instrumentSelectorShow: Show[InstrumentSelector] = {
    Show.show { selector =>
      def prop(focus: InstrumentSelector => Option[String], name: String) =
        focus(selector).map(value => s"$name=$value")

      Seq(
        prop(_.instrumentType.map(_.toString), "instrumentType"),
        prop(_.instrumentName, "instrumentName"),
        prop(_.instrumentUnit, "instrumentUnit"),
        prop(_.meterName, "meterName"),
        prop(_.meterVersion, "meterVersion"),
        prop(_.meterSchemaUrl, "meterSchemaUrl"),
      ).collect { case Some(k) => k }.mkString("InstrumentSelector{", ", ", "}")
    }
  }

  private final case class BuilderImpl(
      instrumentType: Option[InstrumentType] = None,
      instrumentName: Option[String] = None,
      instrumentUnit: Option[String] = None,
      meterName: Option[String] = None,
      meterVersion: Option[String] = None,
      meterSchemaUrl: Option[String] = None
  ) extends Builder
      with InstrumentSelector {

    def withInstrumentType(tpe: InstrumentType): Builder =
      copy(instrumentType = Some(tpe))

    def withInstrumentName(name: String): Builder =
      copy(instrumentName = keepNonEmpty(name))

    def withInstrumentUnit(unit: String): Builder =
      copy(instrumentUnit = keepNonEmpty(unit))

    def withMeterName(name: String): Builder =
      copy(meterName = keepNonEmpty(name))

    def withMeterVersion(version: String): Builder =
      copy(meterVersion = keepNonEmpty(version))

    def withMeterSchemaUrl(schemaUrl: String): Builder =
      copy(meterSchemaUrl = keepNonEmpty(schemaUrl))

    def build: InstrumentSelector = {
      require(
        instrumentType.isDefined || instrumentName.isDefined ||
          instrumentUnit.isDefined || meterName.isDefined ||
          meterVersion.isDefined || meterSchemaUrl.isDefined,
        "at least one criteria must be defined"
      )

      this
    }

    private def keepNonEmpty(value: String): Option[String] = {
      val trimmed = value.trim
      Option.when(trimmed.nonEmpty)(trimmed)
    }
  }

}
