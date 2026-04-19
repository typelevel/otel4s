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

package org.typelevel.otel4s.oteljava.testkit.trace

import cats.data.NonEmptyList
import munit.FunSuite
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

class SpanContextExpectationSuite extends FunSuite {

  test("any matches any span context") {
    assertEquals(SpanContextExpectation.any.check(context()), Right(()))
  }

  test("traceIdHex matches exact trace ID") {
    val actual = context()

    assertEquals(SpanContextExpectation.any.traceIdHex(actual.traceIdHex).check(actual), Right(()))
    assertEquals(
      SpanContextExpectation.any.traceIdHex("00000000000000000000000000000001").check(actual),
      Left(
        NonEmptyList.one(
          SpanContextExpectation.Mismatch.TraceIdMismatch("00000000000000000000000000000001", actual.traceIdHex)
        )
      )
    )
  }

  test("spanId matches typed span ID") {
    val actual = context()

    assertEquals(SpanContextExpectation.any.spanId(actual.spanId).check(actual), Right(()))
    assertEquals(
      SpanContextExpectation.any.spanId(ByteVector.fromValidHex("0000000000000001")).check(actual),
      Left(NonEmptyList.one(SpanContextExpectation.Mismatch.SpanIdMismatch("0000000000000001", actual.spanIdHex)))
    )
  }

  test("sampled and remote match booleans exactly") {
    val actual = context(sampled = true, remote = false)

    assertEquals(SpanContextExpectation.any.sampled(true).remote(false).check(actual), Right(()))
    assertEquals(
      SpanContextExpectation.any.sampled(false).remote(true).check(actual),
      Left(
        NonEmptyList.of(
          SpanContextExpectation.Mismatch.SampledMismatch(false, true),
          SpanContextExpectation.Mismatch.RemoteMismatch(true, false)
        )
      )
    )
  }

  test("traceFlags and traceState match exactly") {
    val state = TraceState.empty.updated("vendor", "value")
    val actual = context(traceFlags = TraceFlags.Default, traceState = state)

    assertEquals(
      SpanContextExpectation.any.traceFlags(TraceFlags.Default).traceState(state).check(actual),
      Right(())
    )
    assertEquals(
      SpanContextExpectation.any.traceState(TraceState.empty).check(actual),
      Left(NonEmptyList.one(SpanContextExpectation.Mismatch.TraceStateMismatch(TraceState.empty, state)))
    )
  }

  test("where adds predicate mismatch") {
    val actual = context()
    val expectation = SpanContextExpectation.any.where("must be remote")(_.isRemote)

    assertEquals(
      expectation.check(actual),
      Left(NonEmptyList.one(SpanContextExpectation.Mismatch.PredicateMismatch(Some("must be remote"))))
    )
  }

  private def context(
      traceId: ByteVector = ByteVector.fromValidHex("0af7651916cd43dd8448eb211c80319c"),
      spanId: ByteVector = ByteVector.fromValidHex("b7ad6b7169203331"),
      traceFlags: TraceFlags = TraceFlags.Default,
      traceState: TraceState = TraceState.empty,
      remote: Boolean = false,
      sampled: Boolean = false
  ) =
    SpanContext(
      traceId = traceId,
      spanId = spanId,
      traceFlags = traceFlags.withSampled(sampled),
      traceState = traceState,
      remote = remote
    )
}
