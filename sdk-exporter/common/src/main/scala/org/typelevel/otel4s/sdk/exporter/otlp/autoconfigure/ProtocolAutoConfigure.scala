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
package autoconfigure

import cats.MonadThrow
import cats.effect.Resource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError

/** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.exporter.otlp.Protocol Protocol]].
  *
  * The general configuration options:
  * {{{
  * | System property             | Environment variable        | Description                                                                                                 |
  * |-----------------------------|-----------------------------|-------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.protocol | OTEL_EXPORTER_OTLP_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
  * }}}
  *
  * The metrics-specific configuration options:
  * {{{
  * | System property                     | Environment variable                | Description                                                                                                 |
  * |-------------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.metrics.protocol | OTEL_EXPORTER_OTLP_METRICS_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
  * }}}
  *
  * The traces-specific configuration options:
  * {{{
  * | System property                    | Environment variable               | Description                                                                                                 |
  * |------------------------------------|------------------------------------|-------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.traces.protocol | OTEL_EXPORTER_OTLP_TRACES_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
  */
private final class ProtocolAutoConfigure[F[_]: MonadThrow](
    targetSpecificKey: Config.Key[Protocol]
) extends AutoConfigure.WithHint[F, Protocol](
      "Protocol",
      Set(ProtocolAutoConfigure.ConfigKeys.GeneralProtocol, targetSpecificKey)
    ) {

  import ProtocolAutoConfigure.ConfigKeys
  import ProtocolAutoConfigure.Defaults

  private val protocols: Map[String, Protocol] =
    Map(
      "http/json" -> Protocol.Http(HttpPayloadEncoding.Json),
      "http/protobuf" -> Protocol.Http(HttpPayloadEncoding.Protobuf)
    )

  protected def fromConfig(config: Config): Resource[F, Protocol] = {
    val protocol = config
      .get(targetSpecificKey)
      .flatMap {
        case Some(value) =>
          Right(value)

        case None =>
          config.getOrElse(ConfigKeys.GeneralProtocol, Defaults.OtlpProtocol)
      }

    Resource.eval(MonadThrow[F].fromEither(protocol))
  }

  private implicit val protocolReader: Config.Reader[Protocol] =
    Config.Reader.decodeWithHint("Protocol") { s =>
      protocols
        .get(s.trim.toLowerCase)
        .toRight(
          ConfigurationError(
            s"Unrecognized protocol [$s]. Supported options [${protocols.keys.mkString(", ")}]"
          )
        )
    }

}

private[exporter] object ProtocolAutoConfigure {

  private object ConfigKeys {
    val GeneralProtocol: Config.Key[Protocol] =
      Config.Key("otel.exporter.otlp.protocol")

    val MetricsProtocol: Config.Key[Protocol] =
      Config.Key("otel.exporter.otlp.metrics.protocol")

    val TracesProtocol: Config.Key[Protocol] =
      Config.Key("otel.exporter.otlp.traces.protocol")
  }

  private object Defaults {
    val OtlpProtocol: Protocol = Protocol.Http(HttpPayloadEncoding.Protobuf)
  }

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.exporter.otlp.Protocol Protocol]].
    *
    * The general configuration options:
    * {{{
    * | System property             | Environment variable        | Description                                                                                                 |
    * |-----------------------------|-----------------------------|-------------------------------------------------------------------------------------------------------------|
    * | otel.exporter.otlp.protocol | OTEL_EXPORTER_OTLP_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
    * }}}
    *
    * The metrics-specific configuration options:
    * {{{
    * | System property                     | Environment variable                | Description                                                                                                 |
    * |-------------------------------------|-------------------------------------|-------------------------------------------------------------------------------------------------------------|
    * | otel.exporter.otlp.metrics.protocol | OTEL_EXPORTER_OTLP_METRICS_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
    */
  def metrics[F[_]: MonadThrow]: AutoConfigure[F, Protocol] =
    new ProtocolAutoConfigure[F](ConfigKeys.MetricsProtocol)

  /** Autoconfigures OTLP [[org.typelevel.otel4s.sdk.exporter.otlp.Protocol Protocol]].
    *
    * The general configuration options:
    * {{{
    * | System property             | Environment variable        | Description                                                                                                 |
    * |-----------------------------|-----------------------------|-------------------------------------------------------------------------------------------------------------|
    * | otel.exporter.otlp.protocol | OTEL_EXPORTER_OTLP_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
    * }}}
    *
    * The traces-specific configuration options:
    * {{{
    * | System property                    | Environment variable               | Description                                                                                                 |
    * |------------------------------------|------------------------------------|-------------------------------------------------------------------------------------------------------------|
    * | otel.exporter.otlp.traces.protocol | OTEL_EXPORTER_OTLP_TRACES_PROTOCOL | The transport protocol to use. Options include `http/protobuf` and `http/json`. Default is `http/protobuf`. |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/sdk-configuration/otlp-exporter/#otel_exporter_otlp_protocol]]
    */
  def traces[F[_]: MonadThrow]: AutoConfigure[F, Protocol] =
    new ProtocolAutoConfigure[F](ConfigKeys.TracesProtocol)
}
