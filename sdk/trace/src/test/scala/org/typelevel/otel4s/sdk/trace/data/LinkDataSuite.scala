/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace
package data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries
import org.typelevel.otel4s.sdk.trace.scalacheck.Cogens
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

class LinkDataSuite extends DisciplineSuite {
  import Cogens.linkDataCogen
  import Arbitraries.linkDataArbitrary

  checkAll("LinkData.HashLaws", HashTests[LinkData].hash)

  test("Show[LinkData]") {
    Prop.forAll(Gens.linkData) { data =>
      val expected =
        show"LinkData{spanContext=${data.spanContext}, attributes=${data.attributes}}"

      assertEquals(Show[LinkData].show(data), expected)
    }
  }

  test("create LinkData with given arguments") {
    Prop.forAll(Gens.linkData) { data =>
      assertEquals(LinkData(data.spanContext, data.attributes), data)
    }
  }

}
