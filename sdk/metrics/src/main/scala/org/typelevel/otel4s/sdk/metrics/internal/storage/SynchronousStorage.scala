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

package org.typelevel.otel4s.sdk.metrics.internal.storage

import cats.Monad
import cats.data.NonEmptyVector
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.internal.Diagnostic
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.aggregation.Aggregator
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.view.AttributesProcessor
import org.typelevel.otel4s.sdk.metrics.view.View

/** Stores aggregated metrics for synchronous instruments.
  */
private final class SynchronousStorage[F[_]: Monad: Diagnostic, A: MeasurementValue](
    reader: RegisteredReader[F],
    val metricDescriptor: MetricDescriptor,
    aggregator: SynchronousStorage.SynchronousAggregator[F, A],
    attributesProcessor: AttributesProcessor,
    maxCardinality: Int,
    accumulators: AtomicCell[F, Map[Attributes, Aggregator.Accumulator[F, A, PointData]]]
) extends MetricStorage.Synchronous[F, A] {

  private val aggregationTemporality =
    reader.reader.aggregationTemporalitySelector.select(
      metricDescriptor.sourceInstrument.instrumentType
    )

  private val isValid: A => Boolean =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(_) =>
        Function.const(true)
      case MeasurementValue.DoubleMeasurementValue(cast) =>
        v => !cast(v).isNaN
    }

  def record(value: A, attributes: Attributes, context: Context): F[Unit] =
    if (isValid(value)) {
      for {
        handle <- getHandle(attributes, context)
        _ <- handle.record(value, attributes, context)
      } yield ()
    } else {
      Monad[F].unit
    }

  def collect(resource: TelemetryResource, scope: InstrumentationScope, window: TimeWindow): F[Option[MetricData]] = {
    val isDelta = aggregationTemporality == AggregationTemporality.Delta

    def toMetricData(points: Vector[PointData]): F[Option[MetricData]] =
      NonEmptyVector.fromVector(points) match {
        case Some(metricPoints) =>
          aggregator
            .toMetricData(
              resource,
              scope,
              metricDescriptor,
              metricPoints,
              aggregationTemporality
            )
            .map(Some(_))

        case None =>
          Monad[F].pure(Option.empty[MetricData])
      }

    for {
      points <- if (isDelta) collectDelta(window) else collectCumulative(window)
      data <- toMetricData(points)
    } yield data
  }

  private def collectDelta(timeWindow: TimeWindow): F[Vector[PointData]] =
    for {
      start <- reader.lastCollectTimestamp
      accumulators <- accumulators.getAndSet(Map.empty)
      window = TimeWindow(start, timeWindow.end)
      points <- accumulators.toVector.traverse { case (attributes, handle) =>
        handle.aggregate(window, attributes, reset = true)
      }
    } yield points.flatten

  private def collectCumulative(window: TimeWindow): F[Vector[PointData]] =
    for {
      accumulators <- accumulators.get
      points <- accumulators.toVector.traverse { case (attributes, handle) =>
        handle.aggregate(window, attributes, reset = false)
      }
    } yield points.flatten

  private def getHandle(attributes: Attributes, context: Context): F[Aggregator.Accumulator[F, A, PointData]] =
    accumulators.get.flatMap { map =>
      val processed = attributesProcessor.process(attributes, context)
      map.get(processed) match {
        case Some(handle) =>
          Monad[F].pure(handle)

        case None =>
          accumulators.evalModify { map =>
            def create(attrs: Attributes) =
              for {
                accumulator <- aggregator.createAccumulator
              } yield (map.updated(attrs, accumulator), accumulator)

            map.get(processed) match {
              case Some(handle) =>
                Monad[F].pure((map, handle))

              case None if map.sizeIs >= maxCardinality =>
                val overflowed = attributes.added(MetricStorage.OverflowAttribute)
                cardinalityWarning >> map
                  .get(overflowed)
                  .fold(create(overflowed))(a => Monad[F].pure((map, a)))

              case None =>
                create(processed)
            }
          }
      }
    }

  private def cardinalityWarning: F[Unit] =
    MetricStorage.cardinalityWarning(
      "SynchronousStorage",
      metricDescriptor.sourceInstrument,
      maxCardinality
    )

}

private object SynchronousStorage {

  private type SynchronousAggregator[F[_], A] =
    Aggregator.Synchronous[F, A] {
      type Point = PointData
    }

  /** Creates a metric storage for a synchronous instrument.
    *
    * @param reader
    *   the reader the storage must use to collect metrics
    *
    * @param reservoirs
    *   the allocator of exemplar reservoirs
    *
    * @param view
    *   the optional view associated with the instrument
    *
    * @param instrumentDescriptor
    *   the descriptor of the instrument
    *
    * @param aggregation
    *   the preferred aggregation
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to store
    */
  def create[F[_]: Concurrent: Diagnostic, A: MeasurementValue: Numeric](
      reader: RegisteredReader[F],
      reservoirs: Reservoirs[F],
      view: Option[View],
      instrumentDescriptor: InstrumentDescriptor.Synchronous,
      aggregation: Aggregation.Synchronous
  ): F[MetricStorage.Synchronous[F, A]] = {
    val descriptor = MetricDescriptor(view, instrumentDescriptor)

    val aggregator: Aggregator.Synchronous[F, A] =
      Aggregator.synchronous(reservoirs, aggregation, instrumentDescriptor)

    val attributesProcessor =
      view.flatMap(_.attributesProcessor).getOrElse(AttributesProcessor.noop)

    val cardinalityLimit =
      view
        .flatMap(_.cardinalityLimit)
        .getOrElse(
          reader.reader.defaultCardinalityLimitSelector
            .select(instrumentDescriptor.instrumentType)
        )

    AtomicCell[F]
      .of(Map.empty[Attributes, Aggregator.Accumulator[F, A, PointData]])
      .map { accumulators =>
        new SynchronousStorage(
          reader,
          descriptor,
          aggregator.asInstanceOf[SynchronousAggregator[F, A]],
          attributesProcessor,
          cardinalityLimit - 1, // -1 so we have space for the 'overflow' attribute
          accumulators
        )
      }
  }

}
