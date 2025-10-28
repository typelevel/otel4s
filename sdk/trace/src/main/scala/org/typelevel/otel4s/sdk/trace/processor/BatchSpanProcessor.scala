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

package org.typelevel.otel4s.sdk
package trace
package processor

import cats.effect.Ref
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.CountDownLatch
import cats.effect.std.Queue
import cats.effect.syntax.monadCancel._
import cats.effect.syntax.spawn._
import cats.effect.syntax.temporal._
import cats.syntax.all._
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.sdk.trace.exporter.SpanExporter

import scala.concurrent.duration._

/** Implementation of the [[SpanProcessor]] that batches spans exported by the SDK then pushes them to the exporter
  * pipeline.
  *
  * All spans reported by the SDK implementation are first added to a queue. If the queue is full (with a
  * `config.maxQueueSize` maximum size), the incoming spans are dropped.
  *
  * Spans are exported either when there are `config.maxExportBatchSize` pending spans or `config.scheduleDelay` has
  * passed since the last export attempt.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#batching-processor]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/languages/java/configuration/#properties-traces]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private final class BatchSpanProcessor[F[_]: Temporal: Diagnostic] private (
    queue: Queue[F, SpanData],
    signal: CountDownLatch[F],
    state: Ref[F, BatchSpanProcessor.State],
    exporter: SpanExporter[F],
    config: BatchSpanProcessor.Config
) extends SpanProcessor.Unsealed[F] {
  import BatchSpanProcessor.State

  private val unit = Temporal[F].unit

  val name: String = "BatchSpanProcessor{" +
    s"exporter=${exporter.name}, " +
    s"scheduleDelay=${config.scheduleDelay}, " +
    s"exporterTimeout=${config.exporterTimeout}, " +
    s"maxQueueSize=${config.maxQueueSize}, " +
    s"maxExportBatchSize=${config.maxExportBatchSize}}"

  val onStart: SpanProcessor.OnStart[F] =
    SpanProcessor.OnStart.noop

  val onEnd: SpanProcessor.OnEnd[F] = SpanProcessor.OnEnd { (span: SpanData) =>
    if (span.spanContext.isSampled) {
      // if 'spansNeeded' is defined, it means the worker is waiting for a certain number of spans
      // and it waits for the 'signal'-latch to be released
      // hence, if the queue size is >= than the number of needed spans, the latch can be released
      def notifyWorker: F[Unit] =
        for {
          queueSize <- queue.size
          state <- state.get
          _ <- if (state.spansNeeded.exists(needed => queueSize >= needed)) signal.release else unit
        } yield ()

      for {
        offered <- queue.tryOffer(span)
        _ <- if (offered) notifyWorker else unit
      } yield ()
    } else {
      unit
    }
  }

  def forceFlush: F[Unit] =
    exportAll

  private def worker: F[Unit] =
    run.foreverM[Unit]

  private def run: F[Unit] =
    Temporal[F].uncancelable { poll =>
      // export the current batch and reset the state
      def doExport(now: FiniteDuration, batch: Vector[SpanData]): F[Unit] =
        poll(exportBatch(batch)).guarantee(
          state.set(State(now + config.scheduleDelay, Vector.empty, None))
        )

      // wait either for:
      // 1) the signal - it means the queue has enough spans
      // 2) the timeout - it means we can export all remaining spans
      def pollMore(now: FiniteDuration, nextExportTime: FiniteDuration, currentBatchSize: Int): F[Unit] = {
        val pollWaitTime = nextExportTime - now
        val spansNeeded = config.maxExportBatchSize - currentBatchSize

        val request =
          for {
            _ <- state.update(_.copy(spansNeeded = Some(spansNeeded)))
            _ <- signal.await.timeoutTo(pollWaitTime, unit)
          } yield ()

        if (pollWaitTime > Duration.Zero) {
          poll(request).guarantee(state.update(_.copy(spansNeeded = None)))
        } else {
          unit
        }
      }

      for {
        st <- poll(state.get)

        // try to take enough spans to fill up the current batch
        spans <- queue.tryTakeN(Some(config.maxExportBatchSize - st.batch.size))

        // modify the state with the updated batch
        nextState <- state.updateAndGet(_.copy(batch = st.batch ++ spans))

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
      spanData <- queue.tryTakeN(None)
      all = st.batch ++ spanData
      _ <- all.grouped(config.maxExportBatchSize).toList.traverse_(exportBatch)
      _ <- state.update(_.copy(batch = Vector.empty, spansNeeded = None))
    } yield ()

  private def exportBatch(batch: Vector[SpanData]): F[Unit] =
    exporter
      .exportSpans(batch)
      .timeoutTo(
        config.exporterTimeout,
        Diagnostic[F].error(
          s"BatchSpanProcessor: the export attempt has been canceled after [${config.exporterTimeout}]"
        )
      )
      .handleErrorWith { e =>
        Diagnostic[F].error(s"BatchSpanProcessor: the export has failed: ${e.getMessage}", e)
      }

}

object BatchSpanProcessor {

  /** Builder for [[BatchSpanProcessor]]. */
  sealed trait Builder[F[_]] {

    /** Sets the delay interval between two consecutive exports.
      *
      * Default value is `5 seconds`.
      */
    def withScheduleDelay(delay: FiniteDuration): Builder[F]

    /** Sets the maximum time an export will be allowed to run before being cancelled.
      *
      * Default value is `30 seconds`.
      */
    def withExporterTimeout(timeout: FiniteDuration): Builder[F]

    /** Sets the maximum number of Spans that are kept in the queue before start dropping. More memory than this value
      * may be allocated to optimize queue access.
      *
      * Default value is `2048`.
      */
    def withMaxQueueSize(maxQueueSize: Int): Builder[F]

    /** Sets the maximum batch size for every export. This must be smaller or equal to `maxQueueSize`.
      *
      * Default value is `512`.
      */
    def withMaxExportBatchSize(maxExportBatchSize: Int): Builder[F]

    /** Creates a [[BatchSpanProcessor]] with the configuration of this builder.
      */
    def build: Resource[F, SpanProcessor[F]]
  }

  /** Create a [[Builder]] for [[BatchSpanProcessor]].
    *
    * @param exporter
    *   the [[exporter.SpanExporter SpanExporter]] to which the spans are pushed
    */
  def builder[F[_]: Temporal: Diagnostic](exporter: SpanExporter[F]): Builder[F] =
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

  /** The configuration of the [[BatchSpanProcessor]].
    *
    * @param scheduleDelay
    *   the maximum delay interval in milliseconds between two consecutive exports
    *
    * @param exporterTimeout
    *   how long the export can run before it is cancelled
    *
    * @param maxQueueSize
    *   the maximum queue size. Once the limit is reached new spans will be dropped
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
      batch: Vector[SpanData],
      spansNeeded: Option[Int]
  )

  private final case class BuilderImpl[F[_]: Temporal: Diagnostic](
      exporter: SpanExporter[F],
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

    def build: Resource[F, SpanProcessor[F]] = {
      val config = Config(
        scheduleDelay,
        exporterTimeout,
        maxQueueSize,
        maxExportBatchSize
      )

      def create: F[BatchSpanProcessor[F]] =
        for {
          queue <- Queue.dropping[F, SpanData](maxQueueSize)
          now <- Temporal[F].monotonic
          state <- Ref.of(State(now + config.scheduleDelay, Vector.empty, None))
          signal <- CountDownLatch[F](1)
        } yield new BatchSpanProcessor[F](
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
