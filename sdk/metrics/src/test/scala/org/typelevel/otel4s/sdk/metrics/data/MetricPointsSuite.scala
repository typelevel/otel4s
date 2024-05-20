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
import org.typelevel.otel4s.sdk.metrics.scalacheck.Arbitraries._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Cogens._
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class MetricPointsSuite extends DisciplineSuite {

  checkAll("MetricPoints.Hash", HashTests[MetricPoints].hash)

  test("Show[MetricPoints]") {
    Prop.forAll(Gens.metricPoints) { data =>
      val expected = data match {
        case sum: MetricPoints.Sum =>
          "MetricPoints.Sum{" +
            s"points=${sum.points.toVector.mkString("{", ",", "}")}, " +
            s"monotonic=${sum.monotonic}, " +
            s"aggregationTemporality=${sum.aggregationTemporality}}"

        case gauge: MetricPoints.Gauge =>
          s"MetricPoints.Gauge{points=${gauge.points.toVector.mkString("{", ",", "}")}}"

        case histogram: MetricPoints.Histogram =>
          "MetricPoints.Histogram{" +
            s"points=${histogram.points.toVector.mkString("{", ",", "}")}, " +
            s"aggregationTemporality=${histogram.aggregationTemporality}}"
      }

      assertEquals(Show[MetricPoints].show(data), expected)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    super.scalaCheckTestParameters
      .withMinSuccessfulTests(20)
      .withMaxSize(20)

}
