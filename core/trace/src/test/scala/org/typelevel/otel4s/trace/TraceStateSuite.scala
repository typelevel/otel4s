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

package org.typelevel.otel4s.trace

import cats.Show
import cats.kernel.laws.discipline.HashTests
import munit.DisciplineSuite
import org.scalacheck.Arbitrary
import org.scalacheck.Cogen
import org.scalacheck.Gen
import org.scalacheck.Prop

class TraceStateSuite extends DisciplineSuite {

  private val MaxEntries = 32
  private val StrMaxSize = 256

  private val keyAllowedFirstChar: Gen[Char] =
    Gen.alphaLowerChar

  private val keyAllowedChars: Gen[Char] =
    Gen.oneOf(
      Gen.alphaLowerChar,
      Gen.numChar,
      Gen.const('_'),
      Gen.const('-'),
      Gen.const('*'),
      Gen.const('/')
    )

  private val keyGen: Gen[String] =
    for {
      first <- keyAllowedFirstChar
      rest <- Gen.stringOfN(StrMaxSize - 1, keyAllowedChars)
    } yield first +: rest

  /** Value is opaque string up to 256 characters printable ASCII RFC0020
    * characters (i.e., the range 0x20 to 0x7E) except comma , and =.
    */
  private val valueGen: Gen[String] = {
    val comma = ','.toInt
    val eq = '='.toInt

    val allowed =
      Gen.oneOf(0x20.to(0x7e).filter(c => c != comma && c != eq).map(_.toChar))

    Gen.stringOfN(StrMaxSize, allowed)
  }

  private val keyValueGen: Gen[(String, String)] =
    for {
      key <- keyGen
      value <- valueGen
    } yield (key, value)

  private implicit val traceStateArbitrary: Arbitrary[TraceState] =
    Arbitrary(
      for {
        entries <- Gen.listOfN(5, keyValueGen)
      } yield entries.foldLeft(TraceState.empty)((b, v) =>
        b.updated(v._1, v._2)
      )
    )

  private implicit val traceStateCogen: Cogen[TraceState] =
    Cogen[Map[String, String]].contramap(_.asMap)

  checkAll("TraceState.HashLaws", HashTests[TraceState].hash)

  test("add entries") {
    Prop.forAll(keyGen, valueGen) { (key, value) =>
      val state = TraceState.empty.updated(key, value)

      assertEquals(state.asMap, Map(key -> value))
      assertEquals(state.size, 1)
      assertEquals(state.isEmpty, false)
      assertEquals(state.get(key), Some(value))
    }
  }

  test("update entries") {
    Prop.forAll(keyGen, valueGen, valueGen) { (key, value1, value2) =>
      val state = TraceState.empty.updated(key, value1).updated(key, value2)

      assertEquals(state.asMap, Map(key -> value2))
      assertEquals(state.size, 1)
      assertEquals(state.isEmpty, false)
      assertEquals(state.get(key), Some(value2))
    }
  }

  test("remove entries") {
    Prop.forAll(keyGen, valueGen) { (key, value) =>
      val state = TraceState.empty.updated(key, value).removed(key)

      assertEquals(state.asMap, Map.empty[String, String])
      assertEquals(state.size, 0)
      assertEquals(state.isEmpty, true)
      assertEquals(state.get(key), None)
    }
  }

  test("ignore 'put' once the limit of entries is reached") {
    Prop.forAll(Gen.listOfN(50, keyValueGen)) { entries =>
      val state = entries.foldLeft(TraceState.empty) {
        case (builder, (key, value)) => builder.updated(key, value)
      }

      assertEquals(state.asMap, entries.take(MaxEntries).toMap)
      assertEquals(state.size, MaxEntries)
      assertEquals(state.isEmpty, false)

      entries.take(MaxEntries).foreach { case (key, value) =>
        assertEquals(state.get(key), Some(value))
      }
    }
  }

  test("ignore invalid keys: empty string") {
    val state = TraceState.empty.updated("", "some-value")
    assertEquals(state.isEmpty, true)
  }

  // a digit is only allowed if the key is in the tenant format (with an '@')
  test("ignore invalid keys: first char is digit") {
    Prop.forAll(Gen.numStr, valueGen) { (key, value) =>
      val state = TraceState.empty.updated(key, value)
      assertEquals(state.isEmpty, true)
    }
  }

  test("ignore invalid keys: vendor id length > 13") {
    val state = TraceState.empty.updated("1@abcdefghijklmn", "value")
    assertEquals(state.isEmpty, true)
  }

  test("ignore invalid keys: key length > 256") {
    val key = new String(Array.fill(StrMaxSize + 1)('a'))
    val state = TraceState.empty.updated(key, "value")
    assertEquals(state.isEmpty, true)
  }

  test("ignore invalid keys: multiple @ signs") {
    val state = TraceState.empty.updated("1@b@c", "value")
    assertEquals(state.isEmpty, true)
  }

  test("allow valid keys: first digit is char (tenant format)") {
    Prop.forAll(Gen.numChar, valueGen) { (key, value) =>
      val state = TraceState.empty.updated(s"$key@tenant", value)

      assertEquals(state.isEmpty, false)
      assertEquals(state.get(s"$key@tenant"), Some(value))
    }
  }

  test("allow valid keys: vendor id length <= 13") {
    val state = TraceState.empty.updated("1@abcdefghijklm", "value")
    assertEquals(state.size, 1)
  }

  test("ignore invalid values: value length > 256") {
    val value = new String(Array.fill(StrMaxSize + 1)('a'))
    val state = TraceState.empty.updated("key", value)
    assertEquals(state.isEmpty, true)
  }

  test("preserve order of inserted entries") {
    val state = TraceState.empty
      .updated("a", "value_a")
      .updated("b", "value_b")
      .updated("c", "value_c")
      .updated("a", "value_a_2")

    val expected = List(
      "a" -> "value_a_2",
      "c" -> "value_c",
      "b" -> "value_b"
    )

    assertEquals(state.asMap.toList, expected)
  }

  test("Show[TraceState]") {
    Prop.forAll(Gen.listOfN(5, keyValueGen)) { entries =>
      val state = entries.foldLeft(TraceState.empty) {
        case (builder, (key, value)) => builder.updated(key, value)
      }

      val entriesString = entries.reverse
        .map { case (key, value) => s"$key=$value" }
        .mkString("{", ",", "}")

      val expected = s"TraceState{entries=$entriesString}"

      assertEquals(Show[TraceState].show(state), expected)
    }
  }

}
