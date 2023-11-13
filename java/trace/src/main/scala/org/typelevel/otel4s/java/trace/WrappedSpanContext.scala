/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.java.trace

import io.opentelemetry.api.trace.{SpanContext => JSpanContext}
import io.opentelemetry.api.trace.{TraceFlags => JTraceFlags}
import io.opentelemetry.api.trace.{TraceState => JTraceState}
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import org.typelevel.scalaccompat.annotation.threadUnsafe3
import scodec.bits.ByteVector

private[java] final case class WrappedSpanContext(
    jSpanContext: JSpanContext
) extends SpanContext {

  @threadUnsafe3
  lazy val traceId: ByteVector =
    ByteVector(jSpanContext.getTraceIdBytes)

  def traceIdHex: String =
    jSpanContext.getTraceId

  @threadUnsafe3
  lazy val spanId: ByteVector =
    ByteVector(jSpanContext.getSpanIdBytes)

  def spanIdHex: String =
    jSpanContext.getSpanId

  @threadUnsafe3
  lazy val traceFlags: TraceFlags =
    TraceFlags.fromByte(jSpanContext.getTraceFlags.asByte)

  @threadUnsafe3
  lazy val traceState: TraceState = {
    val entries = Vector.newBuilder[(String, String)]
    jSpanContext.getTraceState.forEach((k, v) => entries.addOne(k -> v))
    TraceState.fromVectorUnsafe(entries.result())
  }

  def isValid: Boolean =
    jSpanContext.isValid

  def isRemote: Boolean =
    jSpanContext.isRemote
}

private[trace] object WrappedSpanContext {

  def unwrap(context: SpanContext): JSpanContext = {
    def flags = JTraceFlags.fromByte(context.traceFlags.toByte)

    def traceState =
      context.traceState.asMap
        .foldLeft(JTraceState.builder()) { case (builder, (key, value)) =>
          builder.put(key, value)
        }
        .build()

    context match {
      case ctx: WrappedSpanContext =>
        ctx.jSpanContext

      case other if other.isRemote =>
        JSpanContext.createFromRemoteParent(
          other.traceIdHex,
          other.spanIdHex,
          flags,
          traceState
        )

      case other =>
        JSpanContext.create(
          other.traceIdHex,
          other.spanIdHex,
          flags,
          traceState
        )
    }
  }

}
