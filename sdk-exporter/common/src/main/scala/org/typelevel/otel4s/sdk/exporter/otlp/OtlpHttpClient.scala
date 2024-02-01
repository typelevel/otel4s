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
import fs2.compression.Compression
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import org.http4s.EntityEncoder
import org.http4s.Header
import org.http4s.Headers
import org.http4s.HttpVersion
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Status
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.client.middleware.{RetryPolicy => HttpRetryPolicy}
import org.http4s.client.middleware.GZip
import org.http4s.client.middleware.Retry
import org.http4s.ember.client.EmberClientBuilder
import org.typelevel.ci._
import org.typelevel.otel4s.sdk.exporter.RetryPolicy
import scalapb_circe.Printer

import scala.concurrent.TimeoutException
import scala.concurrent.duration.FiniteDuration

/** Exports spans via HTTP. Support `json` and `protobuf` encoding.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/protocol/exporter/]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/]]
  */
private final class OtlpHttpClient[F[_]: Temporal: Console, A] private (
    client: Client[F],
    config: OtlpHttpClient.Config,
)(implicit encoder: ProtoEncoder.Message[List[A]], printer: Printer) {

  private implicit val entityEncoder: EntityEncoder[F, List[A]] =
    config.encoding match {
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

  def doExport[G[_]: Foldable](records: G[A]): F[Unit] = {
    val request =
      Request[F](Method.POST, config.endpoint, HttpVersion.`HTTP/1.1`)
        .withEntity(records.toList)
        .putHeaders(config.headers)

    client
      .run(request)
      .use(response => logBody(response).unlessA(response.status.isSuccess))
      .timeoutTo(
        config.timeout,
        Temporal[F].unit >> Temporal[F].raiseError(
          new TimeoutException(
            s"The export to [${config.endpoint}] has timed out after [${config.timeout}]"
          )
        )
      )
      .handleErrorWith { e =>
        Console[F].errorln(
          s"OtlpHttpClient: cannot export: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
        )
      }
  }

  private def logBody(response: Response[F]): F[Unit] =
    for {
      body <- response.bodyText.compile.string
      _ <- Console[F].println(
        s"[OtlpHttpClient/${config.encoding}] the request failed with [${response.status}]. Body: $body"
      )
    } yield ()

  override def toString: String = {
    val headers = config.headers.mkString(
      "headers={",
      ",",
      "}",
      Headers.SensitiveHeaders
    )

    "OtlpHttpClient{" +
      s"encoding=${config.encoding}, " +
      s"endpoint=${config.endpoint}, " +
      s"timeout=${config.timeout}, " +
      s"gzipCompression=${config.gzipCompression}, " +
      headers +
      "}"
  }
}

private[otlp] object OtlpHttpClient {

  private final case class Config(
      encoding: HttpPayloadEncoding,
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers,
      gzipCompression: Boolean
  )

  def create[F[_]: Async: Network: Compression: Console, A](
      encoding: HttpPayloadEncoding,
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers,
      gzipCompression: Boolean,
      retryPolicy: RetryPolicy,
      tlsContext: Option[TLSContext[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): Resource[F, OtlpHttpClient[F, A]] = {
    val config = Config(encoding, endpoint, timeout, headers, gzipCompression)

    val builder = EmberClientBuilder
      .default[F]
      .withTimeout(config.timeout)

    val gzip: Client[F] => Client[F] =
      if (gzipCompression) GZip[F]() else identity

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

    // see https://opentelemetry.io/docs/specs/otlp/#failures-1
    val retryableCodes = Set(
      Status.TooManyRequests,
      Status.BadGateway,
      Status.ServiceUnavailable,
      Status.GatewayTimeout
    )

    def shouldRetry(result: Either[Throwable, Response[F]]): Boolean =
      result match {
        case Left(_)         => true
        case Right(response) => retryableCodes.contains(response.status)
      }

    val policy = HttpRetryPolicy[F](backoff, (_, res) => shouldRetry(res))

    for {
      client <- tlsContext.foldLeft(builder)(_.withTLSContext(_)).build
    } yield new OtlpHttpClient[F, A](Retry(policy)(gzip(client)), config)
  }

}
