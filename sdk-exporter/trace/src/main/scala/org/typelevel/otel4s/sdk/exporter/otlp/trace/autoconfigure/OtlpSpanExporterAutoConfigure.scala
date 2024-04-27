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
import cats.effect.std.Console
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.Headers
import org.http4s.client.Client
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.exporter.otlp.HttpPayloadEncoding
import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpHttpClientAutoConfigure
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

/** Autoconfigures OTLP
  * [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
  *
  * The general configuration options:
  * {{{
  * | System property                    | Environment variable        | Description                                                                                                                                      |
  * |------------------------------------|-----------------------------|--------------------------------------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.protocol        | OTEL_EXPORTER_OTLP_PROTOCOL | The transport protocol to use on OTLP trace, metric, and log requests. Options include `http/protobuf`, and `http/json`. Default is `http/json`. |
  * }}}
  *
  * The traces-specific configuration options:
  * {{{
  * | System property                    | Environment variable               | Description                                                                                                                        |
  * |------------------------------------|------------------------------------|------------------------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.traces.protocol | OTEL_EXPORTER_OTLP_TRACES_PROTOCOL |The transport protocol to use on OTLP trace log requests. Options include `http/protobuf`, and `http/json`. Default is `http/json`. |                               |
  * }}}
  *
  * @see
  *   [[OtlpHttpClientAutoConfigure]] for OTLP HTTP client configuration
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
  */
private final class OtlpSpanExporterAutoConfigure[
    F[_]: Async: Network: Compression: Console
](customClient: Option[Client[F]])
    extends AutoConfigure.WithHint[F, SpanExporter[F]](
      "OtlpSpanExporter",
      OtlpSpanExporterAutoConfigure.ConfigKeys.All
    )
    with AutoConfigure.Named[F, SpanExporter[F]] {

  import OtlpSpanExporterAutoConfigure.ConfigKeys
  import OtlpSpanExporterAutoConfigure.Defaults
  import OtlpSpanExporterAutoConfigure.Protocol

  def name: String = "otlp"

  protected def fromConfig(config: Config): Resource[F, SpanExporter[F]] = {
    import SpansProtoEncoder.spanDataToRequest
    import SpansProtoEncoder.jsonPrinter

    val protocol = config.get(ConfigKeys.TracesProtocol).flatMap {
      case Some(value) =>
        Right(value)

      case None =>
        config
          .get(ConfigKeys.GeneralProtocol)
          .map(_.getOrElse(Defaults.OtlpProtocol))
    }

    protocol match {
      case Right(Protocol.Http(encoding)) =>
        val defaults = OtlpHttpClientAutoConfigure.Defaults(
          OtlpHttpSpanExporter.Defaults.Endpoint,
          OtlpHttpSpanExporter.Defaults.Endpoint.path.toString,
          Headers.empty,
          OtlpHttpSpanExporter.Defaults.Timeout,
          encoding
        )

        OtlpHttpClientAutoConfigure
          .traces[F, SpanData](defaults, customClient)
          .configure(config)
          .map(client => new OtlpHttpSpanExporter[F](client))

      case Left(e) =>
        Resource.raiseError(e: Throwable)
    }
  }

  private implicit val protocolReader: Config.Reader[Protocol] =
    Config.Reader.decodeWithHint("Protocol") { s =>
      s.trim.toLowerCase match {
        case "http/json" =>
          Right(Protocol.Http(HttpPayloadEncoding.Json))

        case "http/protobuf" =>
          Right(Protocol.Http(HttpPayloadEncoding.Protobuf))

        case _ =>
          Left(
            ConfigurationError(
              s"Unrecognized protocol [$s]. Supported options [http/json, http/protobuf]"
            )
          )
      }
    }

}

object OtlpSpanExporterAutoConfigure {

  private sealed trait Protocol
  private object Protocol {
    final case class Http(encoding: HttpPayloadEncoding) extends Protocol
  }

  private object ConfigKeys {
    val GeneralProtocol: Config.Key[Protocol] =
      Config.Key("otel.exporter.otlp.protocol")

    val TracesProtocol: Config.Key[Protocol] =
      Config.Key("otel.exporter.otlp.traces.protocol")

    val All: Set[Config.Key[_]] = Set(
      GeneralProtocol,
      TracesProtocol
    )
  }

  private object Defaults {
    val OtlpProtocol: Protocol = Protocol.Http(HttpPayloadEncoding.Protobuf)
  }

  /** Autoconfigures OTLP
    * [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]].
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or
    * `otel.exporter.otlp.traces.protocol`.
    *
    * The supported protocols: `http/json`, `http/protobuf`.
    *
    * @see
    *   `OtlpHttpClientAutoConfigure` for the configuration details of the OTLP
    *   HTTP client
    */
  def apply[
      F[_]: Async: Network: Compression: Console
  ]: AutoConfigure.Named[F, SpanExporter[F]] =
    new OtlpSpanExporterAutoConfigure[F](None)

  /** Autoconfigures OTLP
    * [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter SpanExporter]]
    * using the given client.
    *
    * The configuration depends on the `otel.exporter.otlp.protocol` or
    * `otel.exporter.otlp.traces.protocol`.
    *
    * The supported protocols: `http/json`, `http/protobuf`.
    *
    * @see
    *   `OtlpHttpClientAutoConfigure` for the configuration details of the OTLP
    *   HTTP client
    *
    * @note
    *   the 'timeout' and 'tlsContext' settings will be ignored. You must
    *   preconfigure the client manually.
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
      F[_]: Async: Network: Compression: Console
  ](client: Client[F]): AutoConfigure.Named[F, SpanExporter[F]] =
    new OtlpSpanExporterAutoConfigure[F](Some(client))

}
