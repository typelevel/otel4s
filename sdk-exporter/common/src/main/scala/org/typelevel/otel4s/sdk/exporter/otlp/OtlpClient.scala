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

package org.typelevel.otel4s.sdk.exporter.otlp

import cats.Foldable
import cats.effect.Async
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import fs2.Chunk
import fs2.Stream
import fs2.compression.Compression
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import io.opentelemetry.proto.collector.trace.v1.trace_service.ExportTraceServiceResponse
import org.http4s.ContentCoding
import org.http4s.EntityEncoder
import org.http4s.Header
import org.http4s.Headers
import org.http4s.HttpVersion
import org.http4s.Method
import org.http4s.ProductId
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.middleware.{RetryPolicy => HttpRetryPolicy}
import org.http4s.client.middleware.GZip
import org.http4s.client.middleware.Retry
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.h2.H2Keys
import org.http4s.headers.`User-Agent`
import org.typelevel.ci._
import org.typelevel.otel4s.sdk.BuildInfo
import org.typelevel.otel4s.sdk.exporter.RetryPolicy
import org.typelevel.otel4s.sdk.exporter.otlp.grpc.GrpcCodecs
import org.typelevel.otel4s.sdk.exporter.otlp.grpc.GrpcHeaders
import org.typelevel.otel4s.sdk.exporter.otlp.grpc.GrpcStatusException
import scalapb_circe.Printer
import scodec.Attempt
import scodec.DecodeResult
import scodec.Decoder
import scodec.Encoder
import scodec.bits.BitVector
import scodec.bits.ByteVector

import scala.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration
import scala.util.chaining._

private[otlp] abstract class OtlpClient[F[_]: Temporal: Console, A] private (
    config: OtlpClient.Config,
    client: Client[F]
) {
  import OtlpClient.Defaults

  private val userAgent = `User-Agent`(
    ProductId(Defaults.UserAgentName, version = Some(BuildInfo.version))
  )

  protected def toRequest[G[_]: Foldable](records: G[A]): Request[F]

  protected def handleResponse(response: Response[F]): F[Unit]

  final def doExport[G[_]: Foldable](records: G[A]): F[Unit] =
    client
      .run(toRequest(records).putHeaders(userAgent))
      .use(response => handleResponse(response))
      .timeoutTo(
        config.timeout,
        Temporal[F].unit >> Temporal[F].raiseError(
          new TimeoutException(
            s"OtlpClient(${config.protocol}): the export to [${config.endpoint}] has timed out after [${config.timeout}]"
          )
        )
      )
      .handleErrorWith { e =>
        Console[F].errorln(
          s"[OtlpClient(${config.protocol}) ${config.endpoint}]: cannot export: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
        )
      }

  override final def toString: String = {
    val headers =
      config.headers.mkString("headers={", ",", "}", Headers.SensitiveHeaders)

    "OtlpClient{" +
      s"protocol=${config.protocol}, " +
      s"endpoint=${config.endpoint}, " +
      s"timeout=${config.timeout}, " +
      s"compression=${config.compression}, " +
      headers +
      "}"
  }
}

private[otlp] object OtlpClient {

  private final case class Config(
      protocol: OtlpProtocol,
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers,
      compression: PayloadCompression
  )

  private object Defaults {
    val UserAgentName: String = "OTel-OTLP-Exporter-Scala-Otel4s"
  }

  def create[F[_]: Async: Network: Compression: Console, A](
      protocol: OtlpProtocol,
      endpoint: Uri,
      headers: Headers,
      compression: PayloadCompression,
      timeout: FiniteDuration,
      retryPolicy: RetryPolicy,
      tlsContext: Option[TLSContext[F]],
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): Resource[F, OtlpClient[F, A]] = {
    val config = Config(protocol, endpoint, timeout, headers, compression)

    def createClient(enableHttp2: Boolean): Resource[F, Client[F]] =
      customClient match {
        case Some(client) =>
          Resource
            .eval(
              Console[F].println(
                "You are using a custom http4s client with OtlpClient. " +
                  "'timeout' and 'tlsContext' settings are ignored." +
                  "If you are using the gRPC exporter, make sure the client has '.withHttp2' enabled."
              )
            )
            .as(client)

        case None =>
          EmberClientBuilder
            .default[F]
            .withTimeout(timeout)
            .pipe(builder => tlsContext.foldLeft(builder)(_.withTLSContext(_)))
            .pipe {
              case builder if enableHttp2 => builder.withHttp2
              case builder                => builder
            }
            .build
      }

    def backoff(attempt: Int): Option[FiniteDuration] =
      Option.when(attempt < retryPolicy.maxAttempts) {
        val next =
          retryPolicy.initialBackoff * attempt.toLong * retryPolicy.backoffMultiplier

        val delay =
          next.min(retryPolicy.maxBackoff)

        delay match {
          case f: FiniteDuration => f
          case _                 => retryPolicy.maxBackoff
        }
      }

    protocol match {
      case OtlpProtocol.Http(encoding) =>
        val gzip: Client[F] => Client[F] =
          compression match {
            case PayloadCompression.Gzip          => GZip[F]()
            case PayloadCompression.NoCompression => identity
          }

        // see https://opentelemetry.io/docs/specs/otlp/#failures-1
        val retryable = Set(
          Status.TooManyRequests,
          Status.BadGateway,
          Status.ServiceUnavailable,
          Status.GatewayTimeout
        )

        def shouldRetry(result: Either[Throwable, Response[F]]): Boolean =
          result match {
            case Left(_)         => true
            case Right(response) => retryable.contains(response.status)
          }

        val policy = HttpRetryPolicy[F](backoff, (_, res) => shouldRetry(res))

        for {
          client <- createClient(enableHttp2 = false)
        } yield new Http[F, A](config, encoding, Retry(policy)(gzip(client)))

      case OtlpProtocol.Grpc =>
        // see https://opentelemetry.io/docs/specs/otlp/#failures
        // https://grpc.github.io/grpc/core/md_doc_statuscodes.html
        val retryable = Set(
          1, // CANCELLED
          4, // DEADLINE_EXCEEDED
          10, // ABORTED
          11, // OUT_OF_RANGE
          14, // UNAVAILABLE
          15 // DATA_LOSS
        )

        def shouldRetry(result: Either[Throwable, Response[F]]): Boolean =
          result match {
            case Left(GrpcStatusException(code, _)) => retryable.contains(code)
            case Left(_)                            => true
            case Right(_)                           => false
          }

        val policy = HttpRetryPolicy[F](backoff, (_, res) => shouldRetry(res))

        for {
          client <- createClient(enableHttp2 = true)
        } yield new Grpc[F, A](config, Retry(policy)(client))
    }
  }

  private final class Http[F[_]: Temporal: Console, A](
      config: Config,
      encoding: HttpPayloadEncoding,
      client: Client[F]
  )(implicit encoder: ProtoEncoder.Message[List[A]], printer: Printer)
      extends OtlpClient[F, A](config, client) {

    private implicit val entityEncoder: EntityEncoder[F, List[A]] =
      encoding match {
        case HttpPayloadEncoding.Json =>
          import io.circe.Printer
          import org.http4s.circe._
          jsonEncoderWithPrinter(Printer.noSpaces).contramap[List[A]] { spans =>
            ProtoEncoder.toJson(spans)
          }

        case HttpPayloadEncoding.Protobuf =>
          val content = Header.Raw(ci"Content-Type", "application/x-protobuf")
          EntityEncoder.simple(content) { spans =>
            Chunk.array(ProtoEncoder.toByteArray(spans))
          }
      }

    protected def toRequest[G[_]: Foldable](records: G[A]): Request[F] =
      Request[F](Method.POST, config.endpoint, HttpVersion.`HTTP/1.1`)
        .withEntity(records.toList)
        .putHeaders(config.headers)

    protected def handleResponse(response: Response[F]): F[Unit] =
      logBody(response).unlessA(isSuccess(response.status))

    private def isSuccess(status: Status): Boolean =
      status.responseClass == Status.Successful

    private def logBody(response: Response[F]): F[Unit] =
      for {
        body <- response.bodyText.compile.string
        _ <- Console[F].errorln(
          s"[OtlpClient(${config.protocol}) ${config.endpoint}] the request failed with [${response.status}]. Body: $body"
        )
      } yield ()
  }

  /** The implementation utilizes some ideas from:
    *   - https://github.com/http4s/http4s-grpc
    *   - https://github.com/typelevel/fs2-grpc
    *
    * We can consider migration to http4s-grpc once it reaches 1.x.
    */
  private final class Grpc[F[_]: Temporal: Compression: Console, A](
      config: Config,
      client: Client[F]
  )(implicit encoder: ProtoEncoder.Message[List[A]])
      extends OtlpClient[F, A](config, client) {

    private val encode: Encoder[List[A]] = Encoder { a =>
      Attempt.successful(ByteVector.view(ProtoEncoder.toByteArray(a)).bits)
    }

    private val decode: Decoder[ExportTraceServiceResponse] = Decoder { bits =>
      Attempt
        .fromTry(ExportTraceServiceResponse.validate(bits.bytes.toArrayUnsafe))
        .map(a => DecodeResult(a, BitVector.empty))
    }

    private val bodyStreamEncoder = {
      val isGzip = config.compression match {
        case PayloadCompression.Gzip          => true
        case PayloadCompression.NoCompression => false
      }

      GrpcCodecs.encode(encode, isGzip)
    }

    private val bodyStreamDecoder =
      GrpcCodecs.decode(decode)

    private val headers = {
      val coding = config.compression match {
        case PayloadCompression.Gzip          => ContentCoding.gzip
        case PayloadCompression.NoCompression => ContentCoding.identity
      }

      val grpc = Headers(
        GrpcHeaders.TE,
        GrpcHeaders.GrpcEncoding(coding),
        GrpcHeaders.GrpcAcceptEncoding(coding),
        GrpcHeaders.ContentType
      )

      grpc ++ config.headers
    }

    protected def toRequest[G[_]: Foldable](records: G[A]): Request[F] =
      Request[F](Method.POST, config.endpoint, HttpVersion.`HTTP/2`)
        .putHeaders(headers)
        .withBodyStream(Stream(records.toList).through(bodyStreamEncoder))
        .withAttribute(H2Keys.Http2PriorKnowledge, ())

    protected def handleResponse(response: Response[F]): F[Unit] =
      for {
        _ <- checkGrpcStatus(response.headers)
        body <- decodeResponse(response)
        _ <- checkExportStatus(body)
        trailingHeaders <- response.trailerHeaders
        _ <- checkGrpcStatus(trailingHeaders)
      } yield ()

    private def checkExportStatus(response: ExportTraceServiceResponse): F[Unit] =
      response.partialSuccess
        .filter(r => r.errorMessage.nonEmpty || r.rejectedSpans > 0)
        .traverse_ { ps =>
          Console[F].errorln(
            s"[OtlpClient(${config.protocol}) ${config.endpoint}]: some spans [${ps.rejectedSpans}] were rejected due to [${ps.errorMessage}]"
          )
        }

    private def decodeResponse(response: Response[F]): F[ExportTraceServiceResponse] =
      response.body.through(bodyStreamDecoder).take(1).compile.lastOrError

    private def checkGrpcStatus(headers: Headers): F[Unit] = {
      val status = headers.get[GrpcHeaders.GrpcStatus]
      val reason = headers.get[GrpcHeaders.GrpcMessage]

      status match {
        case Some(GrpcHeaders.GrpcStatus(0)) =>
          Temporal[F].unit

        case Some(GrpcHeaders.GrpcStatus(status)) =>
          Temporal[F].raiseError(GrpcStatusException(status, reason.map(_.message)))

        case None =>
          Temporal[F].unit
      }
    }
  }

}
