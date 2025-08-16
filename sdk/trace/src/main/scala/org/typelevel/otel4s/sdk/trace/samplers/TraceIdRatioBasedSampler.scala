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

package org.typelevel.otel4s
package sdk
package trace
package samplers

import cats.Applicative
import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

/** The input `ratio` must be between 0 and 1.0.
  *
  * 0.1 means only 10% of the incoming spans will be sampled, and 1.0 stands for 100%.
  *
  * The ratio-based sampler must be deterministic, so it utilizes the Long value extracted from the trace id.
  *
  * The logic is the following: the sampling result will be [[SamplingResult.RecordAndSample]] if the extracted long
  * value is lower than the upper bound limit (`ratio` * Long.MaxValue).
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#traceidratiobased]]
  */
private final case class TraceIdRatioBasedSampler[F[_]: Applicative] private (
    ratio: Double,
    idUpperBound: Long
) extends Sampler.Unsealed[F] {

  def shouldSample(
      parentContext: Option[SpanContext],
      traceId: ByteVector,
      name: String,
      spanKind: SpanKind,
      attributes: Attributes,
      parentLinks: Vector[LinkData]
  ): F[SamplingResult] =
    Applicative[F].pure {
      if (math.abs(traceIdRandomPart(traceId)) < idUpperBound)
        SamplingResult.RecordAndSample
      else
        SamplingResult.Drop
    }

  private def traceIdRandomPart(traceId: ByteVector): Long =
    traceId.drop(8).toLong()

  // format as: 0.000000
  val description: String = f"TraceIdRatioBased{$ratio%.6f}".replace(",", ".")
}

private object TraceIdRatioBasedSampler {

  /** Creates a new [[TraceIdRatioBasedSampler]] Sampler.
    *
    * The ratio of sampling a trace is equal to that of the specified `ratio`.
    *
    * 0.1 means only 10% of the incoming spans will be sampled, and 1.0 stands for 100%.
    *
    * The ratio-based sampler must be deterministic, so it utilizes the Long value extracted from the trace id.
    *
    * The logic is the following: the sampling result will be [[SamplingResult.RecordAndSample]] if the extracted long
    * value is lower than the upper bound limit (`ratio` * Long.MaxValue).
    *
    * @param ratio
    *   the desired ratio of sampling. Must be >= 0 and <= 1.0.
    */
  def create[F[_]: Applicative](ratio: Double): Sampler[F] = {
    require(ratio >= 0 && ratio <= 1.0, "ratio must be >= 0 and <= 1.0")

    val idUpperBound =
      if (ratio == 0.0) Long.MinValue
      else if (ratio == 1.0) Long.MaxValue
      else (ratio * Long.MaxValue).toLong

    TraceIdRatioBasedSampler(ratio, idUpperBound)
  }

}
