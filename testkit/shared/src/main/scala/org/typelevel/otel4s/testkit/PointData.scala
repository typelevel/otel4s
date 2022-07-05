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

final class PointData[A](
    val startEpochNanos: Long,
    val epochNanos: Long,
    val attributes: List[Attribute[_]],
    val value: A
)

object PointData {
  import Implicits._

  implicit def pointDataHash[A: Hash]: Hash[PointData[A]] =
    Hash.by(p => (p.startEpochNanos, p.epochNanos, p.attributes, p.value))

  implicit def pointDataShow[A: Show]: Show[PointData[A]] =
    Show.show(p =>
      show"PointData(${p.startEpochNanos}, ${p.epochNanos}, ${p.attributes}, ${p.value})"
    )

}
