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

package org.typelevel.otel4s.sdk.context

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.scalacheck.Gens

class TraceContextSuite extends DisciplineSuite {

  checkAll("TraceContext.Hash", HashTests[TraceContext].hash)

  test("Show[TraceContext]") {
    Prop.forAll(Gens.traceContext) { ctx =>
      val expected =
        s"TraceContext{traceId=${ctx.traceId.toHex}, spanId=${ctx.spanId.toHex}, isSampled=${ctx.isSampled}}"

      assertEquals(Show[TraceContext].show(ctx), expected)
      assertEquals(ctx.toString, expected)
    }
  }

}
