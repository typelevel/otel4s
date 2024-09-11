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

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.InMemoryMeterSharedState
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils

class SdkObservableCounterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

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
              NonEmptyVector.one(value),
              attrs,
              window
            ),
            monotonic = true,
            aggregationTemporality = AggregationTemporality.Cumulative
          )
        )

        for {
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          _ <- SdkObservableCounter
            .Builder[IO, A](name, state.state, unit, description)
            .createWithCallback(cb => cb.record(value, attrs))
            .surround(
              state.collectAll(window.end)
            )
            .assertEquals(Vector(expected))

          _ <- SdkObservableCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create(IO.pure(Vector(Measurement(value, attrs))))
            .surround(
              state.collectAll(window.end)
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

}
