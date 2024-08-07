/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.context

import cats.Show
import cats.effect.SyncIO
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class ContextKeySuite extends DisciplineSuite {

  private def keyGen[A]: Gen[Context.Key[A]] =
    for {
      name <- Gen.alphaNumStr
    } yield Context.Key.unique[SyncIO, A](name).unsafeRunSync()

  private implicit val contextKeyArb: Arbitrary[Context.Key[String]] =
    Arbitrary(keyGen)

  private implicit val contextKeyCogen: Cogen[Context.Key[String]] =
    Cogen[Int].contramap(v => v.unique.hashCode)

  checkAll("Context.Key.HashLaws", HashTests[Context.Key[String]].hash)

  test("Show[Context.Key[_]]") {
    Prop.forAll(keyGen[String]) { key =>
      assertEquals(Show[Context.Key[String]].show(key), s"Key(${key.name})")
      assertEquals(Show[Context.Key[String]].show(key), key.toString)
    }
  }

  test("keys with the same name must be unique") {
    val key1 = Context.Key.unique[SyncIO, String]("key").unsafeRunSync()
    val key2 = Context.Key.unique[SyncIO, String]("key").unsafeRunSync()

    assertNotEquals(key1, key2)
  }

}
