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
import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens
import org.typelevel.otel4s.trace.SpanContext
import scodec.bits.ByteVector

import java.net.URLEncoder

class JaegerPropagatorSuite extends ScalaCheckSuite {

  private val propagator = JaegerPropagator.default

  //
  // Common
  //

  test("fields") {
    assertEquals(propagator.fields, List("uber-trace-id"))
  }

  test("toString") {
    assertEquals(propagator.toString, "JaegerPropagator")
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

      assertEquals(result.get("uber-trace-id"), Some(expected))
    }
  }

  test("inject - valid context with baggage") {
    Prop.forAll(Gens.spanContext, Gens.baggage) { (c, baggage) =>
      val ctx = asRemote(c)
      val context = Context.root
        .updated(SdkContextKeys.SpanContextKey, ctx)
        .updated(SdkContextKeys.BaggageKey, baggage)

      val result = propagator.inject(context, Map.empty[String, String])

      assertEquals(result.get("uber-trace-id"), Some(toTraceId(ctx)))
      baggage.asMap.foreach { case (key, entry) =>
        assertEquals(result.get(s"uberctx-$key"), Some(entry.value))
      }
    }
  }

  test("inject - only baggage") {
    Prop.forAll(Gens.baggage) { baggage =>
      val context = Context.root.updated(SdkContextKeys.BaggageKey, baggage)

      val result = propagator.inject(context, Map.empty[String, String])

      baggage.asMap.foreach { case (key, entry) =>
        assertEquals(result.get(s"uberctx-$key"), Some(entry.value))
      }
    }
  }

  test("inject - baggage - URL encode headers") {
    val baggage = Baggage.empty.updated("key", "value 1 / blah")
    val context = Context.root.updated(SdkContextKeys.BaggageKey, baggage)

    val result = propagator.inject(context, Map.empty[String, String])

    assertEquals(result, Map("uberctx-key" -> "value+1+%2F+blah"))
  }

  //
  // Extract
  //

  test("extract - empty context") {
    val ctx = propagator.extract(Context.root, Map.empty[String, String])
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - 'uber-trace-id' header is missing") {
    val ctx = propagator.extract(Context.root, Map("key" -> "value"))
    assertEquals(getSpanContext(ctx), None)
  }

  test("extract - valid 'uber-trace-id' header") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val carrier = Map("uber-trace-id" -> toTraceId(ctx))
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
    }
  }

  test("extract - 'uber-trace-id' and 'uberctx-X' headers are valid") {
    Prop.forAll(Gens.spanContext, Gens.baggage) { (ctx, baggage) =>
      val baggageHeaders = toBaggageHeaders(baggage)
      val carrier = Map("uber-trace-id" -> toTraceId(ctx)) ++ baggageHeaders
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
      assertEquals(getBaggage(result), Some(baggage))
    }
  }

  test("extract - 'uber-trace-id' header has invalid format") {
    val carrier = Map("uber-trace-id" -> "00-11-22-33")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid header - not enough parts") {
    val carrier = Map("uber-trace-id" -> "00:11:22")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid header - too many parts") {
    val carrier = Map("uber-trace-id" -> "00:11:22:33:44")
    val result = propagator.extract(Context.root, carrier)
    assertEquals(getSpanContext(result), None)
  }

  test("extract - invalid trace id (too long)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val suffix = if (ctx.isSampled) "1" else "0"
      val traceId = s"${ctx.traceIdHex}00:${ctx.spanIdHex}:0:$suffix"
      val carrier = Map("uber-trace-id" -> traceId)
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - invalid span id (too long)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val suffix = if (ctx.isSampled) "1" else "0"
      val traceId = s"${ctx.traceIdHex}:${ctx.spanIdHex}00:0:$suffix"
      val carrier = Map("uber-trace-id" -> traceId)
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - invalid flags (too long)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val traceId = s"${ctx.traceIdHex}:${ctx.spanIdHex}:0:10220"
      val carrier = Map("uber-trace-id" -> traceId)
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - invalid flags (non numeric)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val traceId = s"${ctx.traceIdHex}:${ctx.spanIdHex}:0:abc"
      val carrier = Map("uber-trace-id" -> traceId)
      val result = propagator.extract(Context.root, carrier)
      assertEquals(getSpanContext(result), None)
    }
  }

  test("extract - short trace id (pad left zeroes)") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val shortTraceId = ctx.traceIdHex.take(16)

      val suffix = if (ctx.isSampled) "1" else "0"
      val traceId = s"$shortTraceId:${ctx.spanIdHex}:0:$suffix"
      val carrier = Map("uber-trace-id" -> traceId)
      val result = propagator.extract(Context.root, carrier)

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

  test("extract - decode url encoded header") {
    Prop.forAll(Gens.spanContext) { ctx =>
      val traceId = toTraceId(ctx)
      val carrier = Map("uber-trace-id" -> URLEncoder.encode(traceId, "UTF-8"))
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getSpanContext(result), Some(asRemote(ctx)))
    }
  }

  test("extract - context and baggage") {
    Prop.forAll(Gens.baggage) { baggage =>
      val carrier = toBaggageHeaders(baggage)
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))
    }
  }

  test("extract - only baggage") {
    Prop.forAll(Gens.baggage) { baggage =>
      val carrier = toBaggageHeaders(baggage)
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))
    }
  }

  test("extract - only baggage - prefix without key") {
    val carrier = Map("uberctx-" -> "value")
    val result = propagator.extract(Context.root, carrier)

    assertEquals(getBaggage(result), None)
  }

  test("extract - only baggage - single header") {
    Prop.forAll(Gens.baggage) { baggage =>
      val header = baggage.asMap
        .map { case (key, entry) => key + "=" + entry.value }
        .mkString(",")

      val carrier = Map("jaeger-baggage" -> header)
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))
    }
  }

  test("extract - only baggage - single header with spaces") {
    Prop.forAll(Gens.baggage) { baggage =>
      val header = baggage.asMap
        .map { case (key, entry) => key + " = " + entry.value }
        .mkString("  ,  ")

      val carrier = Map("jaeger-baggage" -> header)
      val result = propagator.extract(Context.root, carrier)

      assertEquals(getBaggage(result), Some(baggage))
    }
  }

  test("extract - only baggage - invalid single header") {
    val carrier = Map("jaeger-baggage" -> "foo+bar")
    val result = propagator.extract(Context.root, carrier)

    assertEquals(getBaggage(result), None)
  }

  test("extract - only baggage - single header and multiple headers") {
    Prop.forAll(Gens.baggage) { baggage =>
      val headers = toBaggageHeaders(baggage)
      val carrier =
        Map("jaeger-baggage" -> "key=value,key2 = value2") ++ headers
      val result = propagator.extract(Context.root, carrier)
      val expected = baggage.updated("key", "value").updated("key2", "value2")

      assertEquals(getBaggage(result), Some(expected))
    }
  }

  private def toTraceId(ctx: SpanContext): String = {
    val suffix = if (ctx.isSampled) "1" else "0"
    s"${ctx.traceIdHex}:${ctx.spanIdHex}:0:$suffix"
  }

  private def toBaggageHeaders(baggage: Baggage): Map[String, String] =
    baggage.asMap.map { case (key, entry) => ("uberctx-" + key, entry.value) }

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
