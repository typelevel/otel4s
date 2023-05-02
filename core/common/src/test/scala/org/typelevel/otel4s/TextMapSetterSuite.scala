package org.typelevel.otel4s

import munit.FunSuite

import scala.collection.mutable
import scala.collection.mutable.{ArrayBuffer, Buffer, ListBuffer}

class TextMapSetterSuite extends FunSuite {
  // `TextMapSetter[C]` is not implicitly summoned by this method
  // so that it tests that instances are available in a non-generic
  // context.
  def check[C](tms: TextMapSetter[C])(carrier: C)(expected: C): Unit = {
    tms.unsafeSet(carrier, "1", "one")
    tms.unsafeSet(carrier, "2", "two")
    tms.unsafeSet(carrier, "3", "three")
    assertEquals(carrier, expected)
  }

  test("TextMapSetter[mutable.Map[String, String]") {
    check(implicitly[TextMapSetter[mutable.HashMap[String, String]]])(
      mutable.HashMap.empty
    )(mutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapSetter[mutable.TreeMap[String, String]]])(
      mutable.TreeMap.empty
    )(mutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapSetter[mutable.Map[String, String]]])(
      mutable.Map.empty
    )(mutable.Map("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapSetter[Buffer[(String, String)]") {
    check(implicitly[TextMapSetter[ListBuffer[(String, String)]]])(
      ListBuffer.empty
    )(ListBuffer("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapSetter[ArrayBuffer[(String, String)]]])(
      ArrayBuffer.empty
    )(ArrayBuffer("1" -> "one", "2" -> "two", "3" -> "three"))
    check(implicitly[TextMapSetter[Buffer[(String, String)]]])(Buffer.empty)(
      Buffer("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }
}
