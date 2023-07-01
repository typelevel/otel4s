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
import org.typelevel.otel4s.trace.SpanContext
import scodec.bits.ByteVector

private[java] final case class WrappedSpanContext(
    jSpanContext: JSpanContext
) {

  lazy val traceId: ByteVector =
    ByteVector(jSpanContext.getTraceIdBytes)

  def traceIdHex: String =
    jSpanContext.getTraceId

  lazy val spanId: ByteVector =
    ByteVector(jSpanContext.getSpanIdBytes)

  def spanIdHex: String =
    jSpanContext.getSpanId

  def isValid: Boolean =
    jSpanContext.isValid

  def isRemote: Boolean =
    jSpanContext.isRemote
}

private[trace] object WrappedSpanContext {

  def wrap(jSpanContext: JSpanContext): SpanContext = {

    lazy val traceId: ByteVector =
      ByteVector(jSpanContext.getTraceIdBytes)

    lazy val spanId: ByteVector =
      ByteVector(jSpanContext.getSpanIdBytes)

    val traceFlags: Byte = jSpanContext.getTraceFlags().asByte()

    def isValid: Boolean =
      jSpanContext.isValid

    def isRemote: Boolean =
      jSpanContext.isRemote

    SpanContext.create(traceId, spanId, traceFlags, isValid, isRemote)
  }

  def unwrap(context: SpanContext): JSpanContext = {

    context match {

      case other if other.isRemote =>
        JSpanContext.createFromRemoteParent(
          other.traceIdHex,
          other.spanIdHex,
          TraceFlags.fromByte(other.traceFlags),
          TraceState.getDefault
        )

      case other =>
        JSpanContext.create(
          other.traceIdHex,
          other.spanIdHex,
          TraceFlags.fromByte(other.traceFlags),
          TraceState.getDefault
        )
    }
  }

}
