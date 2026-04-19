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
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.oteljava.testkit.MaximumMatching

/** An exact structural expectation for one exported trace subtree.
  *
  * A `TraceExpectation` combines a [[SpanExpectation]] for the current span with exact expectations for its direct
  * children. Child matching is distinct, so each expected child must match a different collected child span.
  */
sealed trait TraceExpectation {

  /** The expectation applied to the current span in the subtree. */
  def current: SpanExpectation

  /** The exact list of child subtrees expected under [[current]]. */
  def children: List[TraceExpectation]

  /** How direct children are matched. */
  def childrenOrderMode: OrderMode

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): TraceExpectation

  private[trace] def check(tree: TraceExpectations.ActualTrace): Either[NonEmptyList[TraceExpectation.Mismatch], Unit]
}

object TraceExpectation {

  /** A structured reason explaining why a [[TraceExpectation]] did not match a trace subtree. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class SpanMismatch(mismatches: NonEmptyList[SpanExpectation.Mismatch]) extends Mismatch {
      def message: String = s"span mismatch: ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class ChildCountMismatch(expected: Int, actual: Int, actualChildNames: List[String])
        extends Mismatch {
      def message: String =
        s"child count mismatch: expected '$expected', got '$actual'; actual children: [${actualChildNames.mkString(", ")}]"
    }

    private[trace] final case class MissingChild(expectation: TraceExpectation, availableChildNames: List[String])
        extends Mismatch {
      def message: String =
        s"missing child matching expectation; available children: [${availableChildNames.mkString(", ")}]"
    }

    private[trace] final case class DistinctChildMatchUnavailable(
        expectation: TraceExpectation,
        candidateChildNames: List[String]
    ) extends Mismatch {
      def message: String =
        s"no distinct child remained for the expectation; matched children: [${candidateChildNames.mkString(", ")}]"
    }

    private[trace] final case class ChildMismatch(actual: SpanData, mismatches: NonEmptyList[TraceExpectation.Mismatch])
        extends Mismatch {
      def message: String =
        s"trace subtree mismatch for '${actual.getName}': ${mismatches.toList.map(_.message).mkString(", ")}"
    }

    private[trace] final case class CluedMismatch(clue: String, mismatches: NonEmptyList[Mismatch]) extends Mismatch {
      def message: String =
        s"trace expectation mismatch [$clue]: ${mismatches.toList.map(_.message).mkString(", ")}"
    }
  }

  /** Creates a leaf expectation with no child spans. */
  def leaf(current: SpanExpectation): TraceExpectation =
    Impl(current, Nil, OrderMode.Unordered, None)

  /** Creates an expectation whose direct children must match in order. */
  def ordered(current: SpanExpectation, children: TraceExpectation*): TraceExpectation =
    Impl(current, children.toList, OrderMode.Ordered, None)

  /** Creates an expectation whose direct children may match in any order. */
  def unordered(current: SpanExpectation, children: TraceExpectation*): TraceExpectation =
    Impl(current, children.toList, OrderMode.Unordered, None)

  private final case class Impl(
      current: SpanExpectation,
      children: List[TraceExpectation],
      childrenOrderMode: OrderMode,
      clue: Option[String]
  ) extends TraceExpectation {
    def clue(text: String): TraceExpectation = copy(clue = Some(text))

    def check(tree: TraceExpectations.ActualTrace): Either[NonEmptyList[Mismatch], Unit] = {
      val result = {
        val currentCheck =
          current.check(tree.span).left.map(mismatches => NonEmptyList.one(Mismatch.SpanMismatch(mismatches)))

        val childCheck =
          if (children.length != tree.children.length)
            Left(
              NonEmptyList.one(
                Mismatch.ChildCountMismatch(children.length, tree.children.length, tree.children.map(_.span.getName))
              )
            )
          else
            childrenOrderMode match {
              case OrderMode.Ordered =>
                ExpectationChecks.combine(
                  children.zip(tree.children).map { case (expectation, child) =>
                    expectation
                      .check(child)
                      .left
                      .map(mismatches => NonEmptyList.one(Mismatch.ChildMismatch(child.span, mismatches)))
                  }
                )

              case OrderMode.Unordered =>
                val candidates = children.toVector.map { expectation =>
                  tree.children.indices.filter(index => expectation.check(tree.children(index)).isRight).toList
                }
                val matching = MaximumMatching.find(candidates)

                if (matching.isComplete) Right(())
                else
                  Left(
                    NonEmptyList.fromListUnsafe(
                      children.indices.collect {
                        case index if !matching.matchedExpectationIndices(index) =>
                          candidates(index) match {
                            case Nil =>
                              TraceExpectations.bestChildMismatch(tree.children, children(index))
                            case matches =>
                              Mismatch.DistinctChildMatchUnavailable(
                                children(index),
                                matches.map(tree.children(_).span.getName).distinct
                              )
                          }
                      }.toList
                    )
                  )
            }

        ExpectationChecks.combine(currentCheck, childCheck)
      }

      clue match {
        case Some(value) =>
          result.left.map(mismatches => NonEmptyList.one(Mismatch.CluedMismatch(value, mismatches)))
        case None =>
          result
      }
    }
  }
}
