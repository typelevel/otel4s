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

import org.typelevel.otel4s.sdk.Attributes
import org.typelevel.otel4s.trace.SamplingDecision

sealed trait SamplingResult {
  def decision: SamplingDecision
  def attributes: Attributes

  /*
   default TraceState getUpdatedTraceState(TraceState parentTraceState) {
     return parentTraceState;
   }
   */
}

object SamplingResult {

  private val RecordAndSample =
    SamplingResultImpl(SamplingDecision.RecordAndSample, Attributes.Empty)

  private val RecordOnly =
    SamplingResultImpl(SamplingDecision.RecordOnly, Attributes.Empty)

  private val Drop =
    SamplingResultImpl(SamplingDecision.Drop, Attributes.Empty)

  private final case class SamplingResultImpl(
      decision: SamplingDecision,
      attributes: Attributes
  ) extends SamplingResult

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

  def recordAndSample: SamplingResult = RecordAndSample
  def recordOnly: SamplingResult = RecordOnly
  def drop: SamplingResult = Drop

}
