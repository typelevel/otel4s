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

/** Sampling result returned by [[Sampler.shouldSample]].
  */
sealed trait SamplingResult {

  /** The decision on whether a span should be recorded, recorded and sampled or
    * not recorded.
    */
  def decision: SamplingDecision

  /** The attributes that will be attached to the span
    */
  def attributes: Attributes

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

  val RecordAndSample: SamplingResult =
    SamplingResultImpl(SamplingDecision.RecordAndSample, Attributes.Empty)

  val RecordOnly: SamplingResult =
    SamplingResultImpl(SamplingDecision.RecordOnly, Attributes.Empty)

  val Drop: SamplingResult =
    SamplingResultImpl(SamplingDecision.Drop, Attributes.Empty)

  def create(decision: SamplingDecision): SamplingResult =
    decision match {
      case SamplingDecision.RecordAndSample => RecordAndSample
      case SamplingDecision.RecordOnly      => RecordOnly
      case SamplingDecision.Drop            => Drop
    }

  def create(
      decision: SamplingDecision,
      attributes: Attributes
  ): SamplingResult =
    if (attributes.isEmpty) create(decision)
    else SamplingResultImpl(decision, attributes)

  implicit val samplingResultHash: Hash[SamplingResult] =
    Hash.by(r => (r.decision, r.attributes))

  implicit val samplingResultShow: Show[SamplingResult] =
    Show.show { r =>
      show"SamplingResult{decision=${r.decision}, attributes=${r.attributes}}"
    }

  private final case class SamplingResultImpl(
      decision: SamplingDecision,
      attributes: Attributes
  ) extends SamplingResult

}
