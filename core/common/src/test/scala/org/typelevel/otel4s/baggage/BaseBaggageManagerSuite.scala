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

import cats.data.OptionT
import cats.effect.IO
import munit.CatsEffectSuite

abstract class BaseBaggageManagerSuite extends CatsEffectSuite {
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

  testManager(".liftTo preserves .scope") { m =>
    val lifted = m.liftTo[OptionT[IO, *]]
    val baggage = Baggage.empty.updated("key", "value")

    lifted
      .scope(baggage) {
        for {
          current <- lifted.current
          entry <- lifted.get("key")
          value <- lifted.getValue("key")
        } yield {
          assertEquals(current, baggage)
          assertEquals(entry, baggage.get("key"))
          assertEquals(value, Some("value"))
        }
      }
      .value
      .assertEquals(Some(()))
  }

  testManager(".liftTo preserves .local") { m =>
    val lifted = m.liftTo[OptionT[IO, *]]

    lifted
      .local(_.updated("key", "value")) {
        lifted.getValue("key").map(assertEquals(_, Some("value")))
      }
      .value
      .assertEquals(Some(()))
  }

}
