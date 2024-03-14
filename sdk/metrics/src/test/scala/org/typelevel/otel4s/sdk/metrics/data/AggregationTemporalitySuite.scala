/*
 * Copyright 2024 Typelevel
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
package sdk.metrics
package data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class AggregationTemporalitySuite extends DisciplineSuite {

  implicit val aggregationTemporalityArb: Arbitrary[AggregationTemporality] =
    Arbitrary(
      Gen.oneOf(AggregationTemporality.Delta, AggregationTemporality.Cumulative)
    )

  implicit val aggregationTemporalityCogen: Cogen[AggregationTemporality] =
    Cogen[String].contramap(_.toString)

  checkAll(
    "AggregationTemporality.HashLaws",
    HashTests[AggregationTemporality].hash
  )

  test("Show[AggregationTemporality]") {
    Prop.forAll(aggregationTemporalityArb.arbitrary) { temporality =>
      val expected = temporality match {
        case AggregationTemporality.Delta      => "Delta"
        case AggregationTemporality.Cumulative => "Cumulative"
      }

      assertEquals(Show[AggregationTemporality].show(temporality), expected)
    }
  }

}
