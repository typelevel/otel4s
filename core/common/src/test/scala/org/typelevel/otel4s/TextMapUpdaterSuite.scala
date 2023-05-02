package org.typelevel.otel4s

import munit.FunSuite

import scala.collection.immutable

class TextMapUpdaterSuite extends FunSuite {
  // `TextMapUpdater[C]` is not implicitly summoned by this method
  // so that it tests that instances are available in a non-generic
  // context.
  def check[C](tmu: TextMapUpdater[C])(initial: C)(expected: C): Unit = {
    val res =
      List("1" -> "one", "2" -> "two", "3" -> "three")
        .fold(initial) { case (carrier: C, (key: String, value: String)) =>
          tmu.updated(carrier, key, value)
        }
    assertEquals(res, expected)
  }

  test("TextMapUpdater[immutable.Map[String, String]") {
    check(implicitly[TextMapUpdater[immutable.HashMap[String, String]]])(
      immutable.HashMap.empty
    )(immutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapUpdater[immutable.ListMap[String, String]]])(
      immutable.ListMap.empty
    )(immutable.ListMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapUpdater[immutable.Map[String, String]]])(
      immutable.Map.empty
    )(immutable.Map("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapUpdater[immutable.SortedMap[String, String]") {
    check(implicitly[TextMapUpdater[immutable.TreeMap[String, String]]])(
      immutable.TreeMap.empty
    )(immutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapUpdater[immutable.SortedMap[String, String]]])(
      immutable.SortedMap.empty
    )(immutable.SortedMap("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapUpdater[immutable.Seq[(String, String)]") {
    check(implicitly[TextMapUpdater[LazyList[(String, String)]]])(
      LazyList.empty
    )(LazyList("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapUpdater[Vector[(String, String)]]])(Vector.empty)(
      Vector("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapUpdater[immutable.Seq[(String, String)]]])(
      immutable.Seq.empty
    )(immutable.Seq("1" -> "one", "2" -> "two", "3" -> "three"))
  }
}
