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
package exporter

import cats.effect.Ref
import cats.effect.Resource
import cats.effect.Temporal
import cats.effect.std.CountDownLatch
import cats.effect.std.Queue
import cats.effect.syntax.monadCancel._
import cats.effect.syntax.spawn._
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.apply._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

import scala.concurrent.duration._

/** Implementation of the [[SpanProcessor]] that batches spans exported by the
  * SDK then pushes them to the exporter pipeline.
  *
  * All spans reported by the SDK implementation are first added to a
  * synchronized queue (with a `maxQueueSize` maximum size, if queue is full
  * spans are dropped).
  *
  * Spans are exported either when there are `maxExportBatchSize` pending spans
  * or `scheduleDelayNanos` has passed since the last export finished.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
final class BatchSpanProcessor[F[_]: Temporal] private (
    queue: Queue[F, SpanData],
    signal: CountDownLatch[F],
    state: Ref[F, BatchSpanProcessor.State],
    exporter: SpanExporter[F],
    config: BatchSpanProcessor.Config
) extends SpanProcessor[F] {
  import BatchSpanProcessor.State

  val isStartRequired: Boolean = false
  val isEndRequired: Boolean = true

  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] =
    Temporal[F].unit

  def onEnd(span: SpanData): F[Unit] = {
    val canExport = span.spanContext.isSampled

    // if 'spansNeeded' is defined, it means the worker is waiting for a certain number of spans
    // and it waits for the 'signal'-latch to be released
    // hence, if the queue size is >= than the number of needed spans, the latch can be released
    def notifyWorker: F[Unit] =
      (queue.size, state.get)
        .mapN { (queueSize, state) =>
          state.spansNeeded.exists(needed => queueSize >= needed)
        }
        .ifM(signal.release, Temporal[F].unit)

    val enqueue =
      for {
        offered <- queue.tryOffer(span)
        _ <- notifyWorker.whenA(offered)
      } yield ()

    enqueue.whenA(canExport)
  }

  def forceFlush: F[Unit] =
    exportAll

  private def worker: F[Unit] =
    run.foreverM[Unit].guarantee(exportAll)

  private def run: F[Unit] = {
    // export the current batch and reset the state
    def doExport(now: FiniteDuration, batch: List[SpanData]): F[Unit] =
      for {
        _ <- exportBatch(batch)
        _ <- state.set(State(now + config.scheduleDelay, Nil, None))
      } yield ()

    def pollMore(
        now: FiniteDuration,
        nextExportTime: FiniteDuration,
        currentBatchSize: Int
    ): F[Unit] = {
      val pollWaitTime = nextExportTime - now
      val spansNeeded = config.maxExportBatchSize - currentBatchSize

      val request =
        for {
          _ <- state.update(_.copy(spansNeeded = Some(spansNeeded)))
          _ <- signal.await.timeoutTo(pollWaitTime, Temporal[F].unit)
          _ <- state.update(_.copy(spansNeeded = None))
        } yield ()

      request.whenA(pollWaitTime > Duration.Zero)
    }

    for {
      st <- state.get

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
        // 2) the worker is behind the export time
        val canExport =
          batch.size >= config.maxExportBatchSize || now >= nextExportTime

        if (canExport) doExport(now, batch)
        else pollMore(now, nextExportTime, batch.size)
      }
    } yield ()
  }

  // export all available data
  private[trace] def exportAll: F[Unit] =
    for {
      st <- state.get
      spanData <- queue.tryTakeN(None)
      batch = st.batch ++ spanData
      _ <- batch
        .grouped(config.maxExportBatchSize)
        .toList
        .traverse_(exportBatch)
      _ <- state.update(_.copy(batch = Nil, spansNeeded = None))
    } yield ()

  // todo 1: .timeoutTo(config.exporterTimeoutNanos)
  // todo 2: handle errors (log them)
  private def exportBatch(batch: List[SpanData]): F[Unit] =
    exporter.exportSpans(batch)

}

object BatchSpanProcessor {

  private object Defaults {
    val ScheduleDelay: FiniteDuration = 5.second
    val ExportTimeout: FiniteDuration = 30.seconds
    val MaxQueueSize: Int = 2048
    val MaxExportBatchSize: Int = 512
  }

  private final case class Config(
      scheduleDelay: FiniteDuration,
      exporterTimeout: FiniteDuration,
      maxQueueSize: Int,
      maxExportBatchSize: Int
  )

  private final case class State(
      nextExportTime: FiniteDuration,
      batch: List[SpanData],
      spansNeeded: Option[Int]
  )

  /** Builder for [[BatchSpanProcessor]]. */
  trait Builder[F[_]] {

    /** Sets the delay interval between two consecutive exports.
      *
      * Default value is `5 seconds`.
      */
    def withScheduleDelay(delay: FiniteDuration): Builder[F]

    /** Sets the maximum time an export will be allowed to run before being
      * cancelled.
      *
      * Default value is `30 seconds`.
      */
    def withExporterTimeout(timeout: FiniteDuration): Builder[F]

    /** Sets the maximum number of Spans that are kept in the queue before start
      * dropping. More memory than this value may be allocated to optimize queue
      * access.
      *
      * Default value is `2048`.
      */
    def withMaxQueueSize(maxQueueSize: Int): Builder[F]

    /** Sets the maximum batch size for every export. This must be smaller or
      * equal to `maxQueueSize`.
      *
      * Default value is `512`.
      */
    def withMaxExportBatchSize(maxExportBatchSize: Int): Builder[F]

    /** Creates a [[BatchSpanProcessor]] with the configuration of this builder.
      */
    def build: Resource[F, BatchSpanProcessor[F]]
  }

  /** Create a [[Builder]] for [[BatchSpanProcessor]].
    *
    * @param exporter
    *   the [[SpanExporter]] to which the Spans are pushed
    */
  def builder[F[_]: Temporal](exporter: SpanExporter[F]): Builder[F] =
    new BuilderImpl[F](
      exporter = exporter,
      scheduleDelay = Defaults.ScheduleDelay,
      exporterTimeout = Defaults.ExportTimeout,
      maxQueueSize = Defaults.MaxQueueSize,
      maxExportBatchSize = Defaults.MaxExportBatchSize
    )

  private final case class BuilderImpl[F[_]: Temporal](
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

    def build: Resource[F, BatchSpanProcessor[F]] = {
      val config = Config(
        scheduleDelay,
        exporterTimeout,
        maxQueueSize,
        maxExportBatchSize
      )

      def create: F[BatchSpanProcessor[F]] =
        for {
          queue <- Queue.bounded[F, SpanData](maxQueueSize)
          now <- Temporal[F].monotonic
          state <- Ref.of(State(now + config.scheduleDelay, Nil, None))
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
        _ <- processor.worker.background
      } yield processor
    }
  }
}
