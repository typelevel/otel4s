/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp.grpc

import fs2.Chunk
import fs2.Pipe
import fs2.RaiseThrowable
import fs2.Stream
import fs2.compression.Compression
import fs2.interop.scodec.StreamDecoder
import fs2.interop.scodec.StreamEncoder
import scodec.Decoder
import scodec.Encoder

private[otlp] object GrpcCodecs {

  def decode[F[_]: RaiseThrowable: Compression, A](
      decoder: Decoder[A]
  ): Pipe[F, Byte, A] = {
    val entityDecoder: Pipe[F, Byte, A] =
      StreamDecoder.once(decoder).toPipeByte

    val lpmDecoder: Pipe[F, Byte, LengthPrefixedMessage] =
      StreamDecoder.once(LengthPrefixedMessage.codec).toPipeByte

    def decompress: Pipe[F, LengthPrefixedMessage, Byte] =
      _.flatMap { lpm =>
        val payload = Stream.chunk(Chunk.byteVector(lpm.message))
        if (lpm.compressed) {
          payload.through(Compression[F].gunzip()).flatMap(_.content)
        } else {
          payload
        }
      }

    _.through(lpmDecoder).through(decompress).through(entityDecoder)
  }

  def encode[F[_]: RaiseThrowable: Compression, A](
      encoder: Encoder[A],
      gzip: Boolean
  ): Pipe[F, A, Byte] = {
    val compression: Pipe[F, Byte, Byte] =
      if (gzip) Compression[F].gzip() else identity

    val entityEncoder: Pipe[F, A, Byte] =
      StreamEncoder.once(encoder).toPipeByte

    val lpmEncoder: Pipe[F, LengthPrefixedMessage, Byte] =
      StreamEncoder.once(LengthPrefixedMessage.codec).toPipeByte

    _.through(entityEncoder)
      .through(compression)
      .chunks
      .foldMonoid
      .map(chunks => LengthPrefixedMessage(gzip, chunks.toByteVector))
      .through(lpmEncoder)
  }

}
