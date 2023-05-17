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

import cats.laws.discipline.ExhaustiveCheck

// An arbitrary type whose cardinality nicely supports MiniMap
sealed trait Compass
case object North extends Compass
case object South extends Compass
case object East extends Compass
case object West extends Compass

object Compass {
  implicit val exhaustiveCheckForCompass: ExhaustiveCheck[Compass] =
    ExhaustiveCheck.instance(List(North, South, East, West))
}
