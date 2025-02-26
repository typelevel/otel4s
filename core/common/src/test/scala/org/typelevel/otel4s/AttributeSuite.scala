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
  import AttributeSuite._

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

  test("use implicit From to create integer primitive attributes") {
    val longKey = AttributeKey[Long]("number")
    val seqLongKey = AttributeKey[Seq[Long]]("seq")
    val longAttribute = Attribute("number", 1L)
    val seqLongAttribute = Attribute("seq", Seq(1L, 2L, 3L))

    val oneI: Int = 1
    val oneS: Short = 1
    val oneB: Byte = 1

    assertEquals(longKey.maybe(Some(oneI)), Some(longAttribute))
    assertEquals(longKey.maybe(Some(oneS)), Some(longAttribute))
    assertEquals(longKey.maybe(Some(oneB)), Some(longAttribute))
    assertEquals(seqLongKey(Seq[Int](1, 2, 3)), seqLongAttribute)
    assertEquals(seqLongKey(Seq[Short](1, 2, 3)), seqLongAttribute)
    assertEquals(seqLongKey(Seq[Byte](1, 2, 3)), seqLongAttribute)
  }

  test("derive implicit From for Seq of existing From instance") {
    val key = AttributeKey[Seq[String]]("key")
    val expected = Attribute("key", Seq("1", "2"))

    assertEquals(key(Seq(UserId("1"), UserId("2"))), expected)
  }

  test("use implicit Attribute.Make to create an attribute") {
    case class AssetId(id: Int)

    val assetIdKey: AttributeKey[Long] = AttributeKey("asset.id")

    implicit val assetIdFrom: Attribute.From[AssetId, Long] =
      _.id.toLong

    implicit val userIdAttributeMake: Attribute.Make[UserId, String] =
      Attribute.Make.const("user.id")

    implicit val assetIdAttributeMake: Attribute.Make[AssetId, Long] =
      Attribute.Make.const(assetIdKey)

    val builder = Attributes.newBuilder
    builder += Attribute.from(UserId("321"))
    builder ++= Option(Attribute.from(AssetId(123)))

    val attributes = builder.result()

    assertEquals(attributes.get[String]("user.id").map(_.value), Some("321"))
    assertEquals(attributes.get[Long]("asset.id").map(_.value), Some(123L))
  }

}

private object AttributeSuite {
  final case class UserId(id: String)
}
