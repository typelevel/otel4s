/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs.autoconfigure

import cats.effect.IO
import cats.syntax.either._
import munit.CatsEffectSuite
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.logs.LogRecordLimits

class LogRecordLimitsAutoConfigureSuite extends CatsEffectSuite {

  test("load from an empty config - load default") {
    val config = Config(Map.empty, Map.empty, Map.empty)

    LogRecordLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits, LogRecordLimits.default))
      }
  }

  test("load from the config (empty string) - load default") {
    val props = Map(
      "otel.attribute.count.limit" -> "",
      "otel.attribute.value.length.limit" -> "",
    )

    val config = Config.ofProps(props)

    LogRecordLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits, LogRecordLimits.default))
      }
  }

  test("load from the config - use given value") {
    val props = Map(
      "otel.attribute.count.limit" -> "100",
      "otel.attribute.value.length.limit" -> "105",
    )

    val config = Config.ofProps(props)

    val expected =
      "LogRecordLimits{maxNumberOfAttributes=100, maxAttributeValueLength=105}"

    LogRecordLimitsAutoConfigure[IO]
      .configure(config)
      .use { limits =>
        IO(assertEquals(limits.toString, expected))
      }
  }

  test("invalid config value - fail") {
    val config = Config.ofProps(Map("otel.attribute.count.limit" -> "not int"))
    val error =
      "Invalid value for property otel.attribute.count.limit=not int. Must be [Int]"

    LogRecordLimitsAutoConfigure[IO]
      .configure(config)
      .evalMap(IO.println)
      .use_
      .attempt
      .map(_.leftMap(_.getMessage))
      .assertEquals(
        Left(
          s"""Cannot autoconfigure [LogRecordLimits].
             |Cause: $error.
             |Config:
             |1) `otel.attribute.count.limit` - not int
             |2) `otel.attribute.value.length.limit` - N/A""".stripMargin
        )
      )
  }

}
