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

import cats.effect.Resource
import cats.effect.Temporal
import cats.syntax.flatMap._
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter
import org.typelevel.otel4s.sdk.trace.processor.BatchSpanProcessor
import org.typelevel.otel4s.sdk.trace.processor.SpanProcessor

import scala.concurrent.duration.FiniteDuration
import scala.util.chaining._

/** Autoconfigures [[BatchSpanProcessor]].
  *
  * The configuration options:
  * {{{
  * | System property                | Environment variable           | Description                                                           |
  * |--------------------------------|--------------------------------|-----------------------------------------------------------------------|
  * | otel.bsp.schedule.delay        | OTEL_BSP_SCHEDULE_DELAY        | The interval between two consecutive exports. Default is `5 seconds`. |
  * | otel.bsp.max.queue.size        | OTEL_BSP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                            |
  * | otel.bsp.max.export.batch.size | OTEL_BSP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                             |
  * | otel.bsp.export.timeout        | OTEL_BSP_EXPORT_TIMEOUT        | The maximum allowed time to export data. Default is `30 seconds`.     |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#batch-span-processor]]
  *
  * @param exporter
  *   the exporter to use with the configured batch span processor
  */
private final class BatchSpanProcessorAutoConfigure[F[_]: Temporal: Diagnostic](
    exporter: SpanExporter[F]
) extends AutoConfigure.WithHint[F, SpanProcessor[F]](
      "BatchSpanProcessor",
      BatchSpanProcessorAutoConfigure.ConfigKeys.All
    ) {

  import BatchSpanProcessorAutoConfigure.ConfigKeys

  def fromConfig(config: Config): Resource[F, SpanProcessor[F]] = {
    def configure =
      for {
        scheduleDelay <- config.get(ConfigKeys.ScheduleDelay)
        maxQueueSize <- config.get(ConfigKeys.MaxQueueSize)
        maxExportBatchSize <- config.get(ConfigKeys.MaxExportBatchSize)
        exporterTimeout <- config.get(ConfigKeys.ExporterTimeout)
      } yield BatchSpanProcessor
        .builder(exporter)
        .pipe(b => scheduleDelay.foldLeft(b)(_.withScheduleDelay(_)))
        .pipe(b => maxQueueSize.foldLeft(b)(_.withMaxQueueSize(_)))
        .pipe(b => maxExportBatchSize.foldLeft(b)(_.withMaxExportBatchSize(_)))
        .pipe(b => exporterTimeout.foldLeft(b)(_.withExporterTimeout(_)))
        .build

    Resource.suspend(
      Temporal[F]
        .catchNonFatal(configure)
        .flatMap(e => Temporal[F].fromEither(e))
    )
  }

}

private[sdk] object BatchSpanProcessorAutoConfigure {

  private object ConfigKeys {
    val ScheduleDelay: Config.Key[FiniteDuration] =
      Config.Key("otel.bsp.schedule.delay")

    val MaxQueueSize: Config.Key[Int] =
      Config.Key("otel.bsp.max.queue.size")

    val MaxExportBatchSize: Config.Key[Int] =
      Config.Key("otel.bsp.max.export.batch.size")

    val ExporterTimeout: Config.Key[FiniteDuration] =
      Config.Key("otel.bsp.export.timeout")

    val All: Set[Config.Key[_]] =
      Set(ScheduleDelay, MaxQueueSize, MaxExportBatchSize, ExporterTimeout)
  }

  /** Autoconfigures [[BatchSpanProcessor]].
    *
    * The configuration options:
    * {{{
    * | System property                | Environment variable           | Description                                                           |
    * |--------------------------------|--------------------------------|-----------------------------------------------------------------------|
    * | otel.bsp.schedule.delay        | OTEL_BSP_SCHEDULE_DELAY        | The interval between two consecutive exports. Default is `5 seconds`. |
    * | otel.bsp.max.queue.size        | OTEL_BSP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                            |
    * | otel.bsp.max.export.batch.size | OTEL_BSP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                             |
    * | otel.bsp.export.timeout        | OTEL_BSP_EXPORT_TIMEOUT        | The maximum allowed time to export data. Default is `30 seconds`.     |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#batch-span-processor]]
    *
    * @param exporter
    *   the exporter to use with the configured batch span processor
    */
  def apply[F[_]: Temporal: Diagnostic](exporter: SpanExporter[F]): AutoConfigure[F, SpanProcessor[F]] =
    new BatchSpanProcessorAutoConfigure[F](exporter)

}
