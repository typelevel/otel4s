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
import cats.effect.testkit.TestControl
import cats.syntax.functor._
import munit.CatsEffectSuite
import org.typelevel.otel4s.meta.InstrumentMeta

import java.util.concurrent.TimeUnit
import scala.collection.immutable
import scala.concurrent.duration._

class HistogramSuite extends CatsEffectSuite {
  import HistogramSuite._

  test("do not allocate attributes when instrument is noop") {
    val histogram = Histogram.noop[IO, Double]

    var allocated = false

    def allocateAttribute = {
      allocated = true
      List(Attribute("key", "value"))
    }

    // test varargs and Iterable overloads
    for {
      _ <- histogram.record(1.0, allocateAttribute: _*)
      _ <- histogram.record(1.0, allocateAttribute)
      _ <- histogram
        .recordDuration(TimeUnit.SECONDS, allocateAttribute: _*)
        .use_
      _ <- histogram
        .recordDuration(TimeUnit.SECONDS, allocateAttribute)
        .use_
      _ <- histogram
        .recordDuration(TimeUnit.SECONDS, _ => allocateAttribute)
        .use_
    } yield assert(!allocated)
  }

  test("record value and attributes") {
    val attribute = Attribute("key", "value")

    val expected =
      List(
        Record(1.0, Attributes(attribute)),
        Record(-1.0, Attributes.empty),
        Record(2.0, Attributes(attribute, attribute))
      )

    // test varargs and Iterable overloads
    for {
      histogram <- inMemoryHistogram
      _ <- histogram.record(1.0, Attributes(attribute))
      _ <- histogram.record(-1.0)
      _ <- histogram.record(2.0, attribute, attribute)
      records <- histogram.records
    } yield assertEquals(records, expected)
  }

  test("record duration") {
    val attribute = Attribute("key", "value")
    val sleepDuration = 500.millis
    val unit = TimeUnit.MILLISECONDS

    val expected =
      List(
        Record(sleepDuration.toUnit(unit), Attributes(attribute)),
        Record(sleepDuration.toUnit(unit), Attributes(attribute))
      )

    TestControl.executeEmbed {
      // test varargs and Iterable overloads
      for {
        histogram <- inMemoryHistogram
        _ <- histogram
          .recordDuration(unit, attribute)
          .use(_ => IO.sleep(sleepDuration))
        _ <- histogram
          .recordDuration(unit, Attributes(attribute))
          .use(_ => IO.sleep(sleepDuration))
        records <- histogram.records
      } yield assertEquals(records, expected)
    }
  }

  private def inMemoryHistogram: IO[InMemoryHistogram] =
    IO.ref[List[Record[Double]]](Nil).map(ref => new InMemoryHistogram(ref))

}

object HistogramSuite {

  final case class Record[A](value: A, attributes: Attributes)

  class InMemoryHistogram(ref: Ref[IO, List[Record[Double]]]) extends Histogram[IO, Double] {

    val backend: Histogram.Backend[IO, Double] =
      new Histogram.Backend[IO, Double] {
        val meta: InstrumentMeta[IO] = InstrumentMeta.enabled

        def record(
            value: Double,
            attributes: immutable.Iterable[Attribute[_]]
        ): IO[Unit] =
          ref.update(_.appended(Record(value, attributes.to(Attributes))))

        def recordDuration(
            timeUnit: TimeUnit,
            attributes: Resource.ExitCase => immutable.Iterable[Attribute[_]]
        ): Resource[IO, Unit] =
          Resource
            .makeCase(IO.monotonic) { case (start, ec) =>
              for {
                end <- IO.monotonic
                _ <- record(
                  (end - start).toUnit(timeUnit),
                  attributes(ec)
                )
              } yield ()
            }
            .void
      }

    def records: IO[List[Record[Double]]] =
      ref.get
  }
}
