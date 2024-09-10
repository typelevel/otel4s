/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.metrics

import cats.effect.IO
import cats.effect.Resource
import cats.effect.testkit.TestControl
import cats.mtl.Local
import cats.syntax.traverse._
import munit.CatsEffectSuite
import munit.Location
import munit.TestOptions
import org.typelevel.otel4s.Attributes

import java.util.concurrent.TimeUnit
import scala.concurrent.duration._

abstract class BaseMeterSuite extends CatsEffectSuite {
  import BaseMeterSuite._

  type Ctx

  sdkTest("Counter - accept only positive values") { sdk =>
    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.counter[Long]("counter").create
      _ <- counter.add(-1L)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, Nil)
  }

  sdkTest("Counter - record values") { sdk =>
    val expected = MetricData.sum("counter", monotonic = true, 5L, Some(5L))

    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.counter[Long]("counter").create
      _ <- counter.add(5L)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("Counter - increment") { sdk =>
    val expected = MetricData.sum("counter", monotonic = true, 1L, Some(1L))

    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.counter[Long]("counter").create
      _ <- counter.inc()
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("Gauge - record values") { sdk =>
    val expected = MetricData.gauge("gauge", 1L, Some(1L))

    for {
      meter <- sdk.provider.get("meter")
      gauge <- meter.gauge[Long]("gauge").create
      _ <- gauge.record(1L)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("UpDownCounter - record values") { sdk =>
    val expected = MetricData.sum("counter", monotonic = false, 3L, Some(3L))

    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.upDownCounter[Long]("counter").create
      _ <- counter.add(3L)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("UpDownCounter - increment") { sdk =>
    val expected = MetricData.sum("counter", monotonic = false, 1L, Some(1L))

    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.upDownCounter[Long]("counter").create
      _ <- counter.inc()
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("UpDownCounter - decrement") { sdk =>
    val expected = MetricData.sum("counter", monotonic = false, -1L, Some(-1L))

    for {
      meter <- sdk.provider.get("meter")
      counter <- meter.upDownCounter[Long]("counter").create
      _ <- counter.dec()
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("Histogram - allow only non-negative values") { sdk =>
    for {
      meter <- sdk.provider.get("meter")
      histogram <- meter.histogram[Double]("histogram").create
      _ <- histogram.record(-1.0)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, Nil)
  }

  sdkTest("Histogram - record values") { sdk =>
    val values = List(1.0, 2.0, 3.0)
    val expected = MetricData.histogram(
      "histogram",
      values,
      exemplarValue = Some(3.0)
    )

    for {
      meter <- sdk.provider.get("meter")
      histogram <- meter.histogram[Double]("histogram").create
      _ <- values.traverse(value => histogram.record(value))
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("Histogram - record duration") { sdk =>
    val duration = 100.nanos
    val expected = MetricData.histogram(
      "histogram",
      List(duration.toNanos.toDouble)
    )

    TestControl.executeEmbed {
      for {
        meter <- sdk.provider.get("meter")
        histogram <- meter.histogram[Double]("histogram").create
        _ <- histogram
          .recordDuration(TimeUnit.NANOSECONDS)
          .surround(IO.sleep(duration))
        metrics <- sdk.collectMetrics
      } yield assertEquals(metrics, List(expected))
    }
  }

  sdkTest("Histogram - use explicit bucket boundaries") { sdk =>
    val boundaries = BucketBoundaries(1.0, 2.0, 3.0)
    val expected = MetricData.histogram(
      name = "histogram",
      values = List(1.0),
      boundaries = boundaries,
      exemplarValue = Some(1.0)
    )

    for {
      meter <- sdk.provider.get("meter")
      histogram <- meter
        .histogram[Double]("histogram")
        .withExplicitBucketBoundaries(boundaries)
        .create
      _ <- histogram.record(1.0)
      metrics <- sdk.collectMetrics
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("ObservableCounter - record values") { sdk =>
    val expected = MetricData.sum("counter", monotonic = true, 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableCounter[Long]("counter")
        .createWithCallback(cb => cb.record(1L))
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest(
    "ObservableCounter - multiple values for same attributes - retain first"
  ) { sdk =>
    val expected = MetricData.sum("counter", monotonic = true, 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableCounter[Long]("counter")
        .createWithCallback { cb =>
          cb.record(1L) >> cb.record(2L) >> cb.record(3L)
        }
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("ObservableUpDownCounter - record values") { sdk =>
    val expected = MetricData.sum("counter", monotonic = false, 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableUpDownCounter[Long]("counter")
        .createWithCallback(cb => cb.record(1L))
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest(
    "ObservableUpDownCounter - multiple values for same attributes - retain first"
  ) { sdk =>
    val expected = MetricData.sum("counter", monotonic = false, 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableUpDownCounter[Long]("counter")
        .createWithCallback { cb =>
          cb.record(1L) >> cb.record(2L) >> cb.record(3L)
        }
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest("ObservableGauge - record values") { sdk =>
    val expected = MetricData.gauge("gauge", 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableGauge[Long]("gauge")
        .createWithCallback(cb => cb.record(1L))
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  sdkTest(
    "ObservableGauge - multiple values for same attributes - retain first"
  ) { sdk =>
    val expected = MetricData.gauge("gauge", 1L)

    for {
      meter <- sdk.provider.get("meter")
      metrics <- meter
        .observableGauge[Long]("gauge")
        .createWithCallback { cb =>
          cb.record(1L) >> cb.record(2L) >> cb.record(3L)
        }
        .surround(sdk.collectMetrics)
    } yield assertEquals(metrics, List(expected))
  }

  private def sdkTest[A](
      options: TestOptions
  )(body: Sdk[Ctx] => IO[A])(implicit loc: Location): Unit = {
    val io = makeSdk.use { sdk =>
      sdk.local.scope(body(sdk))(tracedContext(TraceId, SpanId))
    }
    test(options)(transform(io))
  }

  protected def tracedContext(traceId: String, spanId: String): Ctx

  protected def transform[A](io: IO[A]): IO[A] = io

  protected def makeSdk: Resource[IO, Sdk[Ctx]]

}

object BaseMeterSuite {

  private val TraceId = "c0d6e01941825730ffedcd00768eb663"
  private val SpanId = "69568a2f0ba45094"

  sealed trait AggregationTemporality
  object AggregationTemporality {
    case object Delta extends AggregationTemporality
    case object Cumulative extends AggregationTemporality
  }

  final case class Exemplar(
      attributes: Attributes,
      timestamp: FiniteDuration,
      traceId: Option[String],
      spanId: Option[String],
      value: Either[Long, Double]
  )

  sealed trait PointData {
    def start: FiniteDuration
    def end: FiniteDuration
    def attributes: Attributes
  }

  object PointData {
    final case class NumberPoint(
        start: FiniteDuration,
        end: FiniteDuration,
        attributes: Attributes,
        value: Either[Long, Double],
        exemplars: Vector[Exemplar]
    ) extends PointData

    final case class Histogram(
        start: FiniteDuration,
        end: FiniteDuration,
        attributes: Attributes,
        sum: Option[Double],
        min: Option[Double],
        max: Option[Double],
        count: Option[Long],
        boundaries: BucketBoundaries,
        counts: Vector[Long],
        exemplars: Vector[Exemplar]
    ) extends PointData
  }

  sealed trait MetricPoints
  object MetricPoints {
    final case class Sum(
        points: Vector[PointData.NumberPoint],
        monotonic: Boolean,
        aggregationTemporality: AggregationTemporality
    ) extends MetricPoints

    final case class Gauge(points: Vector[PointData.NumberPoint]) extends MetricPoints

    final case class Histogram(
        points: Vector[PointData.Histogram],
        aggregationTemporality: AggregationTemporality
    ) extends MetricPoints
  }

  final case class MetricData(
      name: String,
      description: Option[String],
      unit: Option[String],
      data: MetricPoints
  )

  object MetricData {
    private val DefaultBoundaries = BucketBoundaries(
      0.0, 5.0, 10.0, 25.0, 50.0, 75.0, 100.0, 250.0, 500.0, 750.0, 1000.0, 2500.0, 5000.0, 7500.0, 10000.0
    )

    def sum(
        name: String,
        monotonic: Boolean,
        value: Long,
        exemplarValue: Option[Long] = None
    ): MetricData =
      MetricData(
        name,
        None,
        None,
        MetricPoints.Sum(
          points = Vector(
            PointData.NumberPoint(
              Duration.Zero,
              Duration.Zero,
              Attributes.empty,
              Left(value),
              exemplarValue.toVector.map { exemplar =>
                Exemplar(
                  Attributes.empty,
                  Duration.Zero,
                  Some(TraceId),
                  Some(SpanId),
                  Left(exemplar)
                )
              }
            )
          ),
          monotonic = monotonic,
          aggregationTemporality = AggregationTemporality.Cumulative
        )
      )

    def gauge(
        name: String,
        value: Long,
        exemplarValue: Option[Long] = None
    ): MetricData =
      MetricData(
        name,
        None,
        None,
        MetricPoints.Gauge(
          points = Vector(
            PointData.NumberPoint(
              Duration.Zero,
              Duration.Zero,
              Attributes.empty,
              Left(value),
              exemplarValue.toVector.map { exemplar =>
                Exemplar(
                  Attributes.empty,
                  Duration.Zero,
                  Some(TraceId),
                  Some(SpanId),
                  Left(exemplar)
                )
              }
            )
          )
        )
      )

    def histogram(
        name: String,
        values: List[Double],
        boundaries: BucketBoundaries = DefaultBoundaries,
        exemplarValue: Option[Double] = None
    ): MetricData = {
      val counts: Vector[Long] =
        values.foldLeft(Vector.fill(boundaries.length + 1)(0L)) { case (acc, value) =>
          val i = boundaries.boundaries.indexWhere(b => value <= b)
          val idx = if (i == -1) boundaries.length else i

          acc.updated(idx, acc(idx) + 1L)
        }

      MetricData(
        name,
        None,
        None,
        MetricPoints.Histogram(
          points = Vector(
            PointData.Histogram(
              Duration.Zero,
              Duration.Zero,
              Attributes.empty,
              Some(values.sum),
              Some(values.min),
              Some(values.max),
              Some(values.size.toLong),
              boundaries,
              counts,
              exemplarValue.toVector.map { exemplar =>
                Exemplar(
                  Attributes.empty,
                  Duration.Zero,
                  Some(TraceId),
                  Some(SpanId),
                  Right(exemplar)
                )
              }
            )
          ),
          aggregationTemporality = AggregationTemporality.Cumulative
        )
      )
    }

  }

  trait Sdk[Ctx] {
    def provider: MeterProvider[IO]
    def collectMetrics: IO[List[MetricData]]
    def local: Local[IO, Ctx]
  }

}
