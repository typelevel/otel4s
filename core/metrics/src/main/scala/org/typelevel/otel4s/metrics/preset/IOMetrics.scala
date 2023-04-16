package org.typelevel.otel4s.metrics
package preset

import cats.effect.{Resource, Sync}
import cats.effect.unsafe._
import cats.syntax.apply._
import cats.syntax.functor._

object IOMetrics {

  def fromRuntimeMetrics[F[_]: Sync: Meter](
      metrics: IORuntimeMetrics
  ): Resource[F, Unit] = {
    val meter = implicitly[Meter[F]]

    val starvation = metrics.cpuStarvation.map { cpuStarvation =>
      val starvationCount = meter
        .observableUpDownCounter("cpu-starvation-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(cpuStarvation.starvationCount()))
        )

      val maxDrift = meter
        .observableUpDownCounter("max-clock-drift-ms")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(cpuStarvation.maxClockDriftMs()))
        )

      val currentDrift = meter
        .observableUpDownCounter("current-clock-drift-ms")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(cpuStarvation.currentClockDriftMs()))
        )

      (starvationCount, maxDrift, currentDrift).tupled.void
    }

    val compute = metrics.compute.map { compute =>
      val wtc = meter
        .observableUpDownCounter("worker-thread-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.workerThreadCount().toLong))
        )

      val atc = meter
        .observableUpDownCounter("active-thread-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.activeThreadCount().toLong))
        )

      val stc = meter
        .observableUpDownCounter("searching-thread-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.searchingThreadCount().toLong))
        )

      val bwtc = meter
        .observableUpDownCounter("blocker-worker-thread-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.blockedWorkerThreadCount().toLong))
        )

      val lqfc = meter
        .observableUpDownCounter("local-queue-fiber-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.localQueueFiberCount()))
        )

      val sfc = meter
        .observableUpDownCounter("suspended-fiber-count")
        .createWithCallback(cb =>
          Sync[F].defer(cb.record(compute.suspendedFiberCount()))
        )

      (wtc, atc, stc, bwtc, lqfc, sfc).tupled.void
    }

    for {
      _ <- starvation.getOrElse(Resource.unit[F])
      _ <- compute.getOrElse(Resource.unit[F])
    } yield ()
  }

}
