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

import cats.Hash
import cats.Show
import munit._
import org.scalacheck.Gen
import org.scalacheck.Prop

class SamplingDecisionSuite extends ScalaCheckSuite {

  private val samplingDecisionGen: Gen[SamplingDecision] =
    Gen.oneOf(
      SamplingDecision.Drop,
      SamplingDecision.RecordOnly,
      SamplingDecision.RecordAndSample
    )

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

  test("Hash[SamplingDecision]") {
    val allWithIndex = List(
      SamplingDecision.Drop,
      SamplingDecision.RecordOnly,
      SamplingDecision.RecordAndSample
    ).zipWithIndex

    allWithIndex.foreach { case (that, thatIdx) =>
      allWithIndex.foreach { case (other, otherIdx) =>
        assertEquals(
          Hash[SamplingDecision].eqv(that, other),
          thatIdx == otherIdx
        )
      }
    }
  }

}
