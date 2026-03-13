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
import org.typelevel.otel4s.oteljava.testkit.{AttributesExpectation, InstrumentationScopeExpectation}

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
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        List(MetricExpectation.sum[Long]("service.counter").withValue(1L))
      )
    )
  }

  testkitTest("match double values using the default number comparison") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      gauge <- meter.gauge[Double]("service.gauge").create
      _ <- gauge.record(0.1d + 0.2d)
      metrics <- testkit.collectMetrics[MetricData]
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        List(MetricExpectation.gauge[Double]("service.gauge").withValue(0.3d))
      )
    )
  }

  testkitTest("match by exact attributes") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("http.method", "GET")))
      metrics <- testkit.collectMetrics[MetricData]
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        List(
          MetricExpectation
            .sum[Long]("service.counter")
            .withAnyPoint(
              PointExpectation
                .numeric(1L)
                .withAttributesExact(Attribute("http.method", "GET"))
            )
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
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        List(
          MetricExpectation
            .sum[Long]("service.counter")
            .withAnyPoint(
              PointExpectation
                .numeric(1L)
                .withAttributesSubset(Attribute("http.method", "GET"))
            )
        )
      )
    )
  }

  testkitTest("checkAll returns unmatched expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectMetrics[MetricData]
    } yield {
      val result = MetricExpectations.checkAll(
        metrics,
        List(
          MetricExpectation.sum[Long]("service.counter").withValue(1L),
          MetricExpectation.gauge[Long]("service.gauge")
        )
      )

      assert(result.isLeft)
      val mismatches = result.swap.toOption.get
      assertEquals(mismatches.length, 1)
      assertEquals(
        mismatches.head,
        MetricMismatch.notFound(
          MetricExpectation.gauge[Long]("service.gauge"),
          List("service.counter")
        )
      )
    }
  }

  testkitTest("check returns closest mismatch when metric name matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectMetrics[MetricData]
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withScope(
            InstrumentationScopeExpectation
              .name("test")
              .withAttributes(AttributesExpectation.subset(Attributes(Attribute("scope.attr", "value"))))
              .withAttributes(AttributesExpectation.subset(Attributes(Attribute("scope.attr", "value"))))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          val metric = mismatch.metric
          val mismatches = mismatch.mismatches
          assertEquals(metric.getName, "service.counter")
          assertEquals(
            mismatches.toList,
            List(
              MetricExpectation.Mismatch.scopeMismatch(
                cats.data.NonEmptyList.one(
                  InstrumentationScopeExpectation.Mismatch.attributesMismatch(
                    cats.data.NonEmptyList.one(
                      AttributesExpectation.Mismatch.missingAttribute(Attribute("scope.attr", "value"))
                    )
                  )
                )
              )
            )
          )
          assertEquals(
            MetricExpectations.format(
              cats.data.NonEmptyList.one(
                MetricMismatch.closestMismatch(
                  MetricExpectation
                    .sum[Long]("service.counter")
                    .withScope(
                      InstrumentationScopeExpectation
                        .name("test")
                        .withAttributes(AttributesExpectation.subset(Attributes(Attribute("scope.attr", "value"))))
                    ),
                  metric,
                  mismatches
                )
              )
            ),
            "Metric expectations failed:\n1. closest metric 'service.counter' mismatched:\n  - scope mismatch: attributes mismatch: missing attribute String(scope.attr)=value"
          )
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
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
              .numeric(1L)
              .withAttributesSubset(Attribute("region", "eu"))
          )
      )
    )
  }

  testkitTest("check returns structured point mismatches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(2L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectMetrics[MetricData]
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .numeric(1L)
              .withAttributesSubset(Attribute("region", "eu"))
              .withClue("expected EU point")
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          val metric = mismatch.metric
          val mismatches = mismatch.mismatches
          assertEquals(metric.getName, "service.counter")
          assertEquals(
            mismatches.toList,
            List(
              MetricExpectation.Mismatch.pointsMismatch(
                "any",
                cats.data.NonEmptyList.of(
                  PointExpectation.Mismatch.valueMismatch("1", "2"),
                  PointExpectation.Mismatch.attributesMismatch(
                    cats.data.NonEmptyList.one(
                      AttributesExpectation.Mismatch.attributeValueMismatch(
                        Attribute("region", "eu"),
                        Attribute("region", "us")
                      )
                    )
                  )
                ),
                Some("expected EU point")
              )
            )
          )
          assertEquals(
            MetricExpectations.format(cats.data.NonEmptyList.one(result.get)),
            "Metric expectations failed:\n1. closest metric 'service.counter' mismatched:\n  - points mismatch (any [expected EU point]): value mismatch: expected '1', got '2', attributes mismatch: attribute mismatch for 'region': expected String(region)=eu, got String(region)=us"
          )
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("point mismatches keep only the closest failing point") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(2L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectMetrics[MetricData]
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withAnyPoint(
            PointExpectation
              .numeric(1L)
              .withAttributesSubset(Attribute("region", "eu"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          val mismatches = mismatch.mismatches
          assertEquals(mismatches.length, 1)
          mismatches.head match {
            case pointMismatch: MetricExpectation.Mismatch.PointsMismatch =>
              assertEquals(pointMismatch.mode, "any")
              assertEquals(pointMismatch.clue, None)
              val pointMismatches = pointMismatch.mismatches
              assertEquals(pointMismatches.length, 1)
              assert(
                pointMismatches.head == PointExpectation.Mismatch.valueMismatch("1", "2") ||
                  pointMismatches.head == PointExpectation.Mismatch.attributesMismatch(
                    cats.data.NonEmptyList.one(
                      AttributesExpectation.Mismatch.attributeValueMismatch(
                        Attribute("region", "eu"),
                        Attribute("region", "us")
                      )
                    )
                  )
              )
            case other =>
              fail(s"expected point mismatch, got $other")
          }
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  private def testkitTest[A](
      options: TestOptions,
  )(body: MetricsTestkit[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(MetricsTestkit.inMemory[IO]().use(body))

  private def assertSuccess(result: Either[cats.data.NonEmptyList[MetricMismatch], Unit]): Unit =
    result match {
      case Right(_) =>
        ()
      case Left(mismatches) =>
        fail(MetricExpectations.format(mismatches))
    }

}
