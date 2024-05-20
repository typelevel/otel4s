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
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData.TraceContext
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.PointData.Histogram
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

import scala.concurrent.duration._

class ExplicitBucketHistogramAggregatorSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  private val traceContextKey = Context.Key
    .unique[SyncIO, TraceContext]("trace-context")
    .unsafeRunSync()

  private def reservoirs(implicit R: Random[IO]): Reservoirs[IO] =
    Reservoirs.alwaysOn[IO](_.get(traceContextKey))

  test("aggregate with reset - return a snapshot and reset the state") {
    PropF.forAllF(
      Gen.listOf(Gen.double),
      Gens.bucketBoundaries,
      Gens.attributes,
      Gens.attributes,
      Gens.traceContext
    ) { (values, boundaries, exemplarAttributes, attributes, traceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, traceContext)

        val aggregator =
          ExplicitBucketHistogramAggregator[IO, Double](reservoirs, boundaries)

        val timeWindow =
          TimeWindow(100.millis, 200.millis)

        val expected = {
          val stats = makeStats(values)
          val counts = makeCounts(values, boundaries)

          val filteredAttributes =
            exemplarAttributes.filterNot(a => attributes.get(a.key).isDefined)

          val exemplars =
            makeExemplarValues(values, boundaries).map { value =>
              ExemplarData.double(
                filteredAttributes,
                Duration.Zero,
                Some(traceContext),
                value
              )
            }

          PointData.histogram(
            timeWindow,
            attributes,
            exemplars,
            stats,
            boundaries,
            counts
          )
        }

        val empty =
          PointData.histogram(
            timeWindow = timeWindow,
            attributes = attributes,
            exemplars = Vector.empty,
            stats = None,
            boundaries = boundaries,
            counts = Vector.fill(boundaries.length + 1)(0L)
          )

        TestControl.executeEmbed {
          for {
            accumulator <- aggregator.createAccumulator
            _ <- values.traverse_ { value =>
              accumulator.record(value, exemplarAttributes, ctx)
            }
            r1 <- accumulator.aggregate(timeWindow, attributes, reset = true)
            r2 <- accumulator.aggregate(timeWindow, attributes, reset = true)
          } yield {
            assertEquals(r1: Option[PointData], Some(expected))
            assertEquals(r2: Option[PointData], Some(empty))
          }
        }
      }
    }
  }

  test("aggregate without reset - return a cumulative snapshot") {
    PropF.forAllF(
      Gen.listOf(Gen.double),
      Gens.bucketBoundaries,
      Gens.attributes,
      Gens.attributes,
      Gens.traceContext
    ) { (values, boundaries, exemplarAttributes, attributes, traceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, traceContext)

        val aggregator =
          ExplicitBucketHistogramAggregator[IO, Double](reservoirs, boundaries)

        val timeWindow =
          TimeWindow(100.millis, 200.millis)

        def expected(values: List[Double]) = {
          val stats = makeStats(values)
          val counts = makeCounts(values, boundaries)

          val filteredAttributes =
            exemplarAttributes.filterNot(a => attributes.get(a.key).isDefined)

          val exemplars =
            makeExemplarValues(values, boundaries).map { value =>
              ExemplarData.double(
                filteredAttributes,
                Duration.Zero,
                Some(traceContext),
                value
              )
            }

          PointData.histogram(
            timeWindow,
            attributes,
            exemplars,
            stats,
            boundaries,
            counts
          )
        }

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
              Some(expected(values ++ values))
            )
          }
        }
      }
    }
  }

  test("toMetricData") {
    PropF.forAllF(
      Gens.bucketBoundaries,
      Gens.telemetryResource,
      Gens.instrumentationScope,
      Gens.instrumentDescriptor,
      Gens.nonEmptyVector(Gens.histogramPointData),
      Gens.aggregationTemporality
    ) { (boundaries, resource, scope, descriptor, points, temporality) =>
      type HistogramAggregator = Aggregator.Synchronous[IO, Double] {
        type Point = PointData.Histogram
      }

      val aggregator =
        ExplicitBucketHistogramAggregator[IO, Double](
          Reservoirs.alwaysOff,
          boundaries
        ).asInstanceOf[HistogramAggregator]

      val expected =
        MetricData(
          resource = resource,
          scope = scope,
          name = descriptor.name.toString,
          description = descriptor.description,
          unit = descriptor.unit,
          data = MetricPoints.histogram(points, temporality)
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

  private def makeStats(values: List[Double]): Option[Histogram.Stats] =
    Option.when(values.nonEmpty)(
      PointData.Histogram.Stats(
        sum = values.sum,
        min = values.min,
        max = values.max,
        count = values.size.toLong
      )
    )

  private def makeCounts(
      values: List[Double],
      boundaries: BucketBoundaries
  ): Vector[Long] =
    values.foldLeft(Vector.fill(boundaries.length + 1)(0L)) {
      case (acc, value) =>
        val i = boundaries.boundaries.indexWhere(b => value <= b)
        val idx = if (i == -1) boundaries.length else i

        acc.updated(idx, acc(idx) + 1L)
    }

  // retains last exemplar for each bucket
  private def makeExemplarValues(
      values: List[Double],
      boundaries: BucketBoundaries
  ): Vector[Double] =
    values
      .foldLeft(
        Vector.fill(boundaries.length + 1)(Option.empty[Double])
      ) { case (acc, value) =>
        val i = boundaries.boundaries.indexWhere(b => value <= b)
        val idx = if (i == -1) boundaries.length else i

        acc.updated(idx, Some(value))
      }
      .collect { case Some(value) => value }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(30)
      .withMaxSize(30)

}
