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

import cats.Parallel
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.autoconfigure.ConfigurationError
import org.typelevel.otel4s.sdk.trace.SdkTracerProvider
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.processor.BatchSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SimpleSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor

private[sdk] object TracerProviderConfiguration {

  def configure[F[_]: Temporal: Parallel: Console](
      builder: SdkTracerProvider.Builder[F],
      config: Config
  ): Resource[F, SdkTracerProvider.Builder[F]] =
    for {
      sampler <- Resource.eval(
        Temporal[F].fromEither(SamplerConfiguration.configure(config))
      )
      exporters <- SpanExporterConfiguration.configure[F](config)
      processors <- configureProcessors(config, exporters)
      withSampler = builder.withSampler(sampler)
    } yield processors.foldLeft(withSampler)(_.addSpanProcessor(_))

  private def configureProcessors[F[_]: Temporal: Parallel: Console](
      config: Config,
      exporters: Map[String, SpanExporter[F]]
  ): Resource[F, List[SpanProcessor[F]]] = {
    val logging = exporters.get("logging") match {
      case Some(logging) => List(SimpleSpanProcessor(logging))
      case None          => Nil
    }

    val others = exporters.removed("logging")
    if (others.nonEmpty) {
      val exporter = others.values.toList.combineAll
      for {
        processor <- configureBatchSpanProcessor(config, exporter)
      } yield logging :+ processor
    } else {
      Resource.pure(logging)
    }
  }

  private def configureBatchSpanProcessor[F[_]: Temporal](
      config: Config,
      exporter: SpanExporter[F]
  ): Resource[F, SpanProcessor[F]] = {
    def configure: Either[ConfigurationError, BatchSpanProcessor.Builder[F]] =
      for {
        scheduleDelay <- config.getFiniteDuration("otel.bsp.schedule.delay")
        maxQueueSize <- config.getInt("otel.bsp.max.queue.size")
        maxExportBatchSize <- config.getInt(
          "otel.bsp.map.export.batch.size"
        )
        exporterTimeout <- config.getFiniteDuration("otel.bsp.export.timeout")
      } yield {
        val builder = BatchSpanProcessor.builder(exporter)

        val withScheduleDelay =
          scheduleDelay.foldLeft(builder)(_.withScheduleDelay(_))

        val withMaxQueueSize =
          maxQueueSize.foldLeft(withScheduleDelay)(_.withMaxQueueSize(_))

        val withMaxExportBatchSize =
          maxExportBatchSize.foldLeft(withMaxQueueSize)(
            _.withMaxExportBatchSize(_)
          )

        val withExporterTimeout =
          exporterTimeout.foldLeft(withMaxExportBatchSize)(
            _.withExporterTimeout(_)
          )

        withExporterTimeout
      }

    for {
      builder <- Resource.eval(Temporal[F].fromEither(configure))
      processor <- builder.build
    } yield processor
  }

}
