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
package metrics

import cats.Hash
import cats.Show
import cats.syntax.show._

final class Metric(
    val name: String,
    val description: Option[String],
    val unit: Option[String],
    val scope: InstrumentationScope,
    val resource: InstrumentationResource,
    val data: MetricData
) {

  override def hashCode(): Int =
    Hash[Metric].hash(this)

  override def toString: String =
    Show[Metric].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Metric =>
        Hash[Metric].eqv(this, other)
      case _ =>
        false
    }

}

object Metric {

  implicit val metricHash: Hash[Metric] =
    Hash.by(p => (p.name, p.description, p.unit, p.scope, p.resource, p.data))

  implicit val metricShow: Show[Metric] =
    Show.show(p =>
      show"Metric(${p.name}, ${p.description}, ${p.unit}, ${p.scope}, ${p.resource}, ${p.data})"
    )

}
