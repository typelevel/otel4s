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

import cats.syntax.either._
import cats.syntax.show._
import org.http4s.ContentCoding
import org.http4s.Header
import org.http4s.ParseFailure
import org.http4s.ParseResult
import org.typelevel.ci._

private[otlp] object GrpcHeaders {

  val ContentType: Header.Raw =
    Header.Raw(ci"Content-Type", "application/grpc+proto")

  val TE: Header.Raw =
    Header.Raw(ci"te", "trailers")

  final case class GrpcEncoding(coding: ContentCoding)

  object GrpcEncoding {
    implicit val header: Header[GrpcEncoding, Header.Single] =
      Header.create(
        ci"grpc-encoding",
        _.coding.coding,
        s => ContentCoding.parse(s).map(c => GrpcEncoding(c))
      )
  }

  final case class GrpcAcceptEncoding(coding: ContentCoding)

  object GrpcAcceptEncoding {
    implicit val header: Header[GrpcAcceptEncoding, Header.Single] =
      Header.create(
        ci"grpc-accept-encoding",
        _.coding.coding,
        s => ContentCoding.parse(s).map(c => GrpcAcceptEncoding(c))
      )
  }

  // https://grpc.github.io/grpc/core/md_doc_statuscodes.html
  final case class GrpcStatus(statusCode: Int)

  object GrpcStatus {
    private val parser =
      cats.parse.Numbers.nonNegativeIntString.map(s => GrpcStatus(s.toInt))

    implicit val header: Header[GrpcStatus, Header.Single] =
      Header.create(
        ci"grpc-status",
        _.statusCode.toString,
        s =>
          parser
            .parseAll(s)
            .leftMap(e => ParseFailure("Invalid GrpcStatus", e.show))
      )
  }

  final case class GrpcMessage(message: String)

  object GrpcMessage {
    implicit val header: Header[GrpcMessage, Header.Single] =
      Header.create(
        ci"grpc-message",
        _.message,
        s => ParseResult.success(GrpcMessage(s))
      )
  }

}
