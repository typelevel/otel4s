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
import org.typelevel.otel4s.sdk.test.InMemoryConsole

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
      def test[A: MeasurementValue: Numeric]: IO[Unit] = {
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

        InMemoryConsole.create[IO].flatMap { implicit C: InMemoryConsole[IO] =>
          for {
            state <- InMemoryMeterSharedState.create[IO](
              resource,
              scope,
              timeWindow.start
            )

            counter <- SdkCounter
              .Builder[IO, A](name, state.state, unit, description)
              .create

            _ <- counter.add(value, attributes)
            metrics <- state.collectAll(timeWindow.end)
            _ <- C.entries.assertEquals(consoleEntries)
          } yield assertEquals(metrics, Vector.empty)
        }
      }

      for {
        _ <- test[Double]
        _ <- test[Long]
      } yield ()
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
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          counter <- SdkCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create

          _ <- counter.inc(attrs)
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
          state <- InMemoryMeterSharedState.create[IO](
            resource,
            scope,
            window.start
          )

          counter <- SdkCounter
            .Builder[IO, A](name, state.state, unit, description)
            .create

          _ <- values.traverse_(value => counter.add(value, attrs))
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
