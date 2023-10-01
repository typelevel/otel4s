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

import org.typelevel.otel4s.TextMapGetter
import org.typelevel.otel4s.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.propagation.TextMapPropagator
import org.typelevel.otel4s.sdk.trace.SdkTraceScope
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId
import org.typelevel.otel4s.trace.TraceFlags
import scodec.bits.ByteVector

object W3CTraceContextPropagator extends TextMapPropagator {

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

  def injected[A: TextMapUpdater](ctx: Context, carrier: A): A =
    SdkTraceScope.fromContext(ctx).filter(_.isValid) match {
      case Some(spanContext) =>
        val chars = new Array[Char](Const.TraceParentHeaderSize)
        chars(0) = Const.Version.charAt(0)
        chars(1) = Const.Version.charAt(1)
        chars(2) = Const.TraceParentDelimiter

        val traceId = spanContext.traceIdHex
        traceId.getChars(0, traceId.length, chars, Const.TraceIdOffset)

        chars(Const.SpanIdOffset - 1) = Const.TraceParentDelimiter

        val spanId = spanContext.spanIdHex
        spanId.getChars(0, spanId.length, chars, Const.SpanIdOffset)

        chars(Const.TraceOptionOffset - 1) = Const.TraceParentDelimiter
        val traceFlagsHex = spanContext.traceFlags.asHex
        chars(Const.TraceOptionOffset) = traceFlagsHex.charAt(0)
        chars(Const.TraceOptionOffset + 1) = traceFlagsHex.charAt(1)

        val traceParent = new String(chars, 0, Const.TraceParentHeaderSize)

        TextMapUpdater[A].updated(carrier, Fields.TraceParent, traceParent)

      case None =>
        carrier
    }

  private object Fields {
    val TraceParent = "traceparent"
    val TraceState = "tracestate"
  }

  private object Const {
    val Version = "00"
    val VersionSize = 2
    val TraceParentDelimiter = '-'
    val TraceParentDelimiterSize = 1
    val TraceIdHexSize = TraceId.HexLength
    val SpanIdHexSize = SpanId.HexLength
    val TraceOptionHexSize = TraceFlags.HexLength
    val TraceIdOffset = VersionSize + TraceParentDelimiterSize
    val SpanIdOffset = TraceIdOffset + TraceIdHexSize + TraceParentDelimiterSize
    val TraceOptionOffset =
      SpanIdOffset + SpanIdHexSize + TraceParentDelimiterSize
    val TraceParentHeaderSize = TraceOptionOffset + TraceOptionHexSize
    val Version00 = "00"
    val ValidVersions: Set[String] = Set.tabulate(255) { i =>
      val version = Integer.toHexString(i)
      if (version.length < 2) '0' + version else version
    }
  }

  private def extractContextFromTraceParent(
      traceParent: String
  ): Option[SpanContext] = {
    import Const._

    val isValid = (
      traceParent.length == TraceParentHeaderSize ||
        (traceParent.length > TraceParentHeaderSize && traceParent.charAt(
          TraceParentHeaderSize
        ) == TraceParentDelimiter)
    ) && traceParent.charAt(TraceIdOffset - 1) == TraceParentDelimiter &&
      traceParent.charAt(SpanIdOffset - 1) == TraceParentDelimiter &&
      traceParent.charAt(TraceOptionOffset - 1) == TraceParentDelimiter

    if (!isValid) {
      //   logger.fine("Unparseable traceparent header. Returning INVALID span context.")
      None
    } else {
      val version = traceParent.substring(0, 2)
      if (!ValidVersions.contains(version)) {
        None
      } else if (
        version == Version00 && traceParent.length > TraceParentHeaderSize
      ) {
        None
      } else {
        val traceId =
          traceParent.substring(TraceIdOffset, TraceIdOffset + TraceIdHexSize)
        val spanId =
          traceParent.substring(SpanIdOffset, SpanIdOffset + SpanIdHexSize)
        val firstTraceFlagsChar = traceParent.charAt(TraceOptionOffset)
        val secondTraceFlagsChar = traceParent.charAt(TraceOptionOffset + 1)

        /*if (!OtelEncodingUtils.isValidBase16Character(firstTraceFlagsChar) ||
          !OtelEncodingUtils.isValidBase16Character(secondTraceFlagsChar)) {
          SpanContext.getInvalid
        } else {
          val traceFlags = TraceFlags.fromByte(
            OtelEncodingUtils.byteFromBase16(firstTraceFlagsChar, secondTraceFlagsChar)
          )
          SpanContext.createFromRemoteParent(traceId, spanId, traceFlags, TraceState.getDefault)
        }*/

        Some(
          SpanContext.create(
            traceId_ = ByteVector.fromValidHex(traceId),
            spanId_ = ByteVector.fromValidHex(spanId),
            traceFlags_ = TraceFlags.Default,
            remote = true
          )
        )
      }
    }

  }

}
