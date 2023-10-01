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

package org.typelevel.otel4s.trace

import cats.{Hash, Show}

/** A decision on whether a span should be recorded or dropped.
  */
sealed abstract class SamplingDecision(val isSampled: Boolean)
    extends Product
    with Serializable

object SamplingDecision {

  /** Span is dropped. The resulting span will be completely no-op.
    */
  case object Drop extends SamplingDecision(false)

  /** Span is recorded only. The resulting span will record all information like
    * timings and attributes but will not be exported. Downstream parent-based
    * samplers will not sample the span.
    */
  case object RecordOnly extends SamplingDecision(false)

  /** Span is recorded and sampled. The resulting span will record all
    * information like timings and attributes and will be exported.
    */
  case object RecordAndSample extends SamplingDecision(true)

  implicit val samplingDecisionHash: Hash[SamplingDecision] =
    Hash.fromUniversalHashCode

  implicit val samplingDecisionShow: Show[SamplingDecision] =
    Show.fromToString

}
