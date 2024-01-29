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

import munit._
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceState

class W3CTraceContextPropagatorSuite extends ScalaCheckSuite {

  private val propagator = W3CTraceContextPropagator.default

  //
  // Common
  //

  test("fields") {
    assertEquals(propagator.fields, List("traceparent", "tracestate"))
  }

  test("toString") {
    assertEquals(propagator.toString, "W3CTraceContextPropagator")
  }

  //
  // Inject
  //

  test("inject nothing when context is empty") {
    val result = propagator.inject(Context.root, Map.empty[String, String])
    assertEquals(result.size, 0)
  }

  test("inject - invalid context") {
    val ctx = SpanContext.invalid
    val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

    assertEquals(
      propagator.inject(context, Map.empty[String, String]),
      Map.empty[String, String]
    )
  }

  test("inject - valid context") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

      val suffix = if (ctx.isSampled) "01" else "00"
      val expected = s"00-${ctx.traceIdHex}-${ctx.spanIdHex}-$suffix"

      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("traceparent"), Some(expected))
    }
  }

  test("inject - valid context with trace state") {
    Prop.forAll(Gens.spanContext, Gens.traceState) { (c, state) =>
      val ctx = asRemote(c, Some(state))
      val context = Context.root.updated(SdkContextKeys.SpanContextKey, ctx)

      val suffix = if (ctx.isSampled) "01" else "00"
      val expectedParent = s"00-${ctx.traceIdHex}-${ctx.spanIdHex}-$suffix"
      val expectedState = toTraceState(state)

      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("traceparent"), Some(expectedParent))
      assertEquals(
        result.get("tracestate"),
        Some(expectedState).filter(_.nonEmpty)
      )
    }
  }
  //
  // Extract
  //

  test("extract - empty context") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - 'traceparent' header is missing") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - valid 'traceparent' header") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map("traceparent" -> toTraceParent(ctx))
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
    }
  }

  test("extract - 'traceparent' and 'tracestate' headers are valid") {
    Prop.forAll(Gens.spanContext, Gens.traceState) { (ctx, state) =>
      val carrier = Map(
        "traceparent" -> toTraceParent(ctx),
        "tracestate" -> toTraceState(state)
      )

      val result = propagator.extract(Context.root, carrier)
      val expected = asRemote(ctx, Some(state))

      assertEquals(getSpanContext(result), Some(expected))
    }
  }

  test("extract - 'traceparent' header is invalid") {
    val carrier = Map("traceparent" -> "00-11-22-33")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  private def toTraceParent(spanContext: SpanContext): String =
    s"00-${spanContext.traceIdHex}-${spanContext.spanIdHex}-${spanContext.traceFlags.toHex}"

  private def toTraceState(state: TraceState): String =
    state.asMap.map { case (key, value) => key + "=" + value }.mkString(",")

  private def getSpanContext(ctx: Context): Option[SpanContext] =
    ctx.get(SdkContextKeys.SpanContextKey)

  private def asRemote(
      ctx: SpanContext,
      traceState: Option[TraceState] = None
  ): SpanContext =
    SpanContext(
      traceId = ctx.traceId,
      spanId = ctx.spanId,
      traceFlags = ctx.traceFlags,
      traceState = traceState.getOrElse(ctx.traceState),
      remote = true
    )

}
