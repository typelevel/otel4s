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

package org.typelevel.otel4s.trace

import cats.effect.SyncIO
import org.typelevel.vault.Key
import org.typelevel.vault.Vault
import scodec.bits.ByteVector

sealed trait SpanContext {

  /** Returns the trace identifier associated with this [[SpanContext]] as
    * 16-byte vector.
    */
  def traceId: ByteVector

  /** Returns the trace identifier associated with this [[SpanContext]] as 32
    * character lowercase hex String.
    */
  final def traceIdHex: String =
    traceId.toHex

  /** Returns the span identifier associated with this [[SpanContext]] as 8-byte
    * vector.
    */
  def spanId: ByteVector

  /** Returns the span identifier associated with this [[SpanContext]] as 16
    * character lowercase hex String.
    */
  final def spanIdHex: String =
    spanId.toHex

  /** Returns details about the trace associated with this [[SpanContext]] as an
    * 8-bit field.
    */
  def traceFlags: TraceFlags

  final def isSampled: Boolean = traceFlags.isSampled

  /** Returns `true` if this [[SpanContext]] is valid.
    */
  def isValid: Boolean

  /** Returns `true` if this [[SpanContext]] was propagated from a remote
    * parent.
    */
  def isRemote: Boolean

  final def storeInContext(context: Vault): Vault =
    context.insert(SpanContext.key, this)
}

object SpanContext {

  object TraceId {
    val Bytes: Int = 16
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
  }

  object SpanId {
    val Bytes: Int = 8
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
  }

  val invalid: SpanContext =
   {
      val traceId: ByteVector = ByteVector.fromValidHex(TraceId.InvalidHex)
      val spanId: ByteVector = ByteVector.fromValidHex(SpanId.InvalidHex)

      val traceFlags: TraceFlags = TraceFlags.fromByte(0)
      val isRemote: Boolean = false

      SpanContextImpl(traceId, spanId, traceFlags, false, isRemote)
    }

  private val key = Key.newKey[SyncIO, SpanContext].unsafeRunSync()

  def fromContext(context: Vault): Option[SpanContext] =
    context.lookup(key)

  /** Checks whether a span id has correct length and is not the
    * invalid id.
    */
  def isValidSpanId(id: ByteVector): Boolean =
    (id.length == SpanContext.SpanId.HexLength) && (id != SpanContext.invalid.spanId)

  /** Checks whether a trace id has correct length and is not the
    * invalid id.
    */
  def isValidTraceId(id: ByteVector): Boolean =
    (id.length == SpanContext.TraceId.HexLength) && (id != SpanContext.invalid.traceId)

  def create(
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      isRemote: Boolean
  ): SpanContext = {
    if (isValidTraceId(traceId) && isValidSpanId(spanId)) {
      SpanContextImpl(traceId, spanId, traceFlags, true, isRemote)
    } else invalid
  }
}

private final case class SpanContextImpl(
    traceId: ByteVector,
    spanId: ByteVector,
    traceFlags: TraceFlags,
    isValid: Boolean,
    isRemote: Boolean
) extends SpanContext 
