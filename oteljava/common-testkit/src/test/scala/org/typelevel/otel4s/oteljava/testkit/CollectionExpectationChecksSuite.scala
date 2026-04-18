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

class CollectionExpectationChecksSuite extends FunSuite {

  private def check(expected: Int, actual: Int): Either[NonEmptyList[String], Unit] =
    if (expected == actual) Right(()) else Left(NonEmptyList.one(s"$expected != $actual"))

  test("withClueContext wraps failures and leaves successes unchanged") {
    assertEquals(
      CollectionExpectationChecks.withClueContext(Some("ctx"), Left(NonEmptyList.one("boom"))) { (clue, mismatches) =>
        s"$clue: ${mismatches.toList.mkString(",")}"
      },
      Left(NonEmptyList.one("ctx: boom"))
    )

    assertEquals(
      CollectionExpectationChecks.withClueContext(Some("ctx"), Right(())) { (clue: String, _: NonEmptyList[String]) =>
        clue
      },
      Right(())
    )
  }

  test("firstFailingIndex and firstMatchingIndex find the expected element") {
    assertEquals(
      CollectionExpectationChecks.firstFailingIndex(List(1, 2, 3), 2)(
        check,
        (index: Int, mismatches: NonEmptyList[String]) => s"$index:${mismatches.head}"
      ),
      Some("0:2 != 1")
    )

    assertEquals(
      CollectionExpectationChecks.firstMatchingIndex(List(1, 2, 3), 2)(_ == _),
      Some(1)
    )
  }

  test("closestMismatch returns the shortest mismatch or the fallback for empty input") {
    def customCheck(expected: Int, actual: Int): Either[NonEmptyList[String], Unit] =
      if (expected == actual) Right(())
      else Left(NonEmptyList.fromListUnsafe(List.fill(math.abs(expected - actual))(s"$expected != $actual")))

    assertEquals(
      CollectionExpectationChecks.closestMismatch(List(1, 4, 8), 5)(customCheck, "empty"),
      NonEmptyList.one("5 != 4")
    )

    assertEquals(
      CollectionExpectationChecks.closestMismatch(List.empty[Int], 5)(customCheck, "empty"),
      NonEmptyList.one("empty")
    )
  }

  test("containsCheck returns missing mismatches for unmatched expectations") {
    val result: Either[NonEmptyList[String], Set[Int]] =
      CollectionExpectationChecks.containsCheck(NonEmptyList.of(1, 2), List(1))(
        _ == _,
        (expected: Int, mismatches: NonEmptyList[String]) => s"$expected:${mismatches.head}",
        (items, expected) => CollectionExpectationChecks.closestMismatch(items, expected)(check, "empty")
      )

    assertEquals(result, Left(NonEmptyList.one("2:2 != 1")))
  }

  test("compositeCheck supports and/or semantics") {
    val leftFailure = Left(NonEmptyList.one("left"))
    val rightFailure = Left(NonEmptyList.one("right"))

    assertEquals(
      CollectionExpectationChecks.compositeCheck(LogicalOperator.And, leftFailure, rightFailure)(mismatches =>
        mismatches.toList.mkString("+")
      ),
      Left(NonEmptyList.one("left+right"))
    )

    assertEquals(
      CollectionExpectationChecks.compositeCheck(LogicalOperator.Or, leftFailure, Right(()))(mismatches =>
        mismatches.toList.mkString("+")
      ),
      Right(())
    )

    assertEquals(
      CollectionExpectationChecks.compositeCheck(LogicalOperator.Or, leftFailure, rightFailure)(mismatches =>
        mismatches.toList.mkString("+")
      ),
      Left(NonEmptyList.one("left+right"))
    )
  }
}
