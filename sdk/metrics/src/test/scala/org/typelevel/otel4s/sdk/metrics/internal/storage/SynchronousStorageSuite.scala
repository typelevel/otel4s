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
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils
import org.typelevel.otel4s.sdk.metrics.view.View

import scala.concurrent.duration._

class SynchronousStorageSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val noopDiagnostic: Diagnostic[IO] = Diagnostic.noop

  test("ignore Double.NaN values") {
    PropF.forAllF(
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow
    ) { (descriptor, resource, scope, timeWindow) =>
      for {
        storage <- createStorage[Double](descriptor)
        _ <- storage.record(Double.NaN, Attributes.empty, Context.root)
        metric <- storage.collect(resource, scope, timeWindow)
      } yield assertEquals(metric, None)
    }
  }

  test("cumulative reader - record values - don't reset after the collection") {
    PropF.forAllF(
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
    ) { (descriptor, resource, scope, attributes, timeWindow, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {
        def expected(repeat: Int) = Option.when(values.nonEmpty)(
          MetricData(
            resource,
            scope,
            descriptor.name.toString,
            descriptor.description,
            descriptor.unit,
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(Vector.fill(repeat)(values).flatten.sum),
                attributes,
                timeWindow
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Cumulative
            )
          )
        )

        for {
          storage <- createStorage[A](descriptor)

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected(1))

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected(2))
        } yield ()
      }

      values match {
        case Left(longs)    => test[Long](longs.toVector)
        case Right(doubles) => test[Double](doubles.toVector)
      }
    }
  }

  test("delta reader - record values - reset after the collection") {
    PropF.forAllF(
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
    ) { (descriptor, resource, scope, attributes, timeWindow, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {
        val expected = Option.when(values.nonEmpty)(
          MetricData(
            resource,
            scope,
            descriptor.name.toString,
            descriptor.description,
            descriptor.unit,
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(values.sum),
                attributes,
                TimeWindow(Duration.Zero, timeWindow.end)
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Delta
            )
          )
        )

        for {
          storage <- createStorage[A](
            descriptor,
            aggregationTemporalitySelector = _ => AggregationTemporality.Delta
          )

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected)

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

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

  test("apply attributes processor") {
    PropF.forAllF(
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
    ) { (descriptor, resource, scope, attributes, timeWindow, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {

        val view = View.builder.addAttributeFilter(_ => false).build

        val expected = Option.when(values.nonEmpty)(
          MetricData(
            resource,
            scope,
            descriptor.name.toString,
            descriptor.description,
            descriptor.unit,
            MetricPoints.sum(
              PointDataUtils.toNumberPoints(
                NonEmptyVector.one(values.sum),
                Attributes.empty,
                TimeWindow(Duration.Zero, timeWindow.end)
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Delta
            )
          )
        )

        for {
          storage <- createStorage[A](
            descriptor,
            view = Some(view),
            aggregationTemporalitySelector = _ => AggregationTemporality.Delta
          )

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected)

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

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

  test("respect cardinality limit") {
    PropF.forAllF(
      Gens.synchronousInstrumentDescriptor,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.attributes,
      Gens.timeWindow,
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
    ) { (descriptor, resource, scope, attributes, timeWindow, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {

        val limit = 1
        val view = View.builder.withCardinalityLimit(limit).build

        val expected = Option.when(values.nonEmpty) {
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
                NonEmptyVector.one(values.sum),
                attrs,
                TimeWindow(Duration.Zero, timeWindow.end)
              ),
              isMonotonic(descriptor),
              AggregationTemporality.Delta
            )
          )
        }

        for {
          storage <- createStorage[A](
            descriptor,
            view = Some(view),
            aggregationTemporalitySelector = _ => AggregationTemporality.Delta
          )

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

          _ <- storage
            .collect(resource, scope, timeWindow)
            .assertEquals(expected)

          _ <- values.traverse(a => storage.record(a, attributes, Context.root))

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

  private def createStorage[A: MeasurementValue: Numeric](
      descriptor: InstrumentDescriptor.Synchronous,
      producer: MetricProducer[IO] = emptyProducer,
      view: Option[View] = None,
      start: FiniteDuration = Duration.Zero,
      aggregationTemporalitySelector: AggregationTemporalitySelector = AggregationTemporalitySelector.alwaysCumulative
  ): IO[MetricStorage.Synchronous[IO, A]] = {
    val inMemory = new InMemoryMetricReader[IO](
      producer,
      aggregationTemporalitySelector
    )

    RegisteredReader.create(start, inMemory).flatMap { reader =>
      MetricStorage.synchronous[IO, A](
        reader,
        Reservoirs.alwaysOff,
        view,
        descriptor,
        Aggregation.Sum
      )
    }
  }

  private def isMonotonic(
      descriptor: InstrumentDescriptor.Synchronous
  ): Boolean =
    descriptor.instrumentType match {
      case InstrumentType.Counter   => true
      case InstrumentType.Histogram => true
      case _                        => false
    }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer.Unsealed[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
    }

}
