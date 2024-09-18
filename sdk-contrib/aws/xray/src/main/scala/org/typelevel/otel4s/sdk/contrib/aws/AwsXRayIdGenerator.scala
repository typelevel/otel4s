package org.typelevel.otel4s.sdk.contrib.aws

import cats.*
import cats.effect.*
import cats.effect.std.*
import cats.syntax.all.*
import org.typelevel.otel4s.sdk.trace.IdGenerator
import org.typelevel.otel4s.trace.SpanContext.{SpanId, TraceId}
import scodec.bits.ByteVector

object AwsXRayIdGenerator {
  def apply[F[_] : Applicative : Clock : Random]: AwsXRayIdGenerator[F] = new AwsXRayIdGenerator
}

class AwsXRayIdGenerator[F[_] : Applicative : Clock : Random] extends IdGenerator[F] {
  override def generateSpanId: F[ByteVector] =
    Random[F]
      .nextLong
      .map(SpanId.fromLong)

  override def generateTraceId: F[ByteVector] =
    (Clock[F].realTime.map(_.toSeconds),
      Random[F].nextInt.map(_ & 0xFFFFFFFFL),
      Random[F].nextLong
    ).mapN { case (timestampSecs, hiRandom, lowRandom) =>
      TraceId.fromLongs(timestampSecs << 32 | hiRandom, lowRandom)
    }
}
