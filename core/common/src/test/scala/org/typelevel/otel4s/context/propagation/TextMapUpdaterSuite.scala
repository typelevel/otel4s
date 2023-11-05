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

package org.typelevel.otel4s.context.propagation

import cats.Eq
import cats.laws.discipline._
import cats.syntax.all._
import munit.DisciplineSuite
import munit.FunSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.typelevel.otel4s.Box
import org.typelevel.otel4s.NotQuiteExhaustiveChecks

import scala.collection.immutable

class TextMapUpdaterSuite extends FunSuite with DisciplineSuite {
  // `TextMapUpdater[C]` is not implicitly summoned by this method
  // so that it tests that instances are available in a non-generic
  // context.
  def check[C](tmu: TextMapUpdater[C])(initial: C)(expected: C): Unit = {
    val res =
      List("1" -> "one", "2" -> "two", "3" -> "three")
        .foldLeft(initial) { case (carrier, (key, value)) =>
          tmu.updated(carrier, key, value)
        }
    assertEquals(res, expected)
  }

  test("TextMapUpdater[immutable.Map[String, String]") {
    check(TextMapUpdater[immutable.HashMap[String, String]])(
      immutable.HashMap.empty
    )(immutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapUpdater[immutable.ListMap[String, String]])(
      immutable.ListMap.empty
    )(immutable.ListMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapUpdater[immutable.Map[String, String]])(
      immutable.Map.empty
    )(immutable.Map("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapUpdater[immutable.SortedMap[String, String]") {
    check(TextMapUpdater[immutable.TreeMap[String, String]])(
      immutable.TreeMap.empty
    )(immutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapUpdater[immutable.SortedMap[String, String]])(
      immutable.SortedMap.empty
    )(immutable.SortedMap("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapUpdater[immutable.Seq[(String, String)]") {
    check(TextMapUpdater[LazyList[(String, String)]])(
      LazyList.empty
    )(LazyList("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapUpdater[Vector[(String, String)]])(Vector.empty)(
      Vector("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapUpdater[immutable.Seq[(String, String)]])(
      immutable.Seq.empty
    )(immutable.Seq("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  implicit def arbUpdater[A: TextMapUpdater]: Arbitrary[TextMapUpdater[A]] =
    Arbitrary(Gen.const(TextMapUpdater[A]))

  locally { // constrain `import NotQuiteExhaustiveChecks._` to a limited scope
    import NotQuiteExhaustiveChecks._

    implicit def updaterEq[A: ExhaustiveCheck: Eq]: Eq[TextMapUpdater[A]] = {
      (x, y) =>
        ExhaustiveCheck[(A, String, String)].allValues
          .forall { case (carrier, key, value) =>
            x.updated(carrier, key, value) === y.updated(carrier, key, value)
          }
    }

    test("TextMapUpdater is invariant") {
      checkAll(
        "TextMapUpdater[Map[String, String]]",
        InvariantTests[TextMapUpdater]
          .invariant[
            Map[String, String],
            Map[String, String],
            Box[Map[String, String]]
          ]
      )
      checkAll(
        "TextMapUpdater[Seq[(String, String)]]",
        InvariantTests[TextMapUpdater]
          .invariant[
            Seq[(String, String)],
            Seq[(String, String)],
            Box[Seq[(String, String)]]
          ]
      )
    }
  }
}
