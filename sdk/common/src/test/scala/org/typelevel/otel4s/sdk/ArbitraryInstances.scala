/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.scalacheck.Gen.listOf
import org.scalacheck.Gen.nonEmptyListOf
import org.typelevel.otel4s.Attribute

object arbitrary extends ArbitraryInstances
trait ArbitraryInstances {

  val nonEmptyString: Gen[String] =
    Arbitrary.arbitrary[String].suchThat(_.nonEmpty)

  implicit val booleanAttribute: Gen[Attribute[Boolean]] = for {
    b <- Arbitrary.arbitrary[Boolean]
    k <- nonEmptyString
  } yield Attribute(k, b)

  implicit val stringAttribute: Gen[Attribute[String]] = for {
    s <- nonEmptyString
    k <- nonEmptyString
  } yield Attribute(k, s)

  implicit val longAttribute: Gen[Attribute[Long]] = for {
    l <- Arbitrary.arbitrary[Long]
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val doubleAttribute: Gen[Attribute[Double]] = for {
    d <- Arbitrary.arbitrary[Double]
    k <- nonEmptyString
  } yield Attribute(k, d)

  implicit val stringListAttribute: Gen[Attribute[List[String]]] = for {
    l <- nonEmptyListOf(nonEmptyString)
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val booleanListAttribute: Gen[Attribute[List[Boolean]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Boolean])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val longListAttribute: Gen[Attribute[List[Long]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Long])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val doubleListAttribute: Gen[Attribute[List[Double]]] = for {
    l <- nonEmptyListOf(Arbitrary.arbitrary[Double])
    k <- nonEmptyString
  } yield Attribute(k, l)

  implicit val attribute: Arbitrary[Attribute[_]] = Arbitrary(
    Gen.oneOf(
      booleanAttribute,
      stringAttribute,
      longAttribute,
      doubleAttribute,
      stringListAttribute,
      booleanListAttribute,
      longListAttribute,
      doubleListAttribute
    )
  )

  implicit val attributes: Arbitrary[Attributes] = Arbitrary(
    listOf(attribute.arbitrary).map(Attributes(_: _*))
  )

  implicit val resource: Gen[Resource] = for {
    attrs <- attributes.arbitrary
    schemaUrl <- Gen.option(nonEmptyString)
  } yield Resource(attrs, schemaUrl)

}
