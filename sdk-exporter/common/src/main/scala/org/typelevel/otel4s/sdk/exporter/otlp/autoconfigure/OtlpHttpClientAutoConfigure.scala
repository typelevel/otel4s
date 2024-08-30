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

/** Autoconfigures [[OtlpHttpClient]].
  *
  * Target-specific properties are prioritized. E.g.
  * `otel.exporter.otlp.traces.endpoint` is prioritized over
  * `otel.exporter.otlp.endpoint`.
  *
  * The general configuration options:
  * {{{
  * | System property                       | Environment variable                  | Description                                                                                                                                                                      |
  * |---------------------------------------|---------------------------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.endpoint           | OTEL_EXPORTER_OTLP_ENDPOINT           | The OTLP traces, metrics, and logs endpoint to connect to. Must be a URL with a scheme of either `http` or `https` based on the use of TLS. Default is `http://localhost:4318/`. |
  * | otel.exporter.otlp.headers            | OTEL_EXPORTER_OTLP_HEADERS            | Key-value pairs separated by commas to pass as request headers on OTLP trace, metric, and log requests.                                                                          |
  * | otel.exporter.otlp.compression        | OTEL_EXPORTER_OTLP_COMPRESSION        | The compression type to use on OTLP trace, metric, and log requests. Options include `gzip`. By default no compression will be used.                                             |
  * | otel.exporter.otlp.timeout            | OTEL_EXPORTER_OTLP_TIMEOUT            | The maximum waiting time to send each OTLP trace, metric, and log batch. Default is `10 seconds`.                                                                                |
  * }}}
  *
  * The traces-specific configuration options:
  * {{{
  * | System property                       | Environment variable                  | Description                                                                                                         |
  * |---------------------------------------|---------------------------------------|---------------------------------------------------------------------------------------------------------------------|
  * | otel.exporter.otlp.traces.headers     | OTEL_EXPORTER_OTLP_TRACES_HEADERS     | Key-value pairs separated by commas to pass as request headers on OTLP trace requests.                              |
  * | otel.exporter.otlp.traces.endpoint    | OTEL_EXPORTER_OTLP_TRACES_ENDPOINT    | The OTLP traces endpoint to connect to. Default is `http://localhost:4318/v1/traces`.                               |
  * | otel.exporter.otlp.traces.compression | OTEL_EXPORTER_OTLP_TRACES_COMPRESSION | The compression type to use on OTLP trace requests. Options include `gzip`. By default no compression will be used. |
  * | otel.exporter.otlp.traces.timeout     | OTEL_EXPORTER_OTLP_TRACES_TIMEOUT     | The maximum waiting time to send each OTLP trace batch. Default is `10 seconds`.                                    |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#otlp-exporter-span-metric-and-log-exporters]]
  */
private final class OtlpHttpClientAutoConfigure[
    F[_]: Async: Network: Compression: Console,
    A
](
    specific: OtlpHttpClientAutoConfigure.ConfigKeys.Keys,
    defaults: OtlpHttpClientAutoConfigure.Defaults,
    customClient: Option[Client[F]],
    configKeys: Set[Config.Key[_]]
)(implicit encoder: ProtoEncoder.Message[List[A]], printer: Printer)
    extends AutoConfigure.WithHint[F, OtlpHttpClient[F, A]](
      "OtlpHttpClient",
      configKeys
    ) {

  import OtlpHttpClientAutoConfigure.{ConfigKeys, Defaults, PayloadCompression}

  protected def fromConfig(
      config: Config
  ): Resource[F, OtlpHttpClient[F, A]] = {

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
        endpoint <- getEndpoint
        timeout <- getOrElse(_.Timeout, _.timeout)
        headers <- getOrElse(_.Headers, _.headers)
        compression <- get(_.Compression)
      } yield OtlpHttpClient.create(
        defaults.encoding,
        endpoint,
        timeout,
        headers,
        compression.isDefined,
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

  private implicit val compressionReader: Config.Reader[PayloadCompression] =
    Config.Reader.decodeWithHint("Compression") { s =>
      s.trim.toLowerCase match {
        case "gzip" =>
          Right(PayloadCompression.Gzip)

        case _ =>
          Left(
            ConfigurationError(
              "Unrecognized compression. Supported options [gzip]"
            )
          )
      }
    }

  private implicit val headersReader: Config.Reader[Headers] =
    Config.Reader[Map[String, String]].map { value =>
      val headers = value.map { case (key, value) =>
        Header.Raw(CIString(key), value)
      }
      new Headers(headers.toList)
    }
}

private[exporter] object OtlpHttpClientAutoConfigure {

  private sealed trait PayloadCompression
  private object PayloadCompression {
    case object Gzip extends PayloadCompression
  }

  final case class Defaults(
      endpoint: Uri,
      apiPath: String,
      headers: Headers,
      timeout: FiniteDuration,
      encoding: HttpPayloadEncoding
  )

  private object ConfigKeys {

    object General extends Keys("otel.exporter.otlp")
    object Metrics extends Keys("otel.exporter.otlp.metrics")
    object Traces extends Keys("otel.exporter.otlp.traces")

    abstract class Keys(namespace: String) {
      val Endpoint: Config.Key[Uri] =
        Config.Key(s"$namespace.endpoint")
      val Headers: Config.Key[Headers] =
        Config.Key(s"$namespace.headers")
      val Compression: Config.Key[PayloadCompression] =
        Config.Key(s"$namespace.compression")
      val Timeout: Config.Key[FiniteDuration] =
        Config.Key(s"$namespace.timeout")

      val All: Set[Config.Key[_]] = Set(Endpoint, Headers, Compression, Timeout)
    }
  }

  /** Autoconfigures [[OtlpHttpClient]] using `otel.exporter.otlp.metrics.{x}`
    * and `otel.exporter.otlp.{x}` properties.
    *
    * @param defaults
    *   the default values to use as a fallback when property is missing in the
    *   config
    */
  def metrics[F[_]: Async: Network: Compression: Console, A](
      defaults: Defaults,
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): AutoConfigure[F, OtlpHttpClient[F, A]] =
    new OtlpHttpClientAutoConfigure[F, A](
      ConfigKeys.Metrics,
      defaults,
      customClient,
      ConfigKeys.General.All ++ ConfigKeys.Metrics.All
    )

  /** Autoconfigures [[OtlpHttpClient]] using `otel.exporter.otlp.traces.{x}`
    * and `otel.exporter.otlp.{x}` properties.
    *
    * @param defaults
    *   the default values to use as a fallback when property is missing in the
    *   config
    */
  def traces[F[_]: Async: Network: Compression: Console, A](
      defaults: Defaults,
      customClient: Option[Client[F]]
  )(implicit
      encoder: ProtoEncoder.Message[List[A]],
      printer: Printer
  ): AutoConfigure[F, OtlpHttpClient[F, A]] =
    new OtlpHttpClientAutoConfigure[F, A](
      ConfigKeys.Traces,
      defaults,
      customClient,
      ConfigKeys.General.All ++ ConfigKeys.Traces.All
    )

}
