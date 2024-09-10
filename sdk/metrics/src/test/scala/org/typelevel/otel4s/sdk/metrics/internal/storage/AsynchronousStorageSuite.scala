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

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.AsynchronousMeasurement
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils
import org.typelevel.otel4s.sdk.metrics.view.View
import org.typelevel.otel4s.sdk.test.NoopConsole

import scala.concurrent.duration._

class AsynchronousStorageSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val noopConsole: Console[IO] = new NoopConsole[IO]
  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

  test("duplicate attributes - keep the first one") {
    PropF.forAllF(
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
    ) { (descriptor, resource, scope, attributes, timeWindow, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {
        val expected = NonEmptyVector.fromVector(values).map { points =>
          MetricData(
            resource,
            scope,
            descriptor.name.toString,
            descriptor.description,
            descriptor.unit,
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(points.head),
                attributes,
                timeWindow
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Cumulative
            )
          )
        }

        for {
          storage <- createStorage[A](descriptor)

          _ <- values.traverse { a =>
            storage.record(AsynchronousMeasurement(timeWindow, attributes, a))
          }

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected)
        } yield ()
      }

      values match {
        case Left(longs)    => test[Long](longs.toVector)
        case Right(doubles) => test[Double](doubles.toVector)
      }
    }
  }

  test("cumulative reader - record values - don't reset after the collection") {
    PropF.forAllF(
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.long, Gen.double)
    ) { (descriptor, resource, scope, attributes, timeWindow, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val expected = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          MetricPoints.sum(
            PointDataUtils.toNumberPoints(
              NonEmptyVector.one(value),
              attributes,
              timeWindow
            ),
            isMonotonic(descriptor),
            AggregationTemporality.Cumulative
          )
        )

        for {
          storage <- createStorage[A](descriptor)

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected))

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected))
        } yield ()
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
      }
    }
  }

  test("delta reader - record values - reset after the collection") {
    PropF.forAllF(
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.long, Gen.double)
    ) { (descriptor, resource, scope, attributes, timeWindow, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        def expected(a: A) = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          MetricPoints.sum(
            PointDataUtils.toNumberPoints(
              NonEmptyVector.one(a),
              attributes,
              TimeWindow(Duration.Zero, timeWindow.end)
            ),
            isMonotonic(descriptor),
            AggregationTemporality.Delta
          )
        )

        for {
          storage <- createStorage[A](
            descriptor,
            aggregationTemporalitySelector = _ => AggregationTemporality.Delta
          )

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected(value)))

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected(Numeric[A].zero)))
        } yield ()
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
      }
    }
  }

  test("apply attributes processor") {
    PropF.forAllF(
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.long, Gen.double)
    ) { (descriptor, resource, scope, attributes, timeWindow, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val view = View.builder.addAttributeFilter(_ => false).build

        val expected = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          MetricPoints.sum(
            PointDataUtils.toNumberPoints(
              NonEmptyVector.one(value),
              Attributes.empty,
              timeWindow
            ),
            isMonotonic(descriptor),
            AggregationTemporality.Cumulative
          )
        )

        for {
          storage <- createStorage[A](descriptor, view = Some(view))

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected))
        } yield ()
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
      }
    }
  }

  test("respect cardinality limit") {
    PropF.forAllF(
      Gens.asynchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.long, Gen.double)
    ) { (descriptor, resource, scope, attributes, timeWindow, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val limit = 1
        val view = View.builder.withCardinalityLimit(limit).build

        val expected = {
          val attrs =
            if (attributes.size >= (limit - 1))
              attributes.added("otel.metric.overflow", true)
            else
              attributes

          MetricData(
            resource,
            scope,
            descriptor.name.toString,
            descriptor.description,
            descriptor.unit,
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(value),
                attrs,
                timeWindow
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Cumulative
            )
          )
        }

        for {
          storage <- createStorage[A](descriptor, view = Some(view))

          _ <- storage.record(
            AsynchronousMeasurement(timeWindow, attributes, value)
          )

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(Some(expected))
        } yield ()
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
      }
    }
  }

  private def createStorage[A: MeasurementValue: Numeric](
      descriptor: InstrumentDescriptor.Asynchronous,
      producer: MetricProducer[IO] = emptyProducer,
      view: Option[View] = None,
      start: FiniteDuration = Duration.Zero,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative
  ): IO[MetricStorage.Asynchronous[IO, A]] = {
    val inMemory = new InMemoryMetricReader[IO](
      producer,
      aggregationTemporalitySelector
    )

    RegisteredReader.create(start, inMemory).flatMap { reader =>
      MetricStorage.asynchronous[IO, A](
        reader,
        view,
        descriptor,
        Aggregation.Sum
      )
    }
  }

  private def isMonotonic(
      descriptor: InstrumentDescriptor.Asynchronous
  ): Boolean =
    descriptor.instrumentType match {
      case InstrumentType.ObservableCounter => true
      case _                                => false
    }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
    }

}
