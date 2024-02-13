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

package org.typelevel.otel4s.oteljava.testkit.metrics.data

import cats.Hash
import cats.Show
import cats.syntax.show._
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile

/** A representation of the `io.opentelemetry.sdk.metrics.data.ValueAtQuantile`.
  */
sealed trait QuantileData {

  def quantile: Double

  def value: Double

  override def hashCode(): Int =
    Hash[QuantileData].hash(this)

  override def toString: String =
    Show[QuantileData].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: QuantileData =>
        Hash[QuantileData].eqv(this, other)
      case _ =>
        false
    }

}

object QuantileData {

  def apply(quantile: Double, value: Double): QuantileData =
    Impl(quantile, value)

  def apply(v: ValueAtQuantile): QuantileData =
    Impl(v.getQuantile, v.getValue)

  implicit val quantileDataHash: Hash[QuantileData] =
    Hash.by(p => (p.quantile, p.value))

  implicit val quantileDataShow: Show[QuantileData] =
    Show.show(p => show"QuantileData(${p.quantile}, ${p.value})")

  private final case class Impl(
      quantile: Double,
      value: Double
  ) extends QuantileData

}
