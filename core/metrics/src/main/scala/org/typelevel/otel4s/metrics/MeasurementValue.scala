/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.metrics

@annotation.implicitNotFound(
  "Could not find the `MeasurementValue` for ${A}. `Long` and `Double` are available out of the box."
)
sealed trait MeasurementValue[A] {
  def contramap[B](f: B => A): MeasurementValue[B]
}

object MeasurementValue {

  def apply[A](implicit ev: MeasurementValue[A]): MeasurementValue[A] = ev

  @annotation.implicitAmbiguous(
    """
Choose the type of an instrument explicitly, for example:
1) `.counter[Long](...)` or `.counter[Double](...)`
2) `.upDownCounter[Long](...)` or `.upDownCounter[Double](...)`
3) `.histogram[Long](...)` or `.histogram[Double](...)`
4) `.observableGauge[Long](...)` or `.observableGauge[Double](...)`
    """
  )
  implicit val longMeasurementValue: MeasurementValue[Long] =
    LongMeasurementValue(identity)

  implicit val doubleMeasurementValue: MeasurementValue[Double] =
    DoubleMeasurementValue(identity)

  private[otel4s] final case class LongMeasurementValue[A](
      cast: A => Long
  ) extends MeasurementValue[A] {
    def contramap[B](f: B => A): MeasurementValue[B] =
      LongMeasurementValue(cast.compose(f))
  }

  private[otel4s] final case class DoubleMeasurementValue[A](
      cast: A => Double
  ) extends MeasurementValue[A] {
    def contramap[B](f: B => A): MeasurementValue[B] =
      DoubleMeasurementValue(cast.compose(f))
  }

}
