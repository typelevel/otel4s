/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure

import cats.effect.Async
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.client.Client
import org.typelevel.otel4s.sdk.autoconfigure.ExportersAutoConfigure
import org.typelevel.otel4s.sdk.exporter.otlp.logs.autoconfigure.OtlpLogRecordExporterAutoConfigure
import org.typelevel.otel4s.sdk.exporter.otlp.metrics.autoconfigure.OtlpMetricExporterAutoConfigure
import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
import org.typelevel.otel4s.sdk.internal.Diagnostic

object OtlpExportersAutoConfigure {

  /** An OTLP configurers of [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]] and
    * [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
    */
  def apply[
      F[_]: Async: Network: Compression: Diagnostic
  ]: ExportersAutoConfigure[F] =
    ExportersAutoConfigure(
      OtlpMetricExporterAutoConfigure[F],
      OtlpSpanExporterAutoConfigure[F],
      OtlpLogRecordExporterAutoConfigure[F]
    )

  /** An OTLP configurers of [[org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter MetricExporter]] and
    * [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
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
    *   _.addExportersConfigurer(
    *     OtlpExportersAutoConfigure.customClient[IO](JdkHttpClient(jdkHttpClient))
    *   )
    * )
    *   }}}
    *
    * @param client
    *   the custom http4s client to use
    */
  def customClient[
      F[_]: Async: Network: Compression: Diagnostic
  ](client: Client[F]): ExportersAutoConfigure[F] =
    ExportersAutoConfigure(
      OtlpMetricExporterAutoConfigure.customClient[F](client),
      OtlpSpanExporterAutoConfigure.customClient[F](client),
      OtlpLogRecordExporterAutoConfigure.customClient[F](client)
    )

}
