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
import munit.CatsEffectSuite
import org.typelevel.otel4s.meta.InstrumentMeta

import scala.collection.immutable

class CounterSuite extends CatsEffectSuite {
  import CounterSuite._

  test("do not allocate attributes when instrument is noop") {
    val counter = Counter.noop[IO, Long]

    var allocated = false

    def allocateAttribute = {
      allocated = true
      List(Attribute("key", "value"))
    }

    // test varargs and Iterable overloads
    for {
      _ <- counter.add(1L, allocateAttribute: _*)
      _ <- counter.add(1L, allocateAttribute)
      _ <- counter.inc(allocateAttribute: _*)
      _ <- counter.inc(allocateAttribute)
    } yield assert(!allocated)
  }

  test("record value and attributes") {
    val attribute = Attribute("key", "value")

    val expected =
      List(
        Record(1L, Attributes(attribute)),
        Record(2L, Attributes.empty),
        Record(3L, Attributes(attribute, attribute))
      )

    // test varargs and Iterable overloads
    for {
      counter <- inMemoryCounter
      _ <- counter.add(1L, Attributes(attribute))
      _ <- counter.add(2L)
      _ <- counter.add(3L, attribute, attribute)
      records <- counter.records
    } yield assertEquals(records, expected)
  }

  test("inc by one") {
    val attribute = Attribute("key", "value")

    val expected =
      List(
        Record(1L, Attributes(attribute)),
        Record(1L, Attributes.empty),
        Record(1L, Attributes(attribute, attribute))
      )

    // test varargs and Iterable overloads
    for {
      counter <- inMemoryCounter
      _ <- counter.inc(Attributes(attribute))
      _ <- counter.inc()
      _ <- counter.inc(attribute, attribute)
      records <- counter.records
    } yield assertEquals(records, expected)
  }

  private def inMemoryCounter: IO[InMemoryCounter] =
    IO.ref[List[Record[Long]]](Nil).map(ref => new InMemoryCounter(ref))

}

object CounterSuite {

  final case class Record[A](value: A, attributes: Attributes)

  class InMemoryCounter(ref: Ref[IO, List[Record[Long]]])
      extends Counter[IO, Long] {

    val backend: Counter.Backend[IO, Long] =
      new Counter.LongBackend[IO] {
        val meta: InstrumentMeta[IO] = InstrumentMeta.enabled

        def add(
            value: Long,
            attributes: immutable.Iterable[Attribute[_]]
        ): IO[Unit] =
          ref.update(_.appended(Record(value, attributes.to(Attributes))))
      }

    def records: IO[List[Record[Long]]] =
      ref.get
  }

}
