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

class ExemplarDataSuite extends DisciplineSuite {

  checkAll("ExemplarData.Hash", HashTests[ExemplarData].hash)

  test("Show[ExemplarData]") {
    Prop.forAll(Gens.exemplarData) { e =>
      val prefix = e match {
        case _: ExemplarData.LongExemplar   => "ExemplarData.Long"
        case _: ExemplarData.DoubleExemplar => "ExemplarData.Double"
      }

      val traceContext = e.traceContext match {
        case Some(ctx) => s", traceContext=$ctx"
        case None      => ""
      }

      val expected =
        s"$prefix{filteredAttributes=${e.filteredAttributes}, timestamp=${e.timestamp}$traceContext, value=${e.value}}"

      assertEquals(Show[ExemplarData].show(e), expected)
    }
  }

}
