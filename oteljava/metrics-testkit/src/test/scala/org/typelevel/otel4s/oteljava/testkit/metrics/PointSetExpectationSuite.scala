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

import cats.data.NonEmptyList
import cats.effect.IO
import io.opentelemetry.sdk.metrics.data.{HistogramPointData => JHistogramPointData}
import io.opentelemetry.sdk.metrics.data.MetricData
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

import scala.jdk.CollectionConverters._

class PointSetExpectationSuite extends CatsEffectSuite {

  testkitTest("any matches arbitrary point collections") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L)
      points <- collectLongPoints(testkit, "service.counter")
    } yield assertSuccess(PointSetExpectation.any[PointExpectation.NumericPointData[Long]].check(points))
  }

  testkitTest("exists matches when at least one point satisfies the expectation") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield assertSuccess(
      PointSetExpectation
        .exists(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")))
        .check(points)
    )
  }

  testkitTest("exists returns MissingExpectedPoint when no point matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val result = PointSetExpectation
        .exists(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")).withClue("US point"))
        .check(points)

      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.MissingExpectedPoint](result)
      assertEquals(mismatch.clue, Some("US point"))
      assert(mismatch.message.contains("missing expected point"))
    }
  }

  testkitTest("forall succeeds when every point matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("kind", "ok"), Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("kind", "ok"), Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield assertSuccess(
      PointSetExpectation
        .forall(PointExpectation.numeric(1L).withAttributesSubset(Attribute("kind", "ok")))
        .check(points)
    )
  }

  test("forall fails on an empty point set") {
    val result = PointSetExpectation
      .forall(PointExpectation.numeric(1L))
      .check(Nil)

    assertMismatchType[PointSetExpectation.Mismatch.NoPointsCollected](result)
  }

  testkitTest("forall reports the first failing point") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("kind", "ok")))
      _ <- counter.add(2L, Attributes(Attribute("kind", "bad")))
      _ <- counter.add(1L, Attributes(Attribute("kind", "ok")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val ordered = points.sortBy(_.value)(Ordering.Long.reverse)
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.FailingPoint](
        PointSetExpectation
          .forall(PointExpectation.numeric(1L).withAttributesSubset(Attribute("kind", "ok")))
          .check(ordered)
      )

      assertEquals(mismatch.index, 0)
      assert(mismatch.message.contains("failing point at index 0"))
    }
  }

  testkitTest("contains succeeds for distinct matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield assertSuccess(
      PointSetExpectation
        .contains(
          PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")),
          PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us"))
        )
        .check(points)
    )
  }

  testkitTest("contains enforces distinct matching for duplicate expectations") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.MatchedPointCountMismatch](
        PointSetExpectation
          .contains(
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu"))
          )
          .check(points)
      )

      assertEquals(mismatch.expected, 2)
      assertEquals(mismatch.actual, 1)
    }
  }

  testkitTest("contains reports missing expected points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.MissingExpectedPoint](
        PointSetExpectation
          .contains(
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")).withClue("US point")
          )
          .check(points)
      )

      assertEquals(mismatch.clue, Some("US point"))
    }
  }

  testkitTest("exactly rejects extra points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      _ <- counter.add(1L, Attributes(Attribute("region", "apac")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.UnexpectedPoint](
        PointSetExpectation
          .exactly(
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")),
            PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us"))
          )
          .check(points)
      )

      assert(mismatch.index >= 0 && mismatch.index < points.length)
    }
  }

  testkitTest("count validates exact cardinality") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.PointCountMismatch](
        PointSetExpectation.count[PointExpectation.NumericPointData[Long]](1).check(points)
      )

      assertEquals(mismatch.expected, 1)
      assertEquals(mismatch.actual, 2)
    }
  }

  testkitTest("minCount and maxCount validate bounds") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val minMismatch = assertMismatchType[PointSetExpectation.Mismatch.MinimumPointCountMismatch](
        PointSetExpectation.minCount[PointExpectation.NumericPointData[Long]](3).check(points)
      )
      val maxMismatch = assertMismatchType[PointSetExpectation.Mismatch.MaximumPointCountMismatch](
        PointSetExpectation.maxCount[PointExpectation.NumericPointData[Long]](1).check(points)
      )

      assertEquals(minMismatch.expectedAtLeast, 3)
      assertEquals(minMismatch.actual, 2)
      assertEquals(maxMismatch.expectedAtMost, 1)
      assertEquals(maxMismatch.actual, 2)
    }
  }

  testkitTest("countWhere counts only matching points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu"), Attribute("host", "a")))
      _ <- counter.add(1L, Attributes(Attribute("region", "eu"), Attribute("host", "b")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      assertSuccess(
        PointSetExpectation
          .countWhere(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")), 2)
          .check(points)
      )

      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.MatchedPointCountMismatch](
        PointSetExpectation
          .countWhere(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")), 1)
          .check(points)
      )

      assertEquals(mismatch.expected, 1)
      assertEquals(mismatch.actual, 2)
    }
  }

  testkitTest("none rejects matching points") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.UnexpectedPoint](
        PointSetExpectation
          .none(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")))
          .check(points)
      )

      assertEquals(mismatch.index, 0)
    }
  }

  testkitTest("predicate exposes collection-wide checks and clue") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      _ <- counter.add(1L, Attributes(Attribute("region", "us")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      assertSuccess(
        PointSetExpectation
          .predicate[PointExpectation.NumericPointData[Long]] {
            (points: List[PointExpectation.NumericPointData[Long]]) =>
              points.map(_.attributes).size == 2
          }
          .check(points)
      )

      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.PredicateFailed](
        PointSetExpectation
          .predicate[PointExpectation.NumericPointData[Long]]("expected a single point")(_.size == 1)
          .check(points)
      )

      assertEquals(mismatch.clue, Some("expected a single point"))
    }
  }

  testkitTest("and combines nested mismatches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.CompositeMismatch](
        PointSetExpectation
          .count[PointExpectation.NumericPointData[Long]](2)
          .and(
            PointSetExpectation.contains(
              PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")).withClue("US point")
            )
          )
          .check(points)
      )

      assertEquals(mismatch.operator, PointSetExpectation.LogicalOperator.And)
      assertEquals(mismatch.mismatches.length, 2)
    }
  }

  testkitTest("or succeeds when either branch matches and reports both when neither matches") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      counter <- meter.counter[Long]("service.counter").create
      _ <- counter.add(1L, Attributes(Attribute("region", "eu")))
      points <- collectLongPoints(testkit, "service.counter")
    } yield {
      assertSuccess(
        PointSetExpectation
          .contains(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "eu")))
          .or(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](3))
          .check(points)
      )

      val mismatch = assertMismatchType[PointSetExpectation.Mismatch.CompositeMismatch](
        PointSetExpectation
          .contains(PointExpectation.numeric(1L).withAttributesSubset(Attribute("region", "us")))
          .or(PointSetExpectation.count[PointExpectation.NumericPointData[Long]](2))
          .check(points)
      )

      assertEquals(mismatch.operator, PointSetExpectation.LogicalOperator.Or)
      assertEquals(mismatch.mismatches.length, 2)
    }
  }

  testkitTest("histogram point sets are supported directly") { testkit =>
    for {
      meter <- testkit.meterProvider.get("test")
      histogram <- meter.histogram[Long]("service.histogram").create
      _ <- histogram.record(10L, Attributes(Attribute("region", "eu")))
      _ <- histogram.record(20L, Attributes(Attribute("region", "us")))
      points <- collectHistogramPoints(testkit, "service.histogram")
    } yield assertSuccess(
      PointSetExpectation
        .contains(
          PointExpectation.histogram.withCount(1L).withSum(10.0).withAttributesSubset(Attribute("region", "eu")),
          PointExpectation.histogram.withCount(1L).withSum(20.0).withAttributesSubset(Attribute("region", "us"))
        )
        .check(points)
    )
  }

  private def testkitTest[A](options: TestOptions)(body: MetricsTestkit[IO] => IO[A])(implicit loc: Location): Unit =
    test(options)(MetricsTestkit.inMemory[IO]().use(body))

  private def collectLongPoints(
      testkit: MetricsTestkit[IO],
      name: String
  ): IO[List[PointExpectation.NumericPointData[Long]]] =
    testkit.collectAllMetrics.map { metrics =>
      metricByName(metrics, name).getLongSumData.getPoints.asScala.toList
        .map(PointExpectation.LongNumericPointData.apply)
    }

  private def collectHistogramPoints(
      testkit: MetricsTestkit[IO],
      name: String
  ): IO[List[JHistogramPointData]] =
    testkit.collectAllMetrics.map { metrics =>
      metricByName(metrics, name).getHistogramData.getPoints.asScala.toList
    }

  private def metricByName(metrics: List[MetricData], name: String): MetricData =
    metrics.find(_.getName == name).getOrElse(fail(s"metric $name was not collected"))

  private def assertSuccess(result: Either[NonEmptyList[PointSetExpectation.Mismatch], Unit]): Unit =
    result match {
      case Right(_)         => ()
      case Left(mismatches) => fail(mismatches.toList.map(_.message).mkString(", "))
    }

  private def assertMismatchType[A <: PointSetExpectation.Mismatch](
      result: Either[NonEmptyList[PointSetExpectation.Mismatch], Unit]
  ): A =
    result match {
      case Right(_) =>
        fail("expected mismatch, got success")
      case Left(mismatches) =>
        mismatches.head match {
          case mismatch: A @unchecked => mismatch
          case other                  => fail(s"unexpected mismatch: $other")
        }
    }
}
