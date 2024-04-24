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

import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader

import scala.concurrent.duration._

/** Autoconfigures
  * [[org.typelevel.otel4s.sdk.metrics.exporter.MetricReader MetricReader]]s.
  *
  * The configuration options:
  * {{{
  * | System property             | Environment variable        | Description                                                                        |
  * |-----------------------------|-----------------------------|------------------------------------------------------------------------------------|
  * | otel.metric.export.interval | OTEL_METRIC_EXPORT_INTERVAL | The time interval between the start of two export attempts. Default is `1 minute`. |
  * | otel.metric.export.timeout  | OTEL_METRIC_EXPORT_TIMEOUT  | Maximum allowed time to export data. Default is `30 seconds`.                      |
  * }}}
  *
  * @see
  *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#periodic-exporting-metricreader]]
  *
  * @param exporters
  *   the exporters to use with metric readers
  */
private final class MetricReadersAutoConfigure[F[_]: Temporal: Console](
    exporters: Set[MetricExporter[F]]
) extends AutoConfigure.WithHint[F, Vector[MetricReader[F]]](
      "MetricReaders",
      MetricReadersAutoConfigure.ConfigKeys.All
    ) {

  import MetricReadersAutoConfigure.ConfigKeys
  import MetricReadersAutoConfigure.Defaults

  protected def fromConfig(
      config: Config
  ): Resource[F, Vector[MetricReader[F]]] = {
    val (pull, push) = exporters.toVector.partitionMap {
      case push: MetricExporter.Push[F] => Right(push)
      case pull: MetricExporter.Pull[F] => Left(pull)
    }

    for {
      pushReaders <- configurePush(config, push)
      pullReaders <- configurePull(pull)
    } yield pushReaders ++ pullReaders
  }

  private def configurePush(
      config: Config,
      exporters: Vector[MetricExporter.Push[F]]
  ): Resource[F, Vector[MetricReader[F]]] =
    for {
      interval <- Resource.eval(
        Temporal[F].fromEither(
          config.getOrElse(ConfigKeys.ExportInterval, Defaults.ExportInterval)
        )
      )

      timeout <- Resource.eval(
        Temporal[F].fromEither(
          config.getOrElse(ConfigKeys.ExportTimeout, Defaults.ExportTimeout)
        )
      )

      readers <- exporters.traverse { exporter =>
        MetricReader.periodic(exporter, interval, timeout)
      }
    } yield readers

  private def configurePull(
      exporters: Vector[MetricExporter.Pull[F]]
  ): Resource[F, Vector[MetricReader[F]]] =
    Resource
      .eval(
        exporters.traverse_ { exporter =>
          Console[F].error(
            s"The pull-based exporter [$exporter] is not supported yet"
          )
        }
      )
      .as(Vector.empty)

}

object MetricReadersAutoConfigure {

  private object ConfigKeys {
    val ExportInterval: Config.Key[FiniteDuration] =
      Config.Key("otel.metric.export.interval")

    val ExportTimeout: Config.Key[FiniteDuration] =
      Config.Key("otel.metric.export.timeout")

    val All: Set[Config.Key[_]] = Set(ExportInterval, ExportTimeout)
  }

  // see https://opentelemetry.io/docs/specs/otel/configuration/sdk-environment-variables/#periodic-exporting-metricreader
  private object Defaults {
    val ExportInterval: FiniteDuration = 1.minute
    val ExportTimeout: FiniteDuration = 30.seconds
  }

  /** Autoconfigures
    * [[org.typelevel.otel4s.sdk.metrics.exporter.MetricReader MetricReader]]s.
    *
    * The configuration options:
    * {{{
    * | System property             | Environment variable        | Description                                                                        |
    * |-----------------------------|-----------------------------|------------------------------------------------------------------------------------|
    * | otel.metric.export.interval | OTEL_METRIC_EXPORT_INTERVAL | The time interval between the start of two export attempts. Default is `1 minute`. |
    * | otel.metric.export.timeout  | OTEL_METRIC_EXPORT_TIMEOUT  | Maximum allowed time to export data. Default is `30 seconds`.                      |
    * }}}
    *
    * @see
    *   [[https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/configuration/sdk-environment-variables.md#periodic-exporting-metricreader]]
    *
    * @param exporters
    *   the exporters to use with metric readers
    */
  def apply[F[_]: Temporal: Console](
      exporters: Set[MetricExporter[F]]
  ): AutoConfigure[F, Vector[MetricReader[F]]] =
    new MetricReadersAutoConfigure[F](exporters)

}
