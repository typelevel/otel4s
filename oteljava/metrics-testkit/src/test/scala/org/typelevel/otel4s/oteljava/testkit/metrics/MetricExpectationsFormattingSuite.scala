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
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

class MetricExpectationsFormattingSuite extends CatsEffectSuite {

  testkitTest("format renders not found mismatches with clues and available metrics") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation.gauge[Long]("service.gauge").withClue("missing gauge")
      )

      assertEquals(
        rendered,
        """Metric expectations failed:
          |1. [missing gauge] no metric matched the expectation; available metrics: [service.counter]""".stripMargin
      )
    }
  }

  testkitTest("format renders closest metric type mismatches as bulleted output") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation.gauge[Long]("service.counter")
      )

      assertEquals(
        rendered,
        """Metric expectations failed:
          |1. closest metric 'service.counter' mismatched:
          |  - type mismatch: expected 'LONG_GAUGE', got 'LONG_SUM'""".stripMargin
      )
    }
  }

  testkitTest("format preserves multiple metric-level mismatches in order") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withDescription("requests processed")
          .withUnit("ms")
      )

      assertEquals(
        rendered,
        """Metric expectations failed:
          |1. closest metric 'service.counter' mismatched:
          |  - description mismatch: expected 'requests processed', got ''
          |  - unit mismatch: expected 'ms', got ''""".stripMargin
      )
    }
  }

  testkitTest("format shows metric, point-set, and point clues for nested point mismatches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withClue("counter requirement")
          .withPoints(
            PointSetExpectation
              .exists(
                PointExpectation
                  .numeric(1L)
                  .withAttributesSubset(Attribute("region", "us"))
                  .withClue("US point")
              )
              .withClue("regional points")
          )
      )

      assert(
        rendered.contains(
          "Metric expectations failed:\n1. [counter requirement] closest metric 'service.counter' mismatched:"
        )
      )
      assert(rendered.contains("points mismatch [regional points]"))
      assert(rendered.contains("missing expected point [US point]"))
      assert(rendered.contains("attribute mismatch for 'region'"))
      assert(rendered.contains("expected String(region)=us"))
      assert(rendered.contains("got String(region)=eu"))
    }
  }

  testkitTest("format renders composite point-set mismatches with nested messages") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withPoints(
            PointSetExpectation
              .count[PointExpectation.NumericPointData[Long]](2)
              .withClue("point count rule")
              .and(
                PointSetExpectation
                  .contains(
                    PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")).withClue("US point")
                  )
                  .withClue("region shape rule")
              )
              .withClue("combined point rules")
          )
      )

      assert(rendered.contains("closest metric 'service.counter' mismatched:"))
      assert(
        rendered.contains(
          "points mismatch [combined point rules]: point-set mismatch [combined point rules]: and mismatch:"
        )
      )
      assert(rendered.contains("point-set mismatch [point count rule]: point count mismatch: expected 2, got 1"))
      assert(rendered.contains("point-set mismatch [region shape rule]: missing expected point [US point]"))
      assert(rendered.contains("missing expected point [US point]"))
    }
  }

  testkitTest("format numbers multiple failed expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = formatFailures(
        metrics,
        MetricExpectation.gauge[Long]("service.counter"),
        MetricExpectation.gauge[Long]("service.gauge")
      )

      assertEquals(
        rendered,
        """Metric expectations failed:
          |1. closest metric 'service.counter' mismatched:
          |  - type mismatch: expected 'LONG_GAUGE', got 'LONG_SUM'
          |2. no metric matched the expectation; available metrics: [service.counter]""".stripMargin
      )
    }
  }

  testkitTest("format renders distinct matching failures") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val rendered = MetricExpectations.checkAllDistinct(
        metrics,
        MetricExpectation.sum[Long]("service.counter").withValue(1L),
        MetricExpectation.sum[Long]("service.counter").withValue(1L)
      ) match {
        case Left(mismatches) => MetricExpectations.format(mismatches)
        case Right(_)         => fail("expected mismatches, got success")
      }

      assertEquals(
        rendered,
        """Metric expectations failed:
          |1. no distinct metric remained for the expectation; matched metrics: [service.counter]""".stripMargin
      )
    }
  }

  private def testkitTest[A](options: TestOptions)(body: MetricsTestkit[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(MetricsTestkit.inMemory[IO]().use(body))

  private def formatFailures(metrics: List[MetricData], expectations: MetricExpectation*): String =
    MetricExpectations.checkAll(metrics, expectations.toList) match {
      case Left(mismatches) => MetricExpectations.format(mismatches)
      case Right(_)         => fail("expected mismatches, got success")
    }
}
