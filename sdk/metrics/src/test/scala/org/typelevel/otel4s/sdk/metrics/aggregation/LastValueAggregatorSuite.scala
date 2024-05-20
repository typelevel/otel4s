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
import cats.syntax.foldable._
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.Gen
import org.scalacheck.Test
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.MetricPoints
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.data.TimeWindow
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

import scala.concurrent.duration._

class LastValueAggregatorSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  test("synchronous - aggregate with reset - return the last seen value") {
    PropF.forAllF(
      Gens.nonEmptyVector(Gen.long),
      Gens.attributes
    ) { (values, attrs) =>
      val aggregator =
        LastValueAggregator.synchronous[IO, Long]

      val timeWindow =
        TimeWindow(100.millis, 200.millis)

      val expected = Some(
        PointData.longNumber(timeWindow, attrs, Vector.empty, values.last)
      )

      for {
        accumulator <- aggregator.createAccumulator
        _ <- values.traverse_ { value =>
          accumulator.record(value, Attributes.empty, Context.root)
        }
        r1 <- accumulator.aggregate(timeWindow, attrs, reset = true)
        r2 <- accumulator.aggregate(timeWindow, attrs, reset = true)
      } yield {
        assertEquals(r1: Option[PointData], expected)
        assertEquals(r2: Option[PointData], None)
      }
    }
  }

  test("synchronous - aggregate without reset - return the last stored value") {
    PropF.forAllF(
      Gens.nonEmptyVector(Gen.long),
      Gens.attributes
    ) { (values, attrs) =>
      val aggregator =
        LastValueAggregator.synchronous[IO, Long]

      val timeWindow =
        TimeWindow(100.millis, 200.millis)

      val expected = Some(
        PointData.longNumber(timeWindow, attrs, Vector.empty, values.last)
      )

      for {
        accumulator <- aggregator.createAccumulator
        _ <- values.traverse_ { value =>
          accumulator.record(value, Attributes.empty, Context.root)
        }
        r1 <- accumulator.aggregate(timeWindow, attrs, reset = false)
        r2 <- accumulator.aggregate(timeWindow, attrs, reset = false)
      } yield {
        assertEquals(r1: Option[PointData], expected)
        assertEquals(r2: Option[PointData], expected)
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
      type LongAggregator = Aggregator.Synchronous[IO, Long] {
        type Point = PointData.LongNumber
      }

      val aggregator =
        LastValueAggregator.synchronous[IO, Long].asInstanceOf[LongAggregator]

      val expected =
        MetricData(
          resource = resource,
          scope = scope,
          name = descriptor.name.toString,
          description = descriptor.description,
          unit = descriptor.unit,
          data = MetricPoints.gauge(points)
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

  test("asynchronous - diff - return the 'current' value") {
    val aggregator = LastValueAggregator.asynchronous[IO, Long]

    PropF.forAllF(
      Gens.asynchronousMeasurement(Gen.long),
      Gens.asynchronousMeasurement(Gen.long)
    ) { (previous, current) =>
      IO(assertEquals(aggregator.diff(previous, current), current))
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
      val aggregator = LastValueAggregator.asynchronous[IO, Long]

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
          data = MetricPoints.gauge(points)
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
