package org.typelevel.otel4s

import munit.FunSuite

import scala.collection.{immutable, mutable}

class TextMapGetterSuite extends FunSuite {
  // `TextMapGetter[C]` is not implicitly summoned by this method
  // so that it tests that instances are available in a non-generic
  // context.
  def check[C](tmg: TextMapGetter[C])(carrier: C): Unit = {
    assertEquals(tmg.keys(carrier).toSeq, Seq("1", "2", "3"))
    assertEquals(tmg.get(carrier, "1"), Some("one"))
    assertEquals(tmg.get(carrier, "2"), Some("two"))
    assertEquals(tmg.get(carrier, "3"), Some("three"))
    assert(tmg.get(carrier, "0").isEmpty)
    assert(tmg.get(carrier, "4").isEmpty)
  }

  test("TextMapGetter[Map[String, String]") {
    check(implicitly[TextMapGetter[mutable.HashMap[String, String]]])(
      mutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapGetter[immutable.TreeMap[String, String]]])(
      immutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapGetter[collection.Map[String, String]]])(
      collection.Map("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }

  test("TextMapGetter[Seq[(String, String)]") {
    check(implicitly[TextMapGetter[mutable.ListBuffer[(String, String)]]])(
      mutable.ListBuffer("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapGetter[LazyList[(String, String)]]])(
      LazyList("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapGetter[mutable.ArraySeq[(String, String)]]])(
      mutable.ArraySeq("1" -> "one", "2" -> "two", "3" -> "three")
    )
    check(implicitly[TextMapGetter[collection.Seq[(String, String)]]])(
      collection.Seq("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }

  test("TextMapGetter[Array[(String, String)]") {
    check(implicitly[TextMapGetter[Array[(String, String)]]])(
      Array("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }
}
