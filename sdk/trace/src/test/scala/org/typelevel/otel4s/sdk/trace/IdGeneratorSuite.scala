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

import cats.effect.IO
import cats.effect.std.Random
import munit.CatsEffectSuite
import org.typelevel.otel4s.trace.SpanContext

class IdGeneratorSuite extends CatsEffectSuite {
  private val Attempts = 1_000_000

  generatorTest("generate a valid trace id") { generator =>
    generator.generateTraceId
      .map(v => assert(SpanContext.TraceId.isValid(v)))
      .replicateA_(Attempts)
  }

  generatorTest("generate a valid span id") { generator =>
    generator.generateSpanId
      .map(v => assert(SpanContext.SpanId.isValid(v)))
      .replicateA_(Attempts)
  }

  generatorTest("should be trusted") { generator =>
    IO(assert(generator.canSkipIdValidation))
  }

  private def generatorTest(
      name: String
  )(body: IdGenerator[IO] => IO[Unit]): Unit =
    test(name) {
      Random
        .scalaUtilRandom[IO]
        .flatMap { implicit random: Random[IO] => body(IdGenerator.random[IO]) }
    }

}
