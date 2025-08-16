/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs
package data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import munit.internal.PlatformCompat
import org.scalacheck.Prop
import org.scalacheck.Test
import org.typelevel.otel4s.sdk.logs.scalacheck.Arbitraries
import org.typelevel.otel4s.sdk.logs.scalacheck.Cogens
import org.typelevel.otel4s.sdk.logs.scalacheck.Gens

class LogRecordDataSuite extends DisciplineSuite {
  import Cogens.logRecordDataCogen
  import Arbitraries.logRecordDataArbitrary

  checkAll("LogRecordData.HashLaws", HashTests[LogRecordData].hash)

  test("Show[LogRecordData]") {
    Prop.forAll(Gens.logRecordData) { data =>
      val expected =
        "LogRecordData{" +
          s"timestamp=${data.timestamp}, " +
          s"observedTimestamp=${data.observedTimestamp}, " +
          s"traceContext=${data.traceContext.fold("None")(_.toString)}, " +
          s"severity=${data.severity}, " +
          s"severityText=${data.severityText}, " +
          s"body=${data.body}, " +
          s"eventName=${data.eventName}, " +
          s"attributes=${data.attributes.elements}, " +
          s"instrumentationScope=${data.instrumentationScope}, " +
          s"resource=${data.resource}}"

      assertEquals(Show[LogRecordData].show(data), expected)
      assertEquals(data.toString, expected)
    }
  }

  test("create LogRecordData with given arguments") {
    Prop.forAll(Gens.logRecordData) { data =>
      val created = LogRecordData(
        timestamp = data.timestamp,
        observedTimestamp = data.observedTimestamp,
        traceContext = data.traceContext,
        severity = data.severity,
        severityText = data.severityText,
        body = data.body,
        attributes = data.attributes,
        eventName = data.eventName,
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
