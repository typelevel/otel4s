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

final class InstrumentationScope(
    val name: String,
    val version: Option[String],
    val schemaUrl: Option[String]
) {

  override def hashCode(): Int =
    Hash[InstrumentationScope].hash(this)

  override def toString: String =
    Show[InstrumentationScope].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: InstrumentationScope =>
        Hash[InstrumentationScope].eqv(this, other)
      case _ =>
        false
    }

}

object InstrumentationScope {

  implicit val instrumentationScopeHash: Hash[InstrumentationScope] =
    Hash.by(s => (s.name, s.version, s.schemaUrl))

  implicit val instrumentationScopeShow: Show[InstrumentationScope] =
    Show.show(s =>
      show"InstrumentationScope(${s.name}, ${s.version}, ${s.schemaUrl})"
    )

}
