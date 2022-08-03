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

package org.typelevel.otel4s.testkit

import cats.Hash
import cats.Show
import cats.syntax.show._

final class HistogramPointData(
    val sum: Double,
    val count: Long,
    val boundaries: List[Double],
    val counts: List[Long]
) {

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

  implicit val histogramPointDataHash: Hash[HistogramPointData] =
    Hash.by(p => (p.sum, p.count, p.boundaries, p.counts))

  implicit val histogramPointDataShow: Show[HistogramPointData] =
    Show.show(p =>
      show"HistogramPointData(${p.sum}, ${p.count}, ${p.boundaries}, ${p.counts})"
    )

}
