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
import cats.Foldable
import cats.Monad
import cats.effect.std.Console
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.metrics.data.MetricData

/** A metric exporter that logs every metric using [[cats.effect.std.Console]].
  *
  * @note
  *   use this exporter for debugging purposes because it may affect the
  *   performance
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private final class ConsoleMetricExporter[F[_]: Monad: Console](
    val aggregationTemporalitySelector: AggregationTemporalitySelector
) extends MetricExporter[F] {

  val name: String = "ConsoleMetricExporter"

  def defaultAggregationSelector: AggregationSelector =
    AggregationSelector.default

  def defaultCardinalityLimitSelector: CardinalityLimitSelector =
    CardinalityLimitSelector.default

  def exportMetrics[G[_]: Foldable](metrics: G[MetricData]): F[Unit] = {
    def doExport: F[Unit] =
      for {
        _ <- Console[F].println(
          s"ConsoleMetricExporter: received a collection of [${metrics.size}] metrics for export."
        )
        _ <- metrics.traverse_ { metric =>
          Console[F].println(s"ConsoleMetricExporter: $metric")
        }
      } yield ()

    doExport.whenA(metrics.nonEmpty)
  }

  def flush: F[Unit] = Applicative[F].unit
}

object ConsoleMetricExporter {

  /** Creates a metric exporter that logs every metric using
    * [[cats.effect.std.Console]].
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def apply[F[_]: Monad: Console]: MetricExporter[F] =
    apply(AggregationTemporalitySelector.alwaysCumulative)

  /** Creates a metric exporter that logs every metric using
    * [[cats.effect.std.Console]].
    *
    * @param aggregationTemporalitySelector
    *   the aggregation temporality selector to use
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def apply[F[_]: Monad: Console](
      aggregationTemporalitySelector: AggregationTemporalitySelector
  ): MetricExporter[F] =
    new ConsoleMetricExporter[F](aggregationTemporalitySelector)

}
