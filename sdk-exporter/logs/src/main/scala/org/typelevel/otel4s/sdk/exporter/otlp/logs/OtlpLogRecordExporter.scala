/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s
package sdk
package exporter.otlp
package logs

import cats.Applicative
import cats.Foldable
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import fs2.compression.Compression
import fs2.io.net.Network
import fs2.io.net.tls.TLSContext
import org.http4s.Headers
import org.http4s.Uri
import org.http4s.client.Client
import org.http4s.syntax.literals._
import org.typelevel.otel4s.sdk.exporter.RetryPolicy
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

import scala.concurrent.duration._

/** Exports spans using an underlying OTLP client.
  *
  * Supported protocols:
  *   - `grpc`
  *   - `http/json`
  *   - `http/protobuf`
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/protocol/exporter/]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/concepts/sdk-configuration/otlp-exporter-configuration/]]
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-proto/blob/v1.5.0/opentelemetry/proto/collector/logs/v1/logs_service.proto]]
  */
private final class OtlpLogRecordExporter[F[_]: Applicative] private[otlp] (
    client: OtlpClient[F, LogRecordData]
) extends LogRecordExporter.Unsealed[F] {

  val name: String = s"OtlpLogRecordExporter{client=$client}"

  def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): F[Unit] =
    client.doExport(logs)

  def flush: F[Unit] = Applicative[F].unit
}

object OtlpLogRecordExporter {

  private[otlp] object Defaults {
    val Protocol: OtlpProtocol = OtlpProtocol.httpProtobuf
    val HttpEndpoint: Uri = uri"http://localhost:4318/v1/logs"
    val GrpcEndpoint: Uri = uri"http://localhost:4317/opentelemetry.proto.collector.logs.v1.LogsService/Export"
    val Timeout: FiniteDuration = 10.seconds
    val Compression: PayloadCompression = PayloadCompression.none
  }

  /** A builder of [[OtlpLogRecordExporter]] */
  sealed trait Builder[F[_]] {

    /** Sets the OTLP endpoint to connect to.
      *
      * The endpoint must start with either `http://` or `https://`, and include the full HTTP path.
      *
      * Default value is `http://localhost:4318/v1/logs`.
      */
    def withEndpoint(endpoint: Uri): Builder[F]

    /** Sets the maximum time to wait for the collector to process an exported batch of spans.
      *
      * Default value is `10 seconds`.
      */
    def withTimeout(timeout: FiniteDuration): Builder[F]

    /** Sets the compression to use.
      *
      * Default protocol is [[PayloadCompression.none]].
      */
    def withCompression(compression: PayloadCompression): Builder[F]

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
      * Default protocol is [[OtlpProtocol.httpProtobuf]].
      */
    def withProtocol(protocol: OtlpProtocol): Builder[F]

    /** Configures the exporter to use the given client.
      *
      * @note
      *   the 'timeout' and 'tlsContext' settings will be ignored. You must preconfigure the client manually.
      *
      * @param client
      *   the custom http4s client to use
      */
    def withClient(client: Client[F]): Builder[F]

    /** Creates a [[OtlpLogRecordExporter]] using the configuration of this builder.
      */
    def build: Resource[F, LogRecordExporter[F]]
  }

  /** Creates a [[Builder]] of [[OtlpLogRecordExporter]] with the default configuration:
    *   - protocol: `http/protobuf`
    *   - endpoint: `http://localhost:4318/v1/logs`
    *   - timeout: `10 seconds`
    *   - retry policy: 5 exponential attempts, initial backoff is `1 second`, max backoff is `5 seconds`
    */
  def builder[F[_]: Async: Network: Compression: Console]: Builder[F] =
    BuilderImpl(
      protocol = Defaults.Protocol,
      endpoint = None,
      compression = Defaults.Compression,
      timeout = Defaults.Timeout,
      headers = Headers.empty,
      retryPolicy = RetryPolicy.default,
      tlsContext = None,
      client = None
    )

  private final case class BuilderImpl[F[_]: Async: Network: Compression: Console](
      protocol: OtlpProtocol,
      endpoint: Option[Uri],
      compression: PayloadCompression,
      timeout: FiniteDuration,
      headers: Headers,
      retryPolicy: RetryPolicy,
      tlsContext: Option[TLSContext[F]],
      client: Option[Client[F]]
  ) extends Builder[F] {

    def withTimeout(timeout: FiniteDuration): Builder[F] =
      copy(timeout = timeout)

    def withEndpoint(endpoint: Uri): Builder[F] =
      copy(endpoint = Some(endpoint))

    def addHeaders(headers: Headers): Builder[F] =
      copy(headers = this.headers ++ headers)

    def withCompression(compression: PayloadCompression): Builder[F] =
      copy(compression = compression)

    def withTLSContext(context: TLSContext[F]): Builder[F] =
      copy(tlsContext = Some(context))

    def withRetryPolicy(policy: RetryPolicy): Builder[F] =
      copy(retryPolicy = policy)

    def withProtocol(protocol: OtlpProtocol): Builder[F] =
      copy(protocol = protocol)

    def withClient(client: Client[F]): Builder[F] =
      copy(client = Some(client))

    def build: Resource[F, LogRecordExporter[F]] = {
      import LogsProtoEncoder.logRecordDataToRequest
      import LogsProtoEncoder.jsonPrinter

      val endpoint = this.endpoint.getOrElse {
        protocol match {
          case _: OtlpProtocol.Http => Defaults.HttpEndpoint
          case OtlpProtocol.Grpc    => Defaults.GrpcEndpoint
        }
      }

      for {
        client <- OtlpClient.create[F, LogRecordData](
          protocol,
          endpoint,
          headers,
          compression,
          timeout,
          retryPolicy,
          tlsContext,
          client
        )
      } yield new OtlpLogRecordExporter[F](client)
    }
  }

}
