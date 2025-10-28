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

package org.typelevel.otel4s.sdk.exporter.otlp.trace
package autoconfigure

import cats.effect.Async
import cats.effect.Resource
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.Headers
import org.http4s.client.Client
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpClientAutoConfigure
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

/** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
  *
  * @see
  *   [[OtlpClientAutoConfigure]] for OTLP client configuration
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
  */
private final class OtlpSpanExporterAutoConfigure[
    F[_]: Async: Network: Compression: Diagnostic
](customClient: Option[Client[F]])
    extends AutoConfigure.WithHint[F, SpanExporter[F]](
      "OtlpSpanExporter",
      Set.empty
    )
    with AutoConfigure.Named.Unsealed[F, SpanExporter[F]] {

  def name: String = "otlp"

  protected def fromConfig(config: Config): Resource[F, SpanExporter[F]] = {
    import SpansProtoEncoder.spanDataToRequest
    import SpansProtoEncoder.jsonPrinter

    val defaults = OtlpClientAutoConfigure.Defaults(
      OtlpSpanExporter.Defaults.Protocol,
      OtlpSpanExporter.Defaults.HttpEndpoint,
      OtlpSpanExporter.Defaults.HttpEndpoint.path.toString,
      Headers.empty,
      OtlpSpanExporter.Defaults.Timeout,
      OtlpSpanExporter.Defaults.Compression
    )

    OtlpClientAutoConfigure
      .traces[F, SpanData](defaults, customClient)
      .configure(config)
      .map(client => new OtlpSpanExporter(client))
  }

}

object OtlpSpanExporterAutoConfigure {

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.traces.protocol`.
    *
    * Supported protocols:
    *   - `grpc`
    *   - `http/json`
    *   - `http/protobuf`
    *
    * @see
    *   `OtlpHttpClientAutoConfigure` for the configuration details of the OTLP HTTP client
    */
  def apply[
      F[_]: Async: Network: Compression: Diagnostic
  ]: AutoConfigure.Named[F, SpanExporter[F]] =
    new OtlpSpanExporterAutoConfigure[F](None)

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]] using the given client.
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.traces.protocol`.
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
    *   _.addSpanExporterConfigurer(
    *     OtlpSpanExporterAutoConfigure.customClient[IO](JdkHttpClient(jdkHttpClient))
    *   )
    * )
    *   }}}
    *
    * @param client
    *   the custom http4s client to use
    */
  def customClient[
      F[_]: Async: Network: Compression: Diagnostic
  ](client: Client[F]): AutoConfigure.Named[F, SpanExporter[F]] =
    new OtlpSpanExporterAutoConfigure[F](Some(client))

}
