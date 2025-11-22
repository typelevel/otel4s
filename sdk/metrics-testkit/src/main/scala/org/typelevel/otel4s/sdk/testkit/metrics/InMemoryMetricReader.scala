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
) extends MetricReader.Unsealed[F] {

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

  /** Builder for [[InMemoryMetricReader]]. */
  sealed trait Builder[F[_]] {

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

    /** Creates [[InMemoryMetricReader]] using the configuration of this builder. */
    def build: F[InMemoryMetricReader[F]]

  }

  /** Creates a [[Builder]] of [[InMemoryMetricReader]] with the default configuration. */
  def builder[F[_]: Concurrent]: Builder[F] =
    BuilderImpl[F]()

  /** Creates an [[InMemoryMetricReader]] that keeps metrics in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def create[F[_]: Concurrent](
      customize: Builder[F] => Builder[F] = identity[Builder[F]](_)
  ): F[InMemoryMetricReader[F]] =
    customize(builder[F]).build

  private final case class BuilderImpl[F[_]: Concurrent](
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ) extends Builder[F] {

    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F] =
      copy(aggregationTemporalitySelector = selector)

    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F] =
      copy(defaultAggregationSelector = selector)

    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F] =
      copy(defaultCardinalityLimitSelector = selector)

    def build: F[InMemoryMetricReader[F]] =
      for {
        producers <- Ref.empty[F, Vector[MetricProducer[F]]]
      } yield new InMemoryMetricReader[F](
        producers,
        aggregationTemporalitySelector,
        defaultAggregationSelector,
        defaultCardinalityLimitSelector
      )
  }

}
