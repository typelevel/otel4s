package org.typelevel.otel4s

import munit.FunSuite

class AttributeSuite extends FunSuite {

  private case class UserId(id: String)
  private implicit val userIdKeySelect: AttributeKey.KeySelect.Projection[UserId, String] =
    AttributeKey.KeySelect.projection(_.id)

  test("use projected KeySelect to derive a type of an attribute") {
    val stringAttribute = Attribute("user.id", "123")
    val liftedAttribute = Attribute("user.id", UserId("123"))

    assertEquals(stringAttribute, liftedAttribute)
  }

  test("use projected KeySelect to get an attribute from a collection") {
    val attributes = Attributes(Attribute("user.id", UserId("1")))

    assertEquals(attributes.get[UserId]("user.id"), attributes.get[String]("user.id"))
  }

}
