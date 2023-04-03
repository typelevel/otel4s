package org.typelevel.otel4s.metrics
package preset

import cats.effect.Sync
import cats.effect.unsafe._

object CEMetrics {

  def make[F[_]: Sync: Meter](e: IORuntimeMetrics) = {
    val meter = implicitly[Meter[F]]

    e.cpuStarvation.map { cpuStarvation => }

    e.compute.map { compute =>
      val wtc = meter
        .observableUpDownCounter("worker-thread-count")
        .create(Sync[F].delay(compute.workerThreadCount()))

      val wtc = meter
        .observableUpDownCounter("active-thread-count")
        .create(Sync[F].delay(compute.activeThreadCount()))

      val stc = meter
        .observableUpDownCounter("searching-thread-count")
        .create(Sync[F].delay(compute.searchingThreadCount()))

      val stc = meter
        .observableUpDownCounter("searching-thread-count")
        .create(Sync[F].delay(compute.searchingThreadCount()))

      val stc = meter
        .observableUpDownCounter("searching-thread-count")
        .create(Sync[F].delay(compute.searchingThreadCount()))

      val stc = meter
        .observableUpDownCounter("searching-thread-count")
        .create(Sync[F].delay(compute.searchingThreadCount()))
    }

  }

  /*
  def starvationCount(): Long
  def maxClockDriftMs(): Long
  def currentClockDriftMs(): Long
   */

  /*

  compute {
  def workerThreadCount(): Int
  def activeThreadCount(): Int
  def searchingThreadCount(): Int
  def blockedWorkerThreadCount(): Int
  def localQueueFiberCount(): Long
  def suspendedFiberCount(): Long
  }

   */
}
