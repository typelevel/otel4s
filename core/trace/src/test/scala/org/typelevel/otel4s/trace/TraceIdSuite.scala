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
import org.typelevel.otel4s.trace.SpanContext.TraceId
import scodec.bits.ByteVector

class TraceIdSuite extends FunSuite {

  private val first =
    ByteVector.fromValidHex("00000000000000000000000000000061")

  private val second =
    ByteVector.fromValidHex("ff000000000000000000000000000041")

  test("invalid") {
    assertEquals(TraceId.Invalid, ByteVector(Array.ofDim[Byte](16)))
    assertEquals(TraceId.InvalidHex, "00000000000000000000000000000000")
  }

  test("is valid") {
    assertEquals(TraceId.isValid(ByteVector.empty), false)

    // too short
    assertEquals(TraceId.isValid(ByteVector.fromValidHex("001")), false)

    // too long
    assertEquals(
      TraceId.isValid(
        ByteVector.fromValidHex("00000000000000411110000000000000016")
      ),
      false
    )

    assertEquals(TraceId.isValid(TraceId.Invalid), false)

    assertEquals(TraceId.isValid(first), true)
    assertEquals(TraceId.isValid(second), true)
  }

  test("create from longs") {
    assertEquals(TraceId.fromLongs(0, 0), TraceId.Invalid)
    assertEquals(TraceId.fromLongs(0, 0x61), first)
    assertEquals(TraceId.fromLongs(0xff00000000000000L, 0x41), second)
    assertEquals(
      TraceId.fromLongs(0xff01020304050600L, 0xff0a0b0c0d0e0f00L),
      ByteVector.fromValidHex("ff01020304050600ff0a0b0c0d0e0f00")
    )
  }

}
