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

package org.typelevel.otel4s.sdk.logs.autoconfigure

import cats.data.NonEmptyList
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.logs.exporter.ConsoleLogRecordExporter
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

/** Autoconfigures [[LogRecordExporter]]s.
  *
  * The configuration options:
  * {{{
  * | System property    | Environment variable | Description                                                                                 |
  * |--------------------|----------------------|---------------------------------------------------------------------------------------------|
  * | otel.logs.exporter | OTEL_LOGS_EXPORTER   | The exporters to use. Use a comma-separated list for multiple exporters. Default is `otlp`. |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#properties-exporters]]
  */
private final class LogRecordExportersAutoConfigure[F[_]: MonadCancelThrow: Console: Diagnostic](
    extra: Set[AutoConfigure.Named[F, LogRecordExporter[F]]]
) extends AutoConfigure.WithHint[F, Map[String, LogRecordExporter[F]]](
      "LogRecordExporters",
      LogRecordExportersAutoConfigure.ConfigKeys.All
    ) {

  import LogRecordExportersAutoConfigure.ConfigKeys
  import LogRecordExportersAutoConfigure.Const

  private val configurers = {
    val default: Set[AutoConfigure.Named[F, LogRecordExporter[F]]] = Set(
      AutoConfigure.Named.const(Const.NoneExporter, LogRecordExporter.noop[F]),
      AutoConfigure.Named.const(Const.ConsoleExporter, ConsoleLogRecordExporter[F])
    )

    default ++ extra
  }

  def fromConfig(config: Config): Resource[F, Map[String, LogRecordExporter[F]]] = {
    val values = config.getOrElse(ConfigKeys.Exporter, Set.empty[String])
    Resource.eval(MonadCancelThrow[F].fromEither(values)).flatMap {
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

  private def create(name: String, cfg: Config): Resource[F, LogRecordExporter[F]] =
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
    Diagnostic[F].error(
      s"""The configurer for the [${Const.OtlpExporter}] exporter is not registered.
         |
         |Add the `otel4s-sdk-exporter` dependency to the build file:
         |
         |libraryDependencies += "org.typelevel" %%% "otel4s-sdk-exporter" % "x.x.x"
         |
         |and register the configurer via OpenTelemetrySdk:
         |
         |import org.typelevel.otel4s.sdk.OpenTelemetrySdk
         |import org.typelevel.otel4s.sdk.exporter.otlp.autoconfigure.OtlpExportersAutoConfigure
         |
         |OpenTelemetrySdk.autoConfigured[IO](
         |  _.addExportersConfigurer(OtlpExportersAutoConfigure[IO])
         |)
         |
         |or via SdkLogs:
         |
         |import org.typelevel.otel4s.sdk.logs.SdkLogs
         |import org.typelevel.otel4s.sdk.exporter.otlp.logs.autoconfigure.OtlpLogRecordExporterAutoConfigure
         |
         |SdkLogs.autoConfigured[IO](
         |  _.addExporterConfigurer(OtlpLogRecordExporterAutoConfigure[IO])
         |)
         |""".stripMargin
    )
  }
}

private[sdk] object LogRecordExportersAutoConfigure {

  private object ConfigKeys {
    val Exporter: Config.Key[Set[String]] = Config.Key("otel.logs.exporter")

    val All: Set[Config.Key[_]] = Set(Exporter)
  }

  private[logs] object Const {
    val OtlpExporter = "otlp"
    val NoneExporter = "none"
    val ConsoleExporter = "console"
  }

  /** Autoconfigures [[LogRecordExporter]]s.
    *
    * The configuration options:
    * {{{
    * | System property    | Environment variable | Description                                                                              |
    * |--------------------|----------------------|------------------------------------------------------------------------------------------|
    * | otel.logs.exporter | OTEL_LOGS_EXPORTER   | The exporters to use. Use a comma-separated list for multiple exporters. Default is `otlp`. |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#properties-exporters]]
    *
    * @param configurers
    *   the configurers to use
    */
  def apply[F[_]: MonadCancelThrow: Console: Diagnostic](
      configurers: Set[AutoConfigure.Named[F, LogRecordExporter[F]]]
  ): AutoConfigure[F, Map[String, LogRecordExporter[F]]] =
    new LogRecordExportersAutoConfigure[F](configurers)

}
