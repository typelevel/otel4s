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
import fs2.io.net.Network
import io.opentelemetry.proto.collector.trace.v1.trace_service.ExportTraceServiceRequest
import org.http4s.EntityEncoder
import org.http4s.Header
import org.http4s.Headers
import org.http4s.HttpVersion
import org.http4s.Method
import org.http4s.Request
import org.http4s.Response
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder
import org.http4s.syntax.literals._
import org.typelevel.ci._
import org.typelevel.otel4s.sdk.exporter.otlp.OtlpHttpSpanExporter.Encoding
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

import scala.concurrent.duration._

private final class OtlpHttpSpanExporter[F[_]: Temporal: Console] private (
    client: Client[F],
    config: OtlpHttpSpanExporter.Config
) extends SpanExporter[F] {
  import JsonCodecs._

  val name: String = {
    val headers = config.headers.mkString(
      "headers{",
      ",",
      "}",
      Headers.SensitiveHeaders
    )

    "OtlpHttpSpanExporter{" +
      s"encoding=${config.encoding}, " +
      s"endpoint=${config.endpoint}, " +
      s"timeout=${config.timeout}, " +
      s"gzipCompression=${config.gzipCompression}, " +
      headers +
      "}"
  }

  private implicit val spansEncoder: EntityEncoder[F, List[SpanData]] =
    config.encoding match {
      case Encoding.Json =>
        import org.http4s.circe._
        jsonEncoderOf[F, List[SpanData]]

      case Encoding.Protobuf =>
        val content = Header.Raw(ci"Content-Type", "application/x-protobuf")
        EntityEncoder.simple(content) { spans =>
          val request: ExportTraceServiceRequest = ProtoCodecs.toProto(spans)
          Chunk.array(request.toByteArray)
        }
    }

  def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit] = {
    val request = Request[F](Method.POST, config.endpoint, HttpVersion.`HTTP/2`)
      .withEntity(spans.toList)
      .putHeaders(config.headers)

    client
      .run(request)
      .use(response => logBody(response).unlessA(response.status.isSuccess))
      .timeout(config.timeout)
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

object OtlpHttpSpanExporter {

  private object Defaults {
    val Endpoint: Uri = uri"http://localhost:4318/v1/traces"
    val Timeout: FiniteDuration = 10.seconds
    val GzipCompression: Boolean = false
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

  /** A builder of [[OtlpHttpSpanExporter]] */
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
    def enableGzip: Builder[F]

    /** Disables Gzip compression. */
    def disableGzip: Builder[F]

    /** Adds headers to requests. */
    def addHeaders(headers: Headers): Builder[F]

    /** Creates a [[OtlpHttpSpanExporter]] using the configuration of this
      * builder.
      */
    def build: Resource[F, SpanExporter[F]]
  }

  /** Creates a [[Builder]] of [[OtlpHttpSpanExporter]] with the default
    * configuration.
    */
  def builder[F[_]: Async: Network: Console](encoding: Encoding): Builder[F] =
    BuilderImpl(
      encoding = encoding,
      endpoint = Defaults.Endpoint,
      gzipCompression = Defaults.GzipCompression,
      timeout = Defaults.Timeout,
      headers = Headers.empty
    )

  private final case class BuilderImpl[F[_]: Async: Network: Console](
      encoding: Encoding,
      endpoint: Uri,
      gzipCompression: Boolean,
      timeout: FiniteDuration,
      headers: Headers
  ) extends Builder[F] {

    def withTimeout(timeout: FiniteDuration): Builder[F] =
      copy(timeout = timeout)

    def withEndpoint(endpoint: Uri): Builder[F] =
      copy(endpoint = endpoint)

    def addHeaders(headers: Headers): Builder[F] =
      copy(headers = this.headers ++ headers)

    def enableGzip: Builder[F] =
      copy(gzipCompression = true)

    def disableGzip: Builder[F] =
      copy(gzipCompression = false)

    def build: Resource[F, SpanExporter[F]] = {
      val config = Config(encoding, endpoint, timeout, headers, gzipCompression)

      for {
        client <- EmberClientBuilder.default[F].build
        //   client = if (gzipCompression) GZip()(c) else c // todo: doesn't compile in js
      } yield new OtlpHttpSpanExporter[F](client, config)
    }
  }

}
