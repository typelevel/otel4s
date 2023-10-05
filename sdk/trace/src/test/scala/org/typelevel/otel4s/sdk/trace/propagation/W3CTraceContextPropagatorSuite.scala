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

package org.typelevel.otel4s.sdk.trace.propagation

import munit._
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkTraceScope
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import scodec.bits.ByteVector

class W3CTraceContextPropagatorSuite extends FunSuite {

  private val traceIdHex = "00000000000000000000000000000061"
  private val traceId = ByteVector.fromValidHex(traceIdHex)

  private val spanIdHex = "0000000000000061"
  private val spanId = ByteVector.fromValidHex(spanIdHex)

  private val flags = List(TraceFlags.Sampled, TraceFlags.Default)

  private val propagator = W3CTraceContextPropagator

  //
  // Inject
  //

  test("inject nothing when context is empty") {
    val result = propagator.injected(Context.root, Map.empty[String, String])
    assertEquals(result.size, 0)
  }

  test("inject context info") {
    flags.foreach { flag =>
      val spanContext =
        SpanContext.create(traceId, spanId, flag, remote = false)
      val ctx = SdkTraceScope.storeInContext(Context.root, spanContext)
      val result = propagator.injected(ctx, Map.empty[String, String])

      val suffix = if (flag.isSampled) "01" else "00"

      val expected =
        s"00-${spanContext.traceIdHex}-${spanContext.spanIdHex}-$suffix"

      assertEquals(result.get("traceparent"), Some(expected))
    }
  }

  //
  // Extract
  //

  test("extract span context") {
    flags.foreach { flag =>
      val spanContext =
        SpanContext.create(traceId, spanId, flag, remote = false)
      val carrier = Map("traceparent" -> toTraceParent(spanContext))

      val ctx = propagator.extract(Context.root, carrier)

      val expected = SpanContext.create(traceId, spanId, flag, remote = true)

      assertEquals(SdkTraceScope.fromContext(ctx), Some(expected))
    }
  }

  test("extract nothing when carrier is empty") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(SdkTraceScope.fromContext(ctx), None)
  }

  test("extract nothing when carrier doesn't have a mandatory key") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(SdkTraceScope.fromContext(ctx), None)
  }

  test("extract nothing when the traceparent in invalid") {
    val carrier = Map("traceparent" -> "00-11-22-33")
    val ctx = propagator.extract(Context.root, carrier)
    assertEquals(SdkTraceScope.fromContext(ctx), None)
  }

  private def toTraceParent(spanContext: SpanContext): String =
    s"00-${spanContext.traceIdHex}-${spanContext.spanIdHex}-${spanContext.traceFlags.toHex}"
}
