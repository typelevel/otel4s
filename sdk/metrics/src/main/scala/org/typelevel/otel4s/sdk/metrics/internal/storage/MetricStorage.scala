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

import cats.Applicative
import cats.effect.Temporal
import cats.effect.std.Console
import cats.effect.std.Random
import cats.syntax.foldable._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.internal.AsynchronousMeasurement
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.view.View

/** Stores collected `MetricData`.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
private[metrics] trait MetricStorage[F[_]] {

  /** The description of the metric stored in this storage.
    */
  def metricDescriptor: MetricDescriptor

  /** Collects the metrics from this storage.
    *
    * @note
    *   if the aggregation temporality for the metric is `delta`, the state will
    *   be reset.
    *
    * @param resource
    *   the resource to associate the metrics with
    *
    * @param scope
    *   the instrumentation scope to associate the metrics with
    *
    * @param timeWindow
    *   the time window for the current collection of the metrics
    */
  def collect(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      timeWindow: TimeWindow
  ): F[Option[MetricData]]
}

private[metrics] object MetricStorage {

  // indicates when cardinality limit has been exceeded
  private[storage] val OverflowAttribute: Attribute[Boolean] =
    Attribute("otel.metric.overflow", true)

  private[storage] def cardinalityWarning[F[_]: Console](
      storageName: String,
      instrument: InstrumentDescriptor,
      maxCardinality: Int
  ): F[Unit] =
    Console[F].errorln(
      s"$storageName: instrument [${instrument.name}] has exceeded the maximum allowed cardinality [$maxCardinality]"
    )

  /** A storage for the metrics from the synchronous instruments.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to store
    */
  trait Synchronous[F[_], A]
      extends MetricStorage[F]
      with Synchronous.Writeable[F, A]

  object Synchronous {

    trait Writeable[F[_], A] {

      /** Records a measurement with the given values.
        *
        * @param value
        *   the value to record
        *
        * @param attributes
        *   the attributes to associate with the measurement
        *
        * @param context
        *   the context to associate with the measurement
        */
      def record(value: A, attributes: Attributes, context: Context): F[Unit]
    }

    object Writeable {
      def of[F[_]: Applicative, A](
          storages: Vector[Writeable[F, A]]
      ): Writeable[F, A] =
        new Writeable[F, A] {
          def record(
              value: A,
              attributes: Attributes,
              context: Context
          ): F[Unit] =
            storages.traverse_(_.record(value, attributes, context))
        }
    }
  }

  /** A storage for the metrics from the asynchronous instruments.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    *
    * @tparam A
    *   the type of the values to store
    */
  trait Asynchronous[F[_], A] extends MetricStorage[F] {

    /** Records the given measurement.
      *
      * @param measurement
      *   the measurement to record
      */
    def record(measurement: AsynchronousMeasurement[A]): F[Unit]

    /** The associated reader.
      */
    def reader: RegisteredReader[F]
  }

  /** Creates a metric storage for an asynchronous instrument.
    *
    * @param reader
    *   the reader the storage must use to collect metrics
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
  def asynchronous[
      F[_]: Temporal: Console: AskContext,
      A: MeasurementValue: Numeric
  ](
      reader: RegisteredReader[F],
      view: Option[View],
      instrumentDescriptor: InstrumentDescriptor.Asynchronous,
      aggregation: Aggregation.Asynchronous
  ): F[Asynchronous[F, A]] =
    AsynchronousStorage.create(
      reader,
      view,
      instrumentDescriptor,
      aggregation
    )

  /** Creates a metric storage for a synchronous instrument.
    *
    * @param reader
    *   the reader the storage must use to collect metrics
    *
    * @param view
    *   the optional view associated with the instrument
    *
    * @param instrumentDescriptor
    *   the descriptor of the instrument
    *
    * @param exemplarFilter
    *   used by the exemplar reservoir to filter the offered values
    *
    * @param traceContextLookup
    *   used by the exemplar reservoir to extract tracing information from the
    *   context
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
  def synchronous[
      F[_]: Temporal: Console: Random,
      A: MeasurementValue: Numeric
  ](
      reader: RegisteredReader[F],
      view: Option[View],
      instrumentDescriptor: InstrumentDescriptor.Synchronous,
      exemplarFilter: ExemplarFilter,
      traceContextLookup: TraceContextLookup,
      aggregation: Aggregation.Synchronous
  ): F[Synchronous[F, A]] =
    SynchronousStorage.create(
      reader,
      view,
      instrumentDescriptor,
      exemplarFilter,
      traceContextLookup,
      aggregation
    )

}
