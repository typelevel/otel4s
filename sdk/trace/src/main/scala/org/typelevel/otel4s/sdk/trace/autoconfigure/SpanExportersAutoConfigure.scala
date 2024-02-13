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

package org.typelevel.otel4s.sdk.trace.autoconfigure

import cats.MonadThrow
import cats.data.NonEmptyList
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.exporter.LoggingSpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

/** Autoconfigures [[SpanExporter]]s.
  *
  * The configuration options:
  * {{{
  * | System property      | Environment variable | Description                                                                                   |
  * |----------------------|----------------------|-----------------------------------------------------------------------------------------------|
  * | otel.traces.exporter | OTEL_TRACES_EXPORTER | The exporters to use. Use a comma-separated list for multiple propagators. Default is `otlp`. |
  * }}}
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#propagator]]
  */
private final class SpanExportersAutoConfigure[F[_]: MonadThrow: Console](
    extra: Set[AutoConfigure.Named[F, SpanExporter[F]]]
) extends AutoConfigure.WithHint[F, Map[String, SpanExporter[F]]](
      "SpanExporters",
      SpanExportersAutoConfigure.ConfigKeys.All
    ) {

  import SpanExportersAutoConfigure.ConfigKeys
  import SpanExportersAutoConfigure.Const

  private val configurers = {
    val default: Set[AutoConfigure.Named[F, SpanExporter[F]]] = Set(
      AutoConfigure.Named.const(Const.NoneExporter, SpanExporter.noop[F]),
      AutoConfigure.Named.const(Const.LoggingExporter, LoggingSpanExporter[F])
    )

    default ++ extra
  }

  def fromConfig(config: Config): Resource[F, Map[String, SpanExporter[F]]] = {
    val values = config.getOrElse(ConfigKeys.Exporter, Set.empty[String])
    Resource.eval(MonadThrow[F].fromEither(values)).flatMap {
      case names if names.contains(Const.NoneExporter) && names.sizeIs > 1 =>
        Resource.raiseError(
          ConfigurationError(
            s"[${ConfigKeys.Exporter}] contains '${Const.NoneExporter}' along with other exporters"
          ): Throwable
        )

      case exporterNames =>
        val names = NonEmptyList
          .fromList(exporterNames.toList)
          .getOrElse(NonEmptyList.one(Const.OtlpExporter))

        names
          .traverse(name => create(name, config).tupleLeft(name))
          .map(_.toList.toMap)
    }
  }

  private def create(name: String, cfg: Config): Resource[F, SpanExporter[F]] =
    configurers.find(_.name == name) match {
      case Some(configure) =>
        configure.configure(cfg)

      case None =>
        Resource.eval(otlpMissingWarning.whenA(name == Const.OtlpExporter)) >>
          Resource.raiseError(
            ConfigurationError.unrecognized(
              ConfigKeys.Exporter.name,
              name,
              configurers.map(_.name)
            ): Throwable
          )
    }

  private def otlpMissingWarning: F[Unit] = {
    Console[F].errorln(
      s"""The configurer for the [${Const.OtlpExporter}] exporter is not registered.
        |
        |Add the `otel4s-sdk-exporter` dependency to the build file:
        |
        |libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
        |
        |and register the configurer:
        |
        |import org.typelevel.otel4s.sdk.OpenTelemetrySdk
        |import org.typelevel.otel4s.sdk.exporter.otlp.trace.autoconfigure.OtlpSpanExporterAutoConfigure
        |
        |OpenTelemetrySdk.autoConfigured[IO](
        |  _.addExporterConfigurer(OtlpSpanExporterAutoConfigure[IO])
        |)
        |""".stripMargin
    )
  }

}

private[sdk] object SpanExportersAutoConfigure {

  private object ConfigKeys {
    val Exporter: Config.Key[Set[String]] = Config.Key("otel.traces.exporter")

    val All: Set[Config.Key[_]] = Set(Exporter)
  }

  private[trace] object Const {
    val OtlpExporter = "otlp"
    val NoneExporter = "none"
    val LoggingExporter = "logging"
  }

  /** Autoconfigures [[SpanExporter]]s.
    *
    * The configuration options:
    * {{{
    * | System property      | Environment variable | Description                                                                                   |
    * |----------------------|----------------------|-----------------------------------------------------------------------------------------------|
    * | otel.traces.exporter | OTEL_TRACES_EXPORTER | The exporters be use. Use a comma-separated list for multiple propagators. Default is `otlp`. |
    * }}}
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-java/blob/main/sdk-extensions/autoconfigure/README.md#span-exporters]]
    *
    * @param configurers
    *   the configurers to use
    */
  def apply[F[_]: MonadThrow: Console](
      configurers: Set[AutoConfigure.Named[F, SpanExporter[F]]]
  ): AutoConfigure[F, Map[String, SpanExporter[F]]] =
    new SpanExportersAutoConfigure[F](configurers)

}
