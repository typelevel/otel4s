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

package org.typelevel.otel4s.testkit.metrics

import cats.Hash
import cats.Show
import cats.syntax.show._

final class SummaryPointData(
    val sum: Double,
    val count: Long,
    val values: List[QuantileData]
) {

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

object SummaryPointData {

  implicit val summaryPointDataHash: Hash[SummaryPointData] =
    Hash.by(p => (p.sum, p.count, p.values))

  implicit val summaryPointDataShow: Show[SummaryPointData] =
    Show.show(p => show"SummaryPointData(${p.sum}, ${p.count}, ${p.values})")

}
