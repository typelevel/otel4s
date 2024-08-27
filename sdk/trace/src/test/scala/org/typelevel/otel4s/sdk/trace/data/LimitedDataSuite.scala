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

package org.typelevel.otel4s.sdk.trace.data

import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeKey
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.trace.scalacheck.Gens

class LimitedDataSuite extends ScalaCheckSuite {

  test("drop extra elements when appending one by one") {
    Prop.forAll(
      Gens.attributes(100),
      Gens.attributes(200)
    ) { (attributes, extraAttributes) =>
      val data = LimitedData
        .attributes(150, Int.MaxValue)
        .appendAll(attributes)
      val dataWithDropped = extraAttributes.foldLeft(data) {
        (data, attribute) =>
          data.append(attribute)
      }

      assertEquals(dataWithDropped.dropped, 150)
    }
  }

  test("drop extra elements when appending all at once") {
    Prop.forAll(
      Gens.attributes(100),
      Gens.attributes(200)
    ) { (attributes, extraAttributes) =>
      val data = LimitedData
        .attributes(150, Int.MaxValue)
        .appendAll(attributes)
      val dataWithDropped = data.appendAll(extraAttributes)

      assertEquals(dataWithDropped.dropped, 150)
    }
  }

  test("drop extra elements when prepending all at once") {
    Prop.forAll(
      Gens.attributes(100),
      Gens.attributes(200)
    ) { (attributes, extraAttributes) =>
      val data = LimitedData
        .attributes(150, Int.MaxValue)
        .appendAll(attributes)
      val dataWithDropped = data.prependAll(extraAttributes)

      assertEquals(dataWithDropped.dropped, 150)
    }
  }

  test("limit attribute's values") {
    val stringKey = AttributeKey.string("stringKey")
    val booleanSeqKey = AttributeKey.booleanSeq("booleanSeqKey")
    val doubleSeqKey = AttributeKey.doubleSeq("doubleSeqKey")
    val stringSeqKey = AttributeKey.stringSeq("stringSeqKey")
    val longSeqKey = AttributeKey.longSeq("longSeqKey")

    Prop.forAll(
      Gen.posNum[Int],
      Gens.nonEmptyString,
      Gens.nonEmptyVector(Gen.oneOf(true, false)),
      Gens.nonEmptyVector(Gen.double),
      Gens.nonEmptyVector(Gens.nonEmptyString),
      Gens.nonEmptyVector(Gen.long)
    ) {
      (
          valueLengthLimit,
          stringValue,
          booleanSeqValue,
          doubleSeqValue,
          stringSeqValue,
          longSeqValue
      ) =>
        val data = LimitedData
          .attributes(Int.MaxValue, valueLengthLimit)
          .append(Attribute(stringKey, stringValue))
          .appendAll(
            Attributes(
              Attribute(booleanSeqKey, booleanSeqValue.toVector),
              Attribute(doubleSeqKey, doubleSeqValue.toVector)
            )
          )
          .prependAll(
            Attributes(
              Attribute(stringSeqKey, stringSeqValue.toVector),
              Attribute(longSeqKey, longSeqValue.toVector)
            )
          )

        data.elements
          .get(stringKey)
          .exists(_.value.length <= valueLengthLimit) &&
        data.elements
          .get(booleanSeqKey)
          .exists(_.value.length <= valueLengthLimit) &&
        data.elements
          .get(doubleSeqKey)
          .exists(_.value.length <= valueLengthLimit) &&
        data.elements
          .get(stringSeqKey)
          .exists(_.value.length <= valueLengthLimit) &&
        data.elements
          .get(longSeqKey)
          .exists(_.value.length <= valueLengthLimit)
    }
  }

}
