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

package org.typelevel.otel4s.sdk.metrics.aggregation

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.AsynchronousMeasurement
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor

/** Aggregators are responsible for holding aggregated values and taking a
  * snapshot of these values upon export.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#aggregation]]
  */
private[metrics] object Aggregator {

  /** An aggregator for synchronous instruments:
    *   - Counter
    *   - UpDownCounter
    *   - Histogram
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  trait Synchronous[F[_], A] {
    type Point <: PointData

    /** Creates an empty accumulator to aggregate measurements.
      */
    def createAccumulator: F[Aggregator.Accumulator[F, A, Point]]

    /** Returns the MetricData using the given values.
      *
      * @param resource
      *   the resource to associate the `MetricData` with
      *
      * @param scope
      *   the instrumentation scope to associate the `MetricData` with
      *
      * @param descriptor
      *   the descriptor of the instrument
      *
      * @param points
      *   the measurements to create a `MetricData` with
      *
      * @param temporality
      *   the aggregation temporality of the resulting `MetricData`
      */
    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        points: Vector[Point],
        temporality: AggregationTemporality
    ): F[MetricData]
  }

  /** An aggregator for asynchronous instruments:
    *   - ObservableCounter
    *   - ObservableUpDownCounter
    *   - ObservableGauge
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    */
  trait Asynchronous[F[_], A] {

    /** Returns a new delta aggregation by comparing two cumulative
      * measurements.
      *
      * @param previous
      *   the previously captured measurement
      *
      * @param current
      *   the newly captured (delta) measurement
      */
    def diff(
        previous: AsynchronousMeasurement[A],
        current: AsynchronousMeasurement[A]
    ): AsynchronousMeasurement[A]

    /** Returns the `MetricData` using the given values.
      *
      * @param resource
      *   the resource to associate the `MetricData` with
      *
      * @param scope
      *   the instrumentation scope to associate the `MetricData` with
      *
      * @param descriptor
      *   the descriptor of the instrument
      *
      * @param measurements
      *   the measurements to create a `MetricData` with
      *
      * @param temporality
      *   the aggregation temporality of the resulting `MetricData`
      */
    def toMetricData(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        descriptor: MetricDescriptor,
        measurements: Vector[AsynchronousMeasurement[A]],
        temporality: AggregationTemporality
    ): F[MetricData]
  }

  /** Records incoming raw values (measurements) and aggregates them into the
    * `P` (PointData).
    *
    * Used by the synchronous instruments.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to record
    *
    * @tparam P
    *   the type of the aggregated PointData
    */
  trait Accumulator[F[_], A, P <: PointData] {

    /** Creates a `PointData` using accumulated data.
      *
      * @param timeWindow
      *   the time window to associate the points with
      *
      * @param attributes
      *   the attributes to associate the points with
      *
      * @param reset
      *   whether to reset the internal state
      */
    def aggregate(
        timeWindow: TimeWindow,
        attributes: Attributes,
        reset: Boolean
    ): F[Option[P]]

    /** Records the value.
      *
      * @param value
      *   the value to record
      *
      * @param attributes
      *   the attributes to record by the exemplar reservoir
      *
      * @param context
      *   the context to record by the exemplar reservoir
      */
    def record(
        value: A,
        attributes: Attributes,
        context: Context
    ): F[Unit]
  }

}
