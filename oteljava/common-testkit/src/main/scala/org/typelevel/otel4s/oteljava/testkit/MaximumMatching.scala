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

private[testkit] object MaximumMatching {

  final case class Result(
      matchedExpectationIndices: Set[Int],
      matchedCandidateIndices: Set[Int],
      expectationCount: Int
  ) {
    def isComplete: Boolean =
      matchedExpectationIndices.size == expectationCount

    def size: Int =
      matchedExpectationIndices.size
  }

  def find(candidates: Vector[List[Int]]): Result = {
    val orderedCandidates = candidates.zipWithIndex.sortBy(_._1.length)

    def augment(
        expectationIndex: Int,
        seen: Set[Int],
        matching: Map[Int, Int]
    ): Option[Map[Int, Int]] =
      candidates(expectationIndex).foldLeft(Option.empty[Map[Int, Int]]) {
        case (result @ Some(_), _)                          => result
        case (None, candidateIndex) if seen(candidateIndex) => None
        case (None, candidateIndex)                         =>
          matching.get(candidateIndex) match {
            case None                        => Some(matching.updated(candidateIndex, expectationIndex))
            case Some(otherExpectationIndex) =>
              augment(otherExpectationIndex, seen + candidateIndex, matching).map(
                _.updated(candidateIndex, expectationIndex)
              )
          }
      }

    val matching =
      orderedCandidates.foldLeft(Map.empty[Int, Int]) { case (current, (_, expectationIndex)) =>
        augment(expectationIndex, Set.empty, current).getOrElse(current)
      }

    Result(
      matchedExpectationIndices = matching.values.toSet,
      matchedCandidateIndices = matching.keySet,
      expectationCount = candidates.length
    )
  }
}
