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

package org.typelevel.otel4s.sdk.trace.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import cats.syntax.show._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.trace.SpanLimits

class SpanLimitsAutoConfigureSuite extends CatsEffectSuite {

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    SpanLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits, SpanLimits.default))
      }
  }

  test("load from the config (empty string) - load default") {
    val props = Map(
      "otel.span.attribute.count.limit" -> "",
      "otel.span.event.count.limit" -> "",
      "otel.span.link.count.limit" -> "",
      "otel.event.attribute.count.limit" -> "",
      "otel.link.attribute.count.limit" -> "",
      "otel.span.attribute.value.length.limit" -> "",
    )

    val config = Config.ofProps(props)

    SpanLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits, SpanLimits.default))
      }
  }

  test("load from the config - use given value") {
    val props = Map(
      "otel.span.attribute.count.limit" -> "100",
      "otel.span.event.count.limit" -> "101",
      "otel.span.link.count.limit" -> "102",
      "otel.event.attribute.count.limit" -> "103",
      "otel.link.attribute.count.limit" -> "104",
      "otel.span.attribute.value.length.limit" -> "105",
    )

    val config = Config.ofProps(props)

    val expected =
      "SpanLimits{" +
        "maxNumberOfAttributes=100, " +
        "maxNumberOfEvents=101, " +
        "maxNumberOfLinks=102, " +
        "maxNumberOfAttributesPerEvent=103, " +
        "maxNumberOfAttributesPerLink=104, " +
        "maxAttributeValueLength=105}"

    SpanLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits.show, expected))
      }
  }

  test("invalid config value - fail") {
    val config =
      Config.ofProps(Map("otel.span.attribute.count.limit" -> "not int"))
    val error =
      "Invalid value for property otel.span.attribute.count.limit=not int. Must be [Int]"

    SpanLimitsAutoConfigure[IO]
      .configure(config)
      .evalMap(IO.println)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          s"""Cannot autoconfigure [SpanLimits].
             |Cause: $error.
             |Config:
             |1) `otel.event.attribute.count.limit` - N/A
             |2) `otel.link.attribute.count.limit` - N/A
             |3) `otel.span.attribute.count.limit` - not int
             |4) `otel.span.attribute.value.length.limit` - N/A
             |5) `otel.span.event.count.limit` - N/A
             |6) `otel.span.link.count.limit` - N/A""".stripMargin
        )
      )
  }

}
