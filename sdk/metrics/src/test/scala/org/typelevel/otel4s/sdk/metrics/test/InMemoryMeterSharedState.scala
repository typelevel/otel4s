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

package org.typelevel.otel4s.sdk.metrics.test

import cats.Applicative
import cats.effect.Async
import cats.effect.Concurrent
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.view.ViewRegistry

import scala.concurrent.duration.FiniteDuration

final case class InMemoryMeterSharedState[F[_]](
    state: MeterSharedState[F],
    reader: RegisteredReader[F]
) {
  def collectAll(collectTimestamp: FiniteDuration): F[Vector[MetricData]] =
    state.collectAll(reader, collectTimestamp)
}

object InMemoryMeterSharedState {

  def create[F[_]: Async: Diagnostic](
      resource: TelemetryResource,
      scope: InstrumentationScope,
      start: FiniteDuration,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative
  ): F[InMemoryMeterSharedState[F]] = {
    implicit val askContext: Ask[F, Context] = Ask.const(Context.root)

    for {
      reader <- createReader(start, aggregationTemporalitySelector)
      state <- MeterSharedState.create[F](
        resource,
        scope,
        start,
        Reservoirs.alwaysOff,
        ViewRegistry(Vector.empty),
        Vector(reader)
      )
    } yield InMemoryMeterSharedState(state, reader)
  }

  private def createReader[F[_]: Concurrent](
      start: FiniteDuration,
      aggregationTemporalitySelector: AggregationTemporalitySelector
  ): F[RegisteredReader[F]] = {
    val inMemory = new InMemoryMetricReader[F](
      emptyProducer,
      aggregationTemporalitySelector
    )

    RegisteredReader.create(start, inMemory)
  }

  private def emptyProducer[F[_]: Applicative]: MetricProducer[F] =
    new MetricProducer.Unsealed[F] {
      def produce: F[Vector[MetricData]] = Applicative[F].pure(Vector.empty)
    }
}
