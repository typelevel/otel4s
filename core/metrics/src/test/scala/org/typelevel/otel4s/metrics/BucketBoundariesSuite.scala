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

package org.typelevel.otel4s.metrics

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit._
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class BucketBoundariesSuite extends DisciplineSuite {

  private implicit val boundariesArbitrary: Arbitrary[BucketBoundaries] =
    Arbitrary(
      for {
        size <- Gen.choose(0, 20)
        b <- Gen.containerOfN[Vector, Double](size, Gen.choose(-100.0, 100.0))
      } yield BucketBoundaries(b.distinct.sorted)
    )

  private implicit val boundariesCogen: Cogen[BucketBoundaries] =
    Cogen[Vector[Double]].contramap(_.boundaries)

  checkAll("BucketBoundaries.Hash", HashTests[BucketBoundaries].hash)

  test("Show[BucketBoundaries]") {
    Prop.forAll(boundariesArbitrary.arbitrary) { b =>
      val expected = s"BucketBoundaries{${b.boundaries.mkString(", ")}}"

      assertEquals(Show[BucketBoundaries].show(b), expected)
    }
  }

  test("fail when a boundary is Double.NaN") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: bucket boundary cannot be NaN"
    )(
      BucketBoundaries(1.0, 2.0, Double.NaN, 3.0)
    )
  }

  test("fail when boundaries are not increasing") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: bucket boundaries must be in increasing oder"
    )(
      BucketBoundaries(1.0, 1.2, 0.5, 3.0)
    )
  }

  test("fail when first boundary is -Inf") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: first boundary cannot be -Inf"
    )(
      BucketBoundaries(Double.NegativeInfinity)
    )
  }

  test("fail when last boundary is +Inf") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: last boundary cannot be +Inf"
    )(
      BucketBoundaries(Double.PositiveInfinity)
    )
  }

  test("create successfully when input is valid") {
    val input = Vector(
      Vector.empty[Double],
      Vector(1.0),
      Vector(Double.MinValue),
      Vector(Double.MinValue, Double.MaxValue),
      Vector(Double.MaxValue),
      Vector(-0.5, 0, 0.5, 1.5, 3.5)
    )

    input.foreach { boundaries =>
      assertEquals(BucketBoundaries(boundaries).boundaries, boundaries)
    }
  }

}
