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
import cats.syntax.show._
import scodec.bits.ByteVector

/** A class that represents a span context.
  *
  * A span context contains the state that must propagate to child spans and
  * across process boundaries.
  *
  * It contains the identifiers (a `trace_id` and `span_id`) associated with the
  * span and a set of flags (currently only whether the context is sampled or
  * not), as well as the remote flag.
  */
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

  /** Returns the [[TraceFlags]] associated with this [[SpanContext]].
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

  override final def hashCode(): Int =
    Hash[SpanContext].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: SpanContext => Hash[SpanContext].eqv(this, other)
      case _                  => false
    }

  override final def toString: String =
    Show[SpanContext].show(this)
}

object SpanContext {

  private[otel4s] sealed trait Delegate[A] extends SpanContext {
    def underlying: A
  }

  object TraceId {
    val Bytes: Int = 16
    val HexLength: Int = Bytes * 2
    val InvalidHex: String = "0" * HexLength
    val Invalid: ByteVector = ByteVector.fromValidHex(InvalidHex)

    def fromLongs(hi: Long, lo: Long): ByteVector =
      ByteVector.fromLong(hi, 8) ++ ByteVector.fromLong(lo, 8)

    /** Creates span id from hex.
      *
      * Returns `None` when the input isn't a valid hex or the id is invalid.
      */
    def fromHex(hex: String): Option[ByteVector] =
      ByteVector.fromHex(hex).filter(bv => isValid(bv))

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

    /** Creates span id from hex.
      *
      * Returns `None` when the input isn't a valid hex or the id is invalid.
      */
    def fromHex(hex: String): Option[ByteVector] =
      ByteVector.fromHex(hex).filter(bv => isValid(bv))

    /** Checks whether a span id has correct length and is not the invalid id.
      */
    def isValid(id: ByteVector): Boolean =
      (id.length == Bytes) && (id != Invalid)
  }

  val invalid: SpanContext =
    SpanContextImpl(
      traceId = TraceId.Invalid,
      spanId = SpanId.Invalid,
      traceFlags = TraceFlags.Default,
      isRemote = false,
      isValid = false
    )

  /** Creates a new [[SpanContext]] with the given identifiers and options.
    *
    * If the `traceId` or the `spanId` are invalid (ie. do not conform to the
    * requirements for hexadecimal ids of the appropriate lengths), both will be
    * replaced with the standard "invalid" versions (i.e. all '0's).
    *
    * @param traceId
    *   the trace identifier of the span context
    *
    * @param spanId
    *   the span identifier of the span context
    *
    * @param traceFlags
    *   the trace flags of the span context
    *
    * @param remote
    *   whether the span is propagated from the remote parent or not
    */
  def create(
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      remote: Boolean
  ): SpanContext =
    createInternal(
      traceId = traceId,
      spanId = spanId,
      traceFlags = traceFlags,
      remote = remote,
      skipIdValidation = false
    )

  /** Creates a new [[SpanContext]] with the given identifiers and options.
    *
    * If the id validation isn't skipped and the `traceId` or the `spanId` are
    * invalid (ie. do not conform to the requirements for hexadecimal ids of the
    * appropriate lengths), both will be replaced with the standard "invalid"
    * versions (i.e. all '0's).
    *
    * '''Note''': the method is for the internal use only. It is not supposed to
    * be publicly available.
    *
    * @see
    *   [[create]]
    *
    * @param traceId
    *   the trace identifier of the span context
    *
    * @param spanId
    *   the span identifier of the span context
    *
    * @param traceFlags
    *   the trace flags of the span context
    *
    * @param remote
    *   whether the span is propagated from the remote parent or not
    *
    * @param skipIdValidation
    *   pass true to skip validation of trace ID and span ID as an optimization
    *   in cases where they are known to have been already validated
    */
  private[otel4s] def createInternal(
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      remote: Boolean,
      skipIdValidation: Boolean
  ): SpanContext = {
    if (
      skipIdValidation || (TraceId.isValid(traceId) && SpanId.isValid(spanId))
    ) {
      SpanContextImpl(
        traceId = traceId,
        spanId = spanId,
        traceFlags = traceFlags,
        isRemote = remote,
        isValid = true
      )
    } else {
      SpanContextImpl(
        traceId = invalid.traceId,
        spanId = invalid.spanId,
        traceFlags = traceFlags,
        isRemote = remote,
        isValid = false
      )
    }
  }

  /** Creates a delegated [[SpanContext]].
    *
    * '''Note''': the method is for the internal use only. It is not supposed to
    * be publicly available.
    */
  private[otel4s] def delegate[A](
      underlying: A,
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      remote: Boolean,
      isValid: Boolean
  ): Delegate[A] =
    DelegateImpl(
      underlying = underlying,
      traceId = traceId,
      spanId = spanId,
      traceFlags = traceFlags,
      isRemote = remote,
      isValid = isValid,
    )

  implicit val spanContextHash: Hash[SpanContext] =
    Hash.by { ctx =>
      (ctx.traceIdHex, ctx.spanIdHex, ctx.traceFlags, ctx.isValid, ctx.isRemote)
    }

  implicit val spanContextShow: Show[SpanContext] =
    Show.show { ctx =>
      show"SpanContext{traceId=${ctx.traceIdHex}, spanId=${ctx.spanIdHex}, traceFlags=${ctx.traceFlags}, remote=${ctx.isRemote}, valid=${ctx.isValid}}"
    }

  private final case class SpanContextImpl(
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      isRemote: Boolean,
      isValid: Boolean
  ) extends SpanContext

  private final case class DelegateImpl[A](
      underlying: A,
      traceId: ByteVector,
      spanId: ByteVector,
      traceFlags: TraceFlags,
      isRemote: Boolean,
      isValid: Boolean
  ) extends Delegate[A]

}
