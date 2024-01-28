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

package org.typelevel.otel4s.sdk.metrics.internal.aggregation

import cats.Monad
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.Data
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.internal.ExemplarReservoir
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor

import scala.concurrent.duration.FiniteDuration

private final class SumAggregator[
    F[_]: Monad,
    Input,
    P <: PointData,
    E <: ExemplarData
](
    makeReservoir: F[ExemplarReservoir[F, E]],
    makeAdder: F[Adder[F, Input]],
    pointDataBuilder: PointDataBuilder[Input, P, E],
    createData: (Vector[P], Boolean, AggregationTemporality) => Data
) extends Aggregator[F] {
  import SumAggregator.Handle

  type Point = P

  def createHandle: F[Aggregator.Handle[F, Point]] =
    for {
      adder <- makeAdder
      reservoir <- makeReservoir
    } yield new Handle[F, Input, Point, E](adder, reservoir, pointDataBuilder)

  def toMetricData(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      descriptor: MetricDescriptor,
      points: Vector[Point],
      temporality: AggregationTemporality
  ): F[MetricData] =
    Monad[F].pure(
      MetricData(
        resource,
        scope,
        descriptor.name,
        descriptor.description,
        descriptor.sourceInstrument.unit,
        createData(points, true, temporality) // todo isMonotonic?
      )
    )

}

private object SumAggregator {

  type OfLong[F[_]] =
    SumAggregator[F, Long, PointData.LongPoint, ExemplarData.LongExemplar]

  type OfDouble[F[_]] =
    SumAggregator[F, Double, PointData.DoublePoint, ExemplarData.DoubleExemplar]

  def ofLong[F[_]: Monad](
      reservoirSize: Int,
      filter: ExemplarFilter
  ): OfLong[F] = {
    val reservoir = ExemplarReservoir
      .longFixedSize[F](reservoirSize) // todo size = availableProcessors
      .map(r => ExemplarReservoir.filtered(filter, r))

    new SumAggregator(
      reservoir,
      Adder.makeLong,
      PointDataBuilder.longPoint,
      Data.LongSum
    )
  }

  def ofDouble[F[_]: Monad](
      reservoirSize: Int,
      filter: ExemplarFilter
  ): OfDouble[F] = {
    val reservoir = ExemplarReservoir
      .doubleFixedSize[F](reservoirSize) // todo size = availableProcessors
      .map(r => ExemplarReservoir.filtered(filter, r))

    new SumAggregator(
      reservoir,
      Adder.makeDouble,
      PointDataBuilder.doublePoint,
      Data.DoubleSum
    )
  }

  private class Handle[F[_]: Monad, I, P <: PointData, E <: ExemplarData](
      adder: Adder[F, I],
      reservoir: ExemplarReservoir[F, E],
      builder: PointDataBuilder[I, P, E]
  ) extends Aggregator.Handle[F, P] {

    def aggregate(
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[P]] = {
      for {
        value <- adder.sum(reset)
        exemplars <- reservoir.collectAndReset(attributes)
      } yield Some(
        builder.create(
          startTimestamp,
          collectTimestamp,
          attributes,
          exemplars,
          value
        )
      )
    }

    def record[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      MeasurementValue[A] match {
        case MeasurementValue.LongMeasurementValue(cast) =>
          recordLong(cast(value), attributes, context)
        case MeasurementValue.DoubleMeasurementValue(cast) =>
          recordDouble(cast(value), attributes, context)
      }

    private def recordLong(value: Long, a: Attributes, c: Context): F[Unit] =
      reservoir.offerLongMeasurement(value, a, c) >> adder.addLong(value)

    private def recordDouble(
        value: Double,
        a: Attributes,
        c: Context
    ): F[Unit] =
      reservoir.offerDoubleMeasurement(value, a, c) >> adder.addDouble(value)
  }

}
