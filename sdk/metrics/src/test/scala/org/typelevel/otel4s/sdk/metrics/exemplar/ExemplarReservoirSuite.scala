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

package org.typelevel.otel4s.sdk.metrics.exemplar

import cats.effect.IO
import cats.effect.SyncIO
import cats.effect.std.Random
import cats.effect.testkit.TestControl
import munit.CatsEffectSuite
import munit.ScalaCheckEffectSuite
import org.scalacheck.effect.PropF
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.data.ExemplarData.TraceContext
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._

import scala.concurrent.duration._

class ExemplarReservoirSuite
    extends CatsEffectSuite
    with ScalaCheckEffectSuite {

  private val traceContextKey = Context.Key
    .unique[SyncIO, TraceContext]("trace-context")
    .unsafeRunSync()

  private val contextLookup: TraceContextLookup =
    _.get(traceContextKey)

  private val boundaries: BucketBoundaries =
    BucketBoundaries(Vector(1.0, 5.0, 10.0))

  //
  // Fixed size
  //

  test("fixed size - no measurements - return empty exemplar") {
    Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
      for {
        reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
        result <- reservoir.collectAndReset(Attributes.empty)
      } yield assertEquals(result, Vector.empty)
    }
  }

  test("fixed size - extract trace context") {
    PropF.forAllF { (value: Long, attrs: Attributes, trace: TraceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, trace)

        val expected = Exemplar(
          filteredAttributes = attrs,
          timestamp = Duration.Zero,
          traceContext = Some(trace),
          value = value
        )

        TestControl.executeEmbed {
          for {
            reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
            _ <- reservoir.offer(value, attrs, ctx)
            result <- reservoir.collectAndReset(Attributes.empty)
          } yield assertEquals(result, Vector(expected))
        }
      }
    }
  }

  test("fixed size - filter attributes (keep unique)") {
    PropF.forAllF { (value: Long, trace: TraceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val ctx = Context.root.updated(traceContextKey, trace)

        val string = Attribute("string", "value")
        val long = Attribute("long", 1L)

        val exemplarAttributes = Attributes(string, long)
        val metricPointAttributes = Attributes(string)

        val expected = Exemplar(
          filteredAttributes = Attributes(long),
          timestamp = Duration.Zero,
          traceContext = Some(trace),
          value = value
        )

        TestControl.executeEmbed {
          for {
            reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
            _ <- reservoir.offer(value, exemplarAttributes, ctx)
            result <- reservoir.collectAndReset(metricPointAttributes)
          } yield assertEquals(result, Vector(expected))
        }
      }
    }
  }

  test("fixed size - size = 1 - reset the state after collection") {
    Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
      val ctx = Context.root
      val attrs = Attributes.empty

      def expected(value: Long) = Exemplar(
        filteredAttributes = attrs,
        timestamp = Duration.Zero,
        traceContext = None,
        value = value
      )

      TestControl.executeEmbed {
        for {
          reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
          _ <- reservoir.offer(1L, attrs, ctx)
          result1 <- reservoir.collectAndReset(Attributes.empty)
          _ <- reservoir.offer(2L, attrs, ctx)
          result2 <- reservoir.collectAndReset(Attributes.empty)
        } yield {
          assertEquals(result1, Vector(expected(1L)))
          assertEquals(result2, Vector(expected(2L)))
        }
      }
    }
  }

  test("fixed size - size = 2 - preserve latest samples") {
    // put first 2 samples into bucket 0 and all other into bucket 1
    val rnd = new java.util.Random {
      override def nextInt(bound: Int): Int = {
        bound match {
          case 1 | 2 => 0
          case _     => 1
        }
      }
    }

    Random.javaUtilRandom[IO](rnd).flatMap { implicit R: Random[IO] =>
      val ctx = Context.root

      def exemplar(value: Long) = Exemplar(
        filteredAttributes = Attributes(Attribute("idx", value)),
        timestamp = Duration.Zero,
        traceContext = None,
        value = value
      )

      val expected = Vector(
        exemplar(2L),
        exemplar(3L)
      )

      TestControl.executeEmbed {
        for {
          reservoir <- ExemplarReservoir.fixedSize[IO, Long](2, contextLookup)
          _ <- reservoir.offer(1L, Attributes(Attribute("idx", 1L)), ctx)
          _ <- reservoir.offer(2L, Attributes(Attribute("idx", 2L)), ctx)
          _ <- reservoir.offer(3L, Attributes(Attribute("idx", 3L)), ctx)
          result <- reservoir.collectAndReset(Attributes.empty)
        } yield assertEquals(result, expected)
      }
    }
  }

  //
  // Histogram bucket
  //

  test("histogram bucket - no measurements - return empty exemplar") {
    for {
      reservoir <- ExemplarReservoir.histogramBucket[IO, Long](
        boundaries,
        contextLookup
      )
      result <- reservoir.collectAndReset(Attributes.empty)
    } yield assertEquals(result, Vector.empty)
  }

  test("histogram bucket - extract trace context") {
    PropF.forAllF { (value: Long, attrs: Attributes, trace: TraceContext) =>
      val ctx = Context.root.updated(traceContextKey, trace)

      val expected = Exemplar(
        filteredAttributes = attrs,
        timestamp = Duration.Zero,
        traceContext = Some(trace),
        value = value
      )

      TestControl.executeEmbed {
        for {
          reservoir <- ExemplarReservoir.histogramBucket[IO, Long](
            boundaries,
            contextLookup
          )
          _ <- reservoir.offer(value, attrs, ctx)
          result <- reservoir.collectAndReset(Attributes.empty)
        } yield assertEquals(result, Vector(expected))
      }
    }
  }

  test("histogram bucket - filter attributes (keep unique)") {
    PropF.forAllF { (value: Long, trace: TraceContext) =>
      val ctx = Context.root.updated(traceContextKey, trace)

      val string = Attribute("string", "value")
      val long = Attribute("long", 1L)

      val exemplarAttributes = Attributes(string, long)
      val metricPointAttributes = Attributes(string)

      val expected = Exemplar(
        filteredAttributes = Attributes(long),
        timestamp = Duration.Zero,
        traceContext = Some(trace),
        value = value
      )

      TestControl.executeEmbed {
        for {
          reservoir <- ExemplarReservoir.histogramBucket[IO, Long](
            boundaries,
            contextLookup
          )
          _ <- reservoir.offer(value, exemplarAttributes, ctx)
          result <- reservoir.collectAndReset(metricPointAttributes)
        } yield assertEquals(result, Vector(expected))
      }
    }
  }

  test("histogram bucket - multiple bucket - preserve latest samples") {
    PropF.forAllF { (trace: TraceContext) =>
      val ctx = Context.root.updated(traceContextKey, trace)

      def exemplar(value: Long, idx: Long) = Exemplar(
        filteredAttributes = Attributes(Attribute("idx", idx)),
        timestamp = Duration.Zero,
        traceContext = Some(trace),
        value = value
      )

      val expected = Vector(
        exemplar(1L, 1L),
        exemplar(2L, 2L),
        exemplar(10L, 4L),
        exemplar(11L, 5L)
      )

      TestControl.executeEmbed {
        for {
          reservoir <- ExemplarReservoir.histogramBucket[IO, Long](
            boundaries,
            contextLookup
          )
          _ <- reservoir.offer(1L, Attributes(Attribute("idx", 1L)), ctx)
          _ <- reservoir.offer(2L, Attributes(Attribute("idx", 2L)), ctx)
          _ <- reservoir.offer(7L, Attributes(Attribute("idx", 3L)), ctx)
          _ <- reservoir.offer(10L, Attributes(Attribute("idx", 4L)), ctx)
          _ <- reservoir.offer(11L, Attributes(Attribute("idx", 5L)), ctx)
          result <- reservoir.collectAndReset(Attributes.empty)
        } yield assertEquals(result, expected)
      }
    }
  }

  //
  // Filtered
  //

  test("filtered - always off filter - prevent sampling") {
    PropF.forAllF { (value: Long, attrs: Attributes, trace: TraceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val filter = ExemplarFilter.alwaysOff
        val ctx = Context.root.updated(traceContextKey, trace)

        for {
          reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
          filtered = ExemplarReservoir.filtered(filter, reservoir)
          _ <- filtered.offer(value, attrs, ctx)
          result <- filtered.collectAndReset(Attributes.empty)
        } yield assertEquals(result, Vector.empty)
      }
    }
  }

  test("filtered - always on filter - sample all") {
    PropF.forAllF { (value: Long, attrs: Attributes, trace: TraceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val filter = ExemplarFilter.alwaysOn
        val ctx = Context.root.updated(traceContextKey, trace)
        val expected = Exemplar(
          filteredAttributes = attrs,
          timestamp = Duration.Zero,
          traceContext = Some(trace),
          value = value
        )

        TestControl.executeEmbed {
          for {
            reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
            filtered = ExemplarReservoir.filtered(filter, reservoir)
            _ <- filtered.offer(value, attrs, ctx)
            result <- filtered.collectAndReset(Attributes.empty)
          } yield assertEquals(result, Vector(expected))
        }
      }
    }
  }

  test("filtered - trace based filter - sample according to the context") {
    PropF.forAllF { (value: Long, attrs: Attributes, trace: TraceContext) =>
      Random.scalaUtilRandom[IO].flatMap { implicit R: Random[IO] =>
        val filter = ExemplarFilter.traceBased(contextLookup)
        val ctx = Context.root.updated(traceContextKey, trace)
        val exemplar = Exemplar(
          filteredAttributes = attrs,
          timestamp = Duration.Zero,
          traceContext = Some(trace),
          value = value
        )
        val expected = if (trace.isSampled) Vector(exemplar) else Vector.empty

        TestControl.executeEmbed {
          for {
            reservoir <- ExemplarReservoir.fixedSize[IO, Long](1, contextLookup)
            filtered = ExemplarReservoir.filtered(filter, reservoir)
            _ <- filtered.offer(value, attrs, ctx)
            result <- filtered.collectAndReset(Attributes.empty)
          } yield assertEquals(result, expected)
        }
      }
    }
  }

}
