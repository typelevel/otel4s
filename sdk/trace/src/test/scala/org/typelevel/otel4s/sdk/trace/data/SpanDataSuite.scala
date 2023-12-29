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

package org.typelevel.otel4s.sdk.trace
package data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.foldable._
import cats.syntax.show._
import munit.DisciplineSuite
import munit.internal.PlatformCompat
import org.scalacheck.Arbitrary
import org.scalacheck.Prop
import org.scalacheck.Test

class SpanDataSuite extends DisciplineSuite {
  import Cogens.spanDataCogen

  private implicit val spanDataArbitrary: Arbitrary[SpanData] =
    Arbitrary(Gens.spanData)

  checkAll("SpanData.HashLaws", HashTests[SpanData].hash)

  test("Show[SpanData]") {
    Prop.forAll(Gens.spanData) { data =>
      val endedEpoch = data.endTimestamp.foldMap(ts => show"endTimestamp=$ts, ")

      val expected =
        show"SpanData{" +
          show"name=${data.name}, " +
          show"spanContext=${data.spanContext}, " +
          show"parentSpanContext=${data.parentSpanContext}, " +
          show"kind=${data.kind}, " +
          show"startTimestamp=${data.startTimestamp}, " +
          endedEpoch +
          show"hasEnded=${data.hasEnded}, " +
          show"status=${data.status}, " +
          show"attributes=${data.attributes}, " +
          show"events=${data.events}, " +
          show"links=${data.links}, " +
          show"instrumentationScope=${data.instrumentationScope}, " +
          show"resource=${data.resource}}"

      assertEquals(Show[SpanData].show(data), expected)
    }
  }

  test("create SpanData with given arguments") {
    Prop.forAll(Gens.spanData) { data =>
      val created = SpanData(
        name = data.name,
        spanContext = data.spanContext,
        parentSpanContext = data.parentSpanContext,
        kind = data.kind,
        startTimestamp = data.startTimestamp,
        endTimestamp = data.endTimestamp,
        status = data.status,
        attributes = data.attributes,
        events = data.events,
        links = data.links,
        instrumentationScope = data.instrumentationScope,
        resource = data.resource
      )

      assertEquals(created, data)
    }
  }

  override protected def scalaCheckTestParameters: Test.Parameters =
    if (PlatformCompat.isJVM)
      super.scalaCheckTestParameters
    else
      super.scalaCheckTestParameters
        .withMinSuccessfulTests(10)
        .withMaxSize(10)
}
