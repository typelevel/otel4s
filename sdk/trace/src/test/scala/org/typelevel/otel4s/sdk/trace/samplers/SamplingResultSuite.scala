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
package sdk
package trace
package samplers

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries
import org.typelevel.otel4s.sdk.trace.scalacheck.Cogens
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

class SamplingResultSuite extends DisciplineSuite {
  import SamplingResult.TraceStateUpdater
  import Cogens.samplingResultCogen
  import Arbitraries.samplingResultArbitrary

  checkAll("SamplingResult.HashLaws", HashTests[SamplingResult].hash)

  property("Show[SamplingResult]") {
    Prop.forAll(Gens.samplingResult) { result =>
      val expected =
        show"SamplingResult{decision=${result.decision}, attributes=${result.attributes}, traceStateUpdater=${result.traceStateUpdater}}"

      assertEquals(Show[SamplingResult].show(result), expected)
    }
  }

  property("use const instances when given attributes are empty") {
    Prop.forAll(Gens.samplingDecision) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => SamplingResult.Drop
        case SamplingDecision.RecordOnly      => SamplingResult.RecordOnly
        case SamplingDecision.RecordAndSample => SamplingResult.RecordAndSample
      }

      assertEquals(SamplingResult(decision), expected)
      assertEquals(SamplingResult(decision, Attributes.empty), expected)
      assertEquals(
        SamplingResult(decision, Attributes.empty, TraceStateUpdater.Identity),
        expected
      )
    }
  }

  property("create an instance") {
    Prop.forAll(Gens.samplingDecision, Gens.attributes) { (decision, attrs) =>
      val result = SamplingResult(decision, attrs)
      assertEquals(result.decision, decision)
      assertEquals(result.attributes, attrs)
    }
  }

  test("defaults have empty attributes and identity modifier") {
    val all = Seq(
      SamplingResult.Drop,
      SamplingResult.RecordOnly,
      SamplingResult.RecordAndSample
    )

    all.foreach { result =>
      assertEquals(result.attributes, Attributes.empty)
      assertEquals(result.traceStateUpdater, TraceStateUpdater.Identity)
    }
  }
}
