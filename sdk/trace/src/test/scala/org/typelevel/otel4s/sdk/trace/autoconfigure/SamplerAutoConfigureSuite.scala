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
package autoconfigure

import cats.effect.IO
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.sdk.trace.samplers.Sampler
import org.typelevel.otel4s.sdk.trace.samplers.SamplingResult
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

class SamplerAutoConfigureSuite extends CatsEffectSuite {

  private val default = Sampler.parentBased[IO](Sampler.alwaysOn)

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, default.toString))
    }
  }

  test("load from the config (empty string) - load default") {
    val props = Map("otel.traces.sampler" -> "")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, default.toString))
    }
  }

  test("load from the config - always_on") {
    val props = Map("otel.traces.sampler" -> "always_on")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.alwaysOn[IO].toString))
    }
  }

  test("load from the config - always_off") {
    val props = Map("otel.traces.sampler" -> "always_off")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.alwaysOff[IO].toString))
    }
  }

  test("load from the config - traceidratio - use default ratio") {
    val props = Map("otel.traces.sampler" -> "traceidratio")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.traceIdRatioBased[IO](1.0).toString))
    }
  }

  test("load from the config - traceidratio - use given ratio") {
    val props = Map(
      "otel.traces.sampler" -> "traceidratio",
      "otel.traces.sampler.arg" -> "0.1"
    )
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.traceIdRatioBased[IO](0.1).toString))
    }
  }

  test("load from the config - traceidratio - fail when ratio is incorrect") {
    val props = Map(
      "otel.traces.sampler" -> "traceidratio",
      "otel.traces.sampler.arg" -> "-0.1"
    )
    val config = Config(props, Map.empty, Map.empty)

    SamplerAutoConfigure[IO](Set.empty)
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          """Cannot autoconfigure [Sampler].
            |Cause: [otel.traces.sampler.arg] has invalid ratio [-0.1] - requirement failed: ratio must be >= 0 and <= 1.0.
            |Config:
            |1) `otel.traces.sampler` - traceidratio
            |2) `otel.traces.sampler.arg` - -0.1""".stripMargin
        )
      )
  }

  test("load from the config - parentbased_always_on") {
    val props = Map("otel.traces.sampler" -> "parentbased_always_on")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.parentBased[IO](Sampler.alwaysOn).toString))
    }
  }

  test("load from the config - parentbased_always_off") {
    val props = Map("otel.traces.sampler" -> "parentbased_always_off")
    val config = Config(props, Map.empty, Map.empty)
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, Sampler.parentBased[IO](Sampler.alwaysOff).toString))
    }
  }

  test("load from the config - parentbased_traceidratio - use default ratio") {
    val props = Map("otel.traces.sampler" -> "parentbased_traceidratio")
    val config = Config(props, Map.empty, Map.empty)
    val expected = Sampler.parentBased[IO](Sampler.traceIdRatioBased(1.0))
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, expected.toString))
    }
  }

  test("load from the config - parentbased_traceidratio - use given ratio") {
    val props = Map(
      "otel.traces.sampler" -> "parentbased_traceidratio",
      "otel.traces.sampler.arg" -> "0.1"
    )
    val config = Config(props, Map.empty, Map.empty)
    val expected = Sampler.parentBased[IO](Sampler.traceIdRatioBased(0.1))
    SamplerAutoConfigure[IO](Set.empty).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, expected.toString))
    }
  }

  test("support custom configurers") {
    val props = Map("otel.traces.sampler" -> "custom-sampler")
    val config = Config.ofProps(props)

    val customSampler: Sampler[IO] = new Sampler.Unsealed[IO] {
      def shouldSample(
          parentContext: Option[SpanContext],
          traceId: ByteVector,
          name: String,
          spanKind: SpanKind,
          attributes: Attributes,
          parentLinks: Vector[LinkData]
      ): IO[SamplingResult] =
        IO.pure(SamplingResult.Drop)

      def description: String = "SomeCustomSampler"
    }

    val custom: AutoConfigure.Named[IO, Sampler[IO]] =
      AutoConfigure.Named.const("custom-sampler", customSampler)

    SamplerAutoConfigure[IO](Set(custom)).configure(config).use { sampler =>
      IO(assertEquals(sampler.toString, customSampler.toString))
    }
  }

  test("load from the config - parentbased_traceidratio - fail when ratio is incorrect") {
    val props = Map(
      "otel.traces.sampler" -> "parentbased_traceidratio",
      "otel.traces.sampler.arg" -> "-0.1"
    )
    val config = Config(props, Map.empty, Map.empty)

    SamplerAutoConfigure[IO](Set.empty)
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          """Cannot autoconfigure [Sampler].
            |Cause: [otel.traces.sampler.arg] has invalid ratio [-0.1] - requirement failed: ratio must be >= 0 and <= 1.0.
            |Config:
            |1) `otel.traces.sampler` - parentbased_traceidratio
            |2) `otel.traces.sampler.arg` - -0.1""".stripMargin
        )
      )
  }

  test("load from the config - unknown sampler - fail") {
    val props = Map("otel.traces.sampler" -> "some-sampler")
    val config = Config(props, Map.empty, Map.empty)

    SamplerAutoConfigure[IO](Set.empty)
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          """Cannot autoconfigure [Sampler].
            |Cause: Unrecognized value for [otel.traces.sampler]: some-sampler. Supported options [parentbased_traceidratio, traceidratio, parentbased_always_off, always_on, always_off, parentbased_always_on].
            |Config:
            |1) `otel.traces.sampler` - some-sampler
            |2) `otel.traces.sampler.arg` - N/A""".stripMargin
        )
      )
  }
}
