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

package org.typelevel.otel4s.sdk.exporter.otlp.logs.autoconfigure

import cats.effect.Async
import cats.effect.Resource
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.Headers
import org.http4s.client.Client
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpClientAutoConfigure
import org.typelevel.otel4s.sdk.exporter.otlp.logs.LogsProtoEncoder
import org.typelevel.otel4s.sdk.exporter.otlp.logs.OtlpLogRecordExporter
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

/** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter LogRecordExporter]].
  *
  * @see
  *   [[OtlpClientAutoConfigure]] for OTLP client configuration
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
  */
private final class OtlpLogRecordExporterAutoConfigure[F[_]: Async: Network: Compression: Diagnostic](
    customClient: Option[Client[F]]
) extends AutoConfigure.WithHint[F, LogRecordExporter[F]](
      "OtlpLogRecordExporter",
      Set.empty
    )
    with AutoConfigure.Named.Unsealed[F, LogRecordExporter[F]] {

  def name: String = "otlp"

  protected def fromConfig(config: Config): Resource[F, LogRecordExporter[F]] = {
    import LogsProtoEncoder.logRecordDataToRequest
    import LogsProtoEncoder.jsonPrinter

    val defaults = OtlpClientAutoConfigure.Defaults(
      OtlpLogRecordExporter.Defaults.Protocol,
      OtlpLogRecordExporter.Defaults.HttpEndpoint,
      OtlpLogRecordExporter.Defaults.HttpEndpoint.path.toString,
      Headers.empty,
      OtlpLogRecordExporter.Defaults.Timeout,
      OtlpLogRecordExporter.Defaults.Compression
    )

    OtlpClientAutoConfigure
      .logs[F, LogRecordData](defaults, customClient)
      .configure(config)
      .map { client =>
        new OtlpLogRecordExporter[F](client)
      }
  }

}

object OtlpLogRecordExporterAutoConfigure {

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter LogRecordExporter]].
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.logs.protocol`.
    *
    * Supported protocols:
    *   - `grpc`
    *   - `http/json`
    *   - `http/protobuf`
    *
    * @see
    *   `OtlpHttpClientAutoConfigure` for the configuration details of the OTLP HTTP client
    */
  def apply[F[_]: Async: Network: Compression: Diagnostic]: AutoConfigure.Named[F, LogRecordExporter[F]] =
    new OtlpLogRecordExporterAutoConfigure[F](None)

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter LogRecordExporter]] using the given
    * client.
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.logs.protocol`.
    *
    * Supported protocols:
    *   - `grpc`
    *   - `http/json`
    *   - `http/protobuf`
    *
    * @see
    *   `OtlpHttpClientAutoConfigure` for the configuration details of the OTLP HTTP client
    *
    * @note
    *   the 'timeout' and 'tlsContext' settings will be ignored. You must preconfigure the client manually.
    *
    * @example
    *   {{{
    * import java.net.{InetSocketAddress, ProxySelector}
    * import java.net.http.HttpClient
    * import org.http4s.jdkhttpclient.JdkHttpClient
    *
    * val jdkHttpClient = HttpClient
    *   .newBuilder()
    *   .proxy(ProxySelector.of(InetSocketAddress.createUnresolved("localhost", 3312)))
    *   .build()
    *
    * OpenTelemetrySdk.autoConfigured[IO](
    *   _.addLogRecordExporterConfigurer(
    *     OtlpLogRecordExporterAutoConfigure.customClient[IO](JdkHttpClient(jdkHttpClient))
    *   )
    * )
    *   }}}
    *
    * @param client
    *   the custom http4s client to use
    */
  def customClient[F[_]: Async: Network: Compression: Diagnostic](
      client: Client[F]
  ): AutoConfigure.Named[F, LogRecordExporter[F]] =
    new OtlpLogRecordExporterAutoConfigure[F](Some(client))

}
