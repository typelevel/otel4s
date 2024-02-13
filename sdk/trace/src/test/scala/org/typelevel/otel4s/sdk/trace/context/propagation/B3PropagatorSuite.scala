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

package org.typelevel.otel4s.sdk.trace.context.propagation

import munit.CatsEffectSuite
import munit.ScalaCheckSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

class B3PropagatorSuite extends CatsEffectSuite with ScalaCheckSuite {

  private val single = B3Propagator.singleHeader
  private val multi = B3Propagator.multipleHeaders

  private object Headers {
    val TraceId = "X-B3-TraceId"
    val SpanId = "X-B3-SpanId"
    val Sampled = "X-B3-Sampled"
    val Debug = "X-B3-Flags"
    val Combined = "b3"
  }

  //
  // Common
  //

  test("header names") {
    assertEquals(Headers.TraceId, "X-B3-TraceId")
    assertEquals(Headers.SpanId, "X-B3-SpanId")
    assertEquals(Headers.Sampled, "X-B3-Sampled")
    assertEquals(Headers.Combined, "b3")
  }

  //
  // Multi header
  //

  test("multi - fields") {
    assertEquals(
      multi.fields.toList,
      List("X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled")
    )
  }

  test("multi - toString") {
    assertEquals(
      multi.toString,
      "B3Propagator{b3PropagatorInjector=B3Propagator.Injector.MultipleHeaders}"
    )
  }

  test("multi - inject - invalid context") {
    val ctx = SpanContext(
      traceId = SpanContext.TraceId.Invalid,
      spanId = SpanContext.SpanId.Invalid,
      traceFlags = TraceFlags.Sampled,
      traceState = TraceState.empty.updated("key", "value"),
      remote = false
    )

    val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

    assertEquals(
      multi.inject(context, Map.empty[String, String]),
      Map.empty[String, String]
    )
  }

  test("multi - inject - valid context") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

      val result = multi.inject(context, Map.empty[String, String])
      val expected = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      assertEquals(result, expected)
    }
  }

  test("multi - inject - valid context (debug enabled)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val context = Context.root
        .updated(SdkContextKeys.SpanContextKey, ctx)
        .updated(B3Propagator.ContextKeys.Debug, true)

      val result = multi.inject(context, Map.empty[String, String])
      // the 'sampled' and 'debug' headers must be always set to '1' when debug is enabled
      val expected = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> "1",
        Headers.Debug -> "1",
      )

      assertEquals(result, expected)
    }
  }

  test("multi - extract - empty carrier") {
    val result = multi.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(result), None)
  }

  test("multi - extract - sampled header as int") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      val expected = asRemote(ctx)

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("multi - extract - sampled header as boolean") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "true" else "false")
      )

      val result = multi.extract(Context.root, carrier)
      val expected = asRemote(ctx)

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("multi - extract - debug header is present, sampled header is missing") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Debug -> "1"
      )

      val result = multi.extract(Context.root, carrier)

      // the extract context must always be sampled
      val expected = SpanContext(
        traceId = ctx.traceId,
        spanId = ctx.spanId,
        traceFlags = TraceFlags.Sampled,
        traceState = ctx.traceState,
        remote = true
      )

      assertEquals(getSpanContext(result), Some(expected))
      assertEquals(result.get(B3Propagator.ContextKeys.Debug), Some(true))
    }
  }

  test("multi - extract - debug header is present, sampled header is present") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0"),
        Headers.Debug -> "1"
      )

      val result = multi.extract(Context.root, carrier)

      // the extract context must always be sampled, even when 'sampled' header = false or 0
      val expected = SpanContext(
        traceId = ctx.traceId,
        spanId = ctx.spanId,
        traceFlags = TraceFlags.Sampled,
        traceState = ctx.traceState,
        remote = true
      )

      assertEquals(getSpanContext(result), Some(expected))
      assertEquals(result.get(B3Propagator.ContextKeys.Debug), Some(true))
    }
  }

  test("multi - extract - short trace id (16 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val shortTraceId = ctx.traceIdHex.take(16)

      val carrier = Map(
        Headers.TraceId -> shortTraceId,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      val expected = SpanContext(
        traceId = ByteVector.fromValidHex(shortTraceId).padLeft(16),
        spanId = ctx.spanId,
        traceFlags = ctx.traceFlags,
        traceState = ctx.traceState,
        remote = true
      )

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("multi - extract - invalid trace id - too short (2 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex.take(2),
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid trace id - too long (33 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex.padTo(33, '0'),
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid trace id - all zeros") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> SpanContext.TraceId.InvalidHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid trace id - not hex") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ("g" + ctx.traceIdHex.take(1)),
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid span id - too short (2 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex.take(2),
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid span id - too long (17 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex.padTo(17, '0'),
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid span id - all zeros") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> SpanContext.SpanId.InvalidHex,
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("multi - extract - invalid span id - not hex") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ("g" + ctx.spanIdHex.take(1)),
        Headers.Sampled -> (if (ctx.isSampled) "1" else "0")
      )

      val result = multi.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  //
  // Single header
  //

  test("single - fields") {
    assertEquals(single.fields.toList, List("b3"))
  }

  test("multi - toString") {
    assertEquals(
      single.toString,
      "B3Propagator{b3PropagatorInjector=B3Propagator.Injector.SingleHeader}"
    )
  }

  test("single - inject - invalid context") {
    val ctx = SpanContext(
      traceId = SpanContext.TraceId.Invalid,
      spanId = SpanContext.SpanId.Invalid,
      traceFlags = TraceFlags.Sampled,
      traceState = TraceState.empty.updated("key", "value"),
      remote = false
    )

    val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

    assertEquals(
      single.inject(context, Map.empty[String, String]),
      Map.empty[String, String]
    )
  }

  test("single - inject - valid context") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

      val sampled = if (ctx.isSampled) "1" else "0"
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + sampled
      val expected = Map(Headers.Combined -> header)

      val result = single.inject(context, Map.empty[String, String])

      assertEquals(result, expected)
    }
  }

  test("single - inject - valid context (debug enabled)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val context = Context.root
        .updated(SdkContextKeys.SpanContextKey, ctx)
        .updated(B3Propagator.ContextKeys.Debug, true)

      // the 'sampled' flag must be always set to 'd' when debug is enabled
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + "d"
      val expected = Map(Headers.Combined -> header)

      val result = single.inject(context, Map.empty[String, String])

      assertEquals(result, expected)
    }
  }

  test("single - extract - empty carrier") {
    val result = single.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(result), None)
  }

  test("single - extract - sampled header as int") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      val expected = asRemote(ctx)

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("single - extract - sampled header as boolean") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "true" else "false"
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      val expected = asRemote(ctx)

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("single - extract - sampled set to debug") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + "d"
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)

      // the extract context must always be sampled
      val expected = SpanContext(
        traceId = ctx.traceId,
        spanId = ctx.spanId,
        traceFlags = TraceFlags.Sampled,
        traceState = ctx.traceState,
        remote = true
      )

      assertEquals(getSpanContext(result), Some(expected))
      assertEquals(result.get(B3Propagator.ContextKeys.Debug), Some(true))
    }
  }

  test("single - extract - short trace id (16 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val shortTraceId = ctx.traceIdHex.take(16)
      val sampled = if (ctx.isSampled) "1" else "0"
      val header = shortTraceId + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      val expected = SpanContext(
        traceId = ByteVector.fromValidHex(shortTraceId).padLeft(16),
        spanId = ctx.spanId,
        traceFlags = ctx.traceFlags,
        traceState = ctx.traceState,
        remote = true
      )

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("single - extract - invalid trace id - too short (2 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header = ctx.traceIdHex.take(2) + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid trace id - too long (33 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        ctx.traceIdHex.padTo(33, '0') + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid trace id - all zeros") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        SpanContext.TraceId.InvalidHex + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid trace id - not hex") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        "g" + ctx.traceIdHex.take(1) + "-" + ctx.spanIdHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid span id - too short (2 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header = ctx.traceIdHex + "-" + ctx.spanIdHex.take(2) + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid span id - too long (17 characters)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        ctx.traceIdHex + "-" + ctx.spanIdHex.padTo(17, '0') + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid span id - all zeros") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        ctx.traceIdHex + "-" + SpanContext.SpanId.InvalidHex + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - invalid span id - not hex") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        ctx.traceIdHex + "-" + "g" + ctx.spanIdHex.take(1) + "-" + sampled
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("single - extract - too few parts") {
    Prop.forAll(Gens.traceId) { traceId =>
      val carrier = Map(Headers.Combined -> traceId.toBase16)
      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  /*test("single - extract - too many parts") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header =
        ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + sampled + "-extra"
      val carrier = Map(Headers.Combined -> header)

      val result = single.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }*/

  test("single - extract - single and multi headers - prioritize single") {
    Prop.forAll(Gens.spanContext, Gens.traceId, Gens.spanId) {
      (ctx, traceId, spanId) =>
        val sampled = if (ctx.isSampled) "1" else "0"
        val header = ctx.traceIdHex + "-" + ctx.spanIdHex + "-" + sampled
        val carrier = Map(
          Headers.Combined -> header,
          Headers.TraceId -> traceId.toBase16,
          Headers.SpanId -> spanId.toBase16,
          Headers.Sampled -> sampled
        )

        val result = single.extract(Context.root, carrier)
        val expected = asRemote(ctx)

        assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test(
    "single - extract - single and multi headers - use multi when single is invalid"
  ) {
    Prop.forAll(Gens.spanContext, Gens.traceId) { (ctx, traceId) =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val header = traceId.toBase16 + "-" + "abcdefghijklmnop" + "-" + sampled
      val carrier = Map(
        Headers.Combined -> header,
        Headers.TraceId -> ctx.traceIdHex,
        Headers.SpanId -> ctx.spanIdHex,
        Headers.Sampled -> sampled
      )

      val result = single.extract(Context.root, carrier)
      val expected = asRemote(ctx)

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  private def getSpanContext(ctx: Context): Option[SpanContext] =
    ctx.get(SdkContextKeys.SpanContextKey)

  private def asRemote(ctx: SpanContext): SpanContext =
    SpanContext(
      traceId = ctx.traceId,
      spanId = ctx.spanId,
      traceFlags = ctx.traceFlags,
      traceState = ctx.traceState,
      remote = true
    )

}
