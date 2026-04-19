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

package org.typelevel.otel4s.oteljava.testkit.trace

import cats.data.NonEmptyList
import io.opentelemetry.sdk.trace.data.SpanData

/** An exact expectation for the full set of exported traces in one collection. */
sealed trait TraceForestExpectation {

  /** The exact list of expected root traces. */
  def roots: List[TraceExpectation]

  /** How root traces are matched. */
  def rootsOrderMode: OrderMode

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): TraceForestExpectation
}

object TraceForestExpectation {

  /** A structured reason explaining why a [[TraceForestExpectation]] did not match exported traces. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class RootCountMismatch(expected: Int, actual: Int, actualRootNames: List[String])
        extends Mismatch {
      def message: String =
        s"root count mismatch: expected '$expected', got '$actual'; actual roots: [${actualRootNames.mkString(", ")}]"
    }

    private[trace] final case class MissingRoot(expectation: TraceExpectation, availableRootNames: List[String])
        extends Mismatch {
      def message: String =
        s"missing root matching expectation; available roots: [${availableRootNames.mkString(", ")}]"
    }

    private[trace] final case class DistinctRootMatchUnavailable(
        expectation: TraceExpectation,
        candidateRootNames: List[String]
    ) extends Mismatch {
      def message: String =
        s"no distinct root remained for the expectation; matched roots: [${candidateRootNames.mkString(", ")}]"
    }

    private[trace] final case class RootMismatch(actual: SpanData, mismatches: NonEmptyList[TraceExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"trace mismatch for root '${actual.getName}': ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class CluedMismatch(clue: String, mismatches: NonEmptyList[Mismatch]) extends Mismatch {
      def message: String =
        s"trace expectations mismatch [$clue]: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
  }

  /** Creates an expectation that requires no exported root traces. */
  def empty: TraceForestExpectation =
    Impl(Nil, OrderMode.Unordered, None)

  /** Creates an expectation whose root traces must match in order. */
  def ordered(first: TraceExpectation, rest: TraceExpectation*): TraceForestExpectation =
    Impl(first +: rest.toList, OrderMode.Ordered, None)

  /** Creates an expectation whose root traces may match in any order. */
  def unordered(first: TraceExpectation, rest: TraceExpectation*): TraceForestExpectation =
    Impl(first +: rest.toList, OrderMode.Unordered, None)

  private final case class Impl(
      roots: List[TraceExpectation],
      rootsOrderMode: OrderMode,
      clue: Option[String]
  ) extends TraceForestExpectation {
    def clue(text: String): TraceForestExpectation = copy(clue = Some(text))
  }
}
