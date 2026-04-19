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
import org.typelevel.otel4s.oteljava.testkit.FlatExpectationMatching
import org.typelevel.otel4s.oteljava.testkit.MaximumMatching

/** Helpers for matching collected spans against exact trace expectations. */
object TraceExpectations {
  private[trace] final case class ActualTrace(span: SpanData, children: List[ActualTrace])

  /** Checks that the collected spans form exactly the trace trees described by the expectation. */
  def check(
      spans: List[SpanData],
      expectation: TraceForestExpectation
  ): Either[NonEmptyList[TraceForestExpectation.Mismatch], Unit] = {
    val result = {
      val traces = buildTraces(spans)
      val countMismatch =
        if (expectation.roots.length == traces.length) Nil
        else {
          List(
            TraceForestExpectation.Mismatch.RootCountMismatch(
              expectation.roots.length,
              traces.length,
              traces.map(_.span.getName)
            )
          )
        }

      val rootMismatches =
        if (expectation.roots.length != traces.length) Nil
        else
          expectation.rootsOrderMode match {
            case OrderMode.Ordered =>
              expectation.roots.zip(traces).flatMap { case (rootExpectation, actualTrace) =>
                rootExpectation.check(actualTrace).left.toOption.map { mismatches =>
                  TraceForestExpectation.Mismatch.RootMismatch(actualTrace.span, mismatches)
                }
              }

            case OrderMode.Unordered =>
              val candidates = expectation.roots.toVector.map { root =>
                traces.indices.filter(index => root.check(traces(index)).isRight).toList
              }
              val matching = MaximumMatching.find(candidates)

              expectation.roots.indices.collect {
                case index if !matching.matchedExpectationIndices(index) =>
                  candidates(index) match {
                    case Nil =>
                      bestRootMismatch(traces, expectation.roots(index))
                    case matches =>
                      TraceForestExpectation.Mismatch.DistinctRootMatchUnavailable(
                        expectation.roots(index),
                        matches.map(traces(_).span.getName).distinct
                      )
                  }
              }.toList
          }

      NonEmptyList.fromList(countMismatch ++ rootMismatches).toLeft(())
    }

    expectation.clue match {
      case Some(value) =>
        result.left.map(mismatches =>
          NonEmptyList.one(TraceForestExpectation.Mismatch.CluedMismatch(value, mismatches))
        )
      case None =>
        result
    }
  }

  /** Formats mismatches into a multi-line human-readable failure message. */
  def format(mismatches: NonEmptyList[TraceForestExpectation.Mismatch]): String =
    FlatExpectationMatching.format("Trace expectations", mismatches)(_.message)

  private[trace] def bestRootMismatch(
      traces: List[ActualTrace],
      expectation: TraceExpectation
  ): TraceForestExpectation.Mismatch =
    traces
      .flatMap(tree => expectation.check(tree).left.toOption.map(tree -> _))
      .sortBy { case (tree, mismatches) =>
        val nameMismatch = mismatches.exists {
          case spanMismatch: TraceExpectation.Mismatch.SpanMismatch =>
            spanMismatch.mismatches.exists {
              case _: SpanExpectation.Mismatch.NameMismatch => true
              case _                                        => false
            }
          case _ => false
        }
        (nameMismatch, mismatches.length, tree.children.length)
      }
      .headOption
      .map { case (tree, mismatches) => TraceForestExpectation.Mismatch.RootMismatch(tree.span, mismatches) }
      .getOrElse(TraceForestExpectation.Mismatch.MissingRoot(expectation, traces.map(_.span.getName)))

  private[trace] def bestChildMismatch(
      traces: List[ActualTrace],
      expectation: TraceExpectation
  ): TraceExpectation.Mismatch =
    traces
      .flatMap(tree => expectation.check(tree).left.toOption.map(tree -> _))
      .sortBy { case (tree, mismatches) =>
        val nameMismatch = mismatches.exists {
          case spanMismatch: TraceExpectation.Mismatch.SpanMismatch =>
            spanMismatch.mismatches.exists {
              case _: SpanExpectation.Mismatch.NameMismatch => true
              case _                                        => false
            }
          case _ => false
        }
        (nameMismatch, mismatches.length, tree.children.length)
      }
      .headOption
      .map { case (tree, mismatches) => TraceExpectation.Mismatch.ChildMismatch(tree.span, mismatches) }
      .getOrElse(TraceExpectation.Mismatch.MissingChild(expectation, traces.map(_.span.getName)))

  private def buildTraces(spans: List[SpanData]): List[ActualTrace] = {
    type SpanKey = (String, String)

    def keyOf(span: SpanData): SpanKey =
      (span.getSpanContext.getTraceId, span.getSpanContext.getSpanId)

    def parentKeyOf(span: SpanData): Option[SpanKey] = {
      val parent = span.getParentSpanContext
      Option.when(parent.isValid)(parent.getTraceId -> parent.getSpanId)
    }

    val spansById = spans.map(span => keyOf(span) -> span).toMap
    val childrenByParentId = spans.foldLeft(Map.empty[SpanKey, List[SpanData]]) { case (acc, span) =>
      parentKeyOf(span)
        .filter(spansById.contains)
        .fold(acc)(parentId => acc.updated(parentId, acc.getOrElse(parentId, Nil) :+ span))
    }

    def loop(span: SpanData): ActualTrace =
      ActualTrace(span, childrenByParentId.getOrElse(keyOf(span), Nil).map(loop))

    spans
      .filter(span => parentKeyOf(span).forall(parent => !spansById.contains(parent)))
      .map(loop)
  }
}
