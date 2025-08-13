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

import org.typelevel.otel4s.baggage.Baggage
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

private final class OtTracePropagator extends TextMapPropagator.Unsealed[Context] {

  import OtTracePropagator.Headers
  import OtTracePropagator.Const

  override val fields: List[String] = List(
    Headers.TraceId,
    Headers.SpanId,
    Headers.Sampled
  )

  override def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = {
    val spanContext = for {
      traceIdHex <- TextMapGetter[A].get(carrier, Headers.TraceId)
      traceId <- ByteVector.fromHex(traceIdHex) if isValidTraceId(traceId)

      spanHexId <- TextMapGetter[A].get(carrier, Headers.SpanId)
      spanId <- ByteVector.fromHex(spanHexId) if isValidSpanId(spanId)

      sampled <- TextMapGetter[A]
        .get(carrier, Headers.Sampled)
        .flatMap(_.toBooleanOption)

    } yield SpanContext(
      traceId = padLeft(traceId, SpanContext.TraceId.Bytes.toLong),
      spanId = spanId,
      traceFlags = if (sampled) TraceFlags.Sampled else TraceFlags.Default,
      traceState = TraceState.empty,
      remote = true
    )
    // Baggage is only extracted if there is a valid SpanContext
    spanContext match {
      case Some(spanContext) =>
        ctx
          .updated(SdkContextKeys.SpanContextKey, spanContext)
          .updated(SdkContextKeys.BaggageKey, decodeBaggage(carrier))
      case None => ctx
    }
  }

  override def inject[A: TextMapUpdater](ctx: Context, carrier: A): A = {

    ctx.get(SdkContextKeys.SpanContextKey).filter(_.isValid) match {
      case Some(spanContext) =>
        val sampled =
          if (spanContext.isSampled) Const.IsSampled else Const.NotSampled

        val headers = Map(
          Headers.TraceId -> spanContext.traceIdHex.substring(TraceId.Bytes),
          Headers.SpanId -> spanContext.spanIdHex,
          Headers.Sampled -> sampled
        ) ++ encodeBaggage(
          ctx
        ) // Baggage is only injected if there is a valid SpanContext

        headers.foldLeft(carrier) { case (carrier, (key, value)) =>
          TextMapUpdater[A].updated(carrier, key, value)
        }

      case None => carrier
    }

  }

  override def toString: String = "OtTracePropagator"

  private def encodeBaggage(ctx: Context) =
    ctx
      .get(SdkContextKeys.BaggageKey)
      .map(_.asMap.map { case (key, entry) =>
        Const.BaggagePrefix + key -> entry.value
      })
      .getOrElse(Map.empty)

  private def decodeBaggage[A: TextMapGetter](carrier: A) = {
    val keys = TextMapGetter[A].keys(carrier)
    val prefixLength = Const.BaggagePrefix.length

    keys.foldLeft(Baggage.empty) {
      case (baggage, key) if key.toLowerCase.startsWith(Const.BaggagePrefix) =>
        TextMapGetter[A]
          .get(carrier, key)
          .fold(baggage)(v => baggage.updated(key.drop(prefixLength), v))

      case (baggage, _) => baggage
    }
  }

  private def isValidTraceId(value: ByteVector): Boolean =
    TraceId.isValid(value.padLeft(TraceId.Bytes.toLong))

  private def isValidSpanId(value: ByteVector): Boolean =
    SpanId.isValid(value)

  private def padLeft(input: ByteVector, expectedSize: Long): ByteVector =
    if (input.size <= expectedSize) input.padLeft(expectedSize) else input
}

object OtTracePropagator {

  private val Default = new OtTracePropagator

  private object Headers {
    val TraceId = "ot-tracer-traceid"
    val SpanId = "ot-tracer-spanid"
    val Sampled = "ot-tracer-sampled"

  }

  private object Const {
    val IsSampled = "true"
    val NotSampled = "false"
    val BaggagePrefix = "ot-baggage-"
  }

  /** Returns an instance of the OtTracePropagator.
    */
  def default: TextMapPropagator[Context] = Default
}
