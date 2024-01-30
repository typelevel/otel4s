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

package org.typelevel.otel4s.sdk.exporter
package otlp.metrics

import cats.Foldable
import cats.effect.{Async, Resource, Temporal}
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
import org.http4s.{EntityEncoder, Header, Headers, HttpVersion, Method, ProductId, Request, Response, Status, Uri}
import org.http4s.client.Client
import org.http4s.client.middleware.{GZip, Retry, RetryPolicy => HttpRetryPolicy}
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.headers.`User-Agent`
import org.http4s.syntax.literals._
import org.typelevel.ci._
import org.typelevel.otel4s.sdk.BuildInfo
import org.typelevel.otel4s.sdk.metrics.data.{AggregationTemporality, MetricData}
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

import scala.concurrent.TimeoutException
import scala.concurrent.duration._

/** Exports spans via HTTP. Support `json` and `protobuf` encoding.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/protocol/exporter/]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/]]
  */
private final class OtlpHttpMetricExporter[F[_]: Temporal: Console] private(
    client: Client[F],
    config: OtlpHttpMetricExporter.Config
) extends MetricExporter[F] {
  import OtlpHttpMetricExporter.Encoding

  val name: String = {
    val headers = config.headers.mkString(
      "headers={",
      ",",
      "}",
      Headers.SensitiveHeaders
    )

    "OtlpHttpMetricExporter{" +
      s"encoding=${config.encoding}, " +
      s"endpoint=${config.endpoint}, " +
      s"timeout=${config.timeout}, " +
      s"gzipCompression=${config.gzipCompression}, " +
      headers +
      "}"
  }

  private implicit val spansEncoder: EntityEncoder[F, List[MetricData]] =
    config.encoding match {
      case Encoding.Json =>
        import io.circe.Printer
        import org.http4s.circe._
        jsonEncoderWithPrinter(Printer.noSpaces)
          .contramap[List[MetricData]](spans => ProtoEncoder.toJson(spans))

      case Encoding.Protobuf =>
        val content = Header.Raw(ci"Content-Type", "application/x-protobuf")
        EntityEncoder.simple(content) { spans =>
          Chunk.array(ProtoEncoder.toByteArray(spans))
        }
    }

  def aggregationTemporality(instrumentType: InstrumentType): AggregationTemporality = ???

  def exportMetrics[G[_] : Foldable](metrics: G[MetricData]): F[Unit] = {
    val request =
      Request[F](Method.POST, config.endpoint, HttpVersion.`HTTP/1.1`)
        .withEntity(metrics.toList)
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
          s"OtlpHttpSpanExporter: cannot export spans: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
        )
      }
  }

  def flush: F[Unit] = Temporal[F].unit

  private def logBody(response: Response[F]): F[Unit] =
    for {
      body <- response.bodyText.compile.string
      _ <- Console[F].println(
        s"[OtlpHttpSpanExporter/${config.encoding}] the request failed with [${response.status}]. Body: $body"
      )
    } yield ()

}

object OtlpHttpMetricExporter {

  private object Defaults {
    val Endpoint: Uri = uri"http://localhost:4318/v1/traces"
    val Timeout: FiniteDuration = 10.seconds
    val GzipCompression: Boolean = false
    val UserAgentName: String = "OTel-OTLP-Exporter-Scala-Otel4s"
  }

  private final case class Config(
      encoding: Encoding,
      endpoint: Uri,
      timeout: FiniteDuration,
      headers: Headers,
      gzipCompression: Boolean
  )

  sealed trait Encoding
  object Encoding {
    case object Json extends Encoding
    case object Protobuf extends Encoding
  }

  /** A builder of [[OtlpHttpMetricExporter]] */
  sealed trait Builder[F[_]] {

    /** Sets the OTLP endpoint to connect to.
      *
      * The endpoint must start with either `http://` or `https://`, and include
      * the full HTTP path.
      *
      * Default value is `http://localhost:4318/v1/traces`.
      */
    def withEndpoint(endpoint: Uri): Builder[F]

    /** Sets the maximum time to wait for the collector to process an exported
      * batch of spans.
      *
      * Default value is `10 seconds`.
      */
    def withTimeout(timeout: FiniteDuration): Builder[F]

    /** Enables Gzip compression.
      *
      * The compression is disabled by default.
      */
    def withGzip: Builder[F]

    /** Disables Gzip compression. */
    def withoutGzip: Builder[F]

    /** Adds headers to requests. */
    def addHeaders(headers: Headers): Builder[F]

    /** Sets the explicit TLS context the HTTP client should use.
      */
    def withTLSContext(context: TLSContext[F]): Builder[F]

    /** Sets the retry policy to use.
      *
      * Default retry policy is [[RetryPolicy.default]].
      */
    def withRetryPolicy(policy: RetryPolicy): Builder[F]

    /** Configures the exporter to use the given encoding.
      *
      * Default encoding is `Protobuf`.
      */
    def withEncoding(encoding: Encoding): Builder[F]

    /** Creates a [[OtlpHttpMetricExporter]] using the configuration of this
      * builder.
      */
    def build: Resource[F, MetricExporter[F]]
  }

  /** Creates a [[Builder]] of [[OtlpHttpMetricExporter]] with the default
    * configuration:
    *   - encoding: `Protobuf`
    *   - endpoint: `http://localhost:4318/v1/metrics`
    *   - timeout: `10 seconds`
    *   - retry policy: 5 exponential attempts, initial backoff is `1 second`,
    *     max backoff is `5 seconds`
    */
  def builder[F[_]: Async: Network: Compression: Console]: Builder[F] =
    BuilderImpl(
      encoding = Encoding.Protobuf,
      endpoint = Defaults.Endpoint,
      gzipCompression = Defaults.GzipCompression,
      timeout = Defaults.Timeout,
      headers = Headers(
        `User-Agent`(
          ProductId(Defaults.UserAgentName, version = Some(BuildInfo.version))
        )
      ),
      retryPolicy = RetryPolicy.default,
      tlsContext = None
    )

  private final case class BuilderImpl[
      F[_]: Async: Network: Compression: Console
  ](
      encoding: Encoding,
      endpoint: Uri,
      gzipCompression: Boolean,
      timeout: FiniteDuration,
      headers: Headers,
      retryPolicy: RetryPolicy,
      tlsContext: Option[TLSContext[F]]
  ) extends Builder[F] {

    def withTimeout(timeout: FiniteDuration): Builder[F] =
      copy(timeout = timeout)

    def withEndpoint(endpoint: Uri): Builder[F] =
      copy(endpoint = endpoint)

    def addHeaders(headers: Headers): Builder[F] =
      copy(headers = this.headers ++ headers)

    def withGzip: Builder[F] =
      copy(gzipCompression = true)

    def withoutGzip: Builder[F] =
      copy(gzipCompression = false)

    def withTLSContext(context: TLSContext[F]): Builder[F] =
      copy(tlsContext = Some(context))

    def withRetryPolicy(policy: RetryPolicy): Builder[F] =
      copy(retryPolicy = policy)

    def withEncoding(encoding: Encoding): Builder[F] =
      copy(encoding = encoding)

    def build: Resource[F, MetricExporter[F]] = {
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
      } yield new OtlpHttpMetricExporter[F](Retry(policy)(gzip(client)), config)
    }
  }

}
