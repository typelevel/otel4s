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

package org.typelevel.otel4s
package testkit

import cats.Hash
import cats.Show
import cats.syntax.show._

final class InstrumentationResource(
    val schemaUrl: Option[String],
    val attributes: List[Attribute[_]]
) {

  override def hashCode(): Int =
    Hash[InstrumentationResource].hash(this)

  override def toString: String =
    Show[InstrumentationResource].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: InstrumentationResource =>
        Hash[InstrumentationResource].eqv(this, other)
      case _ =>
        false
    }

}

object InstrumentationResource {
  import Implicits._

  implicit val metricResourceHash: Hash[InstrumentationResource] =
    Hash.by(p => (p.schemaUrl, p.attributes))

  implicit val metricResourceShow: Show[InstrumentationResource] =
    Show.show(p =>
      show"InstrumentationResource(${p.schemaUrl}, ${p.attributes})"
    )

}
