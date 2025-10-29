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

package org.typelevel.otel4s.sdk.testkit.metrics

import cats.Foldable
import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.Queue
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricExporter

final class InMemoryMetricExporter[F[_]: Monad] private (
    queue: Queue[F, MetricData],
    val aggregationTemporalitySelector: AggregationTemporalitySelector,
    val defaultAggregationSelector: AggregationSelector,
    val defaultCardinalityLimitSelector: CardinalityLimitSelector
) extends MetricExporter.Push.Unsealed[F] {

  def name: String = "InMemoryMetricExporter"

  def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit] =
    metrics.traverse_(metric => queue.offer(metric))

  def flush: F[Unit] =
    Monad[F].unit

  def exportedMetrics: F[List[MetricData]] =
    queue.tryTakeN(None)

  def reset: F[Unit] =
    queue.tryTakeN(None).void
}

object InMemoryMetricExporter {

  /** Builder for [[InMemoryMetricExporter]]. */
  sealed trait Builder[F[_]] {

    /** Sets the capacity of the internal queue.
      *
      * @param capacity
      *   the optional capacity to use
      */
    def withCapacity(capacity: Option[Int]): Builder[F]

    /** Sets the aggregation temporality selector.
      *
      * @param selector
      *   the selector to use
      */
    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F]

    /** Sets the default aggregation selector.
      *
      * @param selector
      *   the selector to use
      */
    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F]

    /** Sets the default cardinality limit selector.
      *
      * @param selector
      *   the selector to use
      */
    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F]

    /** Creates [[InMemoryMetricExporter]] using the configuration of this builder. */
    def build: F[InMemoryMetricExporter[F]]

  }

  /** Creates a [[Builder]] of [[InMemoryMetricExporter]] with the default configuration.
    */
  def builder[F[_]: Concurrent]: Builder[F] =
    BuilderImpl[F]()

  /** Creates an [[InMemoryMetricExporter]] that keeps metrics in memory.
    *
    * @see
    *   [[builder]]
    *
    * @param capacity
    *   the capacity of the internal queue
    */
  def create[F[_]: Concurrent](capacity: Option[Int]): F[InMemoryMetricExporter[F]] =
    builder[F].withCapacity(capacity).build

  /** Creates a `MetricExporter` that keeps metrics in-memory.
    *
    * @param capacity
    *   the capacity of the internal queue
    *
    * @param aggregationTemporalitySelector
    *   the preferred aggregation for the given instrument type
    *
    * @param defaultAggregationSelector
    *   the preferred aggregation for the given instrument type. If no views are configured for a metric instrument, an
    *   aggregation provided by the selector will be used.
    *
    * @param defaultCardinalityLimitSelector
    *   the preferred cardinality limit for the given instrument type. If no views are configured for a metric
    *   instrument, an aggregation provided by the selector will be used.
    */
  @deprecated(
    "Use `InMemoryMetricExporter.builder` or an overloaded alternative of the `InMemoryMetricExporter.create`",
    "0.15.0"
  )
  def create[F[_]: Concurrent](
      capacity: Option[Int],
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ): F[InMemoryMetricExporter[F]] =
    builder[F]
      .withCapacity(capacity)
      .withAggregationTemporalitySelector(aggregationTemporalitySelector)
      .withDefaultAggregationSelector(defaultAggregationSelector)
      .withDefaultCardinalityLimitSelector(defaultCardinalityLimitSelector)
      .build

  private final case class BuilderImpl[F[_]: Concurrent](
      capacity: Option[Int] = None,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ) extends Builder[F] {

    def withCapacity(capacity: Option[Int]): Builder[F] =
      copy(capacity = capacity)

    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F] =
      copy(aggregationTemporalitySelector = selector)

    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F] =
      copy(defaultAggregationSelector = selector)

    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F] =
      copy(defaultCardinalityLimitSelector = selector)

    def build: F[InMemoryMetricExporter[F]] =
      for {
        queue <- capacity.fold(Queue.unbounded[F, MetricData])(Queue.bounded(_))
      } yield new InMemoryMetricExporter[F](
        queue,
        aggregationTemporalitySelector,
        defaultAggregationSelector,
        defaultCardinalityLimitSelector
      )
  }

}
