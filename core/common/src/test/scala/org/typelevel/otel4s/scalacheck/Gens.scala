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

trait Gens {

  val nonEmptyString: Gen[String] =
    for {
      id <- Gen.identifier
      str <- Gen.stringOfN(5, Gen.alphaNumChar)
    } yield id ++ str

  val nonZeroLong: Gen[Long] =
    Gen.oneOf(
      Gen.choose(Long.MinValue, -1L),
      Gen.choose(1L, Long.MaxValue)
    )

  val attribute: Gen[Attribute[_]] = {
    implicit val stringArb: Arbitrary[String] =
      Arbitrary(nonEmptyString)

    implicit def seqArb[A: Arbitrary]: Arbitrary[Seq[A]] =
      Arbitrary(Gen.nonEmptyListOf(Arbitrary.arbitrary[A]))

    def attribute[A: AttributeKey.KeySelect: Arbitrary]: Gen[Attribute[A]] =
      for {
        key <- nonEmptyString
        value <- Arbitrary.arbitrary[A]
      } yield Attribute(key, value)

    val string: Gen[Attribute[String]] = attribute[String]
    val boolean: Gen[Attribute[Boolean]] = attribute[Boolean]
    val long: Gen[Attribute[Long]] = attribute[Long]
    val double: Gen[Attribute[Double]] = attribute[Double]

    val stringSeq: Gen[Attribute[Seq[String]]] = attribute[Seq[String]]
    val booleanSeq: Gen[Attribute[Seq[Boolean]]] = attribute[Seq[Boolean]]
    val longSeq: Gen[Attribute[Seq[Long]]] = attribute[Seq[Long]]
    val doubleSeq: Gen[Attribute[Seq[Double]]] = attribute[Seq[Double]]

    Gen.oneOf(
      boolean,
      string,
      long,
      double,
      stringSeq,
      booleanSeq,
      longSeq,
      doubleSeq
    )
  }

  val attributes: Gen[Attributes] =
    for {
      attributes <- Gen.listOf(attribute)
    } yield attributes.to(Attributes)

}

object Gens extends Gens
