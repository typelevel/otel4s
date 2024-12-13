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

import cats.Monad
import cats.data.NonEmptyVector
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter._

final class InMemoryMetricReader[F[_]: Monad](
    producersRef: Ref[F, Vector[MetricProducer[F]]],
    val aggregationTemporalitySelector: AggregationTemporalitySelector,
    val defaultAggregationSelector: AggregationSelector,
    val defaultCardinalityLimitSelector: CardinalityLimitSelector
) extends MetricReader[F] {

  def register(producers: NonEmptyVector[MetricProducer[F]]): F[Unit] =
    producersRef.set(producers.toVector)

  def collectAllMetrics: F[Vector[MetricData]] =
    for {
      producers <- producersRef.get
      metrics <- producers.flatTraverse(_.produce)
    } yield metrics

  def forceFlush: F[Unit] =
    Monad[F].unit

  override def toString: String =
    "InMemoryMetricReader"
}

object InMemoryMetricReader {

  /** Creates a `MetricReader` that keeps metrics in-memory.
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
  def create[F[_]: Concurrent](
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ): F[InMemoryMetricReader[F]] =
    for {
      producers <- Ref.empty[F, Vector[MetricProducer[F]]]
    } yield new InMemoryMetricReader[F](
      producers,
      aggregationTemporalitySelector,
      defaultAggregationSelector,
      defaultCardinalityLimitSelector
    )

}
