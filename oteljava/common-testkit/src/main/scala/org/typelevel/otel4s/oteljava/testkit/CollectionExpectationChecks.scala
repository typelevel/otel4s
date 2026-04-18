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

private[testkit] object CollectionExpectationChecks {

  def withClueContext[M](
      clue: Option[String],
      result: Either[NonEmptyList[M], Unit]
  )(cluedMismatch: (String, NonEmptyList[M]) => M): Either[NonEmptyList[M], Unit] =
    clue match {
      case Some(value) =>
        result match {
          case Right(_)         => Right(())
          case Left(mismatches) => Left(NonEmptyList.one(cluedMismatch(value, mismatches)))
        }
      case None => result
    }

  def firstFailingIndex[A, E, InnerMismatch, OuterMismatch](
      items: List[A],
      expectation: E
  )(
      check: (E, A) => Either[NonEmptyList[InnerMismatch], Unit],
      failingMismatch: (Int, NonEmptyList[InnerMismatch]) => OuterMismatch
  ): Option[OuterMismatch] =
    items.zipWithIndex.collectFirst(Function.unlift { case (item, index) =>
      check(expectation, item).left.toOption.map(failingMismatch(index, _))
    })

  def firstMatchingIndex[A, E](
      items: List[A],
      expectation: E
  )(matches: (E, A) => Boolean): Option[Int] =
    items.zipWithIndex.collectFirst { case (item, index) if matches(expectation, item) => index }

  def closestMismatch[A, E, M](
      items: List[A],
      expectation: E
  )(check: (E, A) => Either[NonEmptyList[M], Unit], whenEmpty: => M): NonEmptyList[M] =
    items
      .flatMap(item => check(expectation, item).left.toOption)
      .sortBy(_.length)
      .headOption
      .getOrElse(NonEmptyList.one(whenEmpty))

  def containsCheck[A, E, M, InnerMismatch](
      expected: NonEmptyList[E],
      items: List[A]
  )(
      matches: (E, A) => Boolean,
      missingMismatch: (E, NonEmptyList[InnerMismatch]) => M,
      closestMismatchFor: (List[A], E) => NonEmptyList[InnerMismatch]
  ): Either[NonEmptyList[M], Set[Int]] = {
    val indexed = items.toVector
    val candidates =
      expected.toList.map(expectation => indexed.indices.filter(i => matches(expectation, indexed(i))).toList).toVector
    val matching = MaximumMatching.find(candidates)

    if (matching.isComplete) Right(matching.matchedCandidateIndices)
    else {
      val mismatches = expected.toList.zipWithIndex.collect {
        case (expectation, index) if !matching.matchedExpectationIndices(index) =>
          missingMismatch(expectation, closestMismatchFor(items, expectation))
      }
      NonEmptyList.fromList(mismatches).toLeft(matching.matchedCandidateIndices)
    }
  }

  def compositeCheck[M](
      operator: LogicalOperator,
      left: Either[NonEmptyList[M], Unit],
      right: => Either[NonEmptyList[M], Unit]
  )(compositeMismatch: NonEmptyList[M] => M): Either[NonEmptyList[M], Unit] =
    operator match {
      case LogicalOperator.And =>
        ExpectationChecks.combine(left, right) match {
          case Right(_)         => Right(())
          case Left(mismatches) => Left(NonEmptyList.one(compositeMismatch(mismatches)))
        }

      case LogicalOperator.Or =>
        left match {
          case success @ Right(_) => success
          case Left(leftMismatch) =>
            right match {
              case success @ Right(_)  => success
              case Left(rightMismatch) =>
                ExpectationChecks.mismatch(compositeMismatch(leftMismatch.concatNel(rightMismatch)))
            }
        }
    }
}
