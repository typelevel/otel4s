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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.metrics.data.PointData.DoubleNumber
import org.typelevel.otel4s.sdk.metrics.data.PointData.LongNumber
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class PointDataSuite extends DisciplineSuite {

  checkAll("PointData.Hash", HashTests[PointData].hash)

  test("Show[PointData]") {
    Prop.forAll(Gens.pointData) { pointData =>
      val expected = pointData match {
        case data: PointData.NumberPoint =>
          val prefix = data match {
            case _: LongNumber   => "LongNumber"
            case _: DoubleNumber => "DoubleNumber"
          }

          s"PointData.$prefix{" +
            s"timeWindow=${data.timeWindow}, " +
            s"attributes=${data.attributes}, " +
            s"exemplars=${data.exemplars}, " +
            s"value=${data.value}}"

        case data: PointData.Histogram =>
          "PointData.Histogram{" +
            s"timeWindow=${data.timeWindow}, " +
            s"attributes=${data.attributes}, " +
            s"exemplars=${data.exemplars}, " +
            s"stats=${data.stats}, " +
            s"boundaries=${data.boundaries}, " +
            s"counts=${data.counts}}"
      }

      assertEquals(Show[PointData].show(pointData), expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
