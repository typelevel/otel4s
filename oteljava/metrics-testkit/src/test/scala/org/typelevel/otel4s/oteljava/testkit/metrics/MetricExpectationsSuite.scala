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
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation

import java.util.concurrent.atomic.AtomicInteger

class MetricExpectationsSuite extends CatsEffectSuite {

  testkitTest("match by name only") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.inc()
      metrics <- testkit.collectAllMetrics
    } yield assertEquals(MetricExpectations.check(metrics, MetricExpectation.name("service.counter")), None)
  }

  testkitTest("match by kind and value") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(metrics, MetricExpectation.sum[Long]("service.counter").value(1L))
    )
  }

  testkitTest("metric-level predicate is supported") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .where("single point expected")(_.getLongSumData.getPoints.size() == 1)
      )
    )
  }

  testkitTest("typed metric-level predicate is skipped for wrong-type candidates") { testkit =>
    val predicateRuns = new AtomicInteger(0)

    for {
      meter <- testkit.meterProvider.get("test")
      gauge <- meter.gauge[Long]("service.counter").create
      _ <- gauge.record(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .where { _ =>
            predicateRuns.incrementAndGet()
            true
          }
      )

      assertEquals(predicateRuns.get(), 0)
      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          assertEquals(mismatch.metric.getName, "service.counter")
          assertEquals(mismatch.mismatches.length, 1)
          assert(mismatch.mismatches.head.isInstanceOf[MetricExpectation.Mismatch.TypeMismatch])
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("closest mismatch prefers same-type candidates over wrong-type ones") { testkit =>
    for {
      gaugeMeter <- testkit.meterProvider.get("gauge-scope")
      sumMeter <- testkit.meterProvider.get("sum-scope")
      gauge <- gaugeMeter.gauge[Long]("service.metric").create
      counter <- sumMeter.counter[Long]("service.metric").create
      _ <- gauge.record(1L)
      _ <- counter.add(2L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.metric")
          .value(1L)
          .scope(
            InstrumentationScopeExpectation
              .name("test")
              .attributesSubset(Attribute("scope.attr", "value"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          assertEquals(mismatch.metric.getType, io.opentelemetry.sdk.metrics.data.MetricDataType.LONG_SUM)
          assert(!mismatch.mismatches.exists(_.isInstanceOf[MetricExpectation.Mismatch.TypeMismatch]))
          assert(mismatch.mismatches.exists(_.isInstanceOf[MetricExpectation.Mismatch.PointsMismatch]))
          assert(mismatch.mismatches.exists(_.isInstanceOf[MetricExpectation.Mismatch.ScopeMismatch]))
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("untyped metric-level predicate still runs regardless of metric type") { testkit =>
    val predicateRuns = new AtomicInteger(0)

    for {
      meter <- testkit.meterProvider.get("test")
      gauge <- meter.gauge[Long]("service.gauge").create
      _ <- gauge.record(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .name("service.gauge")
          .where { _ =>
            predicateRuns.incrementAndGet()
            true
          }
      )

      assertEquals(predicateRuns.get(), 1)
      assertSuccess(result)
    }
  }

  testkitTest("typed numeric point predicates are supported") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("http.method", "GET")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation.exists(
              PointExpectation
                .numeric(1L)
                .where("GET point expected") { point =>
                  point.value == 1L && point.attributes == Attributes(Attribute("http.method", "GET"))
                }
            )
          )
      )
    )
  }

  testkitTest("multiple point constraints accumulate") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation.exists(PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")))
          )
          .points(
            PointSetExpectation.exists(PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us")))
          )
      )
    )
  }

  testkitTest("repeated exists constraints are non-consuming") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation.exists(PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")))
          )
          .points(
            PointSetExpectation.exists(PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")))
          )
      )
    )
  }

  testkitTest("containsPoints matches multiple distinct points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .containsPoints(
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us"))
          )
      )
    )
  }

  testkitTest("containsPoints uses distinct matching for duplicate expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .containsPoints(
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          mismatch.mismatches.head match {
            case pointsMismatch: MetricExpectation.Mismatch.PointsMismatch =>
              assertEquals(pointsMismatch.mismatches.length, 1)
              assert(
                pointsMismatch.mismatches.head.isInstanceOf[PointSetExpectation.Mismatch.MatchedPointCountMismatch]
              )
            case other =>
              fail(s"expected points mismatch, got $other")
          }
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("exactlyPoints succeeds when the set matches exactly") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .exactlyPoints(
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us"))
          )
      )
    )
  }

  testkitTest("exactlyPoints rejects extra points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      _ <- counter.add(1L, Attributes(Attribute("region", "apac")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .exactlyPoints(
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          assertEquals(mismatch.metric.getName, "service.counter")
          assertEquals(mismatch.mismatches.length, 1)
          mismatch.mismatches.head match {
            case pointsMismatch: MetricExpectation.Mismatch.PointsMismatch =>
              assertEquals(pointsMismatch.mismatches.length, 1)
              assert(pointsMismatch.mismatches.head.isInstanceOf[PointSetExpectation.Mismatch.UnexpectedPoint])
            case other =>
              fail(s"expected points mismatch, got $other")
          }
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("withoutPointsMatching rejects forbidden points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .withoutPointsMatching(
            PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          mismatch.mismatches.head match {
            case pointsMismatch: MetricExpectation.Mismatch.PointsMismatch =>
              assertEquals(pointsMismatch.mismatches.length, 1)
              assert(pointsMismatch.mismatches.head.isInstanceOf[PointSetExpectation.Mismatch.UnexpectedPoint])
            case other =>
              fail(s"expected points mismatch, got $other")
          }
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("withAllPoints reports the first failing point") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(2L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation.forall(
              PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu"))
            )
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          mismatch.mismatches.head match {
            case pointsMismatch: MetricExpectation.Mismatch.PointsMismatch =>
              assert(pointsMismatch.mismatches.head.isInstanceOf[PointSetExpectation.Mismatch.FailingPoint])
            case other =>
              fail(s"expected points mismatch, got $other")
          }
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("countWhere counts only matching points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu"), Attribute("host", "a")))
      _ <- counter.add(1L, Attributes(Attribute("region", "eu"), Attribute("host", "b")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation.countWhere(
              PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
              2
            )
          )
      )
    )
  }

  testkitTest("point-set and combines expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation
              .contains(
                PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu")),
                PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us"))
              )
              .and(PointSetExpectation.count(2))
          )
      )
    )
  }

  testkitTest("point-set or allows alternative shapes") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation
              .contains(
                PointExpectation.numeric(1L).attributesSubset(Attribute("region", "eu"))
              )
              .or(
                PointSetExpectation.contains(
                  PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us"))
                )
              )
          )
      )
    )
  }

  testkitTest("pointsWhere supports collection-wide point assertions") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .pointsWhere("expected exactly EU and US points") { points =>
            val actual = points.map(_.attributes).toSet
            actual == Set(
              Attributes(Attribute("region", "eu")),
              Attributes(Attribute("region", "us"))
            )
          }
      )
    )
  }

  testkitTest("histogram metrics support point-set constraints") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      histogram <- meter.histogram[Long]("service.histogram").create
      _ <- histogram.record(10L, Attributes(Attribute("region", "eu")))
      _ <- histogram.record(20L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .histogram("service.histogram")
          .containsPoints(
            PointExpectation.histogram.count(1L).sum(10.0).attributesSubset(Attribute("region", "eu")),
            PointExpectation.histogram.count(1L).sum(20.0).attributesSubset(Attribute("region", "us"))
          )
          .pointCount(2)
      )
    )
  }

  testkitTest("double gauges support point-set constraints") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      gauge <- meter.gauge[Double]("service.gauge").create
      _ <- gauge.record(10.5, Attributes(Attribute("region", "eu")))
      _ <- gauge.record(20.5, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .gauge[Double]("service.gauge")
          .points(
            PointSetExpectation
              .count(2)
              .and(
                PointSetExpectation.contains(
                  PointExpectation.numeric(10.5).attributesSubset(Attribute("region", "eu")),
                  PointExpectation.numeric(20.5).attributesSubset(Attribute("region", "us"))
                )
              )
          )
      )
    )
  }

  testkitTest("metric mismatch formatting includes nested point-set mismatches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.checkAll(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .points(
            PointSetExpectation
              .count(2)
              .and(
                PointSetExpectation.contains(
                  PointExpectation.numeric(1L).attributesSubset(Attribute("region", "us")).clue("US point")
                )
              )
          )
      )

      result match {
        case Left(mismatches) =>
          val rendered = MetricExpectations.format(mismatches)
          assert(rendered.contains("point count mismatch"))
          assert(rendered.contains("missing expected point"))
        case Right(_) =>
          fail("expected mismatches, got success")
      }
    }
  }

  testkitTest("scope mismatch still reports closest metric") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.check(
        metrics,
        MetricExpectation
          .sum[Long]("service.counter")
          .scope(
            InstrumentationScopeExpectation
              .name("test")
              .attributesSubset(Attribute("scope.attr", "value"))
          )
      )

      result match {
        case Some(mismatch: MetricMismatch.ClosestMismatch) =>
          assertEquals(mismatch.metric.getName, "service.counter")
          assert(mismatch.mismatches.head.isInstanceOf[MetricExpectation.Mismatch.ScopeMismatch])
        case other =>
          fail(s"expected closest mismatch, got $other")
      }
    }
  }

  testkitTest("pointCount uses exact point cardinality") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation.sum[Long]("service.counter").pointCount(2)
      )
    )
  }

  testkitTest("checkAll returns unmatched expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.checkAll(
        metrics,
        MetricExpectation.sum[Long]("service.counter").value(1L),
        MetricExpectation.gauge[Long]("service.gauge")
      )

      assert(result.isLeft)
      val mismatches = result.swap.toOption.get
      assertEquals(mismatches.length, 1)
      assertEquals(
        mismatches.head,
        MetricMismatch.notFound(MetricExpectation.gauge[Long]("service.gauge"), List("service.counter"))
      )
    }
  }

  testkitTest("checkAll uses non-consuming metric matching") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAll(
        metrics,
        MetricExpectation.sum[Long]("service.counter").value(1L),
        MetricExpectation.sum[Long]("service.counter").value(1L)
      )
    )
  }

  testkitTest("checkAllDistinct matches identical expectations to different metrics") { testkit =>
    for {
      meter1 <- testkit.meterProvider.get("scope-1")
      meter2 <- testkit.meterProvider.get("scope-2")
      counter1 <- meter1.counter[Long]("service.counter").create
      counter2 <- meter2.counter[Long]("service.counter").create
      _ <- counter1.add(1L)
      _ <- counter2.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAllDistinct(
        metrics,
        MetricExpectation.sum[Long]("service.counter").value(1L),
        MetricExpectation.sum[Long]("service.counter").value(1L)
      )
    )
  }

  testkitTest("checkAllDistinct handles overlapping broad and narrow candidates") { testkit =>
    for {
      sumMeter1 <- testkit.meterProvider.get("sum-scope-1")
      sumMeter2 <- testkit.meterProvider.get("sum-scope-2")
      gaugeMeter <- testkit.meterProvider.get("gauge-scope")
      counter1 <- sumMeter1.counter[Long]("service.metric").create
      counter2 <- sumMeter2.counter[Long]("service.metric").create
      gauge <- gaugeMeter.gauge[Long]("service.metric").create
      _ <- counter1.add(1L)
      _ <- counter2.add(1L)
      _ <- gauge.record(1L)
      metrics <- testkit.collectAllMetrics
    } yield assertSuccess(
      MetricExpectations.checkAllDistinct(
        metrics,
        MetricExpectation.name("service.metric"),
        MetricExpectation.sum[Long]("service.metric").scopeName("sum-scope-1"),
        MetricExpectation.gauge[Long]("service.metric").scopeName("gauge-scope")
      )
    )
  }

  testkitTest("checkAllDistinct rejects reused metric matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      metrics <- testkit.collectAllMetrics
    } yield {
      val result = MetricExpectations.checkAllDistinct(
        metrics,
        MetricExpectation.sum[Long]("service.counter").value(1L),
        MetricExpectation.sum[Long]("service.counter").value(1L)
      )

      result match {
        case Left(mismatches) =>
          assertEquals(mismatches.length, 1)
          mismatches.head match {
            case mismatch: MetricMismatch.DistinctMatchUnavailable =>
              assertEquals(mismatch.candidateMetricNames, List("service.counter"))
            case other =>
              fail(s"expected distinct-match-unavailable mismatch, got $other")
          }
        case Right(_) =>
          fail("expected mismatches, got success")
      }
    }
  }

  private def testkitTest[A](
      options: TestOptions
  )(body: MetricsTestkit[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(MetricsTestkit.inMemory[IO]().use(body))

  private def assertSuccess(result: Either[cats.data.NonEmptyList[MetricMismatch], Unit]): Unit =
    result match {
      case Right(_)         => ()
      case Left(mismatches) => fail(MetricExpectations.format(mismatches))
    }
}
