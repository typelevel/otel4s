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

package org.typelevel.otel4s.sdk.contrib.aws.context.propagation

import munit.ScalaCheckSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import org.typelevel.otel4s.trace.SpanContext

class AWSXRayPropagatorSuite extends ScalaCheckSuite {

  private val propagator = AWSXRayPropagator()

  //
  // Common
  //

  test("fields") {
    assertEquals(propagator.fields, List("X-Amzn-Trace-Id"))
  }

  test("toString") {
    assertEquals(propagator.toString, "AWSXRayPropagator")
  }

  //
  // Inject
  //

  test("inject nothing when context is empty") {
    val result = propagator.inject(Context.root, Map.empty[String, String])
    assertEquals(result.size, 0)
  }

  test("inject - invalid context - do nothing") {
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
      val expected = toTraceId(ctx)
      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("X-Amzn-Trace-Id"), Some(expected))
    }
  }

  //
  // Extract
  //

  test("extract - empty context") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - 'X-Amzn-Trace-Id' header is missing") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - valid 'X-Amzn-Trace-Id' header") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map("X-Amzn-Trace-Id" -> toTraceId(ctx))
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
    }
  }

  test("extract - 'X-Amzn-Trace-Id' header has invalid format") {
    val carrier = Map("X-Amzn-Trace-Id" -> "Root=00-11-22-33")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid header - missing values") {
    val carrier = Map("X-Amzn-Trace-Id" -> "Root=;Parent=;Sampled=")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid Root format - missing version") {
    val carrier = Map(
      "X-Amzn-Trace-Id" -> "Root=5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1"
    )
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid Root format - no separator") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val carrier = Map(
        "X-Amzn-Trace-Id" -> s"Root=1-${ctx.traceIdHex};Parent=${ctx.spanIdHex};Sampled=$sampled"
      )
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - invalid span id (too long)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val sampled = if (ctx.isSampled) "1" else "0"
      val traceId = s"${ctx.traceIdHex}:${ctx.spanIdHex}00:0:$sampled"
      val carrier = Map("X-Amzn-Trace-Id" -> traceId)
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - invalid flags (too long)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        "X-Amzn-Trace-Id" -> s"Root=1-${ctx.traceIdHex.take(8)}-${ctx.traceIdHex.drop(8)};Parent=${ctx.spanIdHex};Sampled=123"
      )
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  private def toTraceId(ctx: SpanContext): String = {
    val sampled = if (ctx.isSampled) "1" else "0"
    val traceId = ctx.traceIdHex.take(8) + "-" + ctx.traceIdHex.drop(8)
    s"Root=1-$traceId;Parent=${ctx.spanIdHex};Sampled=$sampled"
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
