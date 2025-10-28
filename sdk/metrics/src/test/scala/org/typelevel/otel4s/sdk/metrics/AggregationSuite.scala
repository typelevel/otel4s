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

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop
import org.typelevel.otel4s.sdk.metrics.scalacheck.Gens

class AggregationSuite extends DisciplineSuite {

  private implicit val aggregationArbitrary: Arbitrary[Aggregation] =
    Arbitrary(
      Gen.oneOf(
        Gen.const(Aggregation.drop),
        Gen.const(Aggregation.sum),
        Gen.const(Aggregation.lastValue),
        Gen.const(Aggregation.explicitBucketHistogram),
        Gens.bucketBoundaries.map(Aggregation.explicitBucketHistogram)
      )
    )

  private implicit val aggregationCogen: Cogen[Aggregation] =
    Cogen[String].contramap(_.toString)

  checkAll("Aggregation.HashLaws", HashTests[Aggregation].hash)

  test("Show[Aggregation]") {
    Prop.forAll(aggregationArbitrary.arbitrary) { aggregation =>
      val expected = aggregation match {
        case Aggregation.Drop                                => "Aggregation.Drop"
        case Aggregation.Default                             => "Aggregation.Default"
        case Aggregation.Sum                                 => "Aggregation.Sum"
        case Aggregation.LastValue                           => "Aggregation.LastValue"
        case Aggregation.ExplicitBucketHistogram(boundaries) =>
          s"Aggregation.ExplicitBucketHistogram{boundaries=$boundaries}"
      }

      assertEquals(Show[Aggregation].show(aggregation), expected)
      assertEquals(aggregation.toString, expected)
    }
  }

  test("compatible with") {
    Prop.forAll(aggregationArbitrary.arbitrary) {
      case drop @ Aggregation.Drop =>
        InstrumentType.values.foreach { tpe =>
          assert(drop.compatibleWith(tpe))
        }

      case default @ Aggregation.Default =>
        InstrumentType.values.foreach { tpe =>
          assert(default.compatibleWith(tpe))
        }

      case sum @ Aggregation.Sum =>
        val compatible: Set[InstrumentType] = Set(
          InstrumentType.Counter,
          InstrumentType.UpDownCounter,
          InstrumentType.ObservableGauge,
          InstrumentType.ObservableUpDownCounter,
          InstrumentType.Histogram
        )

        compatible.foreach(tpe => assert(sum.compatibleWith(tpe)))

        InstrumentType.values.filterNot(compatible).foreach { tpe =>
          assert(!sum.compatibleWith(tpe))
        }

      case lastValue @ Aggregation.LastValue =>
        val compatible: Set[InstrumentType] = Set(
          InstrumentType.Gauge,
          InstrumentType.ObservableGauge
        )

        compatible.foreach(tpe => assert(lastValue.compatibleWith(tpe)))

        InstrumentType.values.filterNot(compatible).foreach { tpe =>
          assert(!lastValue.compatibleWith(tpe))
        }

      case bucket @ Aggregation.ExplicitBucketHistogram(_) =>
        val compatible: Set[InstrumentType] = Set(
          InstrumentType.Counter,
          InstrumentType.Histogram
        )

        compatible.foreach(tpe => assert(bucket.compatibleWith(tpe)))

        InstrumentType.values.filterNot(compatible).foreach { tpe =>
          assert(!bucket.compatibleWith(tpe))
        }
    }
  }

}
