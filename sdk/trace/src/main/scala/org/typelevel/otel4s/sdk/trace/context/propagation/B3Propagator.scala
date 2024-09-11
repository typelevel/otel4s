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

package org.typelevel.otel4s.sdk.trace
package context
package propagation

import cats.effect.SyncIO
import org.typelevel.otel4s.context.propagation.TextMapGetter
import org.typelevel.otel4s.context.propagation.TextMapPropagator
import org.typelevel.otel4s.context.propagation.TextMapUpdater
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanContext.SpanId
import org.typelevel.otel4s.trace.SpanContext.TraceId
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

/** A B3 propagator.
  *
  * The propagator extracts span details from the `single` and `multiple` headers.
  *
  * The injection format depends on the provided `injector`.
  *
  * @see
  *   [[https://github.com/openzipkin/b3-propagation]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/context/api-propagators/#b3-requirements]]
  *
  * @param injector
  *   the injector to use
  */
private final class B3Propagator private (
    injector: B3Propagator.Injector
) extends TextMapPropagator[Context] {
  import B3Propagator.Extractor

  val fields: Iterable[String] = injector.fields

  def extract[A](
      ctx: Context,
      carrier: A
  )(implicit getter: TextMapGetter[A]): Context =
    Extractor.Single
      .extract(ctx, carrier)
      .orElse(Extractor.Multiple.extract(ctx, carrier))
      .getOrElse(ctx)

  def inject[A](ctx: Context, carrier: A)(implicit u: TextMapUpdater[A]): A =
    injector.inject(ctx, carrier)

  override def toString: String =
    s"B3Propagator{b3PropagatorInjector=$injector}"
}

object B3Propagator {
  private val SingleHeader = new B3Propagator(Injector.Single)
  private val MultipleHeaders = new B3Propagator(Injector.Multiple)

  object ContextKeys {
    val Debug: Context.Key[Boolean] =
      Context.Key
        .unique[SyncIO, Boolean]("otel4s-b3-debug")
        .unsafeRunSync()
  }

  /** Returns a [[B3Propagator]] that injects span details into a single `b3` header.
    */
  def singleHeader: TextMapPropagator[Context] =
    SingleHeader

  /** Returns a [[B3Propagator]] that injects span details into multiple headers: `X-B3-TraceId`, `X-B3-SpanId`,
    * `X-B3-Sampled`.
    */
  def multipleHeaders: TextMapPropagator[Context] =
    MultipleHeaders

  private object Headers {
    // used by the multiple-headers propagator
    val TraceId = "X-B3-TraceId"
    val SpanId = "X-B3-SpanId"
    val Sampled = "X-B3-Sampled"
    val Debug = "X-B3-Flags"

    // used by the single-header propagator
    val Combined = "b3"
  }

  private object Const {
    val IsSampled = "1"
    val NotSampled = "0"
    val DebugSampled = "d"
    val Delimiter = "-"
  }

  private object Constraints {
    val MinTraceIdLength = 8
    val MaxTraceIdLength = 16
  }

  private trait Injector {
    def fields: List[String]
    def inject[A](ctx: Context, carrier: A)(implicit u: TextMapUpdater[A]): A
  }

  private object Injector {
    object Single extends Injector {
      val fields: List[String] = List(Headers.Combined)

      def inject[A](
          context: Context,
          carrier: A
      )(implicit updater: TextMapUpdater[A]): A = {
        context.get(SdkContextKeys.SpanContextKey) match {
          case Some(spanContext) if spanContext.isValid =>
            val sampled = context.get(ContextKeys.Debug) match {
              case Some(true) =>
                Const.DebugSampled

              case _ =>
                if (spanContext.isSampled) Const.IsSampled else Const.NotSampled
            }

            val header =
              spanContext.traceIdHex + Const.Delimiter +
                spanContext.spanIdHex + Const.Delimiter +
                sampled

            updater.updated(carrier, Headers.Combined, header)

          case _ =>
            carrier
        }
      }

      override def toString: String =
        "B3Propagator.Injector.SingleHeader"
    }

    object Multiple extends Injector {
      val fields: List[String] =
        List(Headers.TraceId, Headers.SpanId, Headers.Sampled)

      def inject[A](
          context: Context,
          carrier: A
      )(implicit updater: TextMapUpdater[A]): A =
        context.get(SdkContextKeys.SpanContextKey) match {
          case Some(spanContext) if spanContext.isValid =>
            val (c, sampled) = context.get(ContextKeys.Debug) match {
              case Some(true) =>
                val next =
                  updater.updated(carrier, Headers.Debug, Const.IsSampled)
                (next, Const.IsSampled)

              case _ =>
                val sampled =
                  if (spanContext.isSampled) Const.IsSampled
                  else Const.NotSampled
                (carrier, sampled)
            }

            val headers = List(
              Headers.TraceId -> spanContext.traceIdHex,
              Headers.SpanId -> spanContext.spanIdHex,
              Headers.Sampled -> sampled
            )

            headers.foldLeft(c) { case (carrier, (key, value)) =>
              updater.updated(carrier, key, value)
            }

          case _ =>
            carrier
        }

      override def toString: String =
        "B3Propagator.Injector.MultipleHeaders"
    }
  }

  private trait Extractor {
    def extract[A](ctx: Context, carrier: A)(implicit
        getter: TextMapGetter[A]
    ): Option[Context]
  }

  private object Extractor {
    object Single extends Extractor {
      def extract[A](
          ctx: Context,
          carrier: A
      )(implicit getter: TextMapGetter[A]): Option[Context] = {
        getter.get(carrier, Headers.Combined) match {
          case Some(value) =>
            val parts = value.split(Const.Delimiter)

            if (parts.sizeIs < 2 || parts.sizeIs > 4) {
              None
            } else {
              val traceIdHex = parts(0)
              val spanIdHex = parts(1)
              val sampled = Option.when(parts.length >= 3)(parts(2))

              // if debug flag is set, then set sampled flag, and also set B3 debug to true in the context
              // for onward use by B3 injector
              def create(traceId: ByteVector, spanId: ByteVector): Context =
                if (sampled.contains(Const.DebugSampled)) {
                  val spanContext =
                    buildSpanContext(traceId, spanId, Const.IsSampled)
                  ctx
                    .updated(ContextKeys.Debug, true)
                    .updated(SdkContextKeys.SpanContextKey, spanContext)
                } else {
                  val s = sampled.getOrElse(Const.NotSampled)
                  val spanContext = buildSpanContext(traceId, spanId, s)
                  ctx.updated(SdkContextKeys.SpanContextKey, spanContext)
                }

              for {
                traceId <- ByteVector.fromHex(traceIdHex)
                if isValidTraceId(traceId)
                spanId <- ByteVector.fromHex(spanIdHex) if isValidSpanId(spanId)
              } yield create(traceId, spanId)

            }

          case None =>
            None
        }
      }
    }

    object Multiple extends Extractor {
      def extract[A](ctx: Context, carrier: A)(implicit
          getter: TextMapGetter[A]
      ): Option[Context] = {

        // if debug flag is set, then set sampled flag, and also set B3 debug to true in the context
        // for onward use by B3 injector
        def create(traceId: ByteVector, spanId: ByteVector): Context =
          if (getter.get(carrier, Headers.Debug).contains(Const.IsSampled)) {
            val spanContext = buildSpanContext(traceId, spanId, Const.IsSampled)
            ctx
              .updated(ContextKeys.Debug, true)
              .updated(SdkContextKeys.SpanContextKey, spanContext)
          } else {
            val sampled =
              getter.get(carrier, Headers.Sampled).getOrElse(Const.NotSampled)
            val spanContext = buildSpanContext(traceId, spanId, sampled)
            ctx.updated(SdkContextKeys.SpanContextKey, spanContext)
          }

        for {
          traceIdHex <- getter.get(carrier, Headers.TraceId)
          traceId <- ByteVector.fromHex(traceIdHex) if isValidTraceId(traceId)

          spanIdHex <- getter.get(carrier, Headers.SpanId)
          spanId <- ByteVector.fromHex(spanIdHex) if isValidSpanId(spanId)
        } yield create(traceId, spanId)
      }

    }
  }

  private def buildSpanContext(
      traceId: ByteVector,
      spanId: ByteVector,
      sampled: String
  ): SpanContext = {
    val flags =
      if (Const.IsSampled == sampled || sampled.toBooleanOption.contains(true))
        TraceFlags.Sampled
      else
        TraceFlags.Default

    SpanContext(
      traceId.padLeft(TraceId.Bytes.toLong),
      spanId,
      flags,
      TraceState.empty,
      remote = true
    )
  }

  private def isValidTraceId(value: ByteVector): Boolean =
    (value.length == Constraints.MinTraceIdLength || value.length == Constraints.MaxTraceIdLength) &&
      TraceId.isValid(value.padLeft(TraceId.Bytes.toLong))

  private def isValidSpanId(value: ByteVector): Boolean =
    SpanId.isValid(value)

}
