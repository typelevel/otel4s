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
import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.MetricData
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.oteljava.testkit.metrics.MetricsTestkit
import org.typelevel.otel4s.scalacheck.Arbitraries._

class IORuntimeMetricsSuite extends CatsEffectSuite with ScalaCheckEffectSuite {
  import IORuntimeMetrics.Config.{CpuStarvationConfig, WorkStealingThreadPoolConfig}
  import IORuntimeMetrics.Config.WorkStealingThreadPoolConfig.{ComputeConfig, WorkerThreadsConfig}
  import IORuntimeMetrics.Config.WorkStealingThreadPoolConfig.WorkerThreadsConfig._

  test("register metrics using default config") {
    MetricsTestkit.inMemory[IO]().use { testkit =>
      implicit val meterProvider: MeterProvider[IO] = testkit.meterProvider

      val expected =
        cpuStarvationMetrics ++ computeMetrics ++ threadMetrics ++ localQueueMetrics ++ timerHeapMetrics ++ pollerMetrics

      for {
        metrics <- IORuntimeMetrics
          .register[IO](munitIORuntime.metrics, IORuntimeMetrics.Config.default)
          .surround(testkit.collectMetrics[MetricData])
      } yield assertEquals(metrics.map(toMetric).sortBy(_.name), expected.sortBy(_.name))
    }
  }

  test("register metrics according to the config") {
    PropF.forAllF { (config: IORuntimeMetrics.Config) =>
      MetricsTestkit.inMemory[IO]().use { testkit =>
        implicit val meterProvider: MeterProvider[IO] = testkit.meterProvider

        val expected = List(
          config.cpuStarvation.enabled -> cpuStarvationMetrics,
          config.workStealingThreadPool.compute.enabled -> computeMetrics,
          config.workStealingThreadPool.workerThreads.thread.enabled -> threadMetrics,
          config.workStealingThreadPool.workerThreads.localQueue.enabled -> localQueueMetrics,
          config.workStealingThreadPool.workerThreads.timerHeap.enabled -> timerHeapMetrics,
          config.workStealingThreadPool.workerThreads.poller.enabled -> pollerMetrics
        ).collect { case (true, metrics) => metrics }.flatten

        for {
          metrics <- IORuntimeMetrics
            .register[IO](munitIORuntime.metrics, config)
            .surround(testkit.collectMetrics[MetricData])
        } yield assertEquals(metrics.map(toMetric).sortBy(_.name), expected.sortBy(_.name))
      }
    }
  }

  test("Show[IORuntimeMetrics.Config]") {
    Prop.forAll { (config: IORuntimeMetrics.Config) =>
      val cpuStarvation = config.cpuStarvation
      val compute = config.workStealingThreadPool.compute
      val workerThreads = config.workStealingThreadPool.workerThreads

      val expected = "IORuntimeMetrics.Config{" +
        s"cpuStarvation=CpuStarvationConfig{enabled=${cpuStarvation.enabled}, attributes=${cpuStarvation.attributes}}, " +
        "workStealingThreadPool=WorkStealingThreadPoolConfig{" +
        s"compute=ComputeConfig{enabled=${compute.enabled}, attributes=${compute.attributes}}, " +
        "workerThreads=WorkerThreadsConfig{" +
        s"thread=ThreadConfig{enabled=${workerThreads.thread.enabled}, attributes=${workerThreads.thread.attributes}}, " +
        s"localQueue=LocalQueueConfig{enabled=${workerThreads.localQueue.enabled}, attributes=${workerThreads.localQueue.attributes}}, " +
        s"timerHeap=TimerHeapConfig{enabled=${workerThreads.timerHeap.enabled}, attributes=${workerThreads.timerHeap.attributes}}, " +
        s"poller=PollerConfig{enabled=${workerThreads.poller.enabled}, attributes=${workerThreads.poller.attributes}}}}" +
        "}"

      assertEquals(Show[IORuntimeMetrics.Config].show(config), expected)
      assertEquals(config.toString, expected)
    }
  }

  private case class Metric(name: String, description: Option[String], unit: Option[String])

  private def toMetric(metric: MetricData): Metric =
    Metric(metric.getName, Option(metric.getDescription).filter(_.nonEmpty), Option(metric.getUnit).filter(_.nonEmpty))

  private val cpuStarvationMetrics = List(
    Metric(
      "cats.effect.runtime.cpu.starvation.count",
      Some("The number of CPU starvation events."),
      None
    ),
    Metric(
      "cats.effect.runtime.cpu.starvation.clock.drift.current",
      Some("The current CPU drift in milliseconds."),
      Some("ms")
    ),
    Metric(
      "cats.effect.runtime.cpu.starvation.clock.drift.max",
      Some("The max CPU drift in milliseconds."),
      Some("ms")
    )
  )

  private val computeMetrics = List(
    Metric(
      "cats.effect.runtime.wstp.compute.thread.count",
      Some("The number of worker thread instances backing the work-stealing thread pool (WSTP)."),
      Some("{thread}")
    ),
    Metric(
      "cats.effect.runtime.wstp.compute.thread.active.count",
      Some("The number of active worker thread instances currently executing fibers on the compute thread pool."),
      Some("{thread}")
    ),
    Metric(
      "cats.effect.runtime.wstp.compute.thread.searching.count",
      Some("The number of worker thread instances currently searching for fibers to steal from other worker threads."),
      Some("{thread}")
    ),
    Metric(
      "cats.effect.runtime.wstp.compute.thread.blocked.count",
      Some("The number of worker thread instances that can run blocking actions on the compute thread pool."),
      Some("{thread}")
    ),
    Metric(
      "cats.effect.runtime.wstp.compute.fiber.enqueued.count",
      Some("The total number of fibers enqueued on all local queues."),
      Some("{fiber}")
    ),
    Metric(
      "cats.effect.runtime.wstp.compute.fiber.suspended.count",
      Some("The number of fibers which are currently asynchronously suspended."),
      Some("{fiber}")
    )
  )

  private val threadMetrics = List(
    Metric(
      "cats.effect.runtime.wstp.worker.thread.idle.duration",
      Some("The total amount of time in nanoseconds that this WorkerThread has been idle."),
      Some("ns")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.thread.event.count",
      Some("The total number of events that happened to this WorkerThread."),
      Some("{event}")
    ),
  )

  private val localQueueMetrics = List(
    Metric(
      "cats.effect.runtime.wstp.worker.localqueue.fiber.count",
      Some("The total number of fibers enqueued during the lifetime of the local queue."),
      Some("{fiber}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.localqueue.fiber.enqueued.count",
      Some("The current number of enqueued fibers."),
      Some("{fiber}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.localqueue.fiber.spillover.count",
      Some("The total number of fibers spilt over to the external queue."),
      Some("{fiber}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.localqueue.fiber.steal_attempt.count",
      Some("The total number of successful steal attempts by other worker threads."),
      Some("{fiber}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.localqueue.fiber.stolen.count",
      Some("The total number of stolen fibers by other worker threads."),
      Some("{fiber}")
    ),
  )

  private val timerHeapMetrics = List(
    Metric(
      "cats.effect.runtime.wstp.worker.timerheap.outstanding.count",
      Some("The current number of the outstanding timers, that remain to be executed."),
      Some("{timer}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.timerheap.timer.count",
      Some("The total number of the timers per state."),
      Some("{timer}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.timerheap.packed.count",
      Some("The total number of times the heap packed itself to remove canceled timers."),
      Some("{event}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.timerheap.next.due",
      Some("Returns the time in nanoseconds till the next due to fire."),
      Some("ns")
    ),
  )

  private val pollerMetrics = List(
    Metric(
      "cats.effect.runtime.wstp.worker.poller.operation.outstanding.count",
      Some("The current number of outstanding operations per category."),
      Some("{operation}")
    ),
    Metric(
      "cats.effect.runtime.wstp.worker.poller.operation.count",
      Some("The total number of the operations per category and outcome."),
      Some("{operation}")
    )
  )

  private implicit val cpuStarvationConfigArbitrary: Arbitrary[CpuStarvationConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) CpuStarvationConfig.enabled(attributes) else CpuStarvationConfig.disabled
    )

  private implicit val computeConfigArbitrary: Arbitrary[ComputeConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) ComputeConfig.enabled(attributes) else ComputeConfig.disabled
    )

  private implicit val localQueueConfigArbitrary: Arbitrary[LocalQueueConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) LocalQueueConfig.enabled(attributes) else LocalQueueConfig.disabled
    )

  private implicit val threadConfigArbitrary: Arbitrary[ThreadConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) ThreadConfig.enabled(attributes) else ThreadConfig.disabled
    )

  private implicit val timerHeapConfigArbitrary: Arbitrary[TimerHeapConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) TimerHeapConfig.enabled(attributes) else TimerHeapConfig.disabled
    )

  private implicit val pollerConfigArbitrary: Arbitrary[PollerConfig] =
    Arbitrary(
      for {
        enabled <- Arbitrary.arbitrary[Boolean]
        attributes <- Arbitrary.arbitrary[Attributes]
      } yield if (enabled) PollerConfig.enabled(attributes) else PollerConfig.disabled
    )

  private implicit val workerThreadsConfigArbitrary: Arbitrary[WorkerThreadsConfig] =
    Arbitrary(
      for {
        thread <- Arbitrary.arbitrary[ThreadConfig]
        localQueue <- Arbitrary.arbitrary[LocalQueueConfig]
        timerHeap <- Arbitrary.arbitrary[TimerHeapConfig]
        poller <- Arbitrary.arbitrary[PollerConfig]
      } yield WorkerThreadsConfig(thread, localQueue, timerHeap, poller)
    )

  private implicit val configArbitrary: Arbitrary[IORuntimeMetrics.Config] =
    Arbitrary(
      for {
        cpuStarvation <- Arbitrary.arbitrary[CpuStarvationConfig]
        pool <- Arbitrary.arbitrary[ComputeConfig]
        workerThreads <- Arbitrary.arbitrary[WorkerThreadsConfig]
      } yield IORuntimeMetrics.Config(cpuStarvation, WorkStealingThreadPoolConfig(pool, workerThreads))
    )

}
