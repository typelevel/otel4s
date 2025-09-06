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
package otlp
package autoconfigure

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.either._
import cats.syntax.functor._
import fs2.compression.Compression
import fs2.io.net.Network
import org.http4s.Header
import org.http4s.Headers
import org.http4s.Uri
import org.http4s.client.Client
import org.typelevel.ci.CIString
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import scalapb_circe.Printer

import scala.concurrent.duration.FiniteDuration

/** Autoconfigures [[OtlpClient]].
  *
  * Target-specific properties are prioritized. E.g. `otel.exporter.otlp.traces.endpoint` is prioritized over
  * `otel.exporter.otlp.endpoint`.
  *
  * The general configuration options:
  * {{{
  * | System property                       | Environment variable                  | Description                                                                                                                                                                      |
  * |---------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.protocol           | OTEL_EXPORTER_OTLP_PROTOCOL           | The transport protocol to use. Options include `grpc`, `http/protobuf`, and `http/json`. Default is `http/protobuf`.                                                             |
  * | otel.exporter.otlp.endpoint           | OTEL_EXPORTER_OTLP_ENDPOINT           | The OTLP traces, metrics, and logs endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4318/`. |
  * | otel.exporter.otlp.headers            | OTEL_EXPORTER_OTLP_HEADERS            | Key-value pairs separated by commas to pass as request headers on OTLP trace, metric, and log requests.                                                                          |
  * | otel.exporter.otlp.compression        | OTEL_EXPORTER_OTLP_COMPRESSION        | The compression type to use on OTLP trace, metric, and log requests. Options include `gzip`. By default no compression will be used.                                             |
  * | otel.exporter.otlp.timeout            | OTEL_EXPORTER_OTLP_TIMEOUT            | The maximum waiting time to send each OTLP trace, metric, and log batch. Default is `10 seconds`.                                                                                |
  * }}}
  *
  * The metrics-specific configuration options:
  * {{{
  * | System property                        | Environment variable                   | Description                                                                                                          |
  * |----------------------------------------|----------------------------------------|----------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.metrics.protocol    | OTEL_EXPORTER_OTLP_METRICS_PROTOCOL    | The transport protocol to use. Options include `grpc`, `http/protobuf`, and `http/json`. Default is `http/protobuf`. |
  * | otel.exporter.otlp.metrics.headers     | OTEL_EXPORTER_OTLP_METRICS_HEADERS     | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                               |
  * | otel.exporter.otlp.metrics.endpoint    | OTEL_EXPORTER_OTLP_METRICS_ENDPOINT    | The OTLP traces endpoint to connect to. Default is `http://localhost:4318/v1/metrics`.                               |
  * | otel.exporter.otlp.metrics.compression | OTEL_EXPORTER_OTLP_METRICS_COMPRESSION | The compression type to use on OTLP trace requests. Options include `gzip`. By default no compression will be used.  |
  * | otel.exporter.otlp.metrics.timeout     | OTEL_EXPORTER_OTLP_METRICS_TIMEOUT     | The maximum waiting time to send each OTLP trace batch. Default is `10 seconds`.                                     |
  * }}}
  *
  * The traces-specific configuration options:
  * {{{
  * | System property                       | Environment variable                  | Description                                                                                                          |
  * |---------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.traces.protocol    | OTEL_EXPORTER_OTLP_TRACES_PROTOCOL    | The transport protocol to use. Options include `grpc`, `http/protobuf`, and `http/json`. Default is `http/protobuf`. |
  * | otel.exporter.otlp.traces.headers     | OTEL_EXPORTER_OTLP_TRACES_HEADERS     | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                               |
  * | otel.exporter.otlp.traces.endpoint    | OTEL_EXPORTER_OTLP_TRACES_ENDPOINT    | The OTLP traces endpoint to connect to. Default is `http://localhost:4318/v1/traces`.                                |
  * | otel.exporter.otlp.traces.compression | OTEL_EXPORTER_OTLP_TRACES_COMPRESSION | The compression type to use on OTLP trace requests. Options include `gzip`. By default no compression will be used.  |
  * | otel.exporter.otlp.traces.timeout     | OTEL_EXPORTER_OTLP_TRACES_TIMEOUT     | The maximum waiting time to send each OTLP trace batch. Default is `10 seconds`.                                     |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#otlp-exporter-span-metric-and-log-exporters]]
  */
private final class OtlpClientAutoConfigure[
    F[_]: Async: Network: Compression: Console,
    A
](
    specific: OtlpClientAutoConfigure.ConfigKeys.Keys,
    defaults: OtlpClientAutoConfigure.Defaults,
    customClient: Option[Client[F]],
    configKeys: Set[Config.Key[_]]
)(implicit encoder: ProtoEncoder.Message[List[A]], printer: Printer)
    extends AutoConfigure.WithHint[F, OtlpClient[F, A]](
      "OtlpClient",
      configKeys
    ) {

  import OtlpClientAutoConfigure.{ConfigKeys, Defaults}

  private val protocols: Map[String, OtlpProtocol] =
    Map(
      "http/json" -> OtlpProtocol.httpJson,
      "http/protobuf" -> OtlpProtocol.httpProtobuf,
      "grpc" -> OtlpProtocol.grpc
    )

  private val compressions: Map[String, PayloadCompression] =
    Map(
      "gzip" -> PayloadCompression.gzip,
      "none" -> PayloadCompression.none
    )

  protected def fromConfig(config: Config): Resource[F, OtlpClient[F, A]] = {
    def get[V: Config.Reader](
        select: ConfigKeys.Keys => Config.Key[V]
    ): Either[ConfigurationError, Option[V]] =
      config.get(select(specific)).flatMap {
        case Some(value) =>
          Right(Some(value))

        case None =>
          config.get(select(ConfigKeys.General))
      }

    def getOrElse[V: Config.Reader](
        select: ConfigKeys.Keys => Config.Key[V],
        default: Defaults => V
    ): Either[ConfigurationError, V] =
      get(select).map(_.getOrElse(default(defaults)))

    def getEndpoint =
      config.get(specific.Endpoint).flatMap {
        case Some(value) =>
          Right(value)

        case None =>
          config
            .get(ConfigKeys.General.Endpoint)
            .map(
              _.fold(defaults.endpoint)(
                _.addPath(defaults.apiPath.stripPrefix("/"))
              )
            )
      }

    def tryLoad =
      for {
        protocol <- getOrElse(_.Protocol, _.protocol)
        endpoint <- getEndpoint
        timeout <- getOrElse(_.Timeout, _.timeout)
        headers <- getOrElse(_.Headers, _.headers)
        compression <- getOrElse(_.Compression, _.compression)
      } yield OtlpClient.create(
        protocol,
        endpoint,
        headers,
        compression,
        timeout,
        RetryPolicy.default,
        None,
        customClient
      )

    tryLoad match {
      case Right(resource) => resource
      case Left(error)     => Resource.raiseError(error: Throwable)
    }
  }

  private implicit val uriReader: Config.Reader[Uri] =
    Config.Reader.decodeWithHint("Uri") { s =>
      Uri.fromString(s).leftMap(e => ConfigurationError(e.message))
    }

  private implicit val headersReader: Config.Reader[Headers] =
    Config.Reader[Map[String, String]].map { value =>
      val headers = value.map { case (key, value) =>
        Header.Raw(CIString(key), value)
      }
      new Headers(headers.toList)
    }

  private implicit val compressionReader: Config.Reader[PayloadCompression] =
    Config.Reader.decodeWithHint("Compression") { s =>
      compressions
        .get(s.trim.toLowerCase)
        .toRight(
          ConfigurationError(
            s"Unrecognized compression [$s]. Supported options [${compressions.keys.mkString(", ")}]"
          )
        )
    }

  private implicit val protocolReader: Config.Reader[OtlpProtocol] =
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

private[exporter] object OtlpClientAutoConfigure {

  private[otlp] final case class Defaults(
      protocol: OtlpProtocol,
      endpoint: Uri,
      apiPath: String,
      headers: Headers,
      timeout: FiniteDuration,
      compression: PayloadCompression
  )

  private object ConfigKeys {

    object General extends Keys("otel.exporter.otlp")
    object Metrics extends Keys("otel.exporter.otlp.metrics")
    object Traces extends Keys("otel.exporter.otlp.traces")
    object Logs extends Keys("otel.exporter.otlp.logs")

    abstract class Keys(namespace: String) {
      val Protocol: Config.Key[OtlpProtocol] =
        Config.Key(s"$namespace.protocol")
      val Endpoint: Config.Key[Uri] =
        Config.Key(s"$namespace.endpoint")
      val Headers: Config.Key[Headers] =
        Config.Key(s"$namespace.headers")
      val Compression: Config.Key[PayloadCompression] =
        Config.Key(s"$namespace.compression")
      val Timeout: Config.Key[FiniteDuration] =
        Config.Key(s"$namespace.timeout")

      val All: Set[Config.Key[_]] =
        Set(Protocol, Endpoint, Headers, Compression, Timeout)
    }
  }

  /** Autoconfigures [[OtlpClient]] using `otel.exporter.otlp.metrics.{x}` and `otel.exporter.otlp.{x}` properties.
    *
    * @param defaults
    *   the default values to use as a fallback when property is missing in the config
    */
  def metrics[F[_]: Async: Network: Compression: Console, A](
      defaults: Defaults,
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): AutoConfigure[F, OtlpClient[F, A]] =
    new OtlpClientAutoConfigure[F, A](
      ConfigKeys.Metrics,
      defaults,
      customClient,
      ConfigKeys.General.All ++ ConfigKeys.Metrics.All
    )

  /** Autoconfigures [[OtlpClient]] using `otel.exporter.otlp.traces.{x}` and `otel.exporter.otlp.{x}` properties.
    *
    * @param defaults
    *   the default values to use as a fallback when property is missing in the config
    */
  def traces[F[_]: Async: Network: Compression: Console, A](
      defaults: Defaults,
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): AutoConfigure[F, OtlpClient[F, A]] =
    new OtlpClientAutoConfigure[F, A](
      ConfigKeys.Traces,
      defaults,
      customClient,
      ConfigKeys.General.All ++ ConfigKeys.Traces.All
    )

  /** Autoconfigures [[OtlpClient]] using `otel.exporter.otlp.logs.{x}` and `otel.exporter.otlp.{x}` properties.
    *
    * @param defaults
    *   the default values to use as a fallback when property is missing in the config
    */
  def logs[F[_]: Async: Network: Compression: Console, A](
      defaults: Defaults,
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): AutoConfigure[F, OtlpClient[F, A]] =
    new OtlpClientAutoConfigure[F, A](
      ConfigKeys.Logs,
      defaults,
      customClient,
      ConfigKeys.General.All ++ ConfigKeys.Logs.All
    )

}
