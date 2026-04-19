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
import org.typelevel.otel4s.oteljava.testkit.ExpectationChecks
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.TraceFlags
import org.typelevel.otel4s.trace.TraceState
import scodec.bits.ByteVector

/** A partial expectation for [[org.typelevel.otel4s.trace.SpanContext]].
  *
  * Unspecified properties are ignored.
  */
sealed trait SpanContextExpectation {

  /** An optional human-readable clue shown in mismatch messages. */
  def clue: Option[String]

  /** Requires the trace ID to match exactly. */
  def traceId(traceId: ByteVector): SpanContextExpectation

  /** Requires the trace ID to match the given hex value. */
  def traceIdHex(traceId: String): SpanContextExpectation

  /** Requires the span ID to match exactly. */
  def spanId(spanId: ByteVector): SpanContextExpectation

  /** Requires the span ID to match the given hex value. */
  def spanIdHex(spanId: String): SpanContextExpectation

  /** Requires the sampled flag to match exactly. */
  def sampled(sampled: Boolean): SpanContextExpectation

  /** Requires the remote flag to match exactly. */
  def remote(remote: Boolean): SpanContextExpectation

  /** Requires the trace flags to match exactly. */
  def traceFlags(traceFlags: TraceFlags): SpanContextExpectation

  /** Requires the trace state to match exactly. */
  def traceState(traceState: TraceState): SpanContextExpectation

  /** Attaches a human-readable clue to this expectation. */
  def clue(text: String): SpanContextExpectation

  /** Adds a custom predicate over the span context. */
  def where(f: SpanContext => Boolean): SpanContextExpectation

  /** Adds a custom predicate over the span context with a clue shown in mismatches. */
  def where(clue: String)(f: SpanContext => Boolean): SpanContextExpectation

  /** Checks the given span context and returns structured failures when the expectation does not match. */
  def check(spanContext: SpanContext): Either[NonEmptyList[SpanContextExpectation.Mismatch], Unit]

  /** Returns `true` if this expectation matches the given span context. */
  final def matches(spanContext: SpanContext): Boolean =
    check(spanContext).isRight
}

object SpanContextExpectation {

  /** A structured reason explaining why a [[SpanContextExpectation]] did not match an actual span context. */
  sealed trait Mismatch extends Product with Serializable {
    def message: String
  }

  object Mismatch {

    private[trace] final case class TraceIdMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"trace ID mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class SpanIdMismatch(expected: String, actual: String) extends Mismatch {
      def message: String =
        s"span ID mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class SampledMismatch(expected: Boolean, actual: Boolean) extends Mismatch {
      def message: String =
        s"sampled mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class RemoteMismatch(expected: Boolean, actual: Boolean) extends Mismatch {
      def message: String =
        s"remote mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class TraceFlagsMismatch(expected: TraceFlags, actual: TraceFlags) extends Mismatch {
      def message: String =
        s"trace flags mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class TraceStateMismatch(expected: TraceState, actual: TraceState) extends Mismatch {
      def message: String =
        s"trace state mismatch: expected '$expected', got '$actual'"
    }

    private[trace] final case class PredicateMismatch(clue: Option[String]) extends Mismatch {
      def message: String =
        s"span context predicate returned false${clue.fold("")(v => s": $v")}"
    }
  }

  /** Creates an expectation that matches the full span context exactly. */
  def exact(spanContext: SpanContext): SpanContextExpectation =
    Impl(
      expectedTraceId = Some(spanContext.traceIdHex),
      expectedSpanId = Some(spanContext.spanIdHex),
      expectedSampled = Some(spanContext.isSampled),
      expectedRemote = Some(spanContext.isRemote),
      expectedTraceFlags = Some(spanContext.traceFlags),
      expectedTraceState = Some(spanContext.traceState)
    )

  /** Creates an expectation that matches any span context. */
  def any: SpanContextExpectation =
    Impl()

  private final case class Impl(
      expectedTraceId: Option[String] = None,
      expectedSpanId: Option[String] = None,
      expectedSampled: Option[Boolean] = None,
      expectedRemote: Option[Boolean] = None,
      expectedTraceFlags: Option[TraceFlags] = None,
      expectedTraceState: Option[TraceState] = None,
      clue: Option[String] = None,
      predicates: List[(SpanContext => Boolean, Option[String])] = Nil
  ) extends SpanContextExpectation {

    def traceId(traceId: ByteVector): SpanContextExpectation =
      traceIdHex(traceId.toHex)

    def traceIdHex(traceId: String): SpanContextExpectation =
      copy(expectedTraceId = Some(traceId))

    def spanId(spanId: ByteVector): SpanContextExpectation =
      spanIdHex(spanId.toHex)

    def spanIdHex(spanId: String): SpanContextExpectation =
      copy(expectedSpanId = Some(spanId))

    def sampled(sampled: Boolean): SpanContextExpectation =
      copy(expectedSampled = Some(sampled))

    def remote(remote: Boolean): SpanContextExpectation =
      copy(expectedRemote = Some(remote))

    def traceFlags(traceFlags: TraceFlags): SpanContextExpectation =
      copy(expectedTraceFlags = Some(traceFlags))

    def traceState(traceState: TraceState): SpanContextExpectation =
      copy(expectedTraceState = Some(traceState))

    def clue(text: String): SpanContextExpectation =
      copy(clue = Some(text))

    def where(f: SpanContext => Boolean): SpanContextExpectation =
      copy(predicates = predicates :+ (f -> None))

    def where(clue: String)(f: SpanContext => Boolean): SpanContextExpectation =
      copy(predicates = predicates :+ (f -> Some(clue)))

    def check(spanContext: SpanContext): Either[NonEmptyList[Mismatch], Unit] =
      ExpectationChecks.combine(
        expectedTraceId.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.traceIdHex) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TraceIdMismatch(expected, spanContext.traceIdHex))
        },
        expectedSpanId.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.spanIdHex) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.SpanIdMismatch(expected, spanContext.spanIdHex))
        },
        expectedSampled.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.isSampled) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.SampledMismatch(expected, spanContext.isSampled))
        },
        expectedRemote.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.isRemote) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.RemoteMismatch(expected, spanContext.isRemote))
        },
        expectedTraceFlags.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.traceFlags) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TraceFlagsMismatch(expected, spanContext.traceFlags))
        },
        expectedTraceState.fold(ExpectationChecks.success[Mismatch]) { expected =>
          if (expected == spanContext.traceState) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.TraceStateMismatch(expected, spanContext.traceState))
        },
        ExpectationChecks.combine(predicates.map { case (predicate, clue) =>
          if (predicate(spanContext)) ExpectationChecks.success
          else ExpectationChecks.mismatch(Mismatch.PredicateMismatch(clue))
        })
      )
  }
}
