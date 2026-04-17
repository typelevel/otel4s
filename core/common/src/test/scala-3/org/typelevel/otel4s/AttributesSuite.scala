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

package org.typelevel.otel4s

import munit.FunSuite

import scala.collection.immutable

class AttributesSuite extends FunSuite {

  test("Attributes.from supports AttributeOrOption: Attribute") {
    val attribute = Attribute("key", "value")

    val result = Attributes.from(attribute: AttributeOrOption[String])

    assertEquals(result, Attributes(attribute))
  }

  test("Attributes.from supports AttributeOrOption: Some(attribute)") {
    val attribute = Attribute("key", "value")

    val result = Attributes.from(Some(attribute): AttributeOrOption[String])

    assertEquals(result, Attributes(attribute))
  }

  test("Attributes.from supports AttributeOrOption: None") {
    val result = Attributes.from(None: AttributeOrOption[String])

    assertEquals(result, Attributes.empty)
  }

  test("Attributes.from supports AttributeOrIterableOnce: Attribute") {
    val attribute = Attribute("key", "value")

    val result = Attributes.from(attribute: AttributeOrIterableOnce)

    assertEquals(result, Attributes(attribute))
  }

  test("Attributes.from supports AttributeOrIterableOnce: IterableOnce[Attribute]") {
    val a1 = Attribute("key1", "value1")
    val a2 = Attribute("key2", "value2")

    val result = Attributes.from(List(a1, a2): AttributeOrIterableOnce)

    assertEquals(result, Attributes(a1, a2))
  }

  test("Attributes.from supports Iterable[AttributeOrIterableOnce] and flattens nested collections") {
    val a1 = Attribute("key1", "value1")
    val a2 = Attribute("key2", "value2")
    val a3 = Attribute("key3", "value3")

    val values: immutable.Iterable[AttributeOrIterableOnce] =
      List(a1, List(a2, a3), Vector(a1))

    val result = Attributes.from(values)

    assertEquals(result, Attributes(a1, a2, a3, a1))
  }

}
