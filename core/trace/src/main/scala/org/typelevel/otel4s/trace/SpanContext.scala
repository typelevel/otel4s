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
    new SpanContext {
      val traceId: ByteVector = ByteVector.fromValidHex(traceIdHex)
      val spanId: ByteVector = ByteVector.fromValidHex(spanIdHex)
      val isValid: Boolean = false
      val isRemote: Boolean = false
    }

  private val key = Key.newKey[SyncIO, SpanContext].unsafeRunSync()

  def fromContext[F[_]](context: Vault): Option[SpanContext] =
    context.lookup(key)

  final private[trace] val SampledMask = 1

}

final case class SpanContextImpl(
    traceId: ByteVector,
    spanId: ByteVector,
    flags: Byte,
    isValid: Boolean,
    isRemote: Boolean
) extends SpanContext {

  /** If set, the least significant bit denotes the caller may have recorded
    * trace data.
    */
  def sampledFlag: Boolean =
    (flags & SpanContext.SampledMask) == SpanContext.SampledMask

  /** Checks whether a trace or span id has correct length and is not the
    * invalid id.
    */
  def isValidId(id: ByteVector): Boolean =
    (id.length == SpanContext.TraceId.HexLength) && (id != SpanContext.invalid.traceId)

}
