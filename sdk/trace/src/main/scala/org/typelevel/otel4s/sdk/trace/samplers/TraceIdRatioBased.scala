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

package org.typelevel.otel4s.sdk
package trace
package samplers

import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

final class TraceIdRatioBased private (
    ratio: Double,
    idUpperBound: Long
) extends Sampler {

  def shouldSample(
      parentContext: Option[SpanContext],
      traceId: String,
      name: String,
      kind: SpanKind,
      attributes: Attributes,
      parentLinks: List[LinkData]
  ): SamplingResult = {
    if (math.abs(SpanContext.TraceId.randomPart(traceId)) < idUpperBound)
      SamplingResult.RecordAndSample
    else
      SamplingResult.Drop
  }

  // format as: 0.000000
  val description: String = f"TraceIdRatioBased{$ratio%.6f}".replace(",", ".")
}

private[samplers] object TraceIdRatioBased {

  /** Creates a new [[TraceIdRatioBased]] Sampler.
    *
    * The ratio of sampling a trace is equal to that of the specified ratio.
    *
    * The algorithm used by the Sampler is undefined, notably it may or may not
    * use parts of the trace ID when generating a sampling decision.
    *
    * @param ratio
    *   the desired ratio of sampling. Must be >= 0 and <= 1.0.
    */
  def create(ratio: Double): Sampler = {
    require(ratio >= 0 && ratio <= 1.0, "ratio must be >= 0 and <= 1.0")

    val idUpperBound =
      if (ratio == 0.0) Long.MinValue
      else if (ratio == 1.0) Long.MaxValue
      else (ratio * Long.MaxValue).toLong

    new TraceIdRatioBased(ratio, idUpperBound)
  }

}
