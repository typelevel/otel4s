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

package org.typelevel.otel4s
package sdk.common

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit._
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.scalacheck.Gens

class InstrumentationScopeSuite extends DisciplineSuite {

  checkAll(
    "InstrumentationScope.HashLaws",
    HashTests[InstrumentationScope].hash
  )

  property("Show[InstrumentationScope]") {
    Prop.forAll(Gens.instrumentationScope) { scope =>
      val expected =
        show"InstrumentationScope{name=${scope.name}, version=${scope.version}, schemaUrl=${scope.schemaUrl}, attributes=${scope.attributes}}"

      assertEquals(Show[InstrumentationScope].show(scope), expected)
    }
  }

  property("create via builder") {
    Prop.forAll(Gens.instrumentationScope) { scope =>
      val builder = InstrumentationScope
        .builder(scope.name)
        .withAttributes(scope.attributes)

      val withVersion =
        scope.version.fold(builder)(builder.withVersion)

      val withResource =
        scope.schemaUrl.fold(withVersion)(withVersion.withSchemaUrl)

      assertEquals(withResource.build, scope)
    }
  }

  test("empty instance") {
    val expected = InstrumentationScope("", None, None, Attributes.empty)
    assertEquals(InstrumentationScope.empty, expected)
  }

}
