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

  /** Returns the sampling strategy of this [[SpanContext]]. Indicates whether
    * the span in this context is sampled.
    */
  def samplingDecision: SamplingDecision

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
  }

  object SpanId {
    val Bytes: Int = 8
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
  }

  val invalid: SpanContext =
    new SpanContext {
      val traceIdHex: String = TraceId.InvalidHex
      val traceId: ByteVector = ByteVector.fromValidHex(traceIdHex)
      val spanIdHex: String = SpanId.InvalidHex
      val spanId: ByteVector = ByteVector.fromValidHex(spanIdHex)
      val samplingDecision: SamplingDecision = SamplingDecision.Drop
      val isValid: Boolean = false
      val isRemote: Boolean = false
    }

  private val key = Key.newKey[SyncIO, SpanContext].unsafeRunSync()

  def fromContext[F[_]](context: Vault): Option[SpanContext] =
    context.lookup(key)
}
