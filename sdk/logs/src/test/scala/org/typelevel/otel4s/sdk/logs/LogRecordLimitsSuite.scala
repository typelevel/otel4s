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

import cats.Show
import cats.kernel.laws.discipline.HashTests
import cats.syntax.show._
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class LogRecordLimitsSuite extends DisciplineSuite {

  private val logRecordLimitsGen: Gen[LogRecordLimits] =
    for {
      maxAttributes <- Gen.choose(0, 100)
      maxAttributeLength <- Gen.choose(0, 100)
    } yield LogRecordLimits.builder
      .withMaxNumberOfAttributes(maxAttributes)
      .withMaxAttributeValueLength(maxAttributeLength)
      .build

  private implicit val logRecordLimitsArbitrary: Arbitrary[LogRecordLimits] =
    Arbitrary(logRecordLimitsGen)

  private implicit val logRecordLimitsCogen: Cogen[LogRecordLimits] =
    Cogen[(Int, Int)].contramap(s => (s.maxNumberOfAttributes, s.maxAttributeValueLength))

  checkAll("LogRecordLimits.HashLaws", HashTests[LogRecordLimits].hash)

  property("Show[LogRecordLimits]") {
    Prop.forAll(logRecordLimitsGen) { s =>
      val expected = "LogRecordLimits{" +
        show"maxNumberOfAttributes=${s.maxNumberOfAttributes}, " +
        show"maxAttributeValueLength=${s.maxAttributeValueLength}}"

      assertEquals(Show[LogRecordLimits].show(s), expected)
      assertEquals(s.toString, expected)
    }
  }

}
