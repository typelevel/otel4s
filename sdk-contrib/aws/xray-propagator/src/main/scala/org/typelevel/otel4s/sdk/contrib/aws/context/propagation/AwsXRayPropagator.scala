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

import cats.implicits.catsSyntaxTuple3Semigroupal
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.trace.SdkContextKeys
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

/** An example of the AWS X-Ray Tracing Header:
  * {{{
  *   X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
  * }}}
  *
  * @see
  *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader]]
  */
private final class AwsXRayPropagator extends TextMapPropagator.Unsealed[Context] {
  import AwsXRayPropagator.Const
  import AwsXRayPropagator.Headers

  val fields: List[String] = List(Headers.TraceId)

  def extract[A: TextMapGetter](ctx: Context, carrier: A): Context =
    TextMapGetter[A].get(carrier, Headers.TraceId).flatMap(decode) match {
      case Some(spanContext) =>
        ctx.updated(SdkContextKeys.SpanContextKey, spanContext)

      case None =>
        ctx
    }

  def inject[A: TextMapUpdater](ctx: Context, carrier: A): A =
    ctx.get(SdkContextKeys.SpanContextKey).filter(_.isValid) match {
      case Some(spanContext) =>
        TextMapUpdater[A].updated(carrier, Headers.TraceId, encode(spanContext))

      case None =>
        carrier
    }

  private def encode(spanContext: SpanContext): String = {
    val traceId = {
      val (epoch, unique) = spanContext.traceIdHex.splitAt(8)

      val value =
        Const.Version + Const.TraceIdDelimiter + epoch + Const.TraceIdDelimiter + unique

      Const.TraceIdKey + Const.EntryDelimiter + value
    }

    val parentId =
      Const.ParentIdKey + Const.EntryDelimiter + spanContext.spanIdHex

    val sampled = {
      val flag =
        if (spanContext.isSampled) Const.Sampled else Const.NotSampled

      Const.SampledKey + Const.EntryDelimiter + flag
    }

    traceId + Const.HeaderDelimiter + parentId + Const.HeaderDelimiter + sampled
  }

  private def decode(header: String): Option[SpanContext] = {

    final case class State(
        traceId: Option[ByteVector],
        spanId: Option[ByteVector],
        flags: Option[TraceFlags]
    )

    def decodePart(part: String, state: State): State = {
      val parts = part.split(Const.EntryDelimiter, 2)

      if (parts.length == 2) {
        val key = parts(0)
        val value = parts(1)

        key match {
          case Const.TraceIdKey  => state.copy(traceId = parseTraceId(value))
          case Const.ParentIdKey => state.copy(spanId = parseSpanId(value))
          case Const.SampledKey  => state.copy(flags = parseTraceFlags(value))
          case _                 => state
        }
      } else {
        state
      }
    }

    val state = header
      .split(Const.HeaderDelimiter)
      .foldLeft(State(None, None, None)) { (state, part) =>
        decodePart(part, state)
      }

    (state.traceId, state.spanId, state.flags)
      .mapN { (traceId, spanId, flags) =>
        SpanContext(traceId, spanId, flags, TraceState.empty, remote = true)
      }
  }

  // Value example: 1-5759e988-bd862e3fe1be46a994272793
  private def parseTraceId(value: String): Option[ByteVector] = {
    val parts = value.split(Const.TraceIdDelimiter)
    if (parts.length == 3) {
      val version = parts(0)
      val epoch = parts(1)
      val unique = parts(2)

      if (version == Const.Version) {
        SpanContext.TraceId.fromHex(epoch + unique)
      } else {
        None
      }
    } else {
      None
    }
  }

  private def parseSpanId(value: String): Option[ByteVector] =
    SpanContext.SpanId.fromHex(value)

  private def parseTraceFlags(value: String): Option[TraceFlags] =
    value match {
      case Const.Sampled    => Some(TraceFlags.Sampled)
      case Const.NotSampled => Some(TraceFlags.Default)
      case _                => None
    }

  override def toString: String = "AwsXRayPropagator"

}

object AwsXRayPropagator {
  private val Propagator = new AwsXRayPropagator

  private[propagation] object Headers {
    val TraceId = "X-Amzn-Trace-Id"
  }

  private object Const {
    val name = "xray"

    val HeaderDelimiter = ";"
    val EntryDelimiter = "="
    val TraceIdDelimiter = "-"

    val TraceIdKey = "Root"
    val ParentIdKey = "Parent"
    val SampledKey = "Sampled"

    val Version = "1"
    val Sampled = "1"
    val NotSampled = "0"
  }

  /** Returns an instance of the AwsXRayPropagator.
    *
    * The propagator utilizes `X-Amzn-Trace-Id` header to extract and inject tracing details.
    *
    * An example of the AWS X-Ray Tracing Header:
    * {{{
    *   X-Amzn-Trace-Id: Root=1-5759e988-bd862e3fe1be46a994272793;Parent=53995c3f42cd8ad8;Sampled=1
    * }}}
    *
    * @example
    *   {{{
    * OpenTelemetrySdk.autoConfigured[IO](
    *   _.addTracerProviderCustomizer((b, _) => b.addTextMapPropagators(AwsXRayPropagator())
    * )
    *   }}}
    *
    * @see
    *   [[https://docs.aws.amazon.com/xray/latest/devguide/xray-concepts.html#xray-concepts-tracingheader]]
    */
  def apply(): TextMapPropagator[Context] = Propagator

  /** Returns the named configurer `xray`. You can use it to dynamically enable AWS X-Ray propagator via environment
    * variable or system properties.
    *
    * @example
    *   {{{
    * OpenTelemetrySdk.autoConfigured[IO](
    *   _.addTextMapPropagatorConfigurer(AwsXRayPropagator.configurer[IO])
    * )
    *   }}}
    *
    * Enable propagator via environment variable:
    * {{{
    *  OTEL_PROPAGATORS=xray
    * }}}
    * or system property:
    * {{{
    *    -Dotel.propagators=xray
    * }}}
    */
  def configurer[F[_]]: AutoConfigure.Named[F, TextMapPropagator[Context]] =
    AutoConfigure.Named.const(Const.name, Propagator)

}
