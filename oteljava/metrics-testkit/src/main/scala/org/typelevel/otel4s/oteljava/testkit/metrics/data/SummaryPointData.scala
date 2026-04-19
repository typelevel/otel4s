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
import io.opentelemetry.sdk.metrics.data.{SummaryPointData => JSummaryPointData}

import scala.annotation.nowarn
import scala.jdk.CollectionConverters._

/** A representation of the `io.opentelemetry.sdk.metrics.data.SummaryPointData`.
  */
@deprecated("Use OpenTelemetry Java summary point data directly or the expectation API.", "1.0.0-RC1")
sealed trait SummaryPointData {

  def sum: Double

  def count: Long

  def values: List[QuantileData]

  override def hashCode(): Int =
    Hash[SummaryPointData].hash(this)

  override def toString: String =
    Show[SummaryPointData].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: SummaryPointData =>
        Hash[SummaryPointData].eqv(this, other)
      case _ =>
        false
    }

}

@nowarn("cat=deprecation")
object SummaryPointData {

  @deprecated("Use OpenTelemetry Java summary point data directly or the expectation API.", "1.0.0-RC1")
  def apply(
      sum: Double,
      count: Long,
      values: List[QuantileData]
  ): SummaryPointData =
    Impl(sum, count, values)

  @deprecated("Use OpenTelemetry Java summary point data directly or the expectation API.", "1.0.0-RC1")
  def apply(data: JSummaryPointData): SummaryPointData =
    SummaryPointData(
      sum = data.getSum,
      count = data.getCount,
      values = data.getValues.asScala.toList.map(v => QuantileData(v))
    )

  implicit val summaryPointDataHash: Hash[SummaryPointData] =
    Hash.by(p => (p.sum, p.count, p.values))

  implicit val summaryPointDataShow: Show[SummaryPointData] =
    Show.show(p => show"SummaryPointData(${p.sum}, ${p.count}, ${p.values})")

  private final case class Impl(
      sum: Double,
      count: Long,
      values: List[QuantileData]
  ) extends SummaryPointData
}
