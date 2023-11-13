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

import cats.syntax.apply._
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkTraceScope
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState

import scala.util.matching.Regex

object W3CTraceContextPropagator extends TextMapPropagator[Context] {

  private object Fields {
    val TraceParent = "traceparent"
    val TraceState = "tracestate"
  }

  private val Pattern: Regex =
    "([0-9]{2})-([0-9a-f]{32})-([0-9a-f]{16})-([0-9a-f]{2})".r

  val fields: List[String] = List(Fields.TraceParent, Fields.TraceState)

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context = {
    TextMapGetter[A].get(carrier, Fields.TraceParent) match {
      case Some(value) =>
        extractContextFromTraceParent(value) match {
          case Some(parentContext) =>
            SdkTraceScope.storeInContext(ctx, parentContext)

          case None =>
            ctx
        }

      case None =>
        ctx
    }
  }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A =
    SdkTraceScope.fromContext(ctx).filter(_.isValid) match {
      case Some(spanContext) =>
        val traceParent =
          s"00-${spanContext.traceIdHex}-${spanContext.spanIdHex}-${spanContext.traceFlags.toHex}"

        TextMapUpdater[A].updated(carrier, Fields.TraceParent, traceParent)

      case None =>
        carrier
    }

  private def extractContextFromTraceParent(
      traceParent: String
  ): Option[SpanContext] =
    traceParent match {
      case Pattern(_, traceIdHex, spanIdHex, traceFlagsHex) =>
        (
          TraceId.fromHex(traceIdHex),
          SpanId.fromHex(spanIdHex),
          TraceFlags.fromHex(traceFlagsHex)
        ).mapN { (traceId, spanId, traceFlags) =>
          val state = TraceState.empty
          SpanContext(traceId, spanId, traceFlags, state, remote = true)
        }

      case _ =>
        None
    }

}
