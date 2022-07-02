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

import java.util.concurrent.TimeUnit

import cats.effect.{IO, Ref}
import munit.CatsEffectSuite

import scala.concurrent.duration._

class HistogramSuite extends CatsEffectSuite {
  import HistogramSuite._

  test("record value and attributes") {
    val attribute = Attribute(AttributeKey.string("key"), "value")

    val expected =
      List(
        Record(1.0, Seq(attribute)),
        Record(-1.0, Nil),
        Record(2.0, Seq(attribute, attribute))
      )

    for {
      histogram <- inMemoryHistogram
      _ <- histogram.record(1.0, attribute)
      _ <- histogram.record(-1.0)
      _ <- histogram.record(2.0, attribute, attribute)
      records <- histogram.records
    } yield assertEquals(records, expected)
  }

  test("record duration") {
    val attribute = Attribute(AttributeKey.string("key"), "value")

    for {
      histogram <- inMemoryHistogram
      _ <- histogram
        .recordDuration(TimeUnit.MILLISECONDS, attribute)
        .use(_ => IO.sleep(1.second))
      records <- histogram.records
    } yield {
      assertEquals(records.length, 1)
      assertEqualsDouble(records.head.value, 1000, 100)
      assertEquals(records.head.attributes, Seq(attribute))
    }
  }

  private def inMemoryHistogram: IO[InMemoryHistogram] =
    IO.ref[List[Record[Double]]](Nil).map(ref => new InMemoryHistogram(ref))

}

object HistogramSuite {

  final case class Record[A](value: A, attributes: Seq[Attribute[_]])

  class InMemoryHistogram(ref: Ref[IO, List[Record[Double]]])
      extends Histogram[IO, Double] {

    def record(value: Double, attributes: Attribute[_]*): IO[Unit] =
      ref.update(_.appended(Record(value, attributes)))

    def records: IO[List[Record[Double]]] =
      ref.get
  }
}
