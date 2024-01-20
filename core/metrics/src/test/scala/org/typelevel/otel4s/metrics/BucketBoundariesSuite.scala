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

import munit._

class BucketBoundariesSuite extends FunSuite {

  test("fail when a boundary is Double.NaN") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: bucket boundary cannot be NaN"
    )(
      BucketBoundaries(Vector(1.0, 2.0, Double.NaN, 3.0))
    )
  }

  test("fail when boundaries are not increasing") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: bucket boundaries must be in increasing oder"
    )(
      BucketBoundaries(Vector(1.0, 1.2, 0.5, 3.0))
    )
  }

  test("fail when first boundary is -Inf") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: first boundary cannot be -Inf"
    )(
      BucketBoundaries(Vector(Double.NegativeInfinity))
    )
  }

  test("fail when last boundary is +Inf") {
    interceptMessage[IllegalArgumentException](
      "requirement failed: last boundary cannot be +Inf"
    )(
      BucketBoundaries(Vector(Double.PositiveInfinity))
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
