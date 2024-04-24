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

package org.typelevel.otel4s.sdk.metrics.exporter

import cats.Applicative
import cats.data.NonEmptyVector
import org.typelevel.otel4s.sdk.metrics.data.MetricData

final class InMemoryMetricReader[F[_]: Applicative](
    producer: MetricProducer[F],
    val aggregationTemporalitySelector: AggregationTemporalitySelector
) extends MetricReader[F] {
  def defaultAggregationSelector: AggregationSelector =
    AggregationSelector.default

  def defaultCardinalityLimitSelector: CardinalityLimitSelector =
    CardinalityLimitSelector.default

  def register(producers: NonEmptyVector[MetricProducer[F]]): F[Unit] =
    Applicative[F].unit

  def collectAllMetrics: F[Vector[MetricData]] =
    producer.produce

  def forceFlush: F[Unit] =
    Applicative[F].unit

  override def toString: String = "MetricReader.InMemory"
}
