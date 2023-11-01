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

import munit.FunSuite

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.collection.mutable.Buffer
import scala.collection.mutable.ListBuffer

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
    check(TextMapSetter[mutable.HashMap[String, String]])(
      mutable.HashMap.empty
    )(mutable.HashMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapSetter[mutable.TreeMap[String, String]])(
      mutable.TreeMap.empty
    )(mutable.TreeMap("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapSetter[mutable.Map[String, String]])(
      mutable.Map.empty
    )(mutable.Map("1" -> "one", "2" -> "two", "3" -> "three"))
  }

  test("TextMapSetter[Buffer[(String, String)]") {
    check(TextMapSetter[ListBuffer[(String, String)]])(
      ListBuffer.empty
    )(ListBuffer("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapSetter[ArrayBuffer[(String, String)]])(
      ArrayBuffer.empty
    )(ArrayBuffer("1" -> "one", "2" -> "two", "3" -> "three"))
    check(TextMapSetter[Buffer[(String, String)]])(Buffer.empty)(
      Buffer("1" -> "one", "2" -> "two", "3" -> "three")
    )
  }
}
