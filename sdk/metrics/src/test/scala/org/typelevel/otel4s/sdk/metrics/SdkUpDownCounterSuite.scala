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
import cats.syntax.foldable._
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

class SdkUpDownCounterSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private implicit val askContext: Ask[IO, Context] = Ask.const(Context.root)

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
              NonEmptyVector.one(Numeric[A].one),
              attrs,
              window
            ),
            monotonic = false,
            aggregationTemporality = AggregationTemporality.Cumulative
          )
        )

        for {
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          upDownCounter <- SdkUpDownCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create

          _ <- upDownCounter.inc(attrs)
          metrics <- state.collectAll(window.end)
        } yield assertEquals(metrics, Vector(expected))
      }

      for {
        _ <- test[Long]
        _ <- test[Double]
      } yield ()
    }
  }

  test("decrement") {
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
              NonEmptyVector.one(Numeric[A].negate(Numeric[A].one)),
              attrs,
              window
            ),
            monotonic = false,
            aggregationTemporality = AggregationTemporality.Cumulative
          )
        )

        for {
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          upDownCounter <- SdkUpDownCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create

          _ <- upDownCounter.dec(attrs)
          metrics <- state.collectAll(window.end)
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
      Gen.either(Gen.listOf(Gen.long), Gen.listOf(Gen.double))
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
                NonEmptyVector.one(values.sum),
                attrs,
                window
              ),
              monotonic = false,
              aggregationTemporality = AggregationTemporality.Cumulative
            )
          )
        )

        for {
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          upDownCounter <- SdkUpDownCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create

          _ <- values.traverse_(value => upDownCounter.add(value, attrs))
          metrics <- state.collectAll(window.end)
        } yield assertEquals(metrics, expected.toVector)
      }

      values match {
        case Left(longs)    => test[Long](longs.toVector)
        case Right(doubles) => test[Double](doubles.toVector)
      }
    }
  }

}
