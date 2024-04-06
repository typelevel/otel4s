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

package org.typelevel.otel4s.sdk.metrics
package view

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class InstrumentSelectorSuite extends DisciplineSuite {

  checkAll("InstrumentSelector.HashLaws", HashTests[InstrumentSelector].hash)

  test("Show[InstrumentSelector]") {
    Prop.forAll(Gens.instrumentSelector) { selector =>
      def prop(name: String, focus: InstrumentSelector => Option[String]) =
        focus(selector).map(v => s"$name=$v")

      val params = Seq(
        prop("instrumentType", _.instrumentType.map(_.toString)),
        prop("instrumentName", _.instrumentName),
        prop("instrumentUnit", _.instrumentUnit),
        prop("meterName", _.meterName),
        prop("meterVersion", _.meterVersion),
        prop("meterSchemaUrl", _.meterSchemaUrl)
      ).collect { case Some(v) => v }

      val expected = params.mkString("InstrumentSelector{", ", ", "}")

      assertEquals(Show[InstrumentSelector].show(selector), expected)
    }
  }

  test("fail when none of the criteria is defined") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: at least one criteria must be defined"
    )(
      InstrumentSelector.builder.build
    )
  }

  test("ignore empty strings") {
    val selector = InstrumentSelector.builder
      .withInstrumentType(InstrumentType.Counter)
      .withInstrumentName("")
      .withInstrumentUnit("     ")
      .withMeterName(" ")
      .withMeterVersion("   ")
      .withMeterSchemaUrl("")
      .build

    assertEquals(selector.instrumentType, Some(InstrumentType.Counter))
    assert(selector.instrumentName.isEmpty)
    assert(selector.instrumentUnit.isEmpty)
    assert(selector.meterName.isEmpty)
    assert(selector.meterVersion.isEmpty)
    assert(selector.meterSchemaUrl.isEmpty)
  }

}
