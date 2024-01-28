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

package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeasurementValue

sealed trait InstrumentDescriptor {

  def name: String
  def description: Option[String]
  def unit: Option[String]
  def instrumentType: InstrumentType
  def valueType: InstrumentValueType
  def advice: Advice
}

object InstrumentDescriptor {

  def apply(
      name: String,
      description: Option[String],
      unit: Option[String],
      instrumentType: InstrumentType,
      instrumentValueType: InstrumentValueType,
      advice: Advice
  ): InstrumentDescriptor =
    Impl(name, description, unit, instrumentType, instrumentValueType, advice)

  private final case class Impl(
      name: String,
      description: Option[String],
      unit: Option[String],
      instrumentType: InstrumentType,
      valueType: InstrumentValueType,
      advice: Advice
  ) extends InstrumentDescriptor

}

sealed trait InstrumentType extends Product with Serializable
object InstrumentType {

  case object Counter extends InstrumentType
  case object UpDownCounter extends InstrumentType
  case object Histogram extends InstrumentType
  case object ObservableCounter extends InstrumentType
  case object ObservableUpDownCounter extends InstrumentType
  case object ObservableGauge extends InstrumentType

  val values: Set[InstrumentType] = Set(
    Counter,
    UpDownCounter,
    Histogram,
    ObservableCounter,
    ObservableUpDownCounter,
    ObservableGauge
  )
}

sealed trait InstrumentValueType
object InstrumentValueType {
  case object LongValue extends InstrumentValueType
  case object DoubleValue extends InstrumentValueType

  def of[A: MeasurementValue]: InstrumentValueType =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(_)   => LongValue
      case MeasurementValue.DoubleMeasurementValue(_) => DoubleValue
    }
}

sealed trait Advice {
  def explicitBoundaries: Option[BucketBoundaries]
}
object Advice {
  def empty: Advice = new Advice {
    def explicitBoundaries: Option[BucketBoundaries] = None
  }

  def apply(bucketBoundaries: Option[BucketBoundaries]): Advice =
    new Advice {
      def explicitBoundaries: Option[BucketBoundaries] = bucketBoundaries
    }
}
