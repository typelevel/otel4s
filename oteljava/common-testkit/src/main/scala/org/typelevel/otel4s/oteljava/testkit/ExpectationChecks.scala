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

import cats.syntax.either._
import cats.data.NonEmptyList

private[testkit] object ExpectationChecks {
  def success[A]: Either[NonEmptyList[A], Unit] =
    Right(())

  def mismatch[A](failure: A): Either[NonEmptyList[A], Unit] =
    Left(NonEmptyList.one(failure))

  def combine[A](results: Either[NonEmptyList[A], Unit]*): Either[NonEmptyList[A], Unit] =
    combine(results.toList)

  def combine[A](
      results: Iterable[Either[NonEmptyList[A], Unit]]
  ): Either[NonEmptyList[A], Unit] = {
    val failures = results.collect { case Left(nel) => nel }.toList

    failures match {
      case Nil          => Either.unit
      case head :: tail => Left(tail.foldLeft(head)(_.concatNel(_)))
    }
  }

  def nested[Inner, Outer](
      result: Either[NonEmptyList[Inner], Unit]
  )(
      wrap: NonEmptyList[Inner] => Outer
  ): Either[NonEmptyList[Outer], Unit] =
    result.left.map(failures => NonEmptyList.one(wrap(failures)))

  def compareOption[A](
      expected: Option[Option[String]],
      actual: Option[String]
  )(mismatch: (Option[String], Option[String]) => A): Either[NonEmptyList[A], Unit] =
    expected match {
      case None =>
        Either.unit
      case Some(Some(value)) if actual.contains(value) =>
        Either.unit
      case Some(Some(value)) =>
        Left(NonEmptyList.one(mismatch(Some(value), actual)))
      case Some(None) =>
        Either.cond(actual.isEmpty, (), NonEmptyList.one(mismatch(None, actual)))
    }
}
