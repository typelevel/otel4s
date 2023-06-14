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
import io.opentelemetry.api.trace.TraceFlags
import io.opentelemetry.api.trace.TraceState
import org.typelevel.otel4s.trace.SamplingDecision
import org.typelevel.otel4s.trace.SpanContext
import scodec.bits.ByteVector

private[java] final case class WrappedSpanContext(
    jSpanContext: JSpanContext
) extends SpanContext { // create, not implement, a new SpanContext

  lazy val traceId: ByteVector =
    ByteVector(jSpanContext.getTraceIdBytes)

  def traceIdHex: String =
    jSpanContext.getTraceId

  lazy val spanId: ByteVector =
    ByteVector(jSpanContext.getSpanIdBytes)

  def spanIdHex: String =
    jSpanContext.getSpanId

  lazy val samplingDecision: SamplingDecision =
    SamplingDecision.fromBoolean(jSpanContext.isSampled)

  def isValid: Boolean =
    jSpanContext.isValid

  def isRemote: Boolean =
    jSpanContext.isRemote
}

private[trace] object WrappedSpanContext {

  def unwrap(context: SpanContext): JSpanContext = {
    def flags =
      if (context.samplingDecision.isSampled) TraceFlags.getSampled
      else TraceFlags.getDefault

    context match {
      case ctx: WrappedSpanContext =>
        ctx.jSpanContext

      case other if other.isRemote =>
        JSpanContext.createFromRemoteParent(
          other.traceIdHex,
          other.spanIdHex,
          flags,
          TraceState.getDefault
        )

      case other =>
        JSpanContext.create(
          other.traceIdHex,
          other.spanIdHex,
          flags,
          TraceState.getDefault
        )
    }
  }

}
