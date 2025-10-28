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

import cats.effect.Resource
import cats.effect.Temporal
import cats.syntax.flatMap._
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.autoconfigure.AutoConfigure
import org.typelevel.otel4s.sdk.autoconfigure.Config
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter
import org.typelevel.otel4s.sdk.logs.processor.BatchLogRecordProcessor
import org.typelevel.otel4s.sdk.logs.processor.LogRecordProcessor

import scala.concurrent.duration.FiniteDuration
import scala.util.chaining._

/** Autoconfigures [[BatchLogRecordProcessor]].
  *
  * The configuration options:
  * {{{
  * | System property                 | Environment variable            | Description                                                           |
  * |---------------------------------|---------------------------------|-----------------------------------------------------------------------|
  * | otel.blrp.schedule.delay        | OTEL_BLRP_SCHEDULE_DELAY        | The interval between two consecutive exports. Default is `5 seconds`. |
  * | otel.blrp.max.queue.size        | OTEL_BLRP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                            |
  * | otel.blrp.max.export.batch.size | OTEL_BLRP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                             |
  * | otel.blrp.export.timeout        | OTEL_BLRP_EXPORT_TIMEOUT        | The maximum allowed time to export data. Default is `30 seconds`.     |
  * }}}
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#properties-logs]]
  *
  * @param exporter
  *   the exporter to use with the configured batch log record processor
  */
private final class BatchLogRecordProcessorAutoConfigure[F[_]: Temporal: Diagnostic](
    exporter: LogRecordExporter[F]
) extends AutoConfigure.WithHint[F, LogRecordProcessor[F]](
      "BatchLogRecordProcessor",
      BatchLogRecordProcessorAutoConfigure.ConfigKeys.All
    ) {

  import BatchLogRecordProcessorAutoConfigure.ConfigKeys

  def fromConfig(config: Config): Resource[F, LogRecordProcessor[F]] = {
    def configure =
      for {
        scheduleDelay <- config.get(ConfigKeys.ScheduleDelay)
        maxQueueSize <- config.get(ConfigKeys.MaxQueueSize)
        maxExportBatchSize <- config.get(ConfigKeys.MaxExportBatchSize)
        exporterTimeout <- config.get(ConfigKeys.ExporterTimeout)
      } yield BatchLogRecordProcessor
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

private[sdk] object BatchLogRecordProcessorAutoConfigure {

  private object ConfigKeys {
    val ScheduleDelay: Config.Key[FiniteDuration] =
      Config.Key("otel.blrp.schedule.delay")

    val MaxQueueSize: Config.Key[Int] =
      Config.Key("otel.blrp.max.queue.size")

    val MaxExportBatchSize: Config.Key[Int] =
      Config.Key("otel.blrp.max.export.batch.size")

    val ExporterTimeout: Config.Key[FiniteDuration] =
      Config.Key("otel.blrp.export.timeout")

    val All: Set[Config.Key[_]] =
      Set(ScheduleDelay, MaxQueueSize, MaxExportBatchSize, ExporterTimeout)
  }

  /** Autoconfigures [[BatchLogRecordProcessor]].
    *
    * The configuration options:
    * {{{
    * | System property                 | Environment variable            | Description                                                           |
    * |---------------------------------|---------------------------------|-----------------------------------------------------------------------|
    * | otel.blrp.schedule.delay        | OTEL_BLRP_SCHEDULE_DELAY        | The interval between two consecutive exports. Default is `5 seconds`. |
    * | otel.blrp.max.queue.size        | OTEL_BLRP_MAX_QUEUE_SIZE        | The maximum queue size. Default is `2048`.                            |
    * | otel.blrp.max.export.batch.size | OTEL_BLRP_MAX_EXPORT_BATCH_SIZE | The maximum batch size. Default is `512`.                             |
    * | otel.blrp.export.timeout        | OTEL_BLRP_EXPORT_TIMEOUT        | The maximum allowed time to export data. Default is `30 seconds`.     |
    * }}}
    *
    * @see
    *   [[https://opentelemetry.io/docs/languages/java/configuration/#properties-logs]]
    *
    * @param exporter
    *   the exporter to use with the configured batch log record processor
    */
  def apply[F[_]: Temporal: Diagnostic](exporter: LogRecordExporter[F]): AutoConfigure[F, LogRecordProcessor[F]] =
    new BatchLogRecordProcessorAutoConfigure[F](exporter)

}
