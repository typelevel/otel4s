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
import cats.effect.std.Random
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.Measurement
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

import scala.concurrent.duration.FiniteDuration

class SdkObservableCounterSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

  test("record values") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.nonEmptyString,
      Gen.option(Gen.alphaNumStr),
      Gen.option(Gen.alphaNumStr),
      Gen.either(Gen.posNum[Long], Gen.double)
    ) { (resource, scope, window, attrs, name, unit, description, value) =>
      def test[A: MeasurementValue](value: A): IO[Unit] = {
        val expected = MetricData(
          resource,
          scope,
          name,
          description,
          unit,
          MetricPoints.sum(
            points = PointDataUtils.toNumberPoints(
              Vector(value),
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

          _ <- SdkObservableCounter
            .Builder[IO, A](name, state, unit, description)
            .createWithCallback(cb => cb.record(value, attrs))
            .surround(
              state.collectAll(reader, window.end)
            )
            .assertEquals(Vector(expected))

          _ <- SdkObservableCounter
            .Builder[IO, A](name, state, unit, description)
            .create(IO.pure(Vector(Measurement(value, attrs))))
            .surround(
              state.collectAll(reader, window.end)
            )
            .assertEquals(Vector(expected))
        } yield ()
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
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
  ): IO[MeterSharedState[IO]] =
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
