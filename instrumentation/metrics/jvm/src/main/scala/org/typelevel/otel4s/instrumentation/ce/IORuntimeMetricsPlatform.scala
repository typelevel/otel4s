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

package org.typelevel.otel4s.instrumentation.ce

import cats.Show
import cats.effect.Resource
import cats.effect.Sync
import cats.effect.unsafe.metrics.{IORuntimeMetrics => CatsIORuntimeMetrics}
import cats.effect.unsafe.metrics.WorkStealingThreadPoolMetrics
import cats.effect.unsafe.metrics.WorkerThreadMetrics
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.Meter
import org.typelevel.otel4s.metrics.MeterProvider

private[ce] trait IORuntimeMetricsPlatform {
  self: IORuntimeMetrics.type =>

  sealed trait Config {

    /** The configuration of the CPU starvation metrics.
      */
    def cpuStarvation: Config.CpuStarvationConfig

    /** The configuration of the work-stealing thread pool (WSTP) metrics.
      */
    def workStealingThreadPool: Config.WorkStealingThreadPoolConfig

    override final def toString: String =
      Show[Config].show(this)
  }

  object Config {

    sealed trait CpuStarvationConfig {

      /** Indicates whether metrics are enabled.
        */
      def enabled: Boolean

      /** The attributes to attach to the metrics.
        */
      def attributes: Attributes

      override final def toString: String =
        Show[CpuStarvationConfig].show(this)
    }

    object CpuStarvationConfig {

      /** The metrics are enabled.
        */
      def enabled: CpuStarvationConfig =
        Impl(enabled = true, Attributes.empty)

      /** The metrics are enabled and the given `attributes` will be attached.
        *
        * @param attributes
        *   the attributes to attach to the metrics
        */
      def enabled(attributes: Attributes): CpuStarvationConfig =
        Impl(enabled = true, attributes)

      /** The metrics are disabled.
        */
      def disabled: CpuStarvationConfig =
        Impl(enabled = false, Attributes.empty)

      implicit val cpuStarvationConfigShow: Show[CpuStarvationConfig] = { cfg =>
        s"CpuStarvationConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
      }

      private case class Impl(enabled: Boolean, attributes: Attributes) extends CpuStarvationConfig
    }

    sealed trait WorkStealingThreadPoolConfig {

      /** The configuration of the pool metrics.
        */
      def compute: WorkStealingThreadPoolConfig.ComputeConfig

      /** The configuration of the worker thread metrics.
        */
      def workerThreads: WorkStealingThreadPoolConfig.WorkerThreadsConfig

      override final def toString: String =
        Show[WorkStealingThreadPoolConfig].show(this)
    }

    object WorkStealingThreadPoolConfig {
      sealed trait ComputeConfig {

        /** Indicates whether metrics are enabled.
          */
        def enabled: Boolean

        /** The attributes to attach to the metrics.
          */
        def attributes: Attributes

        override final def toString: String =
          Show[ComputeConfig].show(this)
      }

      object ComputeConfig {

        /** The metrics are enabled.
          */
        def enabled: ComputeConfig =
          Impl(enabled = true, Attributes.empty)

        /** The metrics are enabled and the given `attributes` will be attached.
          *
          * @param attributes
          *   the attributes to attach to the metrics
          */
        def enabled(attributes: Attributes): ComputeConfig =
          Impl(enabled = true, attributes)

        /** The metrics are disabled.
          */
        def disabled: ComputeConfig =
          Impl(enabled = false, Attributes.empty)

        implicit val computeConfigShow: Show[ComputeConfig] = { cfg =>
          s"ComputeConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
        }

        private case class Impl(enabled: Boolean, attributes: Attributes) extends ComputeConfig
      }

      sealed trait WorkerThreadsConfig {

        /** The configuration of the worker thread metrics.
          */
        def thread: WorkerThreadsConfig.ThreadConfig

        /** The configuration of the local queue metrics.
          */
        def localQueue: WorkerThreadsConfig.LocalQueueConfig

        /** The configuration of the timer heap metrics.
          */
        def timerHeap: WorkerThreadsConfig.TimerHeapConfig

        /** The configuration of the ploler metrics.
          */
        def poller: WorkerThreadsConfig.PollerConfig

        override final def toString: String =
          Show[WorkerThreadsConfig].show(this)
      }

      object WorkerThreadsConfig {

        sealed trait ThreadConfig {

          /** Indicates whether metrics are enabled.
            */
          def enabled: Boolean

          /** The attributes to attach to the metrics.
            */
          def attributes: Attributes

          override final def toString: String =
            Show[ThreadConfig].show(this)
        }

        object ThreadConfig {

          /** The metrics are enabled.
            */
          def enabled: ThreadConfig =
            Impl(enabled = true, Attributes.empty)

          /** The metrics are enabled and the given `attributes` will be attached.
            *
            * @param attributes
            *   the attributes to attach to the metrics
            */
          def enabled(attributes: Attributes): ThreadConfig =
            Impl(enabled = true, attributes)

          /** The metrics are disabled.
            */
          def disabled: ThreadConfig =
            Impl(enabled = false, Attributes.empty)

          implicit val threadConfigShow: Show[ThreadConfig] = { cfg =>
            s"ThreadConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
          }

          private case class Impl(enabled: Boolean, attributes: Attributes) extends ThreadConfig

        }

        sealed trait LocalQueueConfig {

          /** Indicates whether metrics are enabled.
            */
          def enabled: Boolean

          /** The attributes to attach to the metrics.
            */
          def attributes: Attributes

          override final def toString: String =
            Show[LocalQueueConfig].show(this)
        }

        object LocalQueueConfig {

          /** The metrics are enabled.
            */
          def enabled: LocalQueueConfig =
            Impl(enabled = true, Attributes.empty)

          /** The metrics are enabled and the given `attributes` will be attached.
            *
            * @param attributes
            *   the attributes to attach to the metrics
            */
          def enabled(attributes: Attributes): LocalQueueConfig =
            Impl(enabled = true, attributes)

          /** The metrics are disabled.
            */
          def disabled: LocalQueueConfig =
            Impl(enabled = false, Attributes.empty)

          implicit val localQueueConfigShow: Show[LocalQueueConfig] = { cfg =>
            s"LocalQueueConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
          }

          private case class Impl(enabled: Boolean, attributes: Attributes) extends LocalQueueConfig
        }

        sealed trait TimerHeapConfig {

          /** Indicates whether metrics are enabled.
            */
          def enabled: Boolean

          /** The attributes to attach to the metrics.
            */
          def attributes: Attributes

          override final def toString: String =
            Show[TimerHeapConfig].show(this)
        }

        object TimerHeapConfig {

          /** The metrics are enabled.
            */
          def enabled: TimerHeapConfig =
            Impl(enabled = true, Attributes.empty)

          /** The metrics are enabled and the given `attributes` will be attached.
            *
            * @param attributes
            *   the attributes to attach to the metrics
            */
          def enabled(attributes: Attributes): TimerHeapConfig =
            Impl(enabled = true, attributes)

          /** The metrics are disabled.
            */
          def disabled: TimerHeapConfig =
            Impl(enabled = false, Attributes.empty)

          implicit val timerHeapConfigShow: Show[TimerHeapConfig] = { cfg =>
            s"TimerHeapConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
          }

          private case class Impl(enabled: Boolean, attributes: Attributes) extends TimerHeapConfig
        }

        sealed trait PollerConfig {

          /** Indicates whether metrics are enabled.
            */
          def enabled: Boolean

          /** The attributes to attach to the metrics.
            */
          def attributes: Attributes

          override final def toString: String =
            Show[PollerConfig].show(this)
        }

        object PollerConfig {

          /** The metrics are enabled.
            */
          def enabled: PollerConfig =
            Impl(enabled = true, Attributes.empty)

          /** The metrics are enabled and the given `attributes` will be attached.
            *
            * @param attributes
            *   the attributes to attach to the metrics
            */
          def enabled(attributes: Attributes): PollerConfig =
            Impl(enabled = true, attributes)

          /** The metrics are disabled.
            */
          def disabled: PollerConfig =
            Impl(enabled = false, Attributes.empty)

          implicit val pollerConfigShow: Show[PollerConfig] = { cfg =>
            s"PollerConfig{enabled=${cfg.enabled}, attributes=${cfg.attributes}}"
          }

          private case class Impl(enabled: Boolean, attributes: Attributes) extends PollerConfig
        }

        /** A configuration with the given configs.
          *
          * @param thread
          *   the worker configuration to use
          *
          * @param localQueue
          *   the local queue configuration to use
          *
          * @param timerHeap
          *   the timer heap configuration to use
          *
          * @param poller
          *   the poller configuration to use
          */
        def apply(
            thread: ThreadConfig,
            localQueue: LocalQueueConfig,
            timerHeap: TimerHeapConfig,
            poller: PollerConfig
        ): WorkerThreadsConfig =
          Impl(thread, localQueue, timerHeap, poller)

        /** All metrics (worker, local queue, timer heap, poller) are enabled.
          */
        def enabled: WorkerThreadsConfig =
          Impl(ThreadConfig.enabled, LocalQueueConfig.enabled, TimerHeapConfig.enabled, PollerConfig.enabled)

        /** All metrics (worker, local queue, timer heap, poller) are disabled.
          */
        def disabled: WorkerThreadsConfig =
          Impl(ThreadConfig.disabled, LocalQueueConfig.disabled, TimerHeapConfig.disabled, PollerConfig.disabled)

        implicit val workerThreadsConfigShow: Show[WorkerThreadsConfig] = { cfg =>
          "WorkerThreadsConfig{" +
            s"thread=${cfg.thread}, " +
            s"localQueue=${cfg.localQueue}, " +
            s"timerHeap=${cfg.timerHeap}, " +
            s"poller=${cfg.poller}}"
        }

        private case class Impl(
            thread: ThreadConfig,
            localQueue: LocalQueueConfig,
            timerHeap: TimerHeapConfig,
            poller: PollerConfig
        ) extends WorkerThreadsConfig
      }

      /** A configuration with the given `compute` and `workerThreads` configurations.
        *
        * @param compute
        *   the compute configuration to use
        *
        * @param workerThreads
        *   the worker threads configuration to use
        */
      def apply(compute: ComputeConfig, workerThreads: WorkerThreadsConfig): WorkStealingThreadPoolConfig =
        Impl(compute, workerThreads)

      /** All metrics (pool, worker threads) are enabled.
        */
      def enabled: WorkStealingThreadPoolConfig =
        Impl(ComputeConfig.enabled, WorkerThreadsConfig.enabled)

      /** All metrics (pool, worker threads) are disabled.
        */
      def disabled: WorkStealingThreadPoolConfig =
        Impl(ComputeConfig.disabled, WorkerThreadsConfig.disabled)

      implicit val workStealingThreadPoolConfigShow: Show[WorkStealingThreadPoolConfig] = { cfg =>
        s"WorkStealingThreadPoolConfig{compute=${cfg.compute}, workerThreads=${cfg.workerThreads}}"
      }

      private case class Impl(
          compute: ComputeConfig,
          workerThreads: WorkerThreadsConfig
      ) extends WorkStealingThreadPoolConfig
    }

    /** The default configuration, the following metrics are enabled:
      *   - CPU starvation
      *   - Work-stealing thread pool - worker
      *   - Work-stealing thread pool - local queue
      *   - Work-stealing thread pool - timer heap
      *   - Work-stealing thread pool - poller
      */
    def default: Config =
      Config(CpuStarvationConfig.enabled, WorkStealingThreadPoolConfig.enabled)

    /** A configuration with the given `cpuStarvation` and `workStealingThreadPool`.
      *
      * @param cpuStarvation
      *   the CPU starvation configuration to use
      *
      * @param workStealingThreadPool
      *   the work stealing thread pool configuration to use
      */
    def apply(cpuStarvation: CpuStarvationConfig, workStealingThreadPool: WorkStealingThreadPoolConfig): Config =
      Impl(cpuStarvation, workStealingThreadPool)

    implicit val configShow: Show[Config] = { cfg =>
      s"IORuntimeMetrics.Config{cpuStarvation=${cfg.cpuStarvation}, workStealingThreadPool=${cfg.workStealingThreadPool}}"
    }

    private case class Impl(
        cpuStarvation: CpuStarvationConfig,
        workStealingThreadPool: WorkStealingThreadPoolConfig
    ) extends Config
  }

  /** Registers the following collectors depending on the `config`:
    *   - runtime pool metrics
    *   - runtime worker thread metrics
    *   - runtime local queue metrics
    *   - runtime timer heap metrics
    *   - runtime poller metrics
    *   - CPU starvation
    *
    * By default, all metrics are enabled.
    *
    * =CPU starvation metrics=
    *
    * Registers the CPU starvation:
    *   - `cats.effect.runtime.cpu.starvation.count`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.current`
    *   - `cats.effect.runtime.cpu.starvation.clock.drift.max`
    *
    * To disable CPU starvation metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.disabled, // disable CPU starvation metrics
    *     WorkStealingThreadPoolConfig.enabled
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to CPU starvation metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled(
    *       Attributes(Attribute("key", "value")) // the attributes
    *     ),
    *     WorkStealingThreadPoolConfig.enabled
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * =WSTP metrics=
    *
    * ==Compute metrics==
    *
    * Registers the runtime compute metrics:
    *   - `cats.effect.runtime.wstp.compute.thread.count`
    *   - `cats.effect.runtime.wstp.compute.thread.active.count`
    *   - `cats.effect.runtime.wstp.compute.thread.blocked.count`
    *   - `cats.effect.runtime.wstp.compute.thread.searching.count`
    *   - `cats.effect.runtime.wstp.compute.fiber.enqueued.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool
    *
    * To disable WSTP pool metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.disabled, // disable compute metrics
    *       WorkerThreadsConfig.enabled
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP pool metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled(
    *         Attributes(Attribute("key", "value")) // the attributes
    *       ),
    *       WorkerThreadsConfig.enabled
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * ==Worker thread metrics==
    *
    * Registers the runtime worker metrics:
    *   - `cats.effect.runtime.wstp.worker.thread.idle.duration`
    *   - `cats.effect.runtime.wstp.worker.thread.event.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool the queue is used by
    *   - `worker.index` - the index of the worker
    *   - `thread.event` - the thread event
    *     - `parked` - a thread is parked
    *     - `polled` - a thread is polled for I/O events
    *     - `blocked` - a thread is switched to a blocking thread and been replaced
    *     - `respawn` - a thread is replaced by a newly spawned thread
    *
    * To disable WSTP worker thread metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.disabled, // disable worker thread metrics
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP worker thread metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled(
    *           Attributes(Attribute("key", "value")) // the attributes
    *         ),
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * ==Local queue metrics==
    *
    * Registers the runtime local queue metrics:
    *   - `cats.effect.runtime.wstp.worker.localqueue.fiber.enqueued.count`
    *   - `cats.effect.runtime.wstp.worker.localqueue.fiber.spillover.count`
    *   - `cats.effect.runtime.wstp.worker.localqueue.fiber.steal.attempt.count`
    *   - `cats.effect.runtime.wstp.worker.localqueue.fiber.stolen.count`
    *   - `cats.effect.runtime.wstp.worker.localqueue.fiber.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool the queue is used by
    *   - `worker.index` - the index of the worker the queue is used by
    *
    * To disable WSTP local queue metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.disabled, // disable local queue metrics
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP local queue metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.enabled(
    *           Attributes(Attribute("key", "value")) // the attributes
    *         ),
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * ==Timer heap metrics==
    *
    * Registers the runtime time heap metrics:
    *   - `cats.effect.runtime.wstp.worker.timerheap.outstanding.count`
    *   - `cats.effect.runtime.wstp.worker.timerheap.packed.count`
    *   - `cats.effect.runtime.wstp.worker.timerheap.timer.count`
    *   - `cats.effect.runtime.wstp.worker.timerheap.next.due`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool the time heap is used by
    *   - `worker.index` - the index of the worker the timer heap is used by
    *   - `timer.state` - the state of the timer
    *     - `executed` - the successfully executed timer
    *     - `scheduled` - the scheduled timer
    *     - `canceled` - the canceled timer
    *
    * To disable WSTP timer heap metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled, // disable timer heap metrics
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP timer heap metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled(
    *           Attributes(Attribute("key", "value")) // the attributes
    *         ),
    *         WorkerThreadsConfig.PollerConfig.enabled
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * ==Poller metrics==
    *
    * Registers the runtime poller metrics:
    *   - `cats.effect.runtime.wstp.worker.poller.operation.outstanding.count`
    *   - `cats.effect.runtime.wstp.worker.poller.operation.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool the poller is used by
    *   - `worker.index` - the index of the worker thread the poller is used by
    *   - `poller.operation` - the operation performed by the poller
    *     - `accept`
    *     - `connect`
    *     - `read`
    *     - `write`
    *   - `poller.operation.status` - the status of the operation
    *     - `submitted` - the operation has been submitted
    *     - `succeeded` - the operation has errored
    *     - `errored` - the operation has errored
    *     - `canceled` - the operation has been canceled
    *
    * To disable WSTP poller metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.disabled // disable poller metrics
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP poller metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.enabled,
    *       WorkerThreadsConfig(
    *         WorkerThreadsConfig.ThreadConfig.enabled,
    *         WorkerThreadsConfig.LocalQueueConfig.enabled,
    *         WorkerThreadsConfig.TimerHeapConfig.enabled,
    *         WorkerThreadsConfig.PollerConfig.enabled(
    *           Attributes(Attribute("key", "value")) // the attributes
    *         )
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * @example
    *   {{{
    * object Main extends IOApp.Simple {
    *   def program(
    *     meterProvider: MeterProvider[IO],
    *     tracerProvider: TracerProvider[IO]
    *   ): IO[Unit] = ???
    *
    *   def run: IO[Unit] =
    *     OtelJava.autoConfigured[IO]().use { otel4s =>
    *       implicit val mp: MeterProvider[IO] = otel4s.meterProvider
    *
    *       IORuntimeMetrics
    *         .register[IO](runtime.metrics, IORuntimeMetrics.Config.default)
    *         .surround {
    *           program(otel4s.meterProvider, otel4s.tracerProvider)
    *         }
    *     }
    * }
    *   }}}
    */
  def register[F[_]: Sync: MeterProvider](
      metrics: CatsIORuntimeMetrics,
      config: Config
  ): Resource[F, Unit] =
    Resource.eval(MeterProvider[F].get(Const.MeterNamespace)).flatMap { implicit meter =>
      val wstpMetrics = metrics.workStealingThreadPool match {
        case Some(pool) =>
          val poolId = pool.identifier

          val computeConfig = config.workStealingThreadPool.compute
          val workerThreadsConfig = config.workStealingThreadPool.workerThreads

          for {
            _ <- computeMetrics(poolId, pool, computeConfig.attributes)
              .whenA(computeConfig.enabled)

            _ <- threadMetrics(poolId, pool.workerThreads, workerThreadsConfig.thread.attributes)
              .whenA(workerThreadsConfig.thread.enabled)

            _ <- localQueueMetrics(poolId, pool.workerThreads, workerThreadsConfig.localQueue.attributes)
              .whenA(workerThreadsConfig.localQueue.enabled)

            _ <- timerHeapMetrics(poolId, pool.workerThreads, workerThreadsConfig.timerHeap.attributes)
              .whenA(workerThreadsConfig.timerHeap.enabled)

            _ <- pollerMetrics(poolId, pool.workerThreads, workerThreadsConfig.poller.attributes)
              .whenA(workerThreadsConfig.poller.enabled)
          } yield ()

        case None =>
          Resource.unit[F]
      }

      for {
        _ <- cpuStarvationMetrics(metrics.cpuStarvation, config.cpuStarvation.attributes)
          .whenA(config.cpuStarvation.enabled)

        _ <- wstpMetrics
      } yield ()
    }

  private def computeMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: WorkStealingThreadPoolMetrics,
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.wstp.compute"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableGauge[Long](s"$prefix.thread.count")
        .withDescription(
          "The number of worker thread instances backing the work-stealing thread pool (WSTP)."
        )
        .withUnit("{thread}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.thread.active.count")
        .withDescription(
          "The number of active worker thread instances currently executing fibers on the compute thread pool."
        )
        .withUnit("{thread}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.thread.searching.count")
        .withDescription(
          "The number of worker thread instances currently searching for fibers to steal from other worker threads."
        )
        .withUnit("{thread}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.thread.blocked.count")
        .withDescription(
          "The number of worker thread instances that can run blocking actions on the compute thread pool."
        )
        .withUnit("{thread}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.fiber.enqueued.count")
        .withDescription("The total number of fibers enqueued on all local queues.")
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.fiber.suspended.count")
        .withDescription("The number of fibers which are currently asynchronously suspended.")
        .withUnit("{fiber}")
        .createObserver
    ) { (total, active, searching, blocked, enqueued, suspended) =>
      val attributes = Attributes(Attribute("pool.id", poolId)) ++ extraAttributes

      for {
        snapshot <- Sync[F].delay(
          (
            metrics.workerThreadCount(),
            metrics.activeThreadCount(),
            metrics.searchingThreadCount(),
            metrics.blockedWorkerThreadCount(),
            metrics.localQueueFiberCount(),
            metrics.suspendedFiberCount(),
          )
        )
        _ <- total.record(snapshot._1, attributes)
        _ <- active.record(snapshot._2, attributes)
        _ <- searching.record(snapshot._3, attributes)
        _ <- blocked.record(snapshot._4, attributes)
        _ <- enqueued.record(snapshot._5, attributes)
        _ <- suspended.record(snapshot._6, attributes)
      } yield ()
    }
  }

  private def threadMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: List[WorkerThreadMetrics],
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.wstp.worker.thread"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableCounter[Long](s"$prefix.idle.duration")
        .withDescription("The total amount of time in nanoseconds that this WorkerThread has been idle.")
        .withUnit("ns")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.event.count")
        .withDescription("The total number of events that happened to this WorkerThread.")
        .withUnit("{event}")
        .createObserver,
    ) { (idleDuration, eventCount) =>
      metrics.traverse_ { workerMetrics =>
        val attributes = Attributes(
          Attribute("pool.id", poolId),
          Attribute("worker.index", workerMetrics.index.toLong)
        ) ++ extraAttributes

        def recordCount(value: Long, state: String): F[Unit] =
          eventCount.record(
            value,
            attributes ++ Attributes(Attribute("thread.event", state))
          )

        for {
          snapshot <- Sync[F].delay(
            (
              workerMetrics.idleTime(),
              workerMetrics.parkedCount(),
              workerMetrics.polledCount(),
              workerMetrics.blockingCount(),
              workerMetrics.respawnCount()
            )
          )
          _ <- idleDuration.record(snapshot._1, attributes)
          _ <- recordCount(snapshot._2, "parked")
          _ <- recordCount(snapshot._3, "polled")
          _ <- recordCount(snapshot._4, "blocked")
          _ <- recordCount(snapshot._5, "respawn")
        } yield ()
      }
    }
  }

  private def localQueueMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: List[WorkerThreadMetrics],
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.wstp.worker.localqueue"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableUpDownCounter[Long](s"$prefix.fiber.enqueued.count")
        .withDescription("The current number of enqueued fibers.")
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.fiber.count")
        .withDescription(
          "The total number of fibers enqueued during the lifetime of the local queue."
        )
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.fiber.spillover.count")
        .withDescription("The total number of fibers spilt over to the external queue.")
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.fiber.steal_attempt.count")
        .withDescription("The total number of successful steal attempts by other worker threads.")
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.fiber.stolen.count")
        .withDescription("The total number of stolen fibers by other worker threads.")
        .withUnit("{fiber}")
        .createObserver
    ) { (fiberEnqueued, fiberTotal, fiberSpillover, stealAttemptCount, stolenCount) =>
      metrics.traverse_ { workerMetrics =>
        val attributes = Attributes(
          Attribute("pool.id", poolId),
          Attribute("worker.index", workerMetrics.index.toLong)
        ) ++ extraAttributes

        for {
          snapshot <- Sync[F].delay(
            (
              workerMetrics.localQueue.fiberCount(),
              workerMetrics.localQueue.totalFiberCount(),
              workerMetrics.localQueue.totalSpilloverCount(),
              workerMetrics.localQueue.successfulStealAttemptCount(),
              workerMetrics.localQueue.stolenFiberCount()
            )
          )
          _ <- fiberEnqueued.record(snapshot._1, attributes)
          _ <- fiberTotal.record(snapshot._2, attributes)
          _ <- fiberSpillover.record(snapshot._3, attributes)
          _ <- stealAttemptCount.record(snapshot._4, attributes)
          _ <- stolenCount.record(snapshot._5, attributes)
        } yield ()
      }
    }
  }

  private def timerHeapMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: List[WorkerThreadMetrics],
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.wstp.worker.timerheap"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableUpDownCounter[Long](s"$prefix.outstanding.count")
        .withDescription("The current number of the outstanding timers, that remain to be executed.")
        .withUnit("{timer}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.timer.count")
        .withDescription("The total number of the timers per state.")
        .withUnit("{timer}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.packed.count")
        .withDescription("The total number of times the heap packed itself to remove canceled timers.")
        .withUnit("{event}")
        .createObserver,
      Meter[F]
        .observableGauge[Long](s"$prefix.next.due")
        .withDescription("Returns the time in nanoseconds till the next due to fire.")
        .withUnit("ns")
        .createObserver
    ) { (outstanding, timerCount, packedCount, nextDue) =>
      metrics.traverse_ { workerMetrics =>
        val attributes = Attributes(
          Attribute("pool.id", poolId),
          Attribute("worker.index", workerMetrics.index.toLong)
        ) ++ extraAttributes

        def recordCount(value: Long, state: String): F[Unit] =
          timerCount.record(
            value,
            attributes ++ Attributes(Attribute("timer.state", state))
          )

        for {
          snapshot <- Sync[F].delay(
            (
              workerMetrics.timerHeap.timersOutstandingCount(),
              workerMetrics.timerHeap.totalTimersExecutedCount(),
              workerMetrics.timerHeap.totalTimersScheduledCount(),
              workerMetrics.timerHeap.totalTimersCanceledCount(),
              workerMetrics.timerHeap.packCount(),
              workerMetrics.timerHeap.nextTimerDue(),
            )
          )
          _ <- outstanding.record(snapshot._1, attributes)
          _ <- recordCount(snapshot._2, "executed")
          _ <- recordCount(snapshot._3, "scheduled")
          _ <- recordCount(snapshot._4, "canceled")
          _ <- packedCount.record(snapshot._5, attributes)
          _ <- nextDue.record(snapshot._6.getOrElse(0L), attributes)
        } yield ()
      }
    }
  }

  private def pollerMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: List[WorkerThreadMetrics],
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.wstp.worker.poller"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableUpDownCounter[Long](s"$prefix.operation.outstanding.count")
        .withDescription("The current number of outstanding operations per category.")
        .withUnit("{operation}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.operation.count")
        .withDescription("The total number of the operations per category and outcome.")
        .withUnit("{operation}")
        .createObserver
    ) { (operationOutstanding, operationCount) =>
      metrics.traverse_ { workerMetrics =>
        val attributes = Attributes(
          Attribute("pool.id", poolId),
          Attribute("worker.index", workerMetrics.index.toLong)
        ) ++ extraAttributes

        val poller = workerMetrics.poller

        def recordOutstanding(value: Long, operation: String): F[Unit] =
          operationOutstanding.record(
            value,
            attributes ++ Attributes(Attribute("poller.operation", operation))
          )

        def recordTotal(value: Long, operation: String, status: String): F[Unit] =
          operationCount.record(
            value,
            attributes ++ Attributes(
              Attribute("poller.operation", operation),
              Attribute("poller.operation.status", status)
            )
          )

        Sync[F].defer {
          for {
            _ <- recordOutstanding(poller.totalAcceptOperationsSubmittedCount(), "accept")
            _ <- recordTotal(poller.totalAcceptOperationsSubmittedCount(), "accept", "submitted")
            _ <- recordTotal(poller.totalAcceptOperationsSucceededCount(), "accept", "succeeded")
            _ <- recordTotal(poller.totalAcceptOperationsErroredCount(), "accept", "errored")
            _ <- recordTotal(poller.totalAcceptOperationsCanceledCount(), "accept", "canceled")

            _ <- recordOutstanding(poller.totalConnectOperationsSubmittedCount(), "connect")
            _ <- recordTotal(poller.totalConnectOperationsSubmittedCount(), "connect", "submitted")
            _ <- recordTotal(poller.totalConnectOperationsSucceededCount(), "connect", "succeeded")
            _ <- recordTotal(poller.totalConnectOperationsErroredCount(), "connect", "errored")
            _ <- recordTotal(poller.totalConnectOperationsCanceledCount(), "connect", "canceled")

            _ <- recordOutstanding(poller.totalReadOperationsSubmittedCount(), "read")
            _ <- recordTotal(poller.totalReadOperationsSubmittedCount(), "read", "submitted")
            _ <- recordTotal(poller.totalReadOperationsSucceededCount(), "read", "succeeded")
            _ <- recordTotal(poller.totalReadOperationsErroredCount(), "read", "errored")
            _ <- recordTotal(poller.totalReadOperationsCanceledCount(), "read", "canceled")

            _ <- recordOutstanding(poller.totalWriteOperationsSubmittedCount(), "write")
            _ <- recordTotal(poller.totalWriteOperationsSubmittedCount(), "write", "submitted")
            _ <- recordTotal(poller.totalWriteOperationsSucceededCount(), "write", "succeeded")
            _ <- recordTotal(poller.totalWriteOperationsErroredCount(), "write", "errored")
            _ <- recordTotal(poller.totalWriteOperationsCanceledCount(), "write", "canceled")
          } yield ()
        }
      }
    }
  }

}
