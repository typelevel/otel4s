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

package org.typelevel.otel4s.sdk.metrics
package view

import cats.Show
import munit.ScalaCheckSuite
import org.scalacheck.Gen
import org.scalacheck.Prop

class ViewSuite extends ScalaCheckSuite {

  private val viewGen: Gen[View] =
    for {
      name <- Gen.alphaNumStr
      description <- Gen.alphaNumStr
      aggregation <- Gen.oneOf(
        Aggregation.default,
        Aggregation.drop,
        Aggregation.sum,
        Aggregation.lastValue,
        Aggregation.explicitBucketHistogram
      )
      cardinalityLimit <- Gen.chooseNum(1, Int.MaxValue)
    } yield View.builder
      .withName(name)
      .withDescription(description)
      .withAggregation(aggregation)
      .withCardinalityLimit(cardinalityLimit)
      .build

  test("default (empty) instance") {
    val view = View.builder.build

    assertEquals(view.name, None)
    assertEquals(view.description, None)
    assertEquals(view.aggregation, None)
    assertEquals(view.cardinalityLimit, None)
  }

  test("ignore empty strings") {
    val view = View.builder.withName("").withDescription("").build

    assertEquals(view.name, None)
    assertEquals(view.description, None)
  }

  test("cardinality limit must be positive") {
    Prop.forAll(Gen.chooseNum(Int.MinValue, Int.MaxValue)) {
      case limit if limit <= 0 =>
        val _ = interceptMessage[IllegalArgumentException](
          s"requirement failed: cardinality limit [$limit] must be positive"
        )(
          View.builder.withCardinalityLimit(limit).build
        )

      case limit =>
        val view = View.builder.withCardinalityLimit(limit).build
        assertEquals(view.cardinalityLimit, Some(limit))
    }
  }

  test("Show[View]") {
    Prop.forAll(viewGen) { view =>
      val expected = {
        def prop[A](focus: View => Option[A], name: String) =
          focus(view).map(value => s"$name=$value")

        Seq(
          prop(_.name, "name"),
          prop(_.description, "description"),
          prop(_.aggregation, "aggregation"),
          prop(_.attributesProcessor, "attributesProcessor"),
          prop(_.cardinalityLimit, "cardinalityLimit")
        ).collect { case Some(k) => k }.mkString("View{", ", ", "}")
      }

      assertEquals(Show[View].show(view), expected)
      assertEquals(view.toString, expected)
    }
  }

}
