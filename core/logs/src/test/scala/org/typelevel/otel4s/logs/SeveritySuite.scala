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
    assertEquals(Severity.Trace.trace1.severity, 1)
    assertEquals(Severity.Trace.trace2.severity, 2)
    assertEquals(Severity.Trace.trace3.severity, 3)
    assertEquals(Severity.Trace.trace4.severity, 4)
    assertEquals(Severity.Debug.debug1.severity, 5)
    assertEquals(Severity.Debug.debug2.severity, 6)
    assertEquals(Severity.Debug.debug3.severity, 7)
    assertEquals(Severity.Debug.debug4.severity, 8)
    assertEquals(Severity.Info.info1.severity, 9)
    assertEquals(Severity.Info.info2.severity, 10)
    assertEquals(Severity.Info.info3.severity, 11)
    assertEquals(Severity.Info.info4.severity, 12)
    assertEquals(Severity.Warn.warn1.severity, 13)
    assertEquals(Severity.Warn.warn2.severity, 14)
    assertEquals(Severity.Warn.warn3.severity, 15)
    assertEquals(Severity.Warn.warn4.severity, 16)
    assertEquals(Severity.Error.error1.severity, 17)
    assertEquals(Severity.Error.error2.severity, 18)
    assertEquals(Severity.Error.error3.severity, 19)
    assertEquals(Severity.Error.error4.severity, 20)
    assertEquals(Severity.Fatal.fatal1.severity, 21)
    assertEquals(Severity.Fatal.fatal2.severity, 22)
    assertEquals(Severity.Fatal.fatal3.severity, 23)
    assertEquals(Severity.Fatal.fatal4.severity, 24)
  }

  test("Severity toString match the OpenTelemetry specification") {
    assertEquals(Severity.Trace.trace1.toString, "TRACE")
    assertEquals(Severity.Trace.trace2.toString, "TRACE2")
    assertEquals(Severity.Trace.trace3.toString, "TRACE3")
    assertEquals(Severity.Trace.trace4.toString, "TRACE4")
    assertEquals(Severity.Debug.debug1.toString, "DEBUG")
    assertEquals(Severity.Debug.debug2.toString, "DEBUG2")
    assertEquals(Severity.Debug.debug3.toString, "DEBUG3")
    assertEquals(Severity.Debug.debug4.toString, "DEBUG4")
    assertEquals(Severity.Info.info1.toString, "INFO")
    assertEquals(Severity.Info.info2.toString, "INFO2")
    assertEquals(Severity.Info.info3.toString, "INFO3")
    assertEquals(Severity.Info.info4.toString, "INFO4")
    assertEquals(Severity.Warn.warn1.toString, "WARN")
    assertEquals(Severity.Warn.warn2.toString, "WARN2")
    assertEquals(Severity.Warn.warn3.toString, "WARN3")
    assertEquals(Severity.Warn.warn4.toString, "WARN4")
    assertEquals(Severity.Error.error1.toString, "ERROR")
    assertEquals(Severity.Error.error2.toString, "ERROR2")
    assertEquals(Severity.Error.error3.toString, "ERROR3")
    assertEquals(Severity.Error.error4.toString, "ERROR4")
    assertEquals(Severity.Fatal.fatal1.toString, "FATAL")
    assertEquals(Severity.Fatal.fatal2.toString, "FATAL2")
    assertEquals(Severity.Fatal.fatal3.toString, "FATAL3")
    assertEquals(Severity.Fatal.fatal4.toString, "FATAL4")
  }

}
