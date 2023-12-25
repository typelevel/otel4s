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
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.exporter.LoggingSpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

private[sdk] object SpanExporterConfiguration {

  def configure[F[_]: MonadThrow: Console](
      config: Config
  ): Resource[F, Map[String, SpanExporter[F]]] = {
    val set = config.getStringSet("otel.traces.exporter")
    Resource.eval(MonadThrow[F].fromEither(set)).flatMap {
      case names if names.contains("none") && names.sizeIs > 1 =>
        Resource.eval(
          MonadThrow[F].raiseError(
            new ConfigurationError(
              "[otel.traces.exporter] contains 'none' along with other exporters",
              None
            )
          )
        )

      case exporterNames if exporterNames.contains("none") =>
        Resource.pure(Map("none" -> SpanExporter.noop[F]))

      case exporterNames =>
        val names = NonEmptyList
          .fromList(exporterNames.toList)
          .getOrElse(NonEmptyList.one("otlp"))

        names
          .traverse(name => create[F](name).tupleLeft(name))
          .map(_.toList.toMap)
    }
  }

  private def create[F[_]: MonadThrow: Console](
      name: String
  ): Resource[F, SpanExporter[F]] =
    name match {
      // case "otlp" => todo use OtlpExporter

      case "logging" =>
        Resource.eval(MonadThrow[F].pure(LoggingSpanExporter[F]))

      case other =>
        Resource.eval(
          MonadThrow[F].raiseError(
            ConfigurationError.unrecognized("otel.traces.exporter", other)
          )
        )
    }

}
