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
package samplers

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit._
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries
import org.typelevel.otel4s.sdk.trace.scalacheck.Cogens
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

class SamplingDecisionSuite extends DisciplineSuite {
  import Cogens.samplingDecisionCogen
  import Arbitraries.samplingDecisionArbitrary

  checkAll("SamplingDecision.HashLaws", HashTests[SamplingDecision].hash)

  property("is sampled") {
    Prop.forAll(Gens.samplingDecision) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => false
        case SamplingDecision.RecordOnly      => false
        case SamplingDecision.RecordAndSample => true
      }

      assertEquals(decision.isSampled, expected)
    }
  }

  property("is recording") {
    Prop.forAll(Gens.samplingDecision) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => false
        case SamplingDecision.RecordOnly      => true
        case SamplingDecision.RecordAndSample => true
      }

      assertEquals(decision.isRecording, expected)
    }
  }

  property("Show[SamplingDecision]") {
    Prop.forAll(Gens.samplingDecision) { decision =>
      val expected = decision match {
        case SamplingDecision.Drop            => "Drop"
        case SamplingDecision.RecordOnly      => "RecordOnly"
        case SamplingDecision.RecordAndSample => "RecordAndSample"
      }

      assertEquals(Show[SamplingDecision].show(decision), expected)
    }
  }

}
