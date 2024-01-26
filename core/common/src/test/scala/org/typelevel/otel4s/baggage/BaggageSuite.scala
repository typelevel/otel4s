/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s.baggage

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class BaggageSuite extends DisciplineSuite {

  private val keyValueGen: Gen[(String, String, Option[String])] =
    for {
      key <- Gen.alphaNumStr
      value <- Gen.alphaNumStr
      metadata <- Gen.option(Gen.alphaNumStr)
    } yield (key, value, metadata)

  private implicit val baggageArbitrary: Arbitrary[Baggage] =
    Arbitrary(
      for {
        entries <- Gen.listOfN(5, keyValueGen)
      } yield entries.foldLeft(Baggage.empty)((b, v) =>
        b.updated(v._1, v._2, v._3)
      )
    )

  private implicit val baggageCogen: Cogen[Baggage] =
    Cogen[Map[String, (String, Option[String])]].contramap { baggage =>
      baggage.asMap.map { case (k, v) =>
        (k, (v.value, v.metadata.map(_.value)))
      }
    }

  checkAll("Baggage.HashLaws", HashTests[Baggage].hash)

  test("add entries") {
    Prop.forAll(keyValueGen) { case (key, value, metadata) =>
      val entry = Baggage.Entry(value, metadata.map(Baggage.Metadata(_)))
      val baggage = Baggage.empty.updated(key, value, metadata)

      assertEquals(baggage.asMap, Map(key -> entry))
      assertEquals(baggage.size, 1)
      assertEquals(baggage.isEmpty, false)
      assertEquals(baggage.get(key), Some(entry))
    }
  }

  test("update entries") {
    Prop.forAll(keyValueGen, Gen.alphaNumStr) {
      case ((key, value1, metadata), value2) =>
        val entry1 = Baggage.Entry(value1, metadata.map(Baggage.Metadata(_)))
        val baggage1 = Baggage.empty.updated(key, value1, metadata)

        assertEquals(baggage1.asMap, Map(key -> entry1))
        assertEquals(baggage1.size, 1)
        assertEquals(baggage1.isEmpty, false)
        assertEquals(baggage1.get(key), Some(entry1))

        val entry2 = Baggage.Entry(value2, None)
        val baggage2 = baggage1.updated(key, value2)

        assertEquals(baggage2.asMap, Map(key -> entry2))
        assertEquals(baggage2.size, 1)
        assertEquals(baggage2.isEmpty, false)
        assertEquals(baggage2.get(key), Some(entry2))
    }
  }

  test("remove entries") {
    Prop.forAll(keyValueGen) { case (key, value, metadata) =>
      val baggage = Baggage.empty.updated(key, value, metadata).removed(key)

      assertEquals(baggage.asMap, Map.empty[String, Baggage.Entry])
      assertEquals(baggage.size, 0)
      assertEquals(baggage.isEmpty, true)
      assertEquals(baggage.get(key), None)
    }
  }

  test("Show[Baggage]") {
    Prop.forAll(Gen.listOfN(5, keyValueGen)) { entries =>
      val baggage = entries.foldLeft(Baggage.empty) {
        case (builder, (key, value, meta)) => builder.updated(key, value, meta)
      }

      val entriesString = entries
        .map {
          case (key, value, Some(meta)) =>
            (key, value + ";" + meta)
          case (key, value, None) =>
            (key, value)
        }
        .toMap
        .map { case (key, value) => s"$key=$value" }
        .mkString(",")

      val expected = s"Baggage{$entriesString}"

      assertEquals(Show[Baggage].show(baggage), expected)
    }
  }

}
