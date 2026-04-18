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

package org.typelevel.otel4s.oteljava.testkit

import cats.data.NonEmptyList
import munit.FunSuite

class FlatExpectationMatchingSuite extends FunSuite {

  private val actual = List("foo", "bar")
  private val expectations = List("foo", "baz")

  private def matches(expected: String, value: String): Boolean =
    expected == value

  private def bestMismatch(values: List[String], expected: String): String =
    s"missing $expected from [${values.mkString(", ")}]"

  private def distinctUnavailable(expected: String, candidates: List[String]): String =
    s"no distinct match for $expected from [${candidates.mkString(", ")}]"

  test("exists and find use the provided predicate") {
    assert(FlatExpectationMatching.exists(actual, "foo")(matches))
    assertEquals(FlatExpectationMatching.find(actual, "bar")(matches), Some("bar"))
    assertEquals(FlatExpectationMatching.find(actual, "baz")(matches), None)
  }

  test("checkAll reports missing expectations") {
    assertEquals(
      FlatExpectationMatching.checkAll(actual, expectations)(matches, bestMismatch),
      Left(NonEmptyList.one("missing baz from [foo, bar]"))
    )
  }

  test("checkAllDistinct reports distinct-unavailable expectations") {
    val duplicateExpectations = List("foo", "foo")

    assertEquals(
      FlatExpectationMatching.checkAllDistinct(actual, duplicateExpectations)(
        matches,
        bestMismatch,
        distinctUnavailable,
        identity
      ),
      Left(NonEmptyList.one("no distinct match for foo from [foo]"))
    )
  }

  test("missing and missingDistinct preserve expectation order") {
    assertEquals(
      FlatExpectationMatching.missing(actual, List("baz", "qux"))(matches, bestMismatch),
      List("missing baz from [foo, bar]", "missing qux from [foo, bar]")
    )

    assertEquals(
      FlatExpectationMatching.missingDistinct(actual, List("foo", "foo", "bar"))(
        matches,
        bestMismatch,
        distinctUnavailable,
        identity
      ),
      List("no distinct match for foo from [foo]")
    )
  }

  test("format renders the expected title and numbering") {
    val rendered = FlatExpectationMatching.format("Example", NonEmptyList.of("first", "second"))(identity)

    assertEquals(rendered, "Example failed:\n1. first\n2. second")
  }
}
