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
import cats.syntax.foldable._
import munit.DisciplineSuite
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class MetricDataSuite extends DisciplineSuite {

  checkAll("MetricData.Hash", HashTests[MetricData].hash)

  test("Show[MetricData]") {
    Prop.forAll(Gens.metricData) { data =>
      val expected = {
        val description = data.description.foldMap(d => s"description=$d, ")
        val unit = data.unit.foldMap(d => s"unit=$d, ")
        "MetricData{" +
          s"name=${data.name}, " +
          description +
          unit +
          s"data=${data.data}, " +
          s"instrumentationScope=${data.instrumentationScope}, " +
          s"resource=${data.resource}}"
      }

      assertEquals(Show[MetricData].show(data), expected)
      assertEquals(data.toString, expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
