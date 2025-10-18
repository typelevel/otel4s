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

package org.typelevel.otel4s.sdk.metrics.autoconfigure

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
import org.typelevel.otel4s.sdk.metrics.exporter.ConsoleMetricExporter
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

/** Autoconfigures [[MetricExporter]]s.
  *
  * The configuration options:
  * {{{
  * | System property       | Environment variable  | Description                                                                                 |
  * |-----------------------|-----------------------|---------------------------------------------------------------------------------------------|
  * | otel.metrics.exporter | OTEL_METRICS_EXPORTER | The exporters to use. Use a comma-separated list for multiple exporters. Default is `otlp`. |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#metric-exporters]]
  */
private final class MetricExportersAutoConfigure[F[_]: MonadCancelThrow: Console: Diagnostic](
    extra: Set[AutoConfigure.Named[F, MetricExporter[F]]]
) extends AutoConfigure.WithHint[F, Map[String, MetricExporter[F]]](
      "MetricExporters",
      MetricExportersAutoConfigure.ConfigKeys.All
    ) {

  import MetricExportersAutoConfigure.ConfigKeys
  import MetricExportersAutoConfigure.Const

  private val configurers = {
    val default: Set[AutoConfigure.Named[F, MetricExporter[F]]] = Set(
      AutoConfigure.Named.const(Const.NoneExporter, MetricExporter.noop[F]),
      AutoConfigure.Named.const(Const.ConsoleExporter, ConsoleMetricExporter[F])
    )

    default ++ extra
  }

  def fromConfig(
      config: Config
  ): Resource[F, Map[String, MetricExporter[F]]] = {
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

  private def create(
      name: String,
      cfg: Config
  ): Resource[F, MetricExporter[F]] =
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
         |or via SdkMetrics:
         |
         |import org.typelevel.otel4s.sdk.metrics.SdkMetrics
         |import org.typelevel.otel4s.sdk.exporter.otlp.metrics.autoconfigure.OtlpMetricExporterAutoConfigure
         |
         |SdkMetrics.autoConfigured[IO](
         |  _.addExporterConfigurer(OtlpMetricExporterAutoConfigure[IO])
         |)
         |""".stripMargin
    )
  }

}

private[sdk] object MetricExportersAutoConfigure {

  private object ConfigKeys {
    val Exporter: Config.Key[Set[String]] = Config.Key("otel.metrics.exporter")

    val All: Set[Config.Key[_]] = Set(Exporter)
  }

  private[metrics] object Const {
    val OtlpExporter = "otlp"
    val NoneExporter = "none"
    val ConsoleExporter = "console"
  }

  /** Autoconfigures [[MetricExporter]]s.
    *
    * The configuration options:
    * {{{
    * | System property       | Environment variable  | Description                                                                                 |
    * |-----------------------|-----------------------|---------------------------------------------------------------------------------------------|
    * | otel.metrics.exporter | OTEL_METRICS_EXPORTER | The exporters to use. Use a comma-separated list for multiple exporters. Default is `otlp`. |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#metric-exporters]]
    *
    * @param configurers
    *   the configurers to use
    */
  def apply[F[_]: MonadCancelThrow: Console: Diagnostic](
      configurers: Set[AutoConfigure.Named[F, MetricExporter[F]]]
  ): AutoConfigure[F, Map[String, MetricExporter[F]]] =
    new MetricExportersAutoConfigure[F](configurers)

}
