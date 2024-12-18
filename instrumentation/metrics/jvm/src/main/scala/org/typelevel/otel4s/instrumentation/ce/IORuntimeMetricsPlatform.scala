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
import cats.effect.unsafe.metrics.WorkStealingPoolMetrics
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

      /** The configuration of the compute metrics.
        */
      def compute: WorkStealingThreadPoolConfig.ComputeConfig

      /** The configuration of the local queue metrics.
        */
      def localQueue: WorkStealingThreadPoolConfig.LocalQueueConfig

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

      /** A configuration with the given `compute` and `localQueue` configurations.
        *
        * @param compute
        *   the compute configuration to use
        *
        * @param localQueue
        *   the local queue configuration to use
        */
      def apply(compute: ComputeConfig, localQueue: LocalQueueConfig): WorkStealingThreadPoolConfig =
        Impl(compute, localQueue)

      /** All metrics (compute, local queue) are enabled.
        */
      def enabled: WorkStealingThreadPoolConfig =
        Impl(ComputeConfig.enabled, LocalQueueConfig.enabled)

      /** All metrics (compute, local queue) are disabled.
        */
      def disabled: WorkStealingThreadPoolConfig =
        Impl(ComputeConfig.disabled, LocalQueueConfig.disabled)

      implicit val workStealingThreadPoolConfig: Show[WorkStealingThreadPoolConfig] = { cfg =>
        s"WorkStealingThreadPoolConfig{compute=${cfg.compute}, localQueue=${cfg.localQueue}}"
      }

      private case class Impl(compute: ComputeConfig, localQueue: LocalQueueConfig) extends WorkStealingThreadPoolConfig
    }

    /** The default configuration, the following metrics are enabled:
      *   - CPU starvation
      *   - Work-stealing thread pool - compute
      *   - Work-stealing thread pool - local queue
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
    *   - runtime compute metrics
    *   - runtime local queue metrics
    *   - CPU starvation
    *
    * By default, all metrics are enabled.
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
    *   - `cats.effect.runtime.compute.thread.count`
    *   - `cats.effect.runtime.compute.thread.active.count`
    *   - `cats.effect.runtime.compute.thread.blocked.count`
    *   - `cats.effect.runtime.compute.thread.searching.count`
    *   - `cats.effect.runtime.compute.fiber.enqueued.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool
    *
    * To disable WSTP compute metrics, customize a config:
    * {{{
    * val config: IORuntimeMetrics.Config = {
    *   import IORuntimeMetrics.Config._
    *   import WorkStealingThreadPoolConfig._
    *
    *   IORuntimeMetrics.Config(
    *     CpuStarvationConfig.enabled,
    *     WorkStealingThreadPoolConfig(
    *       ComputeConfig.disabled, // disable compute metrics
    *       LocalQueueConfig.enabled
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    *
    * To attach attributes to WSTP compute metrics, customize a config:
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
    *       LocalQueueConfig.enabled
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
    *   - `cats.effect.runtime.local.queue.fiber.enqueued.count`
    *   - `cats.effect.runtime.local.queue.fiber.spillover.count`
    *   - `cats.effect.runtime.local.queue.fiber.steal.attempt.count`
    *   - `cats.effect.runtime.local.queue.fiber.stolen.count`
    *   - `cats.effect.runtime.local.queue.fiber.count`
    *
    * Built-in attributes:
    *   - `pool.id` - the id of the work-stealing thread pool the queue is used by
    *   - `queue.index` - the index of the queue
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
    *       LocalQueueConfig.disabled // disable local queue metrics
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
    *       LocalQueueConfig.enabled(
    *         Attributes(Attribute("key", "value")) // the attributes
    *       )
    *     )
    *   )
    * }
    *
    * IORuntimeMetrics.register[IO](runtime.metrics, config)
    * }}}
    */
  def register[F[_]: Sync: MeterProvider](
      metrics: CatsIORuntimeMetrics,
      config: Config
  ): Resource[F, Unit] =
    Resource.eval(MeterProvider[F].get(Const.MeterNamespace)).flatMap { implicit meter =>
      val wstpMetrics = metrics.workStealingThreadPool match {
        case Some(pool) =>
          val poolId = pool.hash

          for {
            _ <- computeMetrics(
              poolId,
              pool.compute,
              config.workStealingThreadPool.compute.attributes
            ).whenA(config.workStealingThreadPool.compute.enabled)

            _ <- localQueueMetrics(
              poolId,
              pool.localQueues,
              config.workStealingThreadPool.localQueue.attributes
            ).whenA(config.workStealingThreadPool.localQueue.enabled)
          } yield ()

        case None =>
          Resource.unit[F]
      }

      for {
        _ <- cpuStarvationMetrics(
          metrics.cpuStarvation,
          config.cpuStarvation.attributes
        ).whenA(config.cpuStarvation.enabled)

        _ <- wstpMetrics
      } yield ()
    }

  private def computeMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: WorkStealingPoolMetrics.ComputeMetrics,
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.compute"

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

  private def localQueueMetrics[F[_]: Sync: Meter](
      poolId: String,
      metrics: List[WorkStealingPoolMetrics.LocalQueueMetrics],
      extraAttributes: Attributes
  ): Resource[F, Unit] = {
    val prefix = s"${Const.MeterNamespace}.local.queue"

    Meter[F].batchCallback.of(
      Meter[F]
        .observableUpDownCounter[Long](s"$prefix.fiber.enqueued.count")
        .withDescription("The number of enqueued fibers.")
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
        .observableCounter[Long](s"$prefix.fiber.steal.attempt.count")
        .withDescription("The total number of successful steal attempts by other worker threads.")
        .withUnit("{fiber}")
        .createObserver,
      Meter[F]
        .observableCounter[Long](s"$prefix.fiber.stolen.count")
        .withDescription("The total number of stolen fibers by other worker threads.")
        .withUnit("{fiber}")
        .createObserver
    ) { (fiberEnqueued, fiberTotal, fiberSpillover, stealAttemptCount, stolenCount) =>
      metrics.traverse_ { queueMetrics =>
        val attributes = Attributes(
          Attribute("pool.id", poolId),
          Attribute("queue.index", queueMetrics.index.toLong)
        ) ++ extraAttributes

        for {
          snapshot <- Sync[F].delay(
            (
              queueMetrics.fiberCount(),
              queueMetrics.totalFiberCount(),
              queueMetrics.totalSpilloverCount(),
              queueMetrics.successfulStealAttemptCount(),
              queueMetrics.stolenFiberCount()
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

}
