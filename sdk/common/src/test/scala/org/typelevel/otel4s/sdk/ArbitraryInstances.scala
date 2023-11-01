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
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Gen.listOf
import org.scalacheck.Gen.nonEmptyListOf
import org.scalacheck.rng.Seed
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.AttributeType

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

  implicit val resource: Arbitrary[Resource] = Arbitrary(for {
    attrs <- attributes.arbitrary
    schemaUrl <- Gen.option(nonEmptyString)
  } yield Resource(attrs, schemaUrl))

  implicit val attributeTypeCogen: Cogen[AttributeType[_]] =
    Cogen[String].contramap(_.toString)

  implicit def attributeKeyCogen[A]: Cogen[AttributeKey[A]] =
    Cogen[(String, String)].contramap[AttributeKey[A]] { attribute =>
      (attribute.name, attribute.`type`.toString)
    }

  implicit def attributeCogen[A: Cogen]: Cogen[Attribute[A]] =
    Cogen[(AttributeKey[A], A)].contramap(a => (a.key, a.value))

  implicit val attributeExistentialCogen: Cogen[Attribute[_]] =
    Cogen { (seed, attr) =>
      def primitive[A: Cogen](seed: Seed): Seed =
        Cogen[A].perturb(seed, attr.value.asInstanceOf[A])

      def list[A: Cogen](seed: Seed): Seed =
        Cogen[List[A]].perturb(seed, attr.value.asInstanceOf[List[A]])

      val valueCogen: Seed => Seed = attr.key.`type` match {
        case AttributeType.Boolean     => primitive[Boolean]
        case AttributeType.Double      => primitive[Double]
        case AttributeType.String      => primitive[String]
        case AttributeType.Long        => primitive[Long]
        case AttributeType.BooleanList => list[Boolean]
        case AttributeType.DoubleList  => list[Double]
        case AttributeType.StringList  => list[String]
        case AttributeType.LongList    => list[Long]
      }

      valueCogen(attributeKeyCogen.perturb(seed, attr.key))
    }

  implicit val attributesCogen: Cogen[Attributes] =
    Cogen[List[Attribute[_]]].contramap(_.toList)

  implicit val resourceCogen: Cogen[Resource] =
    Cogen[(Attributes, Option[String])].contramap { r =>
      (r.attributes, r.schemaUrl)
    }

}
