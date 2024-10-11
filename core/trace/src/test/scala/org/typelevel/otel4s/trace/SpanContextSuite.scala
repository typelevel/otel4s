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

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit._
import org.scalacheck.Prop
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId
import org.typelevel.otel4s.trace.scalacheck.Arbitraries._
import org.typelevel.otel4s.trace.scalacheck.Cogens._
import org.typelevel.otel4s.trace.scalacheck.Gens
import scodec.bits.ByteVector

class SpanContextSuite extends DisciplineSuite {

  private val ValidTraceIdHex =
    "00000000000000000000000000000061"

  private val ValidTraceId =
    ByteVector.fromValidHex(ValidTraceIdHex)

  private val ValidSpanIdHex =
    "0000000000000061"

  private val ValidSpanId =
    ByteVector.fromValidHex(ValidSpanIdHex)

  checkAll("SpanContext.HashLaws", HashTests[SpanContext].hash)

  test("invalid span context") {
    assertEquals(SpanContext.invalid.traceId, TraceId.Invalid)
    assertEquals(SpanContext.invalid.traceIdHex, TraceId.InvalidHex)
    assertEquals(SpanContext.invalid.spanId, SpanId.Invalid)
    assertEquals(SpanContext.invalid.spanIdHex, SpanId.InvalidHex)
    assertEquals(SpanContext.invalid.traceFlags, TraceFlags.Default)
    assertEquals(SpanContext.invalid.traceState, TraceState.empty)
    assertEquals(SpanContext.invalid.isValid, false)
    assertEquals(SpanContext.invalid.isRemote, false)
  }

  test("fallback to an invalid span context when trace id is invalid") {
    val input = List(
      ByteVector.empty,
      ByteVector.fromValidHex("0000"), // too short
      ByteVector.fromValidHex("0000000000000000000000000000006100"), // too long
    )

    def context(traceId: ByteVector) = SpanContext(
      traceId = traceId,
      spanId = ByteVector.fromValidHex("0000000000000061"),
      traceFlags = TraceFlags.Default,
      traceState = TraceState.empty,
      remote = false
    )

    input.foreach { traceId =>
      assertEquals(context(traceId), SpanContext.invalid)
    }
  }

  test("fallback to an invalid span context when span id is invalid") {
    val input = List(
      ByteVector.empty,
      ByteVector.fromValidHex("0000"), // too short
      ByteVector.fromValidHex("000000000000006100"), // too long
    )

    def context(spanId: ByteVector) = SpanContext(
      traceId = ValidTraceId,
      spanId = spanId,
      traceFlags = TraceFlags.Default,
      traceState = TraceState.empty,
      remote = false
    )

    input.foreach { traceId =>
      assertEquals(context(traceId), SpanContext.invalid)
    }
  }

  test("convert ByteVector traceId and spanId to hex") {
    val context = SpanContext(
      traceId = ValidTraceId,
      spanId = ValidSpanId,
      traceFlags = TraceFlags.Sampled,
      traceState = TraceState.empty,
      remote = false
    )

    assertEquals(context.traceId, ValidTraceId)
    assertEquals(context.traceIdHex, ValidTraceIdHex)
    assertEquals(context.spanId, ValidSpanId)
    assertEquals(context.spanIdHex, ValidSpanIdHex)
  }

  test("Show[SpanContext]") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val expected = "SpanContext{" +
        show"traceId=${ctx.traceIdHex}, " +
        show"spanId=${ctx.spanIdHex}, " +
        show"traceFlags=${ctx.traceFlags}, " +
        show"traceState=${ctx.traceState}, " +
        show"remote=${ctx.isRemote}, " +
        show"valid=${ctx.isValid}}"

      assertEquals(Show[SpanContext].show(ctx), expected)
    }
  }

}
