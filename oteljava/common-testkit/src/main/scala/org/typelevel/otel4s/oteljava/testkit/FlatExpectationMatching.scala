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

private[testkit] object FlatExpectationMatching {

  def exists[A, E](actual: List[A], expectation: E)(matches: (E, A) => Boolean): Boolean =
    find(actual, expectation)(matches).nonEmpty

  def find[A, E](actual: List[A], expectation: E)(matches: (E, A) => Boolean): Option[A] =
    actual.find(matches(expectation, _))

  def check[A, E, M](actual: List[A], expectation: E)(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M
  ): Option[M] =
    if (exists(actual, expectation)(matches)) None else Some(bestMismatch(actual, expectation))

  def checkAll[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M
  ): Either[NonEmptyList[M], Unit] =
    NonEmptyList.fromList(missing(actual, expectations)(matches, bestMismatch)).toLeft(())

  def checkAllDistinct[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M,
      distinctUnavailable: (E, List[String]) => M,
      renderActual: A => String
  ): Either[NonEmptyList[M], Unit] =
    NonEmptyList
      .fromList(missingDistinct(actual, expectations)(matches, bestMismatch, distinctUnavailable, renderActual))
      .toLeft(())

  def missing[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M
  ): List[M] =
    expectations.flatMap(expectation => check(actual, expectation)(matches, bestMismatch))

  def missingDistinct[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M,
      distinctUnavailable: (E, List[String]) => M,
      renderActual: A => String
  ): List[M] = {
    val indexedActual = actual.toVector
    val indexedExpectations = expectations.toVector
    val candidates = indexedExpectations.map { expectation =>
      indexedActual.indices.filter(index => matches(expectation, indexedActual(index))).toList
    }
    val matching = MaximumMatching.find(candidates)

    if (matching.isComplete) Nil
    else
      indexedExpectations.indices.collect {
        case index if !matching.matchedExpectationIndices(index) =>
          candidates(index) match {
            case Nil =>
              bestMismatch(actual, indexedExpectations(index))
            case matchingCandidates =>
              distinctUnavailable(
                indexedExpectations(index),
                matchingCandidates.map(indexedActual(_)).map(renderActual).distinct
              )
          }
      }.toList
  }

  def allMatch[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M
  ): Boolean =
    checkAll(actual, expectations)(matches, bestMismatch).isRight

  def allMatchDistinct[A, E, M](actual: List[A], expectations: List[E])(
      matches: (E, A) => Boolean,
      bestMismatch: (List[A], E) => M,
      distinctUnavailable: (E, List[String]) => M,
      renderActual: A => String
  ): Boolean =
    checkAllDistinct(actual, expectations)(matches, bestMismatch, distinctUnavailable, renderActual).isRight

  def format[M](title: String, mismatches: NonEmptyList[M])(message: M => String): String =
    mismatches.toList.zipWithIndex
      .map { case (mismatch, index) => s"${index + 1}. ${message(mismatch)}" }
      .mkString(s"$title failed:\n", "\n", "")
}
