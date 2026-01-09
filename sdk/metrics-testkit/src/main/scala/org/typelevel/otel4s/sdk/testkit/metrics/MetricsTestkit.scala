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

import cats.FlatMap
import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.context.LocalProvider
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.LocalContextProvider
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader

sealed trait MetricsTestkit[F[_]] {

  /** The [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  def meterProvider: MeterProvider[F]

  /** Collects and returns metrics.
    *
    * @note
    *   metrics are recollected on each invocation.
    */
  def collectMetrics: F[List[MetricData]]
}

object MetricsTestkit {
  private[sdk] trait Unsealed[F[_]] extends MetricsTestkit[F]

  /** Builder for [[MetricsTestkit]]. */
  sealed trait Builder[F[_]] {

    /** Adds the meter provider builder customizer. Multiple customizers can be added, and they will be applied in the
      * order they were added.
      *
      * @param customizer
      *   the customizer to add
      */
    def addMeterProviderCustomizer(customizer: SdkMeterProvider.Builder[F] => SdkMeterProvider.Builder[F]): Builder[F]

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

    /** Creates [[MetricsTestkit]] using the configuration of this builder. */
    def build: Resource[F, MetricsTestkit[F]]

  }

  /** Creates a [[Builder]] of [[MetricsTestkit]] with the default configuration. */
  def builder[F[_]: Async: Diagnostic: LocalContextProvider]: Builder[F] =
    new BuilderImpl[F]()

  /** Creates a [[MetricsTestkit]] using [[Builder]]. The instance keeps metrics in memory.
    *
    * @param customize
    *   a function for customizing the builder
    */
  def inMemory[F[_]: Async: Diagnostic: LocalContextProvider](
      customize: Builder[F] => Builder[F] = identity[Builder[F]](_)
  ): Resource[F, MetricsTestkit[F]] =
    customize(builder[F]).build

  private def create[F[_]: Async: Diagnostic: AskContext](
      customize: SdkMeterProvider.Builder[F] => SdkMeterProvider.Builder[F],
      aggregationTemporalitySelector: AggregationTemporalitySelector,
      defaultAggregationSelector: AggregationSelector,
      defaultCardinalityLimitSelector: CardinalityLimitSelector
  ): Resource[F, MetricsTestkit[F]] = {
    def createMeterProvider(
        reader: InMemoryMetricReader[F]
    ): F[MeterProvider[F]] =
      Random.scalaUtilRandom[F].flatMap { implicit random =>
        val builder = SdkMeterProvider.builder[F].registerMetricReader(reader)
        customize(builder).build
      }

    for {
      reader <- Resource.eval(
        InMemoryMetricReader
          .builder[F]
          .withAggregationTemporalitySelector(aggregationTemporalitySelector)
          .withDefaultAggregationSelector(defaultAggregationSelector)
          .withDefaultCardinalityLimitSelector(defaultCardinalityLimitSelector)
          .build
      )
      meterProvider <- Resource.eval(createMeterProvider(reader))
    } yield new Impl(meterProvider, reader)
  }

  private final class Impl[F[_]: FlatMap](
      val meterProvider: MeterProvider[F],
      reader: MetricReader[F]
  ) extends MetricsTestkit[F] {
    def collectMetrics: F[List[MetricData]] =
      reader.collectAllMetrics.map(_.toList)
  }

  private final case class BuilderImpl[F[_]: Async: Diagnostic: LocalContextProvider](
      customizer: SdkMeterProvider.Builder[F] => SdkMeterProvider.Builder[F] = (b: SdkMeterProvider.Builder[F]) => b,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector = AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector = CardinalityLimitSelector.default
  ) extends Builder[F] {

    def addMeterProviderCustomizer(
        customizer: SdkMeterProvider.Builder[F] => SdkMeterProvider.Builder[F]
    ): Builder[F] =
      copy(customizer = this.customizer.andThen(customizer))

    def withAggregationTemporalitySelector(selector: AggregationTemporalitySelector): Builder[F] =
      copy(aggregationTemporalitySelector = selector)

    def withDefaultAggregationSelector(selector: AggregationSelector): Builder[F] =
      copy(defaultAggregationSelector = selector)

    def withDefaultCardinalityLimitSelector(selector: CardinalityLimitSelector): Builder[F] =
      copy(defaultCardinalityLimitSelector = selector)

    def build: Resource[F, MetricsTestkit[F]] =
      Resource.eval(LocalProvider[F, Context].local).flatMap { implicit local =>
        create[F](
          customizer,
          aggregationTemporalitySelector,
          defaultAggregationSelector,
          defaultCardinalityLimitSelector
        )
      }
  }

}
