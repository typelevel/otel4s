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
import cats.effect.kernel.Resource
import cats.syntax.all._
import munit.CatsEffectSuite

class ObservableSuite extends CatsEffectSuite {

  import ObservableSuite._

  test("observable test") {

    for {
      _ <- new InMemoryObservableInstrumentBuilder[Double]
        .createWithCallback(instrument =>
          instrument.record(2.0) *> instrument.record(3.0)
        )
        .use { r =>
          for {
            _ <- r.observations.get.assertEquals(List.empty)
            _ <- r.run
            _ <- r.observations.get.assertEquals(
              List(Record(3.0, Attributes.empty), Record(2.0, Attributes.empty))
            )
          } yield ()
        }
    } yield ()

  }

  test("observable test with list") {

    for {
      counter <- Ref.of[IO, Int](0)
      _ <- new InMemoryObservableInstrumentBuilder[Int]
        .create(
          counter
            .getAndUpdate(_ + 1)
            .map(x =>
              List(
                Measurement(x, List(Attribute("thing", "a"))),
                Measurement(x, List(Attribute("thing", "b")))
              )
            )
        )
        .use { r =>
          for {
            _ <- r.observations.get.assertEquals(List.empty)
            _ <- r.run
            _ <- r.run
            _ <- r.observations.get.assertEquals(
              List(
                Record(1, Attributes(Attribute("thing", "b"))),
                Record(1, Attributes(Attribute("thing", "a"))),
                Record(0, Attributes(Attribute("thing", "b"))),
                Record(0, Attributes(Attribute("thing", "a")))
              )
            )
          } yield ()
        }
    } yield ()

  }

}

object ObservableSuite {

  final case class Record[A](value: A, attributes: Attributes)

  final case class InMemoryObservable[A](
      callback: ObservableMeasurement[IO, A] => IO[Unit],
      observations: Ref[IO, List[Record[A]]]
  ) {
    def run: IO[Unit] =
      callback(new ObservableMeasurement[IO, A] {
        def record(value: A, attributes: Attributes): IO[Unit] =
          observations.update(Record(value, attributes) :: _)
      })
  }

  class InMemoryObservableInstrumentBuilder[A]
      extends ObservableInstrumentBuilder[IO, A, InMemoryObservable[A]] {

    type Self =
      ObservableInstrumentBuilder[IO, A, InMemoryObservable[A]]

    def withUnit(unit: String): Self = this

    def withDescription(description: String): Self = this

    def create(
        measurements: IO[List[Measurement[A]]]
    ): Resource[IO, InMemoryObservable[A]] =
      Resource
        .eval(Ref.of[IO, List[Record[A]]](List.empty))
        .map(obs =>
          InMemoryObservable[A](
            recorder =>
              measurements.flatMap(
                _.traverse_(x => recorder.record(x.value, x.attributes))
              ),
            obs
          )
        )

    def createWithCallback(
        cb: ObservableMeasurement[IO, A] => IO[Unit]
    ): Resource[IO, InMemoryObservable[A]] =
      Resource
        .eval(Ref.of[IO, List[Record[A]]](List.empty))
        .map(obs => InMemoryObservable[A](cb, obs))

  }

}
