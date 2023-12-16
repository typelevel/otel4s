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
import org.scalacheck.Prop

class SamplerSuite extends ScalaCheckSuite {

  test("AlwaysOn - correct description and toString") {
    assertEquals(Sampler.AlwaysOn.toString, "AlwaysOnSampler")
    assertEquals(Sampler.AlwaysOn.description, "AlwaysOnSampler")
  }

  test("AlwaysOn - always return 'RecordAndSample'") {
    Prop.forAll(ShouldSampleInput.shouldSampleInputGen) { input =>
      val expected = SamplingResult.RecordAndSample
      val result = Sampler.AlwaysOn.shouldSample(
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

  test("AlwaysOff - correct description and toString") {
    assertEquals(Sampler.AlwaysOff.toString, "AlwaysOffSampler")
    assertEquals(Sampler.AlwaysOff.description, "AlwaysOffSampler")
  }

  test("AlwaysOff - always return 'Drop'") {
    Prop.forAll(ShouldSampleInput.shouldSampleInputGen) { input =>
      val expected = SamplingResult.Drop
      val result = Sampler.AlwaysOff.shouldSample(
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

}
