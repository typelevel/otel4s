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

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils
import org.typelevel.otel4s.sdk.test.InMemoryConsole

import scala.concurrent.duration._

class SdkObservableMeasurementSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  test("log an error when reader is unset") {
    PropF.forAllF(
      Gens.instrumentationScope,
      Gens.asynchronousInstrumentDescriptor
    ) { (scope, descriptor) =>
      InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
        val consoleEntries = {
          import org.typelevel.otel4s.sdk.test.InMemoryConsole._

          List(
            Entry(
              Op.Errorln,
              "SdkObservableMeasurement: " +
                s"trying to record a measurement for an instrument [${descriptor.name}] while the active reader is unset. " +
                "Dropping the measurement."
            )
          )
        }

        for {
          measurement <- SdkObservableMeasurement.create[IO, Long](
            Vector.empty,
            scope,
            descriptor
          )
          _ <- measurement.record(1L)
          _ <- C.entries.assertEquals(consoleEntries)
        } yield ()
      }
    }
  }

  test("reject Double.Nan values") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.asynchronousInstrumentDescriptor
    ) { (resource, scope, timeWindow, descriptor) =>
      for {
        storage <- createStorage[Double](descriptor)
        measurement <- SdkObservableMeasurement.create[IO, Double](
          Vector.empty,
          scope,
          descriptor
        )
        _ <- measurement.record(Double.NaN)
        result <- storage.collect(resource, scope, timeWindow)
      } yield assertEquals(result, None)
    }
  }

  test("record measurements") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.asynchronousInstrumentDescriptor,
      Gen.either(Gen.long, Gen.double)
    ) { (resource, scope, timeWindow, attributes, descriptor, value) =>
      def test[A: MeasurementValue: Numeric](value: A): IO[Unit] = {
        val expected = MetricData(
          resource,
          scope,
          descriptor.name.toString,
          descriptor.description,
          descriptor.unit,
          MetricPoints.gauge(
            PointDataUtils.toNumberPoints(
              NonEmptyVector.one(value),
              attributes,
              timeWindow
            )
          )
        )

        for {
          storage <- createStorage[A](descriptor)
          measurement <- SdkObservableMeasurement.create[IO, A](
            Vector(storage),
            scope,
            descriptor
          )

          _ <- measurement
            .withActiveReader(storage.reader, timeWindow)
            .surround(measurement.record(value, attributes))

          result <- storage.collect(resource, scope, timeWindow)
        } yield assertEquals(result, Some(expected))
      }

      value match {
        case Left(long)    => test(long)
        case Right(double) => test(double)
      }
    }
  }

  private def createStorage[A: MeasurementValue: Numeric](
      descriptor: InstrumentDescriptor.Asynchronous
  ): IO[MetricStorage.Asynchronous[IO, A]] = {
    implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

    val inMemory = new InMemoryMetricReader[IO](
      emptyProducer,
      AggregationTemporalitySelector.alwaysCumulative
    )

    RegisteredReader.create(Duration.Zero, inMemory).flatMap { reader =>
      MetricStorage.asynchronous[IO, A](
        reader,
        None,
        descriptor,
        Aggregation.LastValue
      )
    }
  }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer.Unsealed[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
    }

}
