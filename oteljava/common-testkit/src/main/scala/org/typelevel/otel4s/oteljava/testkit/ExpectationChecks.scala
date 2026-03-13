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

private[testkit] object ExpectationChecks {
  def success[F]: Either[NonEmptyList[F], Unit] =
    Right(())

  def failure[F](failure: F): Either[NonEmptyList[F], Unit] =
    Left(NonEmptyList.one(failure))

  def combine[F](
      results: Iterable[Either[NonEmptyList[F], Unit]]
  ): Either[NonEmptyList[F], Unit] = {
    val failures = results.iterator.collect { case Left(nel) => nel }.toList

    failures match {
      case Nil          => success
      case head :: tail => Left(tail.foldLeft(head)(_.concatNel(_)))
    }
  }

  def nested[Inner, Outer](
      result: Either[NonEmptyList[Inner], Unit]
  )(
      wrap: NonEmptyList[Inner] => Outer
  ): Either[NonEmptyList[Outer], Unit] =
    result.left.map(failures => NonEmptyList.one(wrap(failures)))
}
