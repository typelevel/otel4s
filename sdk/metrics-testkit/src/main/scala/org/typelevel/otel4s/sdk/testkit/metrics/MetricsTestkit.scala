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
import cats.effect.std.Console
import cats.effect.std.Random
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.SdkMeterProvider
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.CardinalityLimitSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader

trait MetricsTestkit[F[_]] {

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

  /** Creates [[MetricsTestkit]] that keeps metrics in-memory.
    *
    * @note
    *   the implementation does not record exemplars. Use
    *   `OpenTelemetrySdkTestkit` if you need to record exemplars.
    *
    * @param customize
    *   the customization of the builder
    *
    * @param aggregationTemporalitySelector
    *   the preferred aggregation for the given instrument type
    *
    * @param defaultAggregationSelector
    *   the preferred aggregation for the given instrument type. If no views are
    *   configured for a metric instrument, an aggregation provided by the
    *   selector will be used
    *
    * @param defaultCardinalityLimitSelector
    *   the preferred cardinality limit for the given instrument type. If no
    *   views are configured for a metric instrument, a limit provided by the
    *   selector will be used
    */
  def inMemory[F[_]: Async: Console](
      customize: SdkMeterProvider.Builder[F] => SdkMeterProvider.Builder[F] =
        (b: SdkMeterProvider.Builder[F]) => b,
      aggregationTemporalitySelector: AggregationTemporalitySelector =
        AggregationTemporalitySelector.alwaysCumulative,
      defaultAggregationSelector: AggregationSelector =
        AggregationSelector.default,
      defaultCardinalityLimitSelector: CardinalityLimitSelector =
        CardinalityLimitSelector.default
  ): Resource[F, MetricsTestkit[F]] = {
    implicit val askContext: AskContext[F] = Ask.const(Context.root)

    create[F](
      customize,
      aggregationTemporalitySelector,
      defaultAggregationSelector,
      defaultCardinalityLimitSelector
    )
  }

  private[sdk] def create[F[_]: Async: Console: AskContext](
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
        InMemoryMetricReader.create[F](
          aggregationTemporalitySelector,
          defaultAggregationSelector,
          defaultCardinalityLimitSelector
        )
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

}
