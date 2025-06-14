/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter

class ExemplarFilterAutoConfigureSuite extends CatsEffectSuite {

  private val lookup =
    TraceContext.Lookup.noop

  private val configurer =
    ExemplarFilterAutoConfigure[IO](lookup)

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)
    configurer.configure(config).use { filter =>
      IO(assertEquals(filter, ExemplarFilter.traceBased(lookup)))
    }
  }

  test("load from the config (empty string) - load default") {
    val props = Map("otel.metrics.exemplar.filter" -> "")
    val config = Config(props, Map.empty, Map.empty)
    configurer.configure(config).use { filter =>
      IO(assertEquals(filter, ExemplarFilter.traceBased(lookup)))
    }
  }

  test("load from the config - always_on") {
    val props = Map("otel.metrics.exemplar.filter" -> "always_on")
    val config = Config(props, Map.empty, Map.empty)
    configurer.configure(config).use { filter =>
      IO(assertEquals(filter, ExemplarFilter.alwaysOn))
    }
  }

  test("load from the config - always_off") {
    val props = Map("otel.metrics.exemplar.filter" -> "always_off")
    val config = Config(props, Map.empty, Map.empty)
    configurer.configure(config).use { filter =>
      IO(assertEquals(filter, ExemplarFilter.alwaysOff))
    }
  }

  test("load from the config - trace_based") {
    val props = Map("otel.metrics.exemplar.filter" -> "trace_based")
    val config = Config(props, Map.empty, Map.empty)
    configurer.configure(config).use { filter =>
      IO(assertEquals(filter, ExemplarFilter.traceBased(lookup)))
    }
  }

  test("load from the config - unknown filter - fail") {
    val props = Map("otel.metrics.exemplar.filter" -> "some-filter")
    val config = Config(props, Map.empty, Map.empty)

    configurer
      .configure(config)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          """Cannot autoconfigure [ExemplarFilter].
            |Cause: Unrecognized value for [otel.metrics.exemplar.filter]: some-filter. Supported options [always_on, always_off, trace_based].
            |Config:
            |1) `otel.metrics.exemplar.filter` - some-filter""".stripMargin
        )
      )
  }
}
