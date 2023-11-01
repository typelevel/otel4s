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

package org.typelevel.otel4s.sdk.trace.samplers

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class SamplingDecisionSuite extends DisciplineSuite {

  private val samplingDecisionGen: Gen[SamplingDecision] =
    Gen.oneOf(
      SamplingDecision.Drop,
      SamplingDecision.RecordOnly,
      SamplingDecision.RecordAndSample
    )

  private implicit val samplingDecisionArbitrary: Arbitrary[SamplingDecision] =
    Arbitrary(samplingDecisionGen)

  private implicit val samplingDecisionCogen: Cogen[SamplingDecision] =
    Cogen[String].contramap(_.toString)

  checkAll("SamplingDecision.HashLaws", HashTests[SamplingDecision].hash)

  property("is sampled") {
    Prop.forAll(samplingDecisionGen) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => false
        case SamplingDecision.RecordOnly      => false
        case SamplingDecision.RecordAndSample => true
      }

      assertEquals(decision.isSampled, expected)
    }
  }

  property("Show[SamplingDecision]") {
    Prop.forAll(samplingDecisionGen) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => "Drop"
        case SamplingDecision.RecordOnly      => "RecordOnly"
        case SamplingDecision.RecordAndSample => "RecordAndSample"
      }

      assertEquals(Show[SamplingDecision].show(decision), expected)
    }
  }

}
