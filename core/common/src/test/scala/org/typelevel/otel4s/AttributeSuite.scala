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

class AttributeSuite extends FunSuite {

  private case class UserId(id: String)
  private val userIdKey: AttributeKey[String] = AttributeKey("user.id")
  private implicit val userIdFrom: Attribute.From[UserId, String] = _.id

  test("use implicit From to derive a type of an attribute") {
    val stringAttribute = Attribute("user.id", "123")
    val liftedAttribute = Attribute.from("user.id", UserId("123"))

    assertEquals(stringAttribute, liftedAttribute)
  }

  test("use implicit From to add an attribute to a builder") {
    val builder = Attributes.newBuilder

    builder += userIdKey(UserId("1"))
    builder ++= userIdKey.maybe(Some(UserId("2")))
    builder.addOne(Attribute.from(userIdKey, UserId("3")))

    val expected = Attributes(
      Attribute("user.id", "1"),
      Attribute("user.id", "2"),
      Attribute("user.id", "3")
    )

    assertEquals(builder.result(), expected)
  }

}
