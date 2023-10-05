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

package org.typelevel.otel4s.trace

import munit._

class TraceFlagsSuite extends FunSuite {

  test("default instances") {
    assertEquals(TraceFlags.Default.toHex, "00")
    assertEquals(TraceFlags.Sampled.toHex, "01")
  }

  test("is sampled") {
    assertEquals(TraceFlags.fromByte(0xff.toByte).isSampled, true)
    assertEquals(TraceFlags.fromByte(0x01).isSampled, true)
    assertEquals(TraceFlags.fromByte(0x05).isSampled, true)
    assertEquals(TraceFlags.fromByte(0x00).isSampled, false)
  }

  test("create from byte") {
    (0 until 256).foreach { i =>
      assertEquals(TraceFlags.fromByte(i.toByte).toByte, i.toByte)
    }
  }

  test("create from hex") {
    (0 until 256).foreach { i =>
      val hex = Integer.toHexString(i)
      val input = if (hex.length == 1) "0" + hex else hex

      assertEquals(TraceFlags.fromHex(input).map(_.toHex), Some(input))
    }
  }

  test("create from hex (invalid)") {
    assertEquals(TraceFlags.fromHex("zxc"), None)
  }

}
