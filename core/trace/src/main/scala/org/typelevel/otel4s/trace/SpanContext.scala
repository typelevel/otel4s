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

/** A span context contains the state that must propagate to child spans and
  * across process boundaries.
  *
  * It contains the identifiers (a `trace_id` and `span_id`) associated with the
  * span and a set of flags (currently only whether the context is sampled or
  * not), as well as the remote flag.
  */
trait SpanContext {

  /** Returns the trace identifier associated with this [[SpanContext]] as
    * 16-byte vector.
    */
  def traceId: ByteVector

  /** Returns the trace identifier associated with this [[SpanContext]] as 32
    * character lowercase hex String.
    */
  def traceIdHex: String

  /** Returns the span identifier associated with this [[SpanContext]] as 8-byte
    * vector.
    */
  def spanId: ByteVector

  /** Returns the span identifier associated with this [[SpanContext]] as 16
    * character lowercase hex String.
    */
  def spanIdHex: String

  /** Returns details about the trace associated with this [[SpanContext]] as an
    * 8-bit field.
    */
  def traceFlags: TraceFlags

  /** Return `true` if this [[SpanContext]] is sampled.
    */
  final def isSampled: Boolean =
    traceFlags.isSampled

  /** Returns `true` if this [[SpanContext]] is valid.
    */
  def isValid: Boolean

  /** Returns `true` if this [[SpanContext]] was propagated from a remote
    * parent.
    */
  def isRemote: Boolean

  def storeInContext(context: Vault): Vault =
    context.insert(SpanContext.key, this)
}

object SpanContext {

  object TraceId {
    val Bytes: Int = 16
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
    val Invalid: ByteVector = ByteVector.fromValidHex(InvalidHex)

    def fromLongs(hi: Long, lo: Long): ByteVector =
      ByteVector.fromLong(hi, 8) ++ ByteVector.fromLong(lo, 8)

    /** Checks whether a trace id has correct length and is not the invalid id.
      */
    def isValid(id: ByteVector): Boolean =
      (id.length == Bytes) && (id != Invalid)
  }

  object SpanId {
    val Bytes: Int = 8
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
    val Invalid: ByteVector = ByteVector.fromValidHex(InvalidHex)

    def fromLong(value: Long): ByteVector =
      ByteVector.fromLong(value, 8)

    /** Checks whether a span id has correct length and is not the invalid id.
      */
    def isValid(id: ByteVector): Boolean =
      (id.length == Bytes) && (id != Invalid)
  }

  val invalid: SpanContext =
    new SpanContext {
      val traceIdHex: String = TraceId.InvalidHex
      val traceId: ByteVector = ByteVector.fromValidHex(traceIdHex)
      val spanIdHex: String = SpanId.InvalidHex
      val spanId: ByteVector = ByteVector.fromValidHex(spanIdHex)
      val traceFlags: TraceFlags = TraceFlags.Default
      val isValid: Boolean = false
      val isRemote: Boolean = false
    }

  private val key = Key.newKey[SyncIO, SpanContext].unsafeRunSync()

  def fromContext(context: Vault): Option[SpanContext] =
    context.lookup(key)
}
