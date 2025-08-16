/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs

import cats.Hash
import cats.Show

/** Represents the limits enforced during processing of a log record.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecord-limits]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/common/#configurable-parameters]]
  */
sealed trait LogRecordLimits {

  /** The max number of attributes per log record. */
  def maxNumberOfAttributes: Int

  /** The max number of characters for string attribute values. For string array attribute values, applies to each entry
    * individually.
    */
  def maxAttributeValueLength: Int

  override final def hashCode(): Int =
    Hash[LogRecordLimits].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: LogRecordLimits => Hash[LogRecordLimits].eqv(this, other)
      case _                      => false
    }

  override final def toString: String =
    Show[LogRecordLimits].show(this)

}

object LogRecordLimits {

  private object Defaults {
    val MaxNumberOfAttributes = 128
    val MaxAttributeValueLength = Int.MaxValue
  }

  /** Builder for [[LogRecordLimits]] */
  sealed trait Builder {

    /** Sets the max number of attributes per log record. */
    def withMaxNumberOfAttributes(value: Int): Builder

    /** Sets the max number of characters for string attribute values. For string array attribute values, applies to
      * each entry individually.
      */
    def withMaxAttributeValueLength(value: Int): Builder

    /** Creates a [[LogRecordLimits]] with the configuration of this builder. */
    def build: LogRecordLimits
  }

  private val Default: LogRecordLimits =
    builder.build

  /** Creates a [[Builder]] for [[LogRecordLimits]] using the default limits.
    */
  def builder: Builder =
    BuilderImpl(
      maxNumberOfAttributes = Defaults.MaxNumberOfAttributes,
      maxAttributeValueLength = Defaults.MaxAttributeValueLength
    )

  /** Creates a [[LogRecordLimits]] using the given limits. */
  def apply(
      maxNumberOfAttributes: Int,
      maxAttributeValueLength: Int
  ): LogRecordLimits =
    LogRecordLimitsImpl(
      maxNumberOfAttributes = maxNumberOfAttributes,
      maxAttributeValueLength = maxAttributeValueLength
    )

  def default: LogRecordLimits = Default

  implicit val logRecordLimitsHash: Hash[LogRecordLimits] =
    Hash.by(s => (s.maxNumberOfAttributes, s.maxAttributeValueLength))

  implicit val logRecordLimitsShow: Show[LogRecordLimits] =
    Show.show { s =>
      "LogRecordLimits{" +
        s"maxNumberOfAttributes=${s.maxNumberOfAttributes}, " +
        s"maxAttributeValueLength=${s.maxAttributeValueLength}}"
    }

  private final case class LogRecordLimitsImpl(
      maxNumberOfAttributes: Int,
      maxAttributeValueLength: Int,
  ) extends LogRecordLimits

  private final case class BuilderImpl(
      maxNumberOfAttributes: Int,
      maxAttributeValueLength: Int,
  ) extends Builder {
    def withMaxNumberOfAttributes(value: Int): Builder =
      copy(maxNumberOfAttributes = value)

    def withMaxAttributeValueLength(value: Int): Builder =
      copy(maxAttributeValueLength = value)

    def build: LogRecordLimits =
      LogRecordLimitsImpl(
        maxNumberOfAttributes,
        maxAttributeValueLength
      )
  }

}
