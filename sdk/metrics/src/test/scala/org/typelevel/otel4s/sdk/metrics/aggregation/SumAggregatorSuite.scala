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

package org.typelevel.otel4s.sdk.metrics.aggregation

import cats.data.NonEmptyVector
import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.std.Random
import cats.effect.testkit.TestControl
import cats.syntax.foldable._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.metrics.InstrumentType
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

import scala.concurrent.duration._

class SumAggregatorSuite extends CatsEffectSuite with ScalaCheckEffectSuite {

  private val traceContextKey = Context.Key
    .unique[SyncIO, TraceContext]("trace-context")
    .unsafeRunSync()

  private def reservoirs(implicit R: Random[IO]): Reservoirs[IO] =
    Reservoirs.alwaysOn(_.get(traceContextKey))

  // we need to put all exemplar values into the first cell
  private val random = new java.util.Random {
    override def nextInt(bound: Int): Int = 0
  }

  test("synchronous - aggregate with reset - return delta sum") {
    PropF.forAllF(
      Gens.nonEmptyVector(Gen.long),
      Gens.attributes,
      Gens.attributes,
      Gens.traceContext
    ) { (values, exemplarAttributes, attributes, traceContext) =>
      Random.javaUtilRandom[IO](random).flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, traceContext)
        val aggregator = SumAggregator.synchronous[IO, Long](reservoirs, 1)

        val timeWindow =
          TimeWindow(100.millis, 200.millis)

        val expected =
          PointData.longNumber(
            timeWindow,
            attributes,
            Vector(
              ExemplarData.long(
                exemplarAttributes
                  .filterNot(a => attributes.get(a.key).isDefined),
                Duration.Zero,
                Some(traceContext),
                values.last
              )
            ),
            values.toVector.sum
          )

        TestControl.executeEmbed {
          for {
            accumulator <- aggregator.createAccumulator
            _ <- values.traverse_ { value =>
              accumulator.record(value, exemplarAttributes, ctx)
            }
            r <- accumulator.aggregate(timeWindow, attributes, reset = true)
          } yield assertEquals(r: Option[PointData], Some(expected))
        }
      }
    }
  }

  test("synchronous - aggregate without reset - return cumulative sum") {
    PropF.forAllF(
      Gens.nonEmptyVector(Gen.long),
      Gens.attributes,
      Gens.attributes,
      Gens.traceContext
    ) { (values, exemplarAttributes, attributes, traceContext) =>
      Random.javaUtilRandom[IO](random).flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, traceContext)
        val aggregator = SumAggregator.synchronous[IO, Long](reservoirs, 1)

        val timeWindow =
          TimeWindow(100.millis, 200.millis)

        def expected(values: NonEmptyVector[Long]): PointData.LongNumber =
          PointData.longNumber(
            timeWindow,
            attributes,
            Vector(
              ExemplarData.long(
                exemplarAttributes
                  .filterNot(a => attributes.get(a.key).isDefined),
                Duration.Zero,
                Some(traceContext),
                values.last
              )
            ),
            values.toVector.sum
          )

        TestControl.executeEmbed {
          for {
            accumulator <- aggregator.createAccumulator
            _ <- values.traverse_ { value =>
              accumulator.record(value, exemplarAttributes, ctx)
            }
            r1 <- accumulator.aggregate(timeWindow, attributes, reset = false)
            _ <- values.traverse_ { value =>
              accumulator.record(value, exemplarAttributes, ctx)
            }
            r2 <- accumulator.aggregate(timeWindow, attributes, reset = false)
          } yield {
            assertEquals(r1: Option[PointData], Some(expected(values)))
            assertEquals(
              r2: Option[PointData],
              Some(expected(values.concatNev(values)))
            )
          }
        }
      }
    }
  }

  test("synchronous - toMetricData") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.instrumentDescriptor,
      Gens.nonEmptyVector(Gens.longNumberPointData),
      Gens.aggregationTemporality
    ) { (resource, scope, descriptor, points, temporality) =>
      Random.javaUtilRandom[IO](random).flatMap { implicit R: Random[IO] =>
        type LongAggregator = Aggregator.Synchronous[IO, Long] {
          type Point = PointData.LongNumber
        }

        val aggregator =
          SumAggregator
            .synchronous[IO, Long](reservoirs, 1)
            .asInstanceOf[LongAggregator]

        val monotonic =
          descriptor.instrumentType match {
            case InstrumentType.Counter           => true
            case InstrumentType.Histogram         => true
            case InstrumentType.ObservableCounter => true
            case _                                => false
          }

        val expected =
          MetricData(
            resource = resource,
            scope = scope,
            name = descriptor.name.toString,
            description = descriptor.description,
            unit = descriptor.unit,
            data = MetricPoints.sum(points, monotonic, temporality)
          )

        for {
          metricData <- aggregator.toMetricData(
            resource,
            scope,
            MetricDescriptor(None, descriptor),
            points,
            temporality
          )
        } yield assertEquals(metricData, expected)
      }
    }
  }

  test("asynchronous - diff - calculate difference between measurements") {
    val aggregator = SumAggregator.asynchronous[IO, Long]

    PropF.forAllF(
      Gens.asynchronousMeasurement(Gen.long),
      Gens.asynchronousMeasurement(Gen.long)
    ) { (previous, current) =>
      val result = aggregator.diff(previous, current)
      val expected = current.copy(value = current.value - previous.value)

      IO(assertEquals(result, expected))
    }
  }

  test("asynchronous - toMetricData") {
    PropF.forAllF(
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.instrumentDescriptor,
      Gens.nonEmptyVector(Gens.asynchronousMeasurement(Gen.long)),
      Gens.aggregationTemporality
    ) { (resource, scope, descriptor, measurements, temporality) =>
      val aggregator = SumAggregator.asynchronous[IO, Long]

      val monotonic =
        descriptor.instrumentType match {
          case InstrumentType.Counter           => true
          case InstrumentType.Histogram         => true
          case InstrumentType.ObservableCounter => true
          case _                                => false
        }

      val points = measurements.map { m =>
        PointData.longNumber(m.timeWindow, m.attributes, Vector.empty, m.value)
      }

      val expected =
        MetricData(
          resource = resource,
          scope = scope,
          name = descriptor.name.toString,
          description = descriptor.description,
          unit = descriptor.unit,
          data = MetricPoints.sum(points, monotonic, temporality)
        )

      for {
        metricData <- aggregator.toMetricData(
          resource,
          scope,
          MetricDescriptor(None, descriptor),
          measurements,
          temporality
        )
      } yield assertEquals(metricData, expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
