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

package org.typelevel.otel4s.sdk.trace

import cats.Hash
import cats.Show

/** Holds the limits enforced during recording of a span. */
sealed trait SpanLimits {

  /** The max number of attributes per span. */
  def maxNumberOfAttributes: Int

  /** The max number of events per span. */
  def maxNumberOfEvents: Int

  /** The max number of links per span. */
  def maxNumberOfLinks: Int

  /** The max number of attributes per event. */
  def maxNumberOfAttributesPerEvent: Int

  /** The max number of attributes per link. */
  def maxNumberOfAttributesPerLink: Int

  /** The max number of characters for string attribute values. For string array
    * attribute values, applies to each entry individually.
    */
  def maxAttributeValueLength: Int

  override final def hashCode(): Int =
    Hash[SpanLimits].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: SpanLimits => Hash[SpanLimits].eqv(this, other)
      case _                 => false
    }

  override final def toString: String =
    Show[SpanLimits].show(this)
}

object SpanLimits {

  private object Defaults {
    val MaxNumberOfAttributes = 128
    val MaxNumberOfEvents = 128
    val MaxNumberOfLinks = 128
    val MaxNumberOfAttributesPerEvent = 127
    val MaxNumberOfAttributesPerLink = 128
    val MaxAttributeValueLength = Int.MaxValue
  }

  /** Builder for [[SpanLimits]] */
  sealed trait Builder {

    /** Sets the max number of attributes per span. */
    def setMaxNumberOfAttributes(value: Int): Builder

    /** Sets the max number of events per span. */
    def setMaxNumberOfEvents(value: Int): Builder

    /** Sets the max number of links per span. */
    def setMaxNumberOfLinks(value: Int): Builder

    /** Sets the max number of attributes per event. */
    def setMaxNumberOfAttributesPerEvent(value: Int): Builder

    /** Sets the max number of attributes per link. */
    def setMaxNumberOfAttributesPerLink(value: Int): Builder

    /** Sets the max number of characters for string attribute values. For
      * string array attribute values, applies to each entry individually.
      */
    def setMaxAttributeValueLength(value: Int): Builder

    /** Creates a [[SpanLimits]] with the configuration of this builder. */
    def build: SpanLimits
  }

  val Default: SpanLimits =
    builder.build

  /** Creates a [[Builder]] for [[SpanLimits]] using the default limits. */
  def builder: Builder =
    BuilderImpl(
      maxNumberOfAttributes = Defaults.MaxNumberOfAttributes,
      maxNumberOfEvents = Defaults.MaxNumberOfEvents,
      maxNumberOfLinks = Defaults.MaxNumberOfLinks,
      maxNumberOfAttributesPerEvent = Defaults.MaxNumberOfAttributesPerEvent,
      maxNumberOfAttributesPerLink = Defaults.MaxNumberOfAttributesPerLink,
      maxAttributeValueLength = Defaults.MaxAttributeValueLength
    )

  /** Creates a [[SpanLimits]] using the given limits. */
  def create(
      maxNumberOfAttributes: Int,
      maxNumberOfEvents: Int,
      maxNumberOfLinks: Int,
      maxNumberOfAttributesPerEvent: Int,
      maxNumberOfAttributesPerLink: Int,
      maxAttributeValueLength: Int
  ): SpanLimits =
    SpanLimitsImpl(
      maxNumberOfAttributes = maxNumberOfAttributes,
      maxNumberOfEvents = maxNumberOfEvents,
      maxNumberOfLinks = maxNumberOfLinks,
      maxNumberOfAttributesPerEvent = maxNumberOfAttributesPerEvent,
      maxNumberOfAttributesPerLink = maxNumberOfAttributesPerLink,
      maxAttributeValueLength = maxAttributeValueLength
    )

  implicit val spanLimitsHash: Hash[SpanLimits] =
    Hash.by { s =>
      (
        s.maxNumberOfAttributes,
        s.maxNumberOfEvents,
        s.maxNumberOfLinks,
        s.maxNumberOfAttributesPerEvent,
        s.maxNumberOfAttributesPerLink,
        s.maxAttributeValueLength
      )
    }

  implicit val spanLimitsShow: Show[SpanLimits] =
    Show.show { s =>
      "SpanLimits{" +
        s"maxNumberOfAttributes=${s.maxNumberOfAttributes}, " +
        s"maxNumberOfEvents=${s.maxNumberOfEvents}, " +
        s"maxNumberOfLinks=${s.maxNumberOfLinks}, " +
        s"maxNumberOfAttributesPerEvent=${s.maxNumberOfAttributesPerEvent}, " +
        s"maxNumberOfAttributesPerLink=${s.maxNumberOfAttributesPerLink}, " +
        s"maxAttributeValueLength=${s.maxAttributeValueLength}}"
    }

  private final case class SpanLimitsImpl(
      maxNumberOfAttributes: Int,
      maxNumberOfEvents: Int,
      maxNumberOfLinks: Int,
      maxNumberOfAttributesPerEvent: Int,
      maxNumberOfAttributesPerLink: Int,
      maxAttributeValueLength: Int,
  ) extends SpanLimits

  private final case class BuilderImpl(
      maxNumberOfAttributes: Int,
      maxNumberOfEvents: Int,
      maxNumberOfLinks: Int,
      maxNumberOfAttributesPerEvent: Int,
      maxNumberOfAttributesPerLink: Int,
      maxAttributeValueLength: Int,
  ) extends Builder {
    def setMaxNumberOfAttributes(value: Int): Builder =
      copy(maxNumberOfAttributes = value)

    def setMaxNumberOfEvents(value: Int): Builder =
      copy(maxNumberOfEvents = value)

    def setMaxNumberOfLinks(value: Int): Builder =
      copy(maxNumberOfLinks = value)

    def setMaxNumberOfAttributesPerEvent(value: Int): Builder =
      copy(maxNumberOfAttributesPerEvent = value)

    def setMaxNumberOfAttributesPerLink(value: Int): Builder =
      copy(maxNumberOfAttributesPerLink = value)

    def setMaxAttributeValueLength(value: Int): Builder =
      copy(maxAttributeValueLength = value)

    def build: SpanLimits =
      SpanLimitsImpl(
        maxNumberOfAttributes,
        maxNumberOfEvents,
        maxNumberOfLinks,
        maxNumberOfAttributesPerEvent,
        maxNumberOfAttributesPerLink,
        maxAttributeValueLength
      )
  }

}
