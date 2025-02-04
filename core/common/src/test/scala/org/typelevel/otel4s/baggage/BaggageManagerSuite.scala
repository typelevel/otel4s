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
import munit.CatsEffectSuite

abstract class BaggageManagerSuite extends CatsEffectSuite {
  protected def baggageManager: IO[BaggageManager[IO]]

  protected final def testManager(name: String)(f: BaggageManager[IO] => IO[Unit]): Unit =
    test(name)(baggageManager.flatMap(f))

  testManager(".ask consistent with .scope") { m =>
    val b1 = Baggage.empty
      .updated("key", "value")
      .updated("foo", "bar", "baz")
    m.scope(m.ask[Baggage].map(assertEquals(_, b1)))(b1)
  }

  testManager(".ask consistent with .local") { m =>
    m.local {
      for (baggage <- m.ask[Baggage])
        yield {
          assertEquals(baggage.get("key"), Some(Baggage.Entry("value", None)))
          assertEquals(
            baggage.get("foo"),
            Some(Baggage.Entry("bar", Some(Baggage.Metadata("baz"))))
          )
        }
    }(_.updated("key", "value").updated("foo", "bar", "baz"))
  }

  testManager(".current is equivalent to .ask") { m =>
    val check = m.scope {
      for {
        a <- m.ask[Baggage]
        b <- m.current
      } yield assertEquals(a, b)
    }(_)
    check(Baggage.empty)
    check(
      Baggage.empty
        .updated("key", "value")
        .updated("foo", "bar", "baz")
    )
  }

  testManager(".get consistent with .ask") { m =>
    val check = m.scope {
      for {
        baggage <- m.ask[Baggage]
        v1 <- m.get("key")
        v2 <- m.get("foo")
      } yield {
        assertEquals(v1, baggage.get("key"))
        assertEquals(v2, baggage.get("foo"))
      }
    }(_)
    check(Baggage.empty)
    check(
      Baggage.empty
        .updated("key", "value")
        .updated("foo", "bar", "baz")
    )
  }

  testManager(".getValue consistent with .ask") { m =>
    val check = m.scope {
      for {
        baggage <- m.ask[Baggage]
        v1 <- m.getValue("key")
        v2 <- m.getValue("foo")
      } yield {
        assertEquals(v1, baggage.get("key").map(_.value))
        assertEquals(v2, baggage.get("foo").map(_.value))
      }
    }(_)
    check(Baggage.empty)
    check(
      Baggage.empty
        .updated("key", "value")
        .updated("foo", "bar", "baz")
    )
  }
}
