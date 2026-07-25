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

import cats.data.Kleisli
import cats.effect.IO
import cats.effect.Ref
import cats.syntax.flatMap._
import munit.CatsEffectSuite
import org.typelevel.otel4s.metrics.meta.InstrumentMeta

import scala.collection.immutable

class GaugeSuite extends CatsEffectSuite {
  import GaugeSuite._

  test("do not allocate attributes when instrument is noop") {
    val gauge = Gauge.noop[IO, Long]

    var allocated = false

    def allocateAttribute = {
      allocated = true
      List(Attribute("key", "value"))
    }

    // test varargs and Iterable overloads
    for {
      _ <- gauge.record(1L, allocateAttribute: _*)
      _ <- gauge.record(1L, allocateAttribute)
    } yield assert(!allocated)
  }

  test("record value and attributes") {
    val attribute = Attribute("key", "value")

    val expected =
      List(
        Record(1L, Attributes(attribute)),
        Record(2L, Attributes.empty),
        Record(-3L, Attributes(attribute, attribute))
      )

    // test varargs and Iterable overloads
    for {
      gauge <- inMemoryGauge
      _ <- gauge.record(1L, Attributes(attribute))
      _ <- gauge.record(2L)
      _ <- gauge.record(-3L, attribute, attribute)
      records <- gauge.records
    } yield assertEquals(records, expected)
  }

  test("liftTo - record values") {
    type G[A] = Kleisli[IO, Unit, A]

    val attribute = Attribute("key", "value")

    val expected =
      List(
        Record(1L, Attributes(attribute)),
        Record(2L, Attributes.empty),
        Record(-3L, Attributes(attribute, attribute))
      )

    for {
      gauge <- inMemoryGauge
      _ <- (
        gauge.liftTo[G].record(1L, Attributes(attribute)) >>
          gauge.liftTo[G].record(2L) >>
          gauge.liftTo[G].record(-3L, attribute, attribute)
      ).run(())
      records <- gauge.records
    } yield assertEquals(records, expected)
  }

  private def inMemoryGauge: IO[InMemoryGauge] =
    IO.ref[List[Record[Long]]](Nil).map(ref => new InMemoryGauge(ref))

}

object GaugeSuite {

  final case class Record[A](value: A, attributes: Attributes)

  class InMemoryGauge(ref: Ref[IO, List[Record[Long]]]) extends Gauge.Unsealed[IO, Long] {

    val backend: Gauge.Backend[IO, Long] =
      new Gauge.Backend.Unsealed[IO, Long] {
        val meta: InstrumentMeta[IO] = InstrumentMeta.enabled

        def record(value: Long, attributes: immutable.Iterable[Attribute[_]]): IO[Unit] =
          ref.update(_.appended(Record(value, attributes.to(Attributes))))
      }

    def records: IO[List[Record[Long]]] =
      ref.get
  }

}
