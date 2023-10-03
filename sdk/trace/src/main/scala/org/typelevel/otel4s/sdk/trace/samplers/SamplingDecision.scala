/*
 * Copyright 2022 Typelevel
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

/** A decision on whether a span should be recorded, sampled, or dropped.
  */
sealed abstract class SamplingDecision(val isSampled: Boolean)
    extends Product
    with Serializable

object SamplingDecision {

  /** The span is not recorded, and all events and attributes will be dropped.
    */
  case object Drop extends SamplingDecision(false)

  /** The span is recorded, but the Sampled flag will not be set.
    */
  case object RecordOnly extends SamplingDecision(false)

  /** The span is recorded, and the Sampled flag will be set.
    */
  case object RecordAndSample extends SamplingDecision(true)

  implicit val samplingDecisionHash: Hash[SamplingDecision] =
    Hash.fromUniversalHashCode

  implicit val samplingDecisionShow: Show[SamplingDecision] =
    Show.fromToString
}
