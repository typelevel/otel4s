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

package org.typelevel.otel4s
package metrics

import cats.effect.IO
import cats.effect.Ref
import cats.effect.Resource
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.meta.InstrumentMeta

import java.util.concurrent.TimeUnit
import scala.collection.immutable

class MetricsMacroSuite extends CatsEffectSuite {
  import MetricsMacroSuite._

  test("metrics macros accept mixed Scala 3 varargs") {
    val a1 = Attribute("k1", "v1")
    val a2 = Attribute("k2", "v2")
    val a3 = Attribute("k3", "v3")
    val mixed = List(a2, a3)

    val expectedAttributes = Attributes(a1, a2, a3)

    for {
      counter <- inMemoryCounter
      _ <- counter.add(10L, a1, mixed)
      _ <- counter.inc(a1, mixed)
      counterRecords <- counter.records

      upDown <- inMemoryUpDownCounter
      _ <- upDown.add(5L, a1, mixed)
      _ <- upDown.inc(a1, mixed)
      _ <- upDown.dec(a1, mixed)
      upDownRecords <- upDown.records

      gauge <- inMemoryGauge
      _ <- gauge.gauge.record(1.0, a1, mixed)
      gaugeRecords <- gauge.records

      histogram <- inMemoryHistogram
      _ <- histogram.record(2.0, a1, mixed)
      _ <- histogram.recordDuration(TimeUnit.MILLISECONDS, _ => List(a1, a2, a3)).use_
      histogramRecords <- histogram.records
    } yield {
      assertEquals(
        counterRecords,
        List(
          CounterSuite.Record(10L, expectedAttributes),
          CounterSuite.Record(1L, expectedAttributes)
        )
      )

      assertEquals(
        upDownRecords,
        List(
          UpDownCounterSuite.Record(5L, expectedAttributes),
          UpDownCounterSuite.Record(1L, expectedAttributes),
          UpDownCounterSuite.Record(-1L, expectedAttributes)
        )
      )

      assertEquals(
        gaugeRecords,
        List(
          GaugeRecord(1.0, expectedAttributes)
        )
      )

      assertEquals(histogramRecords.map(_.attributes), List(expectedAttributes, expectedAttributes))
    }
  }

  test("noop metrics do not evaluate mixed Scala 3 varargs") {
    val counter = Counter.noop[IO, Long]
    val upDown = UpDownCounter.noop[IO, Long]
    val gauge = Gauge.noop[IO, Double]
    val histogram = Histogram.noop[IO, Double]

    var allocated = false

    def attribute = {
      allocated = true
      Attribute("k1", "v1")
    }

    def attributes = {
      allocated = true
      List(Attribute("k2", "v2"), Attribute("k3", "v3"))
    }

    def durationAttributes(ec: Resource.ExitCase): immutable.Iterable[Attribute[_]] = {
      val _ = ec
      attribute :: attributes
    }

    for {
      _ <- counter.add(1L, attribute, attributes)
      _ <- counter.inc(attribute, attributes)

      _ <- upDown.add(1L, attribute, attributes)
      _ <- upDown.inc(attribute, attributes)
      _ <- upDown.dec(attribute, attributes)

      _ <- gauge.record(1.0, attribute, attributes)

      _ <- histogram.record(1.0, attribute, attributes)
      _ <- histogram.recordDuration(TimeUnit.MILLISECONDS, attribute, attributes).use_
      _ <- histogram.recordDuration(TimeUnit.MILLISECONDS, durationAttributes).use_
    } yield assert(!allocated)
  }

  private def inMemoryCounter: IO[CounterSuite.InMemoryCounter] =
    IO.ref[List[CounterSuite.Record[Long]]](Nil).map(ref => new CounterSuite.InMemoryCounter(ref))

  private def inMemoryUpDownCounter: IO[UpDownCounterSuite.InMemoryUpDownCounter] =
    IO.ref[List[UpDownCounterSuite.Record[Long]]](Nil).map(ref => new UpDownCounterSuite.InMemoryUpDownCounter(ref))

  private def inMemoryHistogram: IO[HistogramSuite.InMemoryHistogram] =
    IO.ref[List[HistogramSuite.Record[Double]]](Nil).map(ref => new HistogramSuite.InMemoryHistogram(ref))

  private def inMemoryGauge: IO[InMemoryGauge] =
    IO.ref[List[GaugeRecord[Double]]](Nil).map(ref => new InMemoryGauge(ref))
}

object MetricsMacroSuite {
  final case class GaugeRecord[A](value: A, attributes: Attributes)

  class InMemoryGauge(ref: Ref[IO, List[GaugeRecord[Double]]]) {
    private val backend: Gauge.Backend[IO, Double] =
      new Gauge.Backend.Unsealed[IO, Double] {
        val meta: InstrumentMeta[IO] = InstrumentMeta.enabled

        def record(
            value: Double,
            attributes: immutable.Iterable[Attribute[_]]
        ): IO[Unit] =
          ref.update(_.appended(GaugeRecord(value, attributes.to(Attributes))))
      }

    val gauge: Gauge[IO, Double] = Gauge.fromBackend(backend)

    def records: IO[List[GaugeRecord[Double]]] = ref.get
  }
}
