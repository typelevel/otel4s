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
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.exporter.LoggingSpanExporter
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

private final class SpanExportersAutoConfigure[F[_]: MonadThrow: Console](
    extra: Set[AutoConfigure.Named[F, SpanExporter[F]]]
) extends AutoConfigure.WithHint[F, Map[String, SpanExporter[F]]](
      "SpanExporter",
      SpanExportersAutoConfigure.ConfigKeys.All
    ) {

  import SpanExportersAutoConfigure.ConfigKeys

  private val configurers = {
    val default: Set[AutoConfigure.Named[F, SpanExporter[F]]] = Set(
      constConfigure("logging", LoggingSpanExporter[F])
    )

    default ++ extra
  }

  def fromConfig(config: Config): Resource[F, Map[String, SpanExporter[F]]] = {
    val values = config.getOrElse(ConfigKeys.Exporter, Set.empty[String])
    Resource.eval(MonadThrow[F].fromEither(values)).flatMap {
      case names if names.contains("none") && names.sizeIs > 1 =>
        Resource.raiseError(
          ConfigurationError(
            s"[${ConfigKeys.Exporter.name}] contains 'none' along with other exporters"
          ): Throwable
        )

      case exporterNames if exporterNames.contains("none") =>
        Resource.pure(Map("none" -> SpanExporter.noop[F]))

      case exporterNames =>
        val names = NonEmptyList
          .fromList(exporterNames.toList)
          .getOrElse(NonEmptyList.one("otlp"))

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
        Resource.raiseError(
          ConfigurationError.unrecognized(
            ConfigKeys.Exporter.name,
            name,
            configurers.map(_.name)
          ): Throwable
        )
    }

  private def constConfigure[A](
      n: String,
      component: A
  ): AutoConfigure.Named[F, A] =
    new AutoConfigure.Named[F, A] {
      def name: String = n
      def configure(config: Config): Resource[F, A] =
        Resource.pure(component)
    }
}

private[sdk] object SpanExportersAutoConfigure {

  private object ConfigKeys {
    val Exporter: Config.Key[Set[String]] = Config.Key("otel.traces.exporter")

    val All: Set[Config.Key[_]] = Set(Exporter)
  }

  def apply[F[_]: MonadThrow: Console](
      configurers: Set[AutoConfigure.Named[F, SpanExporter[F]]]
  ): AutoConfigure[F, Map[String, SpanExporter[F]]] =
    new SpanExportersAutoConfigure[F](configurers)

}
