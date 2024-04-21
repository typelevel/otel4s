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

import munit._
import org.scalacheck.Gen
import org.scalacheck.Prop

class TraceIdRatioBasedSamplerSuite extends ScalaCheckSuite {

  test("has correct description and toString") {
    Prop.forAll(Gen.double) { ratio =>
      val sampler = Sampler.traceIdRatioBased(ratio)
      val expected = f"TraceIdRatioBased{$ratio%.6f}".replace(",", ".")

      assertEquals(sampler.description, expected)
      assertEquals(sampler.toString, expected)
    }
  }

  test("return 'RecordAndSample' when ratio = 1.0") {
    Prop.forAll(ShouldSampleInput.shouldSampleInputGen) { input =>
      val sampler = Sampler.traceIdRatioBased(1.0)
      val expected = SamplingResult.RecordAndSample

      val result = sampler.shouldSample(
        input.parentContext,
        input.traceId,
        input.name,
        input.spanKind,
        input.attributes,
        input.parentLinks
      )

      assertEquals(result, expected)
    }
  }

  test("return 'Drop' when ratio = 0.0") {
    Prop.forAll(ShouldSampleInput.shouldSampleInputGen) { input =>
      val sampler = Sampler.traceIdRatioBased(0.0)
      val expected = SamplingResult.Drop

      val result = sampler.shouldSample(
        input.parentContext,
        input.traceId,
        input.name,
        input.spanKind,
        input.attributes,
        input.parentLinks
      )

      assertEquals(result, expected)
    }
  }

  test("throw an error when ratio is out of range") {
    val negative = Gen.negNum[Double]
    val positive = Gen.chooseNum(1.1, Double.MaxValue)

    Prop.forAll(Gen.oneOf(negative, positive)) { ratio =>
      val _ = interceptMessage[Throwable](
        "requirement failed: ratio must be >= 0 and <= 1.0"
      )(
        Sampler.traceIdRatioBased(ratio)
      )
    }
  }

}
