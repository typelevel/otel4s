/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace.samplers

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.sdk.Attributes
import org.typelevel.otel4s.trace.TraceState

/** Sampling result returned by [[Sampler.shouldSample]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#shouldsample]]
  */
sealed trait SamplingResult {

  /** The decision on whether a span should be recorded, recorded and sampled or
    * not recorded.
    */
  def decision: SamplingDecision

  /** The attributes that will be attached to the span.
    */
  def attributes: Attributes

  /** A modifier of the parent's TraceState.
    *
    * It may return the same trace state that was provided originally, or an
    * updated one.
    *
    * @note
    *   if updated returns an empty trace state, the trace state will be
    *   cleared, so samplers should normally use
    *   [[SamplingResult.TraceStateUpdater.Identity]] to return the passed-in
    *   trace state if it's not intended to be changed.
    */
  def traceStateUpdater: SamplingResult.TraceStateUpdater

  override final def hashCode(): Int =
    Hash[SamplingResult].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: SamplingResult => Hash[SamplingResult].eqv(this, other)
      case _                     => false
    }

  override final def toString: String =
    Show[SamplingResult].show(this)
}

object SamplingResult {

  /** A modifier of the parent's TraceState.
    *
    * It may return the same trace state that was provided originally, or an
    * updated one.
    *
    * @note
    *   if an empty trace state is returned, the trace state will be cleared, so
    *   the updater should normally return the passed-in trace state (via
    *   [[TraceStateUpdater.Identity]]) if it's intended to be changed.
    */
  sealed trait TraceStateUpdater {
    def update(state: TraceState): TraceState
  }

  object TraceStateUpdater {

    /** Always returns the given trace state without modifications. */
    case object Identity extends TraceStateUpdater {
      def update(state: TraceState): TraceState = state
    }

    /** Returns the given trace state modified by the `modify` function. */
    final case class Modifier(modify: TraceState => TraceState)
        extends TraceStateUpdater {
      def update(state: TraceState): TraceState =
        modify(state)
    }

    /** Always returns the `const` state. No matter the input. */
    final case class Const(const: TraceState) extends TraceStateUpdater {
      def update(state: TraceState): TraceState = const
    }

    implicit val traceStateUpdaterHash: Hash[TraceStateUpdater] =
      Hash.fromUniversalHashCode

    implicit val traceStateUpdaterShow: Show[TraceStateUpdater] =
      Show.show {
        case Identity     => "Identity"
        case Modifier(_)  => "Modifier(f)"
        case Const(const) => show"Const($const)"
      }
  }

  /** The [[SamplingResult]] with the [[SamplingDecision.RecordAndSample]]
    * decision, no attributes, and [[TraceStateUpdater.Identity]] updater.
    */
  val RecordAndSample: SamplingResult =
    fromDecision(SamplingDecision.RecordAndSample)

  /** The [[SamplingResult]] with the [[SamplingDecision.RecordOnly]] decision,
    * no attributes, and [[TraceStateUpdater.Identity]] updater.
    */
  val RecordOnly: SamplingResult =
    fromDecision(SamplingDecision.RecordOnly)

  /** The [[SamplingResult]] with the [[SamplingDecision.Drop]] decision, no
    * attributes, and [[TraceStateUpdater.Identity]] updater.
    */
  val Drop: SamplingResult =
    fromDecision(SamplingDecision.Drop)

  /** Creates a [[SamplingResult]] with the given `decision`.
    *
    * @param decision
    *   the [[SamplingDecision]] to associate with the result
    */
  def apply(decision: SamplingDecision): SamplingResult =
    decision match {
      case SamplingDecision.RecordAndSample => RecordAndSample
      case SamplingDecision.RecordOnly      => RecordOnly
      case SamplingDecision.Drop            => Drop
    }

  /** Creates a [[SamplingResult]] with the given `decision` and `attributes`.
    *
    * @param decision
    *   the [[SamplingDecision]] to associate with the result
    *
    * @param attributes
    *   the [[Attributes]] to associate with the result
    */
  def apply(
      decision: SamplingDecision,
      attributes: Attributes
  ): SamplingResult =
    if (attributes.isEmpty) apply(decision)
    else Impl(decision, attributes, TraceStateUpdater.Identity)

  /** Creates a [[SamplingResult]] with the given `decision`, `attributes`, and
    * `traceStateUpdater`.
    *
    * @param decision
    *   the [[SamplingDecision]] to associate with the result
    *
    * @param attributes
    *   the [[Attributes]] to associate with the result
    *
    * @param traceStateUpdater
    *   the [[TraceStateUpdater]] to associate with the result
    */
  def apply(
      decision: SamplingDecision,
      attributes: Attributes,
      traceStateUpdater: TraceStateUpdater
  ): SamplingResult =
    if (traceStateUpdater == TraceStateUpdater.Identity)
      apply(decision, attributes)
    else
      Impl(decision, attributes, traceStateUpdater)

  implicit val samplingResultHash: Hash[SamplingResult] =
    Hash.by(r => (r.decision, r.attributes, r.traceStateUpdater))

  implicit val samplingResultShow: Show[SamplingResult] =
    Show.show { r =>
      show"SamplingResult{decision=${r.decision}, attributes=${r.attributes}, traceStateUpdater=${r.traceStateUpdater}}"
    }

  private def fromDecision(decision: SamplingDecision): SamplingResult =
    Impl(decision, Attributes.empty, TraceStateUpdater.Identity)

  private final case class Impl(
      decision: SamplingDecision,
      attributes: Attributes,
      traceStateUpdater: TraceStateUpdater
  ) extends SamplingResult

}
