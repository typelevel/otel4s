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

package org.typelevel.otel4s.sdk.autoconfigure

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop

class AutoConfigureErrorSuite extends ScalaCheckSuite {

  test("use hint in the error message") {
    Prop.forAll(Gen.alphaNumStr, Gen.alphaNumStr) { (hint, reason) =>
      val error = AutoConfigureError(hint, new Err(reason))

      assertEquals(
        error.getMessage,
        s"Cannot autoconfigure [$hint]. Cause: $reason."
      )
    }
  }

  test("use hint and config keys in the error message") {
    Prop.forAll(Gen.alphaNumStr, Gen.alphaNumStr) { (hint, reason) =>
      val config = Config.ofProps(
        Map(
          "a.b.c.d" -> "value1",
          "c" -> "value;for;key;3"
        )
      )

      val keys: Set[Config.Key[_]] = Set(
        Config.Key[String]("a.b.c.d"),
        Config.Key[Set[String]]("a1.b1.c.1.d3"),
        Config.Key[Map[String, String]]("c"),
        Config.Key[Double]("d")
      )

      val error = AutoConfigureError(hint, new Err(reason), keys, config)

      val expected =
        s"""Cannot autoconfigure [$hint].
           |Cause: $reason.
           |Config:
           |1) `a.b.c.d` - value1
           |2) `a1.b1.c.1.d3` - N/A
           |3) `c` - value;for;key;3
           |4) `d` - N/A""".stripMargin

      assertEquals(error.getMessage, expected)
    }
  }

  private class Err(reason: String) extends RuntimeException(reason)

}
