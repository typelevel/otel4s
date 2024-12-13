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

import cats.effect.IO
import munit._
import org.scalacheck.Test
import org.scalacheck.effect.PropF

class SamplerSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("AlwaysOn - correct description and toString") {
    val sampler = Sampler.alwaysOn[IO]
    assertEquals(sampler.toString, "AlwaysOnSampler")
    assertEquals(sampler.description, "AlwaysOnSampler")
  }

  test("AlwaysOn - always return 'RecordAndSample'") {
    PropF.forAllF(ShouldSampleInput.shouldSampleInputGen) { input =>
      val expected = SamplingResult.RecordAndSample

      Sampler
        .alwaysOn[IO]
        .shouldSample(
          input.parentContext,
          input.traceId,
          input.name,
          input.spanKind,
          input.attributes,
          input.parentLinks
        )
        .assertEquals(expected)
    }
  }

  test("AlwaysOff - correct description and toString") {
    val sampler = Sampler.alwaysOff[IO]
    assertEquals(sampler.toString, "AlwaysOffSampler")
    assertEquals(sampler.description, "AlwaysOffSampler")
  }

  test("AlwaysOff - always return 'Drop'") {
    PropF.forAllF(ShouldSampleInput.shouldSampleInputGen) { input =>
      val expected = SamplingResult.Drop

      Sampler
        .alwaysOff[IO]
        .shouldSample(
          input.parentContext,
          input.traceId,
          input.name,
          input.spanKind,
          input.attributes,
          input.parentLinks
        )
        .assertEquals(expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(10)
      .withMaxSize(10)
}
