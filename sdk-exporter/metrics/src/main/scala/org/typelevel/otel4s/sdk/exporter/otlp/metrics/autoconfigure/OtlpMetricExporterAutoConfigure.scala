/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp.metrics.autoconfigure

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.Headers
import org.http4s.client.Client
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpClientAutoConfigure
import org.typelevel.otel4s.sdk.exporter.otlp.metrics.MetricsProtoEncoder
import org.typelevel.otel4s.sdk.exporter.otlp.metrics.OtlpMetricExporter
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

/** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]].
  *
  * @see
  *   [[OtlpClientAutoConfigure]] for OTLP client configuration
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
  */
private final class OtlpMetricExporterAutoConfigure[
    F[_]: Async: Network: Compression: Console
](customClient: Option[Client[F]])
    extends AutoConfigure.WithHint[F, MetricExporter[F]](
      "OtlpMetricExporter",
      Set.empty
    )
    with AutoConfigure.Named[F, MetricExporter[F]] {

  def name: String = "otlp"

  protected def fromConfig(config: Config): Resource[F, MetricExporter[F]] = {
    import MetricsProtoEncoder.exportMetricsRequest
    import MetricsProtoEncoder.jsonPrinter

    val defaults = OtlpClientAutoConfigure.Defaults(
      OtlpMetricExporter.Defaults.Protocol,
      OtlpMetricExporter.Defaults.HttpEndpoint,
      OtlpMetricExporter.Defaults.HttpEndpoint.path.toString,
      Headers.empty,
      OtlpMetricExporter.Defaults.Timeout,
      OtlpMetricExporter.Defaults.Compression
    )

    OtlpClientAutoConfigure
      .metrics[F, MetricData](defaults, customClient)
      .configure(config)
      .map { client =>
        new OtlpMetricExporter[F](
          client,
          AggregationTemporalitySelector.alwaysCumulative,
          AggregationSelector.default,
          CardinalityLimitSelector.default
        )
      }
  }

}

object OtlpMetricExporterAutoConfigure {

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]].
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.metrics.protocol`.
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
      F[_]: Async: Network: Compression: Console
  ]: AutoConfigure.Named[F, MetricExporter[F]] =
    new OtlpMetricExporterAutoConfigure[F](None)

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]] using the given
    * client.
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or `otel.exporter.otlp.metrics.protocol`.
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
    *   _.addMetricExporterConfigurer(
    *     OtlpMetricExporterAutoConfigure.customClient[IO](JdkHttpClient(jdkHttpClient))
    *   )
    * )
    *   }}}
    *
    * @param client
    *   the custom http4s client to use
    */
  def customClient[
      F[_]: Async: Network: Compression: Console
  ](client: Client[F]): AutoConfigure.Named[F, MetricExporter[F]] =
    new OtlpMetricExporterAutoConfigure[F](Some(client))

}
