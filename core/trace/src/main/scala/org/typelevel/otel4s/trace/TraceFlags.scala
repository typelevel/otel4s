package org.typelevel.otel4s.trace

sealed trait TraceFlags {
  def toByte: Byte 
  def isSampled: Boolean
}

object TraceFlags {
    def fromByte(byte: Byte): TraceFlags = 
        TraceFlagsImpl(byte)

    final private[trace] val SampledMask = 1
}

private final case class TraceFlagsImpl(
    byte: Byte
) extends TraceFlags {
    def toByte: Byte = byte

  /** If set, the least significant bit denotes the caller may have recorded
    * trace data.
    */
  def isSampled: Boolean =
    (byte & TraceFlags.SampledMask) == TraceFlags.SampledMask
}

