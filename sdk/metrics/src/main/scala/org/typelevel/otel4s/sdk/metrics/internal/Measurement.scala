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

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue

import scala.concurrent.duration.FiniteDuration

sealed trait Measurement {
  def startTimestamp: FiniteDuration
  def collectTimestamp: FiniteDuration
  def attributes: Attributes

  def withAttributes(attributes: Attributes): Measurement
}

object Measurement {

  def of[A: MeasurementValue](
      start: FiniteDuration,
      collect: FiniteDuration,
      attributes: Attributes,
      value: A
  ): Measurement =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        LongMeasurement(start, collect, attributes, cast(value))
      case MeasurementValue.DoubleMeasurementValue(cast) =>
        DoubleMeasurement(start, collect, attributes, cast(value))
    }

  final case class LongMeasurement(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      value: Long
  ) extends Measurement {
    def withAttributes(attributes: Attributes): Measurement =
      copy(attributes = attributes)
  }

  final case class DoubleMeasurement(
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes,
      value: Double
  ) extends Measurement {
    def withAttributes(attributes: Attributes): Measurement =
      copy(attributes = attributes)
  }
}
