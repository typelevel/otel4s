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
import scala.collection.mutable

class TextMapGetterSuite extends FunSuite with DisciplineSuite {
  // `TextMapGetter[C]` is not implicitly summoned by this method
  // so that it tests that instances are available in a non-generic
  // context.
  def check[C](tmg: TextMapGetter[C])(carrier: C): Unit = {
    assertEquals(tmg.keys(carrier).toSet, Set("1", "2", "3"))
    assertEquals(tmg.get(carrier, "1"), Some("one"))
    assertEquals(tmg.get(carrier, "2"), Some("two"))
    assertEquals(tmg.get(carrier, "3"), Some("three"))
    assert(tmg.get(carrier, "0").isEmpty)
    assert(tmg.get(carrier, "4").isEmpty)
  }

  test("TextMapGetter[Map[String, String]") {
    check(TextMapGetter[mutable.HashMap[String, String]])(
      mutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapGetter[immutable.TreeMap[String, String]])(
      immutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapGetter[collection.Map[String, String]])(
      collection.Map("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }

  test("TextMapGetter[Seq[(String, String)]") {
    check(TextMapGetter[mutable.ListBuffer[(String, String)]])(
      mutable.ListBuffer("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapGetter[LazyList[(String, String)]])(
      LazyList("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapGetter[mutable.ArraySeq[(String, String)]])(
      mutable.ArraySeq("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(TextMapGetter[collection.Seq[(String, String)]])(
      collection.Seq("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }

  test("TextMapGetter[Array[(String, String)]") {
    check(TextMapGetter[Array[(String, String)]])(
      Array("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }

  test("Seq-like with duplicate keys") {
    check(TextMapGetter[List[(String, String)]])(
      List("1" -> "one", "2" -> "two", "3" -> "three", "1" -> "four")
    )

    val res = TextMapGetter[List[(String, String)]]
      .get(List("1" -> "first", "1" -> "second", "1" -> "last"), "1")
    assertEquals(res, Some("first"))
  }

  implicit def arbGetter[A: TextMapGetter]: Arbitrary[TextMapGetter[A]] =
    Arbitrary(Gen.const(TextMapGetter[A]))

  locally { // constrain `import NotQuiteExhaustiveChecks._` to a limited scope
    import NotQuiteExhaustiveChecks._

    implicit def getterEq[A: ExhaustiveCheck]: Eq[TextMapGetter[A]] = { (x, y) =>
      ExhaustiveCheck[(A, String)].allValues
        .forall { case (carrier, key) =>
          x.get(carrier, key) === y.get(carrier, key)
        } &&
      ExhaustiveCheck[A].allValues
        .forall(carrier => x.keys(carrier).toSet === y.keys(carrier).toSet)
    }

    test("TextMapGetter is contravariant") {
      checkAll(
        "TextMapGetter[Map[String, String]]",
        ContravariantTests[TextMapGetter]
          .contravariant[
            Map[String, String],
            Map[String, String],
            Box[Map[String, String]]
          ]
      )
      checkAll(
        "TextMapGetter[Seq[(String, String)]]",
        ContravariantTests[TextMapGetter]
          .contravariant[
            Seq[(String, String)],
            Seq[(String, String)],
            Box[Seq[(String, String)]]
          ]
      )
      checkAll(
        "TextMapGetter[Array[(String, String)]]",
        ContravariantTests[TextMapGetter]
          .contravariant[
            Array[(String, String)],
            Array[(String, String)],
            Box[Array[(String, String)]]
          ]
      )
    }
  }
}
