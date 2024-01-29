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

import cats.syntax.apply._
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

/** A W3C trace context propagator.
  *
  * @see
  *   [[https://www.w3.org/TR/trace-context/]]
  */
private final class W3CTraceContextPropagator
    extends TextMapPropagator[Context] {

  import W3CTraceContextPropagator.Headers
  import W3CTraceContextPropagator.Const

  val fields: List[String] = List(Headers.TraceParent, Headers.TraceState)

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context =
    TextMapGetter[A].get(carrier, Headers.TraceParent) match {
      case Some(traceParent) =>
        val traceState = TextMapGetter[A].get(carrier, Headers.TraceState)

        decode(traceParent, traceState) match {
          case Some(parentContext) =>
            ctx.updated(SdkContextKeys.SpanContextKey, parentContext)

          case None =>
            ctx
        }

      case None =>
        ctx
    }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A =
    ctx.get(SdkContextKeys.SpanContextKey).filter(_.isValid) match {
      case Some(spanContext) =>
        val traceParent = Const.Version +
          Const.Delimiter + spanContext.traceIdHex +
          Const.Delimiter + spanContext.spanIdHex +
          Const.Delimiter + spanContext.traceFlags.toHex

        val withParent =
          TextMapUpdater[A].updated(carrier, Headers.TraceParent, traceParent)

        encodeTraceState(spanContext.traceState)
          .foldLeft(withParent) { (carrier, state) =>
            TextMapUpdater[A].updated(carrier, Headers.TraceState, state)
          }

      case None =>
        carrier
    }

  /** @see
    *   [[https://www.w3.org/TR/trace-context/#traceparent-header]]
    */
  private def decode(
      traceParent: String,
      traceStateHeader: Option[String]
  ): Option[SpanContext] = {
    val parts = traceParent.split(Const.Delimiter)

    if (parts.size >= 4) {
      val traceIdHex = parts(1)
      val spanIdHex = parts(2)
      val traceFlagsHex = parts(3)

      (
        TraceId.fromHex(traceIdHex),
        SpanId.fromHex(spanIdHex),
        TraceFlags.fromHex(traceFlagsHex)
      ).mapN { (traceId, spanId, traceFlags) =>
        val state = traceStateHeader
          .flatMap(header => decodeTraceState(header))
          .getOrElse(TraceState.empty)
        SpanContext(traceId, spanId, traceFlags, state, remote = true)
      }
    } else {
      None
    }
  }

  /** @see
    *   [[https://www.w3.org/TR/trace-context/#tracestate-header]]
    */
  private def decodeTraceState(traceStateHeader: String): Option[TraceState] = {
    val members = Const.TraceStateEntryPattern.split(traceStateHeader)

    val result = members
      .take(Const.TraceStateMaxMembers)
      .foldLeft(TraceState.empty) { (acc, entry) =>
        val parts = entry.split(Const.TraceStateKeyValueDelimiter)
        if (parts.length == 2) acc.updated(parts(0), parts(1)) else acc
      }

    Option.when(result.size == members.length)(result)
  }

  private def encodeTraceState(traceState: TraceState): Option[String] =
    Option.when(!traceState.isEmpty) {
      traceState.asMap
        .map { case (key, value) =>
          key + Const.TraceStateKeyValueDelimiter + value
        }
        .mkString(Const.TraceStateEntryDelimiter)
    }

  override def toString: String =
    "W3CTraceContextPropagator"
}

object W3CTraceContextPropagator {

  private val Default = new W3CTraceContextPropagator

  private object Headers {
    val TraceParent = "traceparent"
    val TraceState = "tracestate"
  }

  private object Const {
    val Delimiter = "-"
    val Version = "00"
    val TraceStateEntryPattern = "[ \t]*,[ \t]*".r
    val TraceStateEntryDelimiter = ","
    val TraceStateKeyValueDelimiter = "="
    val TraceStateMaxMembers = 32
  }

  /** Returns an instance of the W3CTraceContextPropagator.
    */
  def default: TextMapPropagator[Context] = Default

}
