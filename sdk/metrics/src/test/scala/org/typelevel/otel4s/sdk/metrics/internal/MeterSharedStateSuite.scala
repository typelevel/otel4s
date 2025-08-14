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

package org.typelevel.otel4s.sdk.metrics.internal

import cats.data.NonEmptyList
import cats.data.NonEmptyVector
import cats.effect.IO
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils
import org.typelevel.otel4s.sdk.metrics.view.ViewRegistry

import scala.concurrent.duration._

class MeterSharedStateSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("synchronous instrument: record measurements") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.synchronousInstrumentDescriptor,
      Gen.either(Gen.long, Gen.double)
    ) { (resource, scope, timeWindow, attributes, descriptor, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val points = descriptor.instrumentType match {
          case InstrumentType.Counter =>
            MetricPoints.sum(
              points = PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              ),
              monotonic = true,
              aggregationTemporality = AggregationTemporality.Cumulative
            )

          case InstrumentType.UpDownCounter =>
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              ),
              monotonic = false,
              aggregationTemporality = AggregationTemporality.Cumulative
            )

          case InstrumentType.Gauge =>
            MetricPoints.gauge(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              )
            )

          case InstrumentType.Histogram =>
            MetricPoints.histogram(
              NonEmptyVector.one(
                PointDataUtils.toHistogramPoint(
                  NonEmptyVector.one(value),
                  attributes,
                  timeWindow,
                  Aggregation.Defaults.Boundaries
                )
              ),
              AggregationTemporality.Cumulative
            )
        }

        val expected = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          points
        )

        for {
          reader <- createReader(timeWindow.start)
          state <- createState(resource, scope, reader, timeWindow.start)
          storage <- state.registerMetricStorage[A](descriptor)
          _ <- storage.record(value, attributes, Context.root)
          metrics <- state.collectAll(reader, timeWindow.end)
        } yield assertEquals(metrics, Vector(expected))
      }

      value match {
        case Left(long)    => test(long)
        case Right(double) => test(double)
      }
    }
  }

  test("asynchronous instrument: invoke callback during collection") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.asynchronousInstrumentDescriptor,
      Gen.either(Gen.long, Gen.double)
    ) { (resource, scope, timeWindow, attributes, descriptor, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val points = descriptor.instrumentType match {
          case InstrumentType.ObservableCounter =>
            MetricPoints.sum(
              points = PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              ),
              monotonic = true,
              aggregationTemporality = AggregationTemporality.Cumulative
            )

          case InstrumentType.ObservableUpDownCounter =>
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              ),
              monotonic = false,
              aggregationTemporality = AggregationTemporality.Cumulative
            )

          case InstrumentType.ObservableGauge =>
            MetricPoints.gauge(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attributes,
                timeWindow
              )
            )
        }

        val expected = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          points
        )

        for {
          reader <- createReader(timeWindow.start)
          state <- createState(resource, scope, reader, timeWindow.start)
          measurement <- state.registerObservableMeasurement[A](descriptor)

          registration = new CallbackRegistration[IO](
            NonEmptyList.of(measurement),
            measurement.record(value, attributes)
          )

          metrics <- state
            .withCallback(registration)
            .surround(
              state.collectAll(reader, timeWindow.end)
            )
        } yield assertEquals(metrics, Vector(expected))
      }

      value match {
        case Left(long)    => test(long)
        case Right(double) => test(double)
      }
    }
  }

  private def createReader(start: FiniteDuration): IO[RegisteredReader[IO]] = {
    val inMemory = new InMemoryMetricReader[IO](
      emptyProducer,
      AggregationTemporalitySelector.alwaysCumulative
    )

    RegisteredReader.create(start, inMemory)
  }

  private def createState(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      reader: RegisteredReader[IO],
      start: FiniteDuration
  ): IO[MeterSharedState[IO]] = {
    implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

    MeterSharedState.create[IO](
      resource,
      scope,
      start,
      Reservoirs.alwaysOff,
      ViewRegistry(Vector.empty),
      Vector(reader)
    )
  }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer.Unsealed[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
    }

}
