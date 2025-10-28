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

package org.typelevel.otel4s.sdk.metrics
package exporter

import cats.data.NonEmptyVector
import cats.effect.Resource
import cats.effect.Temporal
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.metrics.data.MetricData

import scala.concurrent.duration.FiniteDuration

/** A reader of metrics that collects metrics from the associated metric producers.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#metricreader]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait MetricReader[F[_]] {

  /** The preferred aggregation temporality for the given instrument.
    */
  def aggregationTemporalitySelector: AggregationTemporalitySelector

  /** The preferred aggregation for the given instrument.
    *
    * If no views are configured for a metric instrument, an aggregation provided by the selector will be used.
    */
  def defaultAggregationSelector: AggregationSelector

  /** The preferred cardinality limit for the given instrument.
    *
    * If no views are configured for a metric instrument, a limit provided by the selector will be used.
    */
  def defaultCardinalityLimitSelector: CardinalityLimitSelector

  /** Sets the metric producer to be used by this reader.
    *
    * @note
    *   this should only be called by the SDK and should be considered an internal API. The producers can be configured
    *   only once.
    *
    * @param producers
    *   the producers to use to collect the metrics
    */
  def register(producers: NonEmptyVector[MetricProducer[F]]): F[Unit]

  /** Collects all metrics from the associated metric producers.
    */
  def collectAllMetrics: F[Vector[MetricData]]

  /** Collects all metrics and sends them to the exporter. Flushes the exporter too.
    */
  def forceFlush: F[Unit]
}

object MetricReader {
  private[sdk] trait Unsealed[F[_]] extends MetricReader[F]

  /** Creates a period metric reader that collects and exports metrics with the given interval.
    *
    * @param exporter
    *   the exporter to send the collected metrics to
    *
    * @param interval
    *   how often to export the metrics
    *
    * @param timeout
    *   how long the export can run before it is cancelled
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def periodic[F[_]: Temporal: Diagnostic](
      exporter: MetricExporter.Push[F],
      interval: FiniteDuration,
      timeout: FiniteDuration
  ): Resource[F, MetricReader[F]] =
    PeriodicMetricReader.create(exporter, interval, timeout)

}
