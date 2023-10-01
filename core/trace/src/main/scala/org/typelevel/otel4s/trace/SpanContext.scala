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

import cats.Hash
import cats.Show
import cats.effect.SyncIO
import cats.syntax.show._
import org.typelevel.vault.Key
import org.typelevel.vault.Vault
import scodec.bits.ByteVector

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

  def traceFlags: TraceFlags

  def isSampled: Boolean = traceFlags.isSampled

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

    def fromLongs(hi: Long, lo: Long): String =
      (ByteVector.fromLong(hi, 8) ++ ByteVector.fromLong(lo, 8)).toHex
  }

  object SpanId {
    val Bytes: Int = 8
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength

    def fromLong(value: Long): String =
      ByteVector.fromLong(value, 8).toHex
  }

  val invalid: SpanContext =
    new SpanContext {
      val traceIdHex: String = TraceId.InvalidHex
      val traceId: ByteVector = ByteVector.fromValidHex(traceIdHex)
      val spanIdHex: String = SpanId.InvalidHex
      val spanId: ByteVector = ByteVector.fromValidHex(spanIdHex)
      val traceFlags: TraceFlags = TraceFlags.fromByte(0)
      val isValid: Boolean = false
      val isRemote: Boolean = false
    }

  private val key = Key.newKey[SyncIO, SpanContext].unsafeRunSync()

  def create(
      traceId_ : ByteVector,
      spanId_ : ByteVector,
      traceFlags_ : TraceFlags,
      remote: Boolean
  ): SpanContext = {
    new SpanContext {
      def traceId: ByteVector = traceId_
      def traceIdHex: String = traceId.toHex
      def spanId: ByteVector = spanId_
      def spanIdHex: String = spanId.toHex
      def traceFlags: TraceFlags = traceFlags_
      def isValid: Boolean = true // todo calculate
      def isRemote: Boolean = remote
    }
  }

  def fromContext(context: Vault): Option[SpanContext] =
    context.lookup(key)

  implicit val spanContextHash: Hash[SpanContext] =
    Hash.by { ctx =>
      (ctx.traceIdHex, ctx.spanIdHex, ctx.traceFlags, ctx.isValid, ctx.isRemote)
    }

  implicit val spanContextShow: Show[SpanContext] =
    Show.show { ctx =>
      show"SpanContext{traceId=${ctx.traceIdHex}, spanId=${ctx.spanIdHex}, traceFlags=${ctx.traceFlags}, remote=${ctx.isRemote}, valid=${ctx.isValid}}"
    }

}
