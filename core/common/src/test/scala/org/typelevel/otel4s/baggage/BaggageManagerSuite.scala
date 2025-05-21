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

package org.typelevel.otel4s.baggage

import cats.effect.IO
import cats.mtl.Local
import munit.CatsEffectSuite

import scala.annotation.nowarn

abstract class BaggageManagerSuite extends CatsEffectSuite {
  protected def baggageManager: IO[BaggageManager[IO]]

  protected final def testManager(name: String)(f: BaggageManager[IO] => IO[Unit]): Unit =
    test(name)(baggageManager.flatMap(f))

  testManager(".current consistent with .scope") { m =>
    val b1 = Baggage.empty
      .updated("key", "value")
      .updated("foo", "bar", "baz")
    m.scope(b1)(m.current.map(assertEquals(_, b1)))
  }

  testManager(".current consistent with .local") { m =>
    m.local(_.updated("key", "value").updated("foo", "bar", "baz")) {
      for (baggage <- m.current)
        yield {
          assertEquals(baggage.get("key"), Some(Baggage.Entry("value", None)))
          assertEquals(
            baggage.get("foo"),
            Some(Baggage.Entry("bar", Some(Baggage.Metadata("baz"))))
          )
        }
    }
  }

  testManager(".get consistent with .current") { m =>
    val check = m.scope(_: Baggage) {
      for {
        baggage <- m.current
        v1 <- m.get("key")
        v2 <- m.get("foo")
      } yield {
        assertEquals(v1, baggage.get("key"))
        assertEquals(v2, baggage.get("foo"))
      }
    }
    check(Baggage.empty) >>
      check(
        Baggage.empty
          .updated("key", "value")
          .updated("foo", "bar", "baz")
      )
  }

  testManager(".getValue consistent with .current") { m =>
    val check = m.scope(_: Baggage) {
      for {
        baggage <- m.current
        v1 <- m.getValue("key")
        v2 <- m.getValue("foo")
      } yield {
        assertEquals(v1, baggage.get("key").map(_.value))
        assertEquals(v2, baggage.get("foo").map(_.value))
      }
    }
    check(Baggage.empty) >>
      check(
        Baggage.empty
          .updated("key", "value")
          .updated("foo", "bar", "baz")
      )
  }

  testManager("deprecated as Local") { implicit m =>
    @nowarn("cat=deprecation")
    def test(): IO[Unit] = {
      val _ = m: Local[IO, Baggage]

      m.local(IO.unit)(_.updated("key", "value")) >>
        m.scope(IO.unit)(Baggage.empty)
    }
    test()
  }
}
