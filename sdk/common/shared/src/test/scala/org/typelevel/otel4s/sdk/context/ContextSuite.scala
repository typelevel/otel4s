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
import munit.DisciplineSuite
import org.scalacheck.Gen
import org.scalacheck.Prop

class ContextSuite extends DisciplineSuite {

  private def keyGen[A]: Gen[Context.Key[A]] =
    for {
      name <- Gen.alphaNumStr
    } yield Context.Key.unique[SyncIO, A](name).unsafeRunSync()

  private val contextGen: Gen[Context] =
    for {
      stringKey <- keyGen[String]
      string <- Gen.alphaNumStr
      intKey <- keyGen[Int]
      int <- Gen.posNum[Int]
      doubleKey <- keyGen[Double]
      double <- Gen.double
      longKey <- keyGen[Long]
      long <- Gen.long
    } yield Context.root
      .updated(stringKey, string)
      .updated(intKey, int)
      .updated(doubleKey, double)
      .updated(longKey, long)

  test("get values from the context") {
    Prop.forAll(keyGen[String], Gen.alphaNumStr) { case (key, value) =>
      val ctx = Context.root.updated(key, value)
      assertEquals(ctx.get(key), Some(value))
    }
  }

  test("override values in the context") {
    Prop.forAll(keyGen[String], Gen.alphaNumStr, Gen.alphaNumStr) { case (key, value1, value2) =>
      val ctx = Context.root.updated(key, value1).updated(key, value2)
      assertEquals(ctx.get(key), Some(value2))
    }
  }

  test("Show[Context]") {
    Prop.forAll(contextGen) { ctx =>
      val expected = ctx match {
        case m: Context.MapContext =>
          m.storage
            .map { case (key, value) => s"${key.name}=$value" }
            .mkString("Context{", ", ", "}")
      }
      assertEquals(Show[Context].show(ctx), expected)
      assertEquals(Show[Context].show(ctx), ctx.toString)
    }
  }

}
