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
import org.typelevel.otel4s.trace.SpanContext.SpanId
import scodec.bits.ByteVector

class SpanIdSuite extends FunSuite {

  private val first =
    ByteVector.fromValidHex("0000000000000061")

  private val second =
    ByteVector.fromValidHex("ff00000000000041")

  test("invalid") {
    assertEquals(SpanId.Invalid, ByteVector(Array.ofDim[Byte](8)))
    assertEquals(SpanId.InvalidHex, "0000000000000000")
  }

  test("is valid") {
    assertEquals(SpanId.isValid(ByteVector.empty), false)

    // too short
    assertEquals(SpanId.isValid(ByteVector.fromValidHex("001")), false)

    // too long
    assertEquals(
      SpanId.isValid(
        ByteVector.fromValidHex("00000000000000411110000000000000016")
      ),
      false
    )

    assertEquals(SpanId.isValid(SpanId.Invalid), false)

    assertEquals(SpanId.isValid(first), true)
    assertEquals(SpanId.isValid(second), true)
  }

  test("create from long") {
    assertEquals(SpanId.fromLong(0), SpanId.Invalid)
    assertEquals(SpanId.fromLong(0x61), first)
    assertEquals(SpanId.fromLong(0xff00000000000041L), second)
  }

}
