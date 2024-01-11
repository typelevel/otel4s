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
package scalacheck

import org.scalacheck.Arbitrary
import org.scalacheck.Gen
import org.typelevel.otel4s.Attribute.KeySelect

trait Gens {

  val nonEmptyString: Gen[String] =
    Gen.alphaNumStr.suchThat(_.nonEmpty)

  val attribute: Gen[Attribute[_]] = {
    implicit val stringArb: Arbitrary[String] =
      Arbitrary(nonEmptyString)

    implicit def listArb[A: Arbitrary]: Arbitrary[List[A]] =
      Arbitrary(Gen.nonEmptyListOf(Arbitrary.arbitrary[A]))

    def attribute[A: KeySelect: Arbitrary]: Gen[Attribute[A]] =
      for {
        key <- nonEmptyString
        value <- Arbitrary.arbitrary[A]
      } yield Attribute(key, value)

    val string: Gen[Attribute[String]] = attribute[String]
    val boolean: Gen[Attribute[Boolean]] = attribute[Boolean]
    val long: Gen[Attribute[Long]] = attribute[Long]
    val double: Gen[Attribute[Double]] = attribute[Double]

    val stringList: Gen[Attribute[List[String]]] = attribute[List[String]]
    val booleanList: Gen[Attribute[List[Boolean]]] = attribute[List[Boolean]]
    val longList: Gen[Attribute[List[Long]]] = attribute[List[Long]]
    val doubleList: Gen[Attribute[List[Double]]] = attribute[List[Double]]

    Gen.oneOf(
      boolean,
      string,
      long,
      double,
      stringList,
      booleanList,
      longList,
      doubleList
    )
  }

  val attributes: Gen[Attributes] =
    for {
      attributes <- Gen.listOf(attribute)
    } yield Attributes.fromSpecific(attributes)

}

object Gens extends Gens
