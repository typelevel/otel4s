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

package org.typelevel.otel4s.sdk.logs.processor

import cats.effect.Ref
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.CountDownLatch
import cats.effect.std.Queue
import cats.effect.syntax.all._
import cats.syntax.all._
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.logs.LogRecordRef
import org.typelevel.otel4s.sdk.logs.data.LogRecordData
import org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter

import scala.concurrent.duration._

/** Implementation of the [[LogRecordProcessor]] that batches logs exported by the SDK then pushes them to the exporter
  * pipeline.
  *
  * All logs reported by the SDK implementation are first added to a queue. If the queue is full (with a
  * `config.maxQueueSize` maximum size), the incoming logs are dropped.
  *
  * Logs are exported either when there are `config.maxExportBatchSize` pending logs or `config.scheduleDelay` has
  * passed since the last export attempt.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#batching-processor]]
  *
  * @tparam F
  *   the higher-kinded type of polymorphic effect
  */
private final class BatchLogRecordProcessor[F[_]: Temporal: Console] private (
    queue: Queue[F, LogRecordData],
    signal: CountDownLatch[F],
    state: Ref[F, BatchLogRecordProcessor.State],
    exporter: LogRecordExporter[F],
    config: BatchLogRecordProcessor.Config
) extends LogRecordProcessor.Unsealed[F] {

  import BatchLogRecordProcessor.State

  private val unit = Temporal[F].unit

  val name: String = "BatchLogRecordProcessor{" +
    s"exporter=${exporter.name}, " +
    s"scheduleDelay=${config.scheduleDelay}, " +
    s"exporterTimeout=${config.exporterTimeout}, " +
    s"maxQueueSize=${config.maxQueueSize}, " +
    s"maxExportBatchSize=${config.maxExportBatchSize}}"

  def onEmit(context: Context, logRecordRef: LogRecordRef[F]): F[Unit] = {
    def notifyWorker: F[Unit] =
      for {
        queueSize <- queue.size
        state <- state.get
        _ <- if (state.logsNeeded.exists(needed => queueSize >= needed)) signal.release else unit
      } yield ()

    for {
      logRecord <- logRecordRef.toLogRecordData
      offered <- queue.tryOffer(logRecord)
      _ <- if (offered) notifyWorker else unit
    } yield ()
  }

  def forceFlush: F[Unit] =
    exportAll

  private def worker: F[Unit] =
    run.foreverM[Unit]

  private def run: F[Unit] =
    Temporal[F].uncancelable { poll =>
      // export the current batch and reset the state
      def doExport(now: FiniteDuration, batch: Vector[LogRecordData]): F[Unit] =
        poll(exportBatch(batch)).guarantee(
          state.set(State(now + config.scheduleDelay, Vector.empty, None))
        )

      // wait either for:
      // 1) the signal - it means the queue has enough logs
      // 2) the timeout - it means we can export all remaining logs
      def pollMore(now: FiniteDuration, nextExportTime: FiniteDuration, currentBatchSize: Int): F[Unit] = {
        val pollWaitTime = nextExportTime - now
        val logsNeeded = config.maxExportBatchSize - currentBatchSize

        val request =
          for {
            _ <- state.update(_.copy(logsNeeded = Some(logsNeeded)))
            _ <- signal.await.timeoutTo(pollWaitTime, unit)
          } yield ()

        if (pollWaitTime > Duration.Zero) {
          poll(request).guarantee(state.update(_.copy(logsNeeded = None)))
        } else {
          unit
        }
      }

      for {
        st <- poll(state.get)

        // try to take enough logs to fill up the current batch
        logs <- queue.tryTakeN(Some(config.maxExportBatchSize - st.batch.size))

        // modify the state with the updated batch
        nextState <- state.updateAndGet(_.copy(batch = st.batch ++ logs))

        // the current timestamp
        now <- Temporal[F].monotonic

        _ <- {
          val batch = nextState.batch
          val nextExportTime = nextState.nextExportTime

          // two reasons to export:
          // 1) the current batch size exceeds the limit
          // 2) the worker is behind the scheduled export time
          val canExport = batch.size >= config.maxExportBatchSize || now >= nextExportTime

          if (canExport) doExport(now, batch)
          else pollMore(now, nextExportTime, batch.size)
        }
      } yield ()
    }

  // export all available data
  private def exportAll: F[Unit] =
    for {
      st <- state.get
      logRecordData <- queue.tryTakeN(None)
      all = st.batch ++ logRecordData
      _ <- all.grouped(config.maxExportBatchSize).toList.traverse_(exportBatch)
      _ <- state.update(_.copy(batch = Vector.empty, logsNeeded = None))
    } yield ()

  private def exportBatch(batch: Vector[LogRecordData]): F[Unit] =
    exporter
      .exportLogRecords(batch)
      .timeoutTo(
        config.exporterTimeout,
        Console[F].errorln(
          s"BatchLogRecordProcessor: the export attempt has been canceled after [${config.exporterTimeout}]"
        )
      )
      .handleErrorWith { e =>
        Console[F].errorln(
          s"BatchLogRecordProcessor: the export has failed: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
        )
      }

}

object BatchLogRecordProcessor {

  /** Builder for [[BatchLogRecordProcessor]]. */
  sealed trait Builder[F[_]] {

    /** Sets the delay interval between two consecutive exports.
      *
      * The default value is `5 seconds`.
      */
    def withScheduleDelay(delay: FiniteDuration): Builder[F]

    /** Sets the maximum time an export will be allowed to run before being canceled.
      *
      * The default value is `30 seconds`.
      */
    def withExporterTimeout(timeout: FiniteDuration): Builder[F]

    /** Sets the maximum number of logs that are kept in the queue before start dropping. More memory than this value
      * may be allocated to optimize queue access.
      *
      * The default value is `2048`.
      */
    def withMaxQueueSize(maxQueueSize: Int): Builder[F]

    /** Sets the maximum batch size for every export. This must be smaller or equal to `maxQueueSize`.
      *
      * The default value is `512`.
      */
    def withMaxExportBatchSize(maxExportBatchSize: Int): Builder[F]

    /** Creates a [[BatchLogRecordProcessor]] with the configuration of this builder.
      */
    def build: Resource[F, LogRecordProcessor[F]]
  }

  /** Create a [[Builder]] for [[BatchLogRecordProcessor]].
    *
    * @param exporter
    *   the [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter LogRecordExporter]] to which the logs are pushed
    */
  def builder[F[_]: Temporal: Console](exporter: LogRecordExporter[F]): Builder[F] =
    new BuilderImpl[F](
      exporter = exporter,
      scheduleDelay = Defaults.ScheduleDelay,
      exporterTimeout = Defaults.ExportTimeout,
      maxQueueSize = Defaults.MaxQueueSize,
      maxExportBatchSize = Defaults.MaxExportBatchSize
    )

  private object Defaults {
    val ScheduleDelay: FiniteDuration = 5.second
    val ExportTimeout: FiniteDuration = 30.seconds
    val MaxQueueSize: Int = 2048
    val MaxExportBatchSize: Int = 512
  }

  /** The configuration of the [[BatchLogRecordProcessor]].
    *
    * @param scheduleDelay
    *   the maximum delay interval in milliseconds between two consecutive exports
    *
    * @param exporterTimeout
    *   how long the export can run before it is canceled
    *
    * @param maxQueueSize
    *   the maximum queue size. Once the limit is reached, new logs will be dropped
    *
    * @param maxExportBatchSize
    *   the maximum batch size of every export
    */
  private final case class Config(
      scheduleDelay: FiniteDuration,
      exporterTimeout: FiniteDuration,
      maxQueueSize: Int,
      maxExportBatchSize: Int
  )

  private final case class State(
      nextExportTime: FiniteDuration,
      batch: Vector[LogRecordData],
      logsNeeded: Option[Int]
  )

  private final case class BuilderImpl[F[_]: Temporal: Console](
      exporter: LogRecordExporter[F],
      scheduleDelay: FiniteDuration,
      exporterTimeout: FiniteDuration,
      maxQueueSize: Int,
      maxExportBatchSize: Int
  ) extends Builder[F] {

    def withScheduleDelay(delay: FiniteDuration): Builder[F] =
      copy(scheduleDelay = delay)

    def withExporterTimeout(timeout: FiniteDuration): Builder[F] =
      copy(exporterTimeout = timeout)

    def withMaxQueueSize(maxQueueSize: Int): Builder[F] =
      copy(maxQueueSize = maxQueueSize)

    def withMaxExportBatchSize(maxExportBatchSize: Int): Builder[F] =
      copy(maxExportBatchSize = maxExportBatchSize)

    def build: Resource[F, LogRecordProcessor[F]] = {
      val config = Config(
        scheduleDelay,
        exporterTimeout,
        maxQueueSize,
        maxExportBatchSize
      )

      def create: F[BatchLogRecordProcessor[F]] =
        for {
          queue <- Queue.dropping[F, LogRecordData](maxQueueSize)
          now <- Temporal[F].monotonic
          state <- Ref.of(State(now + config.scheduleDelay, Vector.empty, None))
          signal <- CountDownLatch[F](1)
        } yield new BatchLogRecordProcessor[F](
          queue = queue,
          signal = signal,
          state = state,
          exporter = exporter,
          config = config
        )

      for {
        processor <- Resource.eval(create)
        _ <- Resource.onFinalize(processor.exportAll)
        _ <- processor.worker.background
      } yield processor
    }
  }

}
