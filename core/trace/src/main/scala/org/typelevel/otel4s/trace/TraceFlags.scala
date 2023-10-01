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
import scodec.bits.ByteVector

/** A valid trace flags is a byte or 2 character lowercase hex (base16) String.
  *
  * These options are propagated to all child spans. These determine features
  * such as whether a Span should be traced.
  */
sealed trait TraceFlags {

  /** Returns the byte representation of this [[TraceFlags]].
    */
  def asByte: Byte

  /** Returns the lowercase hex (base16) representation of this [[TraceFlags]].
    */
  def asHex: String

  /** Returns `true` if the sampling bit is on, otherwise `false`.
    */
  def isSampled: Boolean

  override final def hashCode(): Int =
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
  private val SampledMask = 0x01

  val HexLength = 2

  val Default: TraceFlags = fromByte(0x00)
  val Sampled: TraceFlags = fromByte(0x01)

  /** Returns the [[TraceFlags]] converted from the given byte representation.
    */
  def fromByte(byte: Byte): TraceFlags =
    TraceFlagsImpl(byte)

  implicit val traceFlagsHash: Hash[TraceFlags] =
    Hash.by(_.asByte)

  implicit val traceFlagsShow: Show[TraceFlags] =
    Show.show(_.asHex)

  private final case class TraceFlagsImpl(byte: Byte) extends TraceFlags {
    def asByte: Byte = byte
    def asHex: String = ByteVector.fromByte(byte).toHex

    def isSampled: Boolean =
      (byte & TraceFlags.SampledMask) == TraceFlags.SampledMask
  }

}
