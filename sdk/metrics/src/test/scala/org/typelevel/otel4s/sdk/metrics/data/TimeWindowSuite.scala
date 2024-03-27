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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

import scala.concurrent.duration._

class TimeWindowSuite extends DisciplineSuite {

  checkAll("TimeWindow.Hash", HashTests[TimeWindow].hash)

  test("fail when start > end") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: end must be greater than or equal to start"
    )(
      TimeWindow(2.nanos, 1.nanos)
    )
  }

  test("Show[TimeWindow]") {
    Prop.forAll(Gens.timeWindow) { window =>
      val expected = s"TimeWindow{start=${window.start}, end=${window.end}}"

      assertEquals(Show[TimeWindow].show(window), expected)
    }
  }

}
