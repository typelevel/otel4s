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

import munit.ScalaCheckSuite
import org.scalacheck.Prop
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import org.typelevel.otel4s.trace.SpanContext

class OtTracePropagatorSuite extends ScalaCheckSuite {

  private val propagator = OtTracePropagator.default

  test("fields") {
    assertEquals(
      propagator.fields.toList,
      List("ot-tracer-traceid", "ot-tracer-spanid", "ot-tracer-sampled")
    )
  }

  test("toString") {
    assertEquals(propagator.toString, "OtTracePropagator")
  }

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

      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("ot-tracer-traceid"), Some(ctx.traceIdHex))
      assertEquals(result.get("ot-tracer-spanid"), Some(ctx.spanIdHex))
      assertEquals(
        result.get("ot-tracer-sampled"),
        Some(String.valueOf(ctx.isSampled))
      )

    }
  }

  test("inject - valid context with baggage") {
    Prop.forAll(Gens.spanContext, Gens.baggage) { (ctx, baggage) =>
      val context = Context.root
        .updated(SdkContextKeys.SpanContextKey, ctx)
        .updated(SdkContextKeys.BaggageKey, baggage)

      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("ot-tracer-traceid"), Some(ctx.traceIdHex))
      assertEquals(result.get("ot-tracer-spanid"), Some(ctx.spanIdHex))
      assertEquals(
        result.get("ot-tracer-sampled"),
        Some(String.valueOf(ctx.isSampled))
      )
      baggage.asMap.foreach { case (key, entry) =>
        assertEquals(result.get(s"ot-baggage-$key"), Some(entry.value))
      }

    }
  }

  test("inject - baggage only injected if there is a valid SpanContext") {
    Prop.forAll(Gens.baggage) { baggage =>
      val context = Context.root.updated(SdkContextKeys.BaggageKey, baggage)

      val result = propagator.inject(context, Map.empty[String, String])

      assert(result.isEmpty)

    }
  }

  test("extract - empty context") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - all headers is missing") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - 'ot-tracer-traceid' header is missing") {
    Prop.forAll(Gens.spanContext) { spanCtx =>
      val carrier = Map(
        "ot-tracer-spanid" -> spanCtx.spanIdHex,
        "ot-tracer-sampled" -> String.valueOf(spanCtx.isSampled)
      )

      val ctx = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(ctx), None)
    }
  }
  test("extract - 'ot-tracer-spanid' header is missing") {
    Prop.forAll(Gens.spanContext) { spanCtx =>
      val carrier = Map(
        "ot-tracer-traceid" -> spanCtx.traceIdHex,
        "ot-tracer-sampled" -> String.valueOf(spanCtx.isSampled)
      )

      val ctx = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(ctx), None)
    }
  }

  test("extract - 'ot-tracer-sampled' header is missing") {
    Prop.forAll(Gens.spanContext) { spanCtx =>
      val carrier = Map(
        "ot-tracer-spanid" -> spanCtx.spanIdHex,
        "ot-tracer-traceid" -> spanCtx.traceIdHex
      )

      val ctx = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(ctx), None)
    }
  }

  test("extract - valid headers") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map(
        "ot-tracer-traceid" -> ctx.traceIdHex,
        "ot-tracer-spanid" -> ctx.spanIdHex,
        "ot-tracer-sampled" -> String.valueOf(ctx.isSampled)
      )
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
    }
  }

  test("extract - baggage capitalized headers") {
    Prop.forAll(Gens.spanContext, Gens.baggage) { case (ctx, baggage) =>
      val carrier = Map(
        "ot-tracer-traceid" -> ctx.traceIdHex,
        "ot-tracer-spanid" -> ctx.spanIdHex,
        "ot-tracer-sampled" -> String.valueOf(ctx.isSampled)
      ) ++ toBaggageHeaders("OT-baggage-", baggage)

      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))

    }
  }

  test("extract - baggage full capitalized headers") {
    Prop.forAll(Gens.spanContext, Gens.baggage) { case (ctx, baggage) =>
      val carrier = Map(
        "ot-tracer-traceid" -> ctx.traceIdHex,
        "ot-tracer-spanid" -> ctx.spanIdHex,
        "ot-tracer-sampled" -> String.valueOf(ctx.isSampled)
      ) ++ toBaggageHeaders("ot-baggage-".capitalize, baggage)

      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))

    }
  }

  test("extract - baggage is only extracted if there is a valid SpanContext") {
    Prop.forAll(Gens.baggage) { case (baggage) =>
      val carrier = toBaggageHeaders("ot-baggage-".capitalize, baggage)

      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), None)

    }
  }

  private def toBaggageHeaders(
      prefix: String,
      baggage: Baggage
  ): Map[String, String] =
    baggage.asMap.map { case (key, entry) =>
      (prefix + key, entry.value)
    }

  private def getSpanContext(ctx: Context): Option[SpanContext] =
    ctx.get(SdkContextKeys.SpanContextKey)

  private def getBaggage(ctx: Context): Option[Baggage] =
    ctx.get(SdkContextKeys.BaggageKey)

  private def asRemote(ctx: SpanContext): SpanContext =
    SpanContext(
      traceId = ctx.traceId,
      spanId = ctx.spanId,
      traceFlags = ctx.traceFlags,
      traceState = ctx.traceState,
      remote = true
    )

}
