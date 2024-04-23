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

package org.typelevel.otel4s.sdk.metrics

import cats.effect.IO
import cats.effect.std.Console
import cats.effect.std.Random
import cats.mtl.Ask
import cats.syntax.foldable._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.exemplar.TraceContextLookup
import org.typelevel.otel4s.sdk.metrics.exporter.AggregationTemporalitySelector
import org.typelevel.otel4s.sdk.metrics.exporter.InMemoryMetricReader
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils
import org.typelevel.otel4s.sdk.metrics.view.ViewRegistry
import org.typelevel.otel4s.sdk.test.InMemoryConsole

import scala.concurrent.duration.FiniteDuration

class SdkCounterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

  test("allow only positive values") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.nonEmptyString,
      Gen.option(Gen.alphaNumStr),
      Gen.option(Gen.alphaNumStr)
    ) { (resource, scope, timeWindow, attributes, name, unit, description) =>
      def test[A: MeasurementValue: Numeric](
          reader: RegisteredReader[IO],
          state: MeterSharedState[IO]
      )(implicit C: InMemoryConsole[IO]): IO[Unit] = {
        val value = Numeric[A].negate(Numeric[A].one)

        val consoleEntries = {
          import org.typelevel.otel4s.sdk.test.InMemoryConsole._

          List(
            Entry(
              Op.Errorln,
              s"SdkCounter: counters can only increase. Instrument [$name] has tried to record a negative value [$value]."
            )
          )
        }

        for {
          c <- SdkCounter.Builder[IO, A](name, state, unit, description).create
          _ <- c.add(value, attributes)
          metrics <- state.collectAll(reader, timeWindow.end)
          _ <- C.entries.assertEquals(consoleEntries)
        } yield assertEquals(metrics, Vector.empty)
      }

      InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
        for {
          reader <- createReader(timeWindow.start)
          state <- createState(resource, scope, reader, timeWindow.start)
          _ <- test[Double](reader, state)
          _ <- test[Long](reader, state)
        } yield ()
      }
    }
  }

  test("increment") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.nonEmptyString,
      Gen.option(Gen.alphaNumStr),
      Gen.option(Gen.alphaNumStr)
    ) { (resource, scope, window, attrs, name, unit, description) =>
      def test[A: MeasurementValue: Numeric]: IO[Unit] = {
        val expected = MetricData(
          resource,
          scope,
          name,
          description,
          unit,
          MetricPoints.sum(
            points = PointDataUtils.toNumberPoints(
              Vector(Numeric[A].one),
              attrs,
              window
            ),
            monotonic = true,
            aggregationTemporality = AggregationTemporality.Cumulative
          )
        )

        for {
          reader <- createReader(window.start)
          state <- createState(resource, scope, reader, window.start)
          c <- SdkCounter.Builder[IO, A](name, state, unit, description).create
          _ <- c.inc(attrs)
          metrics <- state.collectAll(reader, window.end)
        } yield assertEquals(metrics, Vector(expected))
      }

      for {
        _ <- test[Long]
        _ <- test[Double]
      } yield ()
    }
  }

  test("record values") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.nonEmptyString,
      Gen.option(Gen.alphaNumStr),
      Gen.option(Gen.alphaNumStr),
      Gen.either(Gen.listOf(Gen.posNum[Long]), Gen.listOf(Gen.double))
    ) { (resource, scope, window, attrs, name, unit, description, values) =>
      def test[A: MeasurementValue: Numeric](values: Vector[A]): IO[Unit] = {
        val expected = Option.when(values.nonEmpty)(
          MetricData(
            resource,
            scope,
            name,
            description,
            unit,
            MetricPoints.sum(
              points = PointDataUtils.toNumberPoints(
                Vector(values.sum),
                attrs,
                window
              ),
              monotonic = true,
              aggregationTemporality = AggregationTemporality.Cumulative
            )
          )
        )

        for {
          reader <- createReader(window.start)
          state <- createState(resource, scope, reader, window.start)
          c <- SdkCounter.Builder[IO, A](name, state, unit, description).create
          _ <- values.traverse_(value => c.add(value, attrs))
          metrics <- state.collectAll(reader, window.end)
        } yield assertEquals(metrics, expected.toVector)
      }

      values match {
        case Left(longs)    => test[Long](longs.toVector)
        case Right(doubles) => test[Double](doubles.toVector)
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
  )(implicit C: Console[IO]): IO[MeterSharedState[IO]] =
    Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
      MeterSharedState.create[IO](
        resource,
        scope,
        start,
        ExemplarFilter.alwaysOff,
        TraceContextLookup.noop,
        ViewRegistry(Vector.empty),
        Vector(reader)
      )
    }

  private def emptyProducer: MetricProducer[IO] =
    new MetricProducer[IO] {
      def produce: IO[Vector[MetricData]] = IO.pure(Vector.empty)
    }

}
