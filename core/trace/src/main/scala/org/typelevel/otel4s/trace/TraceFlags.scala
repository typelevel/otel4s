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

sealed trait TraceFlags {
  def toByte: Byte
  def isSampled: Boolean
}

object TraceFlags {
  private[trace] final val SampledMask = 1

  val Default = fromByte(0x00)
  val Sampled = fromByte(0x01)

  private final case class TraceFlagsImpl(byte: Byte) extends TraceFlags {
    def toByte: Byte = byte

    /** If set, the least significant bit denotes the caller may have recorded
      * trace data.
      */
    def isSampled: Boolean =
      (byte & TraceFlags.SampledMask) == TraceFlags.SampledMask
  }

  def fromByte(byte: Byte): TraceFlags =
    TraceFlagsImpl(byte)

}
