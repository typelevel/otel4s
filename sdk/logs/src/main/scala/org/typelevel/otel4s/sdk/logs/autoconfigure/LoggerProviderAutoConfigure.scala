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

import cats.Parallel
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.all._
import org.typelevel.otel4s.logs.LoggerProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.logs.SdkLoggerProvider
import org.typelevel.otel4s.sdk.logs.autoconfigure.LoggerProviderAutoConfigure.Customizer
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor
import org.typelevel.otel4s.sdk.logs.processor.SimpleLogRecordProcessor

/** Autoconfigures [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]].
  *
  * @param resource
  *   the resource to use
  *
  * @param traceContextLookup
  *   used by the log builder to extract tracing information from the context
  *
  * @param customizer
  *   the function to customize the builder
  *
  * @param exporterConfigurers
  *   the extra exporter configurers
  */
private final class LoggerProviderAutoConfigure[F[_]: Temporal: Parallel: Console: Diagnostic: AskContext](
    resource: TelemetryResource,
    traceContextLookup: TraceContext.Lookup,
    customizer: Customizer[SdkLoggerProvider.Builder[F]],
    exporterConfigurers: Set[AutoConfigure.Named[F, LogRecordExporter[F]]]
) extends AutoConfigure.WithHint[F, LoggerProvider[F, Context]](
      "LoggerProvider",
      Set.empty
    ) {

  protected def fromConfig(config: Config): Resource[F, LoggerProvider[F, Context]] =
    for {
      exporters <- LogRecordExportersAutoConfigure[F](exporterConfigurers).configure(config)
      processors <- configureProcessors(config, exporters)
      logLimits <- LogRecordLimitsAutoConfigure[F].configure(config)
      loggerProviderBuilder = {
        val builder = SdkLoggerProvider
          .builder[F]
          .withResource(resource)
          .withLogRecordLimits(logLimits)
          .withTraceContextLookup(traceContextLookup)

        processors.foldLeft(builder)(_.addLogRecordProcessor(_))
      }

      loggerProvider <- Resource.eval(
        customizer(loggerProviderBuilder, config).build
      )
    } yield loggerProvider

  private def configureProcessors(
      config: Config,
      exporters: Map[String, LogRecordExporter[F]]
  ): Resource[F, List[LogRecordProcessor[F]]] = {
    val noneExporter = LogRecordExportersAutoConfigure.Const.NoneExporter
    val consoleExporter = LogRecordExportersAutoConfigure.Const.ConsoleExporter

    val console = exporters.get(consoleExporter) match {
      case Some(console) => List(SimpleLogRecordProcessor(console))
      case None          => Nil
    }

    val others = exporters.removed(consoleExporter).removed(noneExporter)
    if (others.nonEmpty) {
      val exporter = others.values.toList.combineAll
      BatchLogRecordProcessorAutoConfigure[F](exporter)
        .configure(config)
        .map(processor => console :+ processor)
    } else {
      Resource.pure(console)
    }
  }
}

private[sdk] object LoggerProviderAutoConfigure {

  type Customizer[A] = (A, Config) => A

  /** Autoconfigures [[org.typelevel.otel4s.logs.LoggerProvider LoggerProvider]].
    *
    * @param resource
    *   the resource to use
    *
    * @param traceContextLookup
    *   used by the log builder to extract tracing information from the context
    *
    * @param loggerProviderBuilderCustomizer
    *   the function to customize the builder
    *
    * @param exporterConfigurers
    *   the extra exporter configurers
    */
  def apply[F[_]: Temporal: Parallel: Console: Diagnostic: AskContext](
      resource: TelemetryResource,
      traceContextLookup: TraceContext.Lookup,
      loggerProviderBuilderCustomizer: Customizer[SdkLoggerProvider.Builder[F]],
      exporterConfigurers: Set[AutoConfigure.Named[F, LogRecordExporter[F]]]
  ): AutoConfigure[F, LoggerProvider[F, Context]] =
    new LoggerProviderAutoConfigure[F](
      resource,
      traceContextLookup,
      loggerProviderBuilderCustomizer,
      exporterConfigurers
    )
}
