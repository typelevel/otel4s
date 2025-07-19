/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.logs

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.logs.scalacheck.Arbitraries
import org.typelevel.otel4s.logs.scalacheck.Cogens
import org.typelevel.otel4s.logs.scalacheck.Gens

class SeveritySuite extends DisciplineSuite {
  import Cogens.severityCogen
  import Arbitraries.severityArbitrary

  checkAll("Severity.HashLaws", HashTests[Severity].hash)

  test("Show[Severity]") {
    Prop.forAll(Gens.severity) { severity =>
      assertEquals(Show[Severity].show(severity), severity.toString)
    }
  }

  test("Severity values match the OpenTelemetry specification") {
    assertEquals(Severity.trace.value, 1)
    assertEquals(Severity.trace2.value, 2)
    assertEquals(Severity.trace3.value, 3)
    assertEquals(Severity.trace4.value, 4)
    assertEquals(Severity.debug.value, 5)
    assertEquals(Severity.debug2.value, 6)
    assertEquals(Severity.debug3.value, 7)
    assertEquals(Severity.debug4.value, 8)
    assertEquals(Severity.info.value, 9)
    assertEquals(Severity.info2.value, 10)
    assertEquals(Severity.info3.value, 11)
    assertEquals(Severity.info4.value, 12)
    assertEquals(Severity.warn.value, 13)
    assertEquals(Severity.warn2.value, 14)
    assertEquals(Severity.warn3.value, 15)
    assertEquals(Severity.warn4.value, 16)
    assertEquals(Severity.error.value, 17)
    assertEquals(Severity.error2.value, 18)
    assertEquals(Severity.error3.value, 19)
    assertEquals(Severity.error4.value, 20)
    assertEquals(Severity.fatal.value, 21)
    assertEquals(Severity.fatal2.value, 22)
    assertEquals(Severity.fatal3.value, 23)
    assertEquals(Severity.fatal4.value, 24)
  }

  test("Severity toString match the OpenTelemetry specification") {
    assertEquals(Severity.trace.toString, "TRACE")
    assertEquals(Severity.trace2.toString, "TRACE2")
    assertEquals(Severity.trace3.toString, "TRACE3")
    assertEquals(Severity.trace4.toString, "TRACE4")
    assertEquals(Severity.debug.toString, "DEBUG")
    assertEquals(Severity.debug2.toString, "DEBUG2")
    assertEquals(Severity.debug3.toString, "DEBUG3")
    assertEquals(Severity.debug4.toString, "DEBUG4")
    assertEquals(Severity.info.toString, "INFO")
    assertEquals(Severity.info2.toString, "INFO2")
    assertEquals(Severity.info3.toString, "INFO3")
    assertEquals(Severity.info4.toString, "INFO4")
    assertEquals(Severity.warn.toString, "WARN")
    assertEquals(Severity.warn2.toString, "WARN2")
    assertEquals(Severity.warn3.toString, "WARN3")
    assertEquals(Severity.warn4.toString, "WARN4")
    assertEquals(Severity.error.toString, "ERROR")
    assertEquals(Severity.error2.toString, "ERROR2")
    assertEquals(Severity.error3.toString, "ERROR3")
    assertEquals(Severity.error4.toString, "ERROR4")
    assertEquals(Severity.fatal.toString, "FATAL")
    assertEquals(Severity.fatal2.toString, "FATAL2")
    assertEquals(Severity.fatal3.toString, "FATAL3")
    assertEquals(Severity.fatal4.toString, "FATAL4")
  }

}
