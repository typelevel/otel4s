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
import org.typelevel.scalaccompat.annotation.threadUnsafe3
import scodec.bits.ByteVector

/** A valid trace flags is a byte or 2 character lowercase hex (base16) String.
  *
  * These options are propagated to all child spans. These determine features such as whether a Span should be traced.
  */
sealed trait TraceFlags {

  /** Returns the byte representation of this [[TraceFlags]].
    */
  def toByte: Byte

  /** Returns the lowercase hex (base16) representation of this [[TraceFlags]].
    */
  def toHex: String

  /** Returns `true` if the sampling bit is on, otherwise `false`.
    */
  def isSampled: Boolean

  @threadUnsafe3
  override final lazy val hashCode: Int =
    Hash[TraceFlags].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: TraceFlags => Hash[TraceFlags].eqv(this, other)
      case _                 => false
    }

  override final def toString: String =
    Show[TraceFlags].show(this)
}

object TraceFlags {
  private val SampledMask: Byte = 0x01

  // The default trace flags (not sampled): with all flag bits off
  val Default: TraceFlags = fromByte(0x00)

  // The sampled trace flags: the sampled bit is on
  val Sampled: TraceFlags = fromByte(SampledMask)

  /** Creates the [[TraceFlags]] from the given byte representation.
    */
  def fromByte(byte: Byte): TraceFlags =
    TraceFlagsImpl(byte)

  /** Creates the [[TraceFlags]] from the given hex representation.
    *
    * Returns `None` if input string has invalid (non-hex) characters.
    */
  def fromHex(hex: String): Option[TraceFlags] =
    ByteVector.fromHex(hex).map(b => TraceFlagsImpl(b.toByte()))

  implicit val traceFlagsHash: Hash[TraceFlags] =
    Hash.by(_.toByte)

  implicit val traceFlagsShow: Show[TraceFlags] =
    Show.show(_.toHex)

  private final case class TraceFlagsImpl(byte: Byte) extends TraceFlags {
    def toByte: Byte = byte
    def toHex: String = ByteVector.fromByte(byte).toHex

    /** If set, the least significant bit denotes the caller may have recorded trace data.
      */
    def isSampled: Boolean =
      (byte & TraceFlags.SampledMask) == TraceFlags.SampledMask
  }

}
