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

package org.typelevel.otel4s
package sdk.trace
package data

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.monoid._
import cats.syntax.show._
import munit.DisciplineSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.trace.scalacheck.Arbitraries
import org.typelevel.otel4s.sdk.trace.scalacheck.Cogens
import org.typelevel.otel4s.sdk.trace.scalacheck.Gens

import java.io.PrintWriter
import java.io.StringWriter
import scala.util.control.NoStackTrace

class EventDataSuite extends DisciplineSuite {
  import Cogens.eventDataCogen
  import Arbitraries.eventDataArbitrary

  checkAll("EventData.HashLaws", HashTests[EventData].hash)

  test("Show[EventData]") {
    Prop.forAll(Gens.eventData) { data =>
      val expected =
        show"EventData{name=${data.name}, timestamp=${data.timestamp}, attributes=${data.attributes.elements}}"

      assertEquals(Show[EventData].show(data), expected)
    }
  }

  test("create EventData with given arguments") {
    Prop.forAll(Gens.eventData) { data =>
      assertEquals(EventData(data.name, data.timestamp, data.attributes), data)
    }
  }

  test("create EventData from an exception") {
    Prop.forAll(Gen.finiteDuration, Gens.attributes) { (ts, attributes) =>
      val exception = new RuntimeException("This is fine")

      val stringWriter = new StringWriter()
      val printWriter = new PrintWriter(stringWriter)
      exception.printStackTrace(printWriter)

      val expectedAttributes = Attributes(
        Attribute("exception.type", exception.getClass.getName),
        Attribute("exception.message", exception.getMessage),
        Attribute("exception.stacktrace", stringWriter.toString)
      ) |+| attributes

      val data =
        EventData.fromException(
          ts,
          exception,
          LimitedData
            .attributes(
              SpanLimits.default.maxNumberOfAttributesPerEvent,
              SpanLimits.default.maxAttributeValueLength
            )
            .appendAll(attributes)
        )

      assertEquals(data.name, "exception")
      assertEquals(data.timestamp, ts)
      assertEquals(data.attributes.elements, expectedAttributes)
    }
  }

  test("create EventData from an exception (no message, no stack trace)") {
    Prop.forAll(Gen.finiteDuration, Gens.attributes) { (ts, attributes) =>
      val exception = new RuntimeException with NoStackTrace

      val stringWriter = new StringWriter()
      val printWriter = new PrintWriter(stringWriter)
      exception.printStackTrace(printWriter)

      val expectedAttributes = Attributes(
        Attribute("exception.type", exception.getClass.getName)
      ) |+| attributes

      val data =
        EventData.fromException(
          ts,
          exception,
          LimitedData
            .attributes(
              SpanLimits.default.maxNumberOfAttributesPerEvent,
              SpanLimits.default.maxAttributeValueLength
            )
            .appendAll(attributes)
        )

      assertEquals(data.name, "exception")
      assertEquals(data.timestamp, ts)
      assertEquals(data.attributes.elements, expectedAttributes)
    }
  }
}
