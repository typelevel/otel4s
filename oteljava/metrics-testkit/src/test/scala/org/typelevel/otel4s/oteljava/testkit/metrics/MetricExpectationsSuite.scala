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

package org.typelevel.otel4s.oteljava.testkit.metrics

import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.MetricData
import munit.{CatsEffectSuite, Location, TestOptions}
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

class MetricExpectationsSuite extends CatsEffectSuite {

  testkitTest("match by name only") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.inc()
      metrics <- testkit.collectMetrics[MetricData]
    } yield assertEquals(MetricExpectations.check(metrics, MetricExpectation.name("service.counter")), None)
  }

  testkitTest("match by kind and value") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectMetrics[MetricData]
    } yield assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation.sum[Long]("service.counter").withValue(1L)
      )
    )
  }

  testkitTest("match by exact attributes") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("http.method", "GET")))
      metrics <- testkit.collectMetrics[MetricData]
    } yield assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .value(1L)
              .withAttributes(Attributes(Attribute("http.method", "GET")))
          )
      )
    )
  }

  testkitTest("match by attribute subset") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(
        1L,
        Attributes(
          Attribute("http.method", "GET"),
          Attribute("http.route", "/users")
        )
      )
      metrics <- testkit.collectMetrics[MetricData]
    } yield assert(
      MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .value(1L)
              .withAttributesSubset(Attributes(Attribute("http.method", "GET")))
          )
      )
    )
  }

  testkitTest("missing returns unmatched expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectMetrics[MetricData]
    } yield {
      val missing = MetricExpectations.missing(
        metrics,
        List(
          MetricExpectation.sum[Long]("service.counter").withValue(1L),
          MetricExpectation.gauge[Long]("service.gauge")
        )
      )

      assertEquals(missing.size, 1)
      assert(missing.head.expectation == MetricExpectation.gauge[Long]("service.gauge"))
    }
  }

  testkitTest("withAllPoints requires every point to match") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(2L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectMetrics[MetricData]
    } yield assert(
      !MetricExpectations.exists(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAllPoints(
            PointExpectation
              .value(1L)
              .withAttributesSubset(Attributes(Attribute("region", "eu")))
          )
      )
    )
  }

  private def testkitTest[A](
      options: TestOptions,
  )(body: MetricsTestkit[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(MetricsTestkit.inMemory[IO]().use(body))

}
