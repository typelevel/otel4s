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

class BaggageManagerSuite extends CatsEffectSuite {

  test("BaggageManager.noop - .current - return an empty baggage") {
    val manager = BaggageManager.noop[IO]
    manager.current.assertEquals(Baggage.empty)
  }

  test("BaggageManager.noop - .get - return None") {
    val manager = BaggageManager.noop[IO]
    manager.get("some-key").assertEquals(None)
  }

  test("BaggageManager.noop - .getValue - return None") {
    val manager = BaggageManager.noop[IO]
    manager.getValue("some-key").assertEquals(None)
  }

  test("BaggageManager.noop - .local - do not modify the baggage") {
    val manager = BaggageManager.noop[IO]
    manager.local(_.updated("key", "value")) {
      for {
        _ <- manager.current.assertEquals(Baggage.empty)
        _ <- manager.get("key").assertEquals(None)
        _ <- manager.getValue("key").assertEquals(None)
      } yield ()
    }
  }

  test("BaggageManager.noop - .scope - do not modify the baggage") {
    val manager = BaggageManager.noop[IO]
    manager.scope(Baggage.empty.updated("key", "value")) {
      for {
        _ <- manager.current.assertEquals(Baggage.empty)
        _ <- manager.get("key").assertEquals(None)
        _ <- manager.getValue("key").assertEquals(None)
      } yield ()
    }
  }

  test("BaggageManager.noop - .toString") {
    val manager = BaggageManager.noop[IO]
    assertEquals(manager.toString, "BaggageManager.Noop")
  }

}
