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
import cats.mtl.Ask
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens
import org.typelevel.otel4s.sdk.metrics.test.InMemoryMeterSharedState
import org.typelevel.otel4s.sdk.metrics.test.PointDataUtils

class SdkBatchCallbackSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

  test("record values from multiple callbacks") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.timeWindow,
      Gens.attributes,
      Gens.nonEmptyString,
      Gen.option(Gen.alphaNumStr),
      Gen.option(Gen.alphaNumStr),
      Gen.either(Gen.long, Gen.double)
    ) { (resource, scope, window, attrs, name, description, unit, value) =>
      def test[A: MeasurementValue](value: A): IO[Unit] = {
        val expectedCounter = MetricData(
          resource,
          scope,
          name + "_counter",
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

        val expectedUpDownCounter = MetricData(
          resource,
          scope,
          name + "_up_down_counter",
          description,
          unit,
          MetricPoints.sum(
            points = PointDataUtils.toNumberPoints(
              Vector(value),
              attrs,
              window
            ),
            monotonic = false,
            aggregationTemporality = AggregationTemporality.Cumulative
          )
        )

        val expectedGauge = MetricData(
          resource,
          scope,
          name + "_gauge",
          description,
          unit,
          MetricPoints.gauge(
            points = PointDataUtils.toNumberPoints(
              Vector(value),
              attrs,
              window
            )
          )
        )

        val expected = Vector(
          expectedCounter,
          expectedUpDownCounter,
          expectedGauge
        )

        for {
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          counter = SdkObservableCounter
            .Builder[IO, A](name + "_counter", state.state, unit, description)
            .createObserver

          upDownCounter = SdkObservableUpDownCounter
            .Builder[IO, A](
              name + "_up_down_counter",
              state.state,
              unit,
              description
            )
            .createObserver

          gauge = SdkObservableGauge
            .Builder[IO, A](name + "_gauge", state.state, unit, description)
            .createObserver

          callback = new SdkBatchCallback[IO](state.state)
            .of(counter, upDownCounter, gauge) { (c, udc, g) =>
              for {
                _ <- c.record(value, attrs)
                _ <- udc.record(value, attrs)
                _ <- g.record(value, attrs)
              } yield ()
            }

          metrics <- callback.surround(state.collectAll(window.end))
        } yield assertEquals(metrics, expected)
      }

      value match {
        case Left(long)    => test[Long](long)
        case Right(double) => test[Double](double)
      }
    }
  }

}
