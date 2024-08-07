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

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class SpanLimitsSuite extends DisciplineSuite {

  private val spanLimitsGen: Gen[SpanLimits] =
    for {
      maxAttributes <- Gen.choose(0, 100)
      maxEvents <- Gen.choose(0, 100)
      maxLinks <- Gen.choose(0, 100)
      maxAttributesPerEvent <- Gen.choose(0, 100)
      maxAttributesPerLink <- Gen.choose(0, 100)
      maxAttributeLength <- Gen.choose(0, 100)
    } yield SpanLimits.builder
      .withMaxNumberOfAttributes(maxAttributes)
      .withMaxNumberOfEvents(maxEvents)
      .withMaxNumberOfLinks(maxLinks)
      .withMaxNumberOfAttributesPerEvent(maxAttributesPerEvent)
      .withMaxNumberOfAttributesPerLink(maxAttributesPerLink)
      .withMaxAttributeValueLength(maxAttributeLength)
      .build

  private implicit val spanLimitsArbitrary: Arbitrary[SpanLimits] =
    Arbitrary(spanLimitsGen)

  private implicit val spanLimitsCogen: Cogen[SpanLimits] =
    Cogen[(Int, Int, Int, Int, Int, Int)].contramap { s =>
      (
        s.maxNumberOfAttributes,
        s.maxNumberOfEvents,
        s.maxNumberOfLinks,
        s.maxNumberOfAttributesPerEvent,
        s.maxNumberOfAttributesPerLink,
        s.maxAttributeValueLength
      )
    }

  checkAll("SpanLimits.HashLaws", HashTests[SpanLimits].hash)

  property("Show[SpanLimits]") {
    Prop.forAll(spanLimitsGen) { s =>
      val expected = "SpanLimits{" +
        show"maxNumberOfAttributes=${s.maxNumberOfAttributes}, " +
        show"maxNumberOfEvents=${s.maxNumberOfEvents}, " +
        show"maxNumberOfLinks=${s.maxNumberOfLinks}, " +
        show"maxNumberOfAttributesPerEvent=${s.maxNumberOfAttributesPerEvent}, " +
        show"maxNumberOfAttributesPerLink=${s.maxNumberOfAttributesPerLink}, " +
        show"maxAttributeValueLength=${s.maxAttributeValueLength}}"

      assertEquals(Show[SpanLimits].show(s), expected)
    }
  }

}
