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

package org.typelevel.otel4s.java.trace

import io.opentelemetry.api.trace.{SpanContext => JSpanContext}
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

class SpanContextSuite extends ScalaCheckSuite {

  private val traceIdGen: Gen[ByteVector] =
    for {
      hi <- Gen.long
      lo <- Gen.long.suchThat(_ != 0)
    } yield SpanContext.TraceId.fromLongs(hi, lo)

  private val spanIdGen: Gen[ByteVector] =
    for {
      value <- Gen.long.suchThat(_ != 0)
    } yield SpanContext.SpanId.fromLong(value)

  private val spanContextGen: Gen[SpanContext] =
    for {
      traceId <- traceIdGen
      spanId <- spanIdGen
      traceFlags <- Gen.oneOf(TraceFlags.Sampled, TraceFlags.Default)
      remote <- Gen.oneOf(true, false)
    } yield SpanContext(traceId, spanId, traceFlags, TraceState.empty, remote)

  test("SpanContext.invalid satisfies the specification") {
    val context = SpanContext.invalid
    val jContext = JSpanContext.getInvalid
    assert(context.traceId.toArray.sameElements(jContext.getTraceIdBytes))
    assertEquals(context.traceIdHex, jContext.getTraceId)
    assert(context.spanId.toArray.sameElements(jContext.getSpanIdBytes))
    assertEquals(context.spanIdHex, jContext.getSpanId)
    assertEquals(context.isSampled, jContext.isSampled)
    assertEquals(context.isValid, jContext.isValid)
    assertEquals(context.isRemote, jContext.isRemote)
  }

  test("SpanContext to JSpanContext") {
    Prop.forAll(spanContextGen) { ctx =>
      val jCtx = WrappedSpanContext.unwrap(ctx)

      assert(ctx.traceId.toArray.sameElements(jCtx.getTraceIdBytes))
      assert(ctx.spanId.toArray.sameElements(jCtx.getSpanIdBytes))
      assertEquals(ctx.traceIdHex, jCtx.getTraceId)
      assertEquals(ctx.spanIdHex, jCtx.getSpanId)
      assertEquals(ctx.isSampled, jCtx.isSampled)
      assertEquals(ctx.isValid, jCtx.isValid)
      assertEquals(ctx.isRemote, jCtx.isRemote)
      assertEquals(ctx.traceFlags.toByte, jCtx.getTraceFlags.asByte())
      assertEquals(ctx.traceFlags.toHex, jCtx.getTraceFlags.asHex())
    }
  }

  test("back and forth conversion") {
    Prop.forAll(spanContextGen) { ctx =>
      val jCtx = WrappedSpanContext.unwrap(ctx)
      assertEquals(WrappedSpanContext.wrap(jCtx), ctx)
    }
  }

}
