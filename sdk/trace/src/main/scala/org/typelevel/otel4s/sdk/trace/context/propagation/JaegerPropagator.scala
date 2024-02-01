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
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import scala.util.control.NonFatal

/** A Jaeger propagator.
  *
  * The trace id header format: {trace-id}:{span-id}:{parent-span-id}:{flags}.
  *
  * @see
  *   [[https://www.jaegertracing.io/docs/client-libraries/#propagation-format]]
  */
private final class JaegerPropagator extends TextMapPropagator[Context] {
  import JaegerPropagator.Const
  import JaegerPropagator.Headers

  val fields: List[String] = List(Headers.TraceId)

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = {
    val withSpanCtx =
      TextMapGetter[A].get(carrier, Headers.TraceId) match {
        case Some(traceIdHeader) =>
          prepareHeader(traceIdHeader).flatMap(decode) match {
            case Some(spanContext) if spanContext.isValid =>
              ctx.updated(SdkContextKeys.SpanContextKey, spanContext)

            case _ =>
              ctx
          }

        case None =>
          ctx
      }

    val baggage = decodeBaggage(carrier)
    if (baggage.isEmpty) withSpanCtx
    else withSpanCtx.updated(SdkContextKeys.BaggageKey, baggage)
  }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A = {
    val withSpanContext =
      ctx.get(SdkContextKeys.SpanContextKey).filter(_.isValid) match {
        case Some(spanContext) =>
          val sampled =
            if (spanContext.isSampled) Const.Sampled else Const.NotSampled

          val header =
            spanContext.traceIdHex + Const.Delimiter +
              spanContext.spanIdHex + Const.Delimiter +
              Const.DeprecatedParent + Const.Delimiter +
              sampled

          TextMapUpdater[A].updated(carrier, Headers.TraceId, header)

        case None =>
          carrier
      }

    ctx.get(SdkContextKeys.BaggageKey) match {
      case Some(baggage) =>
        baggage.asMap.foldLeft(withSpanContext) {
          case (carrier, (key, entry)) =>
            val k = Const.BaggagePrefix + key
            TextMapUpdater[A].updated(carrier, k, urlEncode(entry.value))
        }

      case None =>
        withSpanContext
    }
  }

  private def decode(traceIdHeader: String): Option[SpanContext] = {
    val parts = traceIdHeader.split(Const.Delimiter)

    if (parts.size == 4) {
      val traceIdHex = parts(0)
      val spanIdHex = parts(1)
      val traceFlagsHex = parts(3)

      for {
        traceId <- ByteVector.fromHex(traceIdHex)
        spanId <- ByteVector.fromHex(spanIdHex)
        flag <- traceFlagsHex.toByteOption
      } yield SpanContext(
        traceId = padLeft(traceId, SpanContext.TraceId.Bytes.toLong),
        spanId = padLeft(spanId, SpanContext.SpanId.Bytes.toLong),
        traceFlags = TraceFlags.fromByte(flag),
        traceState = TraceState.empty,
        remote = true
      )
    } else {
      None
    }
  }

  private def padLeft(input: ByteVector, expectedSize: Long): ByteVector =
    if (input.size <= expectedSize) input.padLeft(expectedSize) else input

  // a header can be URL encoded, so we try to decode it
  private def prepareHeader(traceIdHeader: String): Option[String] =
    if (traceIdHeader.contains(Const.Delimiter)) {
      Some(traceIdHeader)
    } else {
      try {
        Some(URLDecoder.decode(traceIdHeader, Const.Charset))
      } catch {
        case NonFatal(_) =>
          None
      }
    }

  private def decodeBaggage[A: TextMapGetter](carrier: A): Baggage = {
    val keys = TextMapGetter[A].keys(carrier)
    val prefixLength = Const.BaggagePrefix.length

    def isPrefix(key: String): Boolean =
      key.startsWith(Const.BaggagePrefix) && key.length != prefixLength

    keys.foldLeft(Baggage.empty) {
      case (baggage, key) if isPrefix(key) =>
        TextMapGetter[A]
          .get(carrier, key)
          .map(safeUrlDecode)
          .fold(baggage)(v => baggage.updated(key.drop(prefixLength), v))

      case (baggage, key) if key == Headers.Baggage =>
        TextMapGetter[A]
          .get(carrier, key)
          .fold(baggage)(v => parseBaggageHeader(v, baggage))

      case (baggage, _) =>
        baggage
    }
  }

  private def parseBaggageHeader(header: String, to: Baggage): Baggage =
    header
      .split(Const.BaggageHeaderDelimiter)
      .foldLeft(to) { (baggage, part) =>
        val parts = part.split(Const.BaggageHeaderEntryDelimiter)
        if (parts.size == 2) baggage.updated(parts(0), safeUrlDecode(parts(1)))
        else baggage
      }

  private def urlEncode(input: String): String =
    URLEncoder.encode(input, Const.Charset)

  private def safeUrlDecode(input: String): String =
    try {
      URLDecoder.decode(input, Const.Charset)
    } catch {
      case e if NonFatal(e) => input
    }

  override def toString: String = "JaegerPropagator"
}

object JaegerPropagator {

  private val Default = new JaegerPropagator

  private object Headers {
    val TraceId = "uber-trace-id"
    val Baggage = "jaeger-baggage"
  }

  private object Const {
    val Delimiter = ":"
    val DeprecatedParent = "0"
    val Sampled = "1"
    val NotSampled = "0"
    val BaggagePrefix = "uberctx-"
    val Charset = StandardCharsets.UTF_8.name()
    val BaggageHeaderDelimiter = "\\s*,\\s*"
    val BaggageHeaderEntryDelimiter = "\\s*=\\s*"
  }

  /** Returns an instance of the W3CTraceContextPropagator.
    */
  def default: TextMapPropagator[Context] = Default

}
