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
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}

import scala.jdk.CollectionConverters._

/** A representation of the `io.opentelemetry.sdk.metrics.data.HistogramPointData`.
  */
sealed trait HistogramPointData {

  def sum: Double

  def count: Long

  def boundaries: List[Double]

  def counts: List[Long]

  override def hashCode(): Int =
    Hash[HistogramPointData].hash(this)

  override def toString: String =
    Show[HistogramPointData].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: HistogramPointData =>
        Hash[HistogramPointData].eqv(this, other)
      case _ =>
        false
    }

}

object HistogramPointData {

  def apply(
      sum: Double,
      count: Long,
      boundaries: List[Double],
      counts: List[Long]
  ): HistogramPointData =
    Impl(sum, count, boundaries, counts)

  def apply(data: JHistogramPointData): HistogramPointData =
    Impl(
      sum = data.getSum,
      count = data.getCount,
      boundaries = data.getBoundaries.asScala.toList.map(_.doubleValue()),
      counts = data.getCounts.asScala.toList.map(_.longValue())
    )

  implicit val histogramPointDataHash: Hash[HistogramPointData] =
    Hash.by(p => (p.sum, p.count, p.boundaries, p.counts))

  implicit val histogramPointDataShow: Show[HistogramPointData] =
    Show.show(p => show"HistogramPointData(${p.sum}, ${p.count}, ${p.boundaries}, ${p.counts})")

  final case class Impl(
      sum: Double,
      count: Long,
      boundaries: List[Double],
      counts: List[Long]
  ) extends HistogramPointData
}
