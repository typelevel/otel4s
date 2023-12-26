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

import org.typelevel.otel4s.sdk.trace.data.LinkData
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind
import scodec.bits.ByteVector

/** A Sampler is used to make decisions on Span sampling.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#sampler]]
  */
trait Sampler {

  /** Called during span creation to make a sampling result.
    *
    * @param parentContext
    *   the parent's span context. `None` means there is no parent
    *
    * @param traceId
    *   the trace id of the new span
    *
    * @param name
    *   the name of the new span
    *
    * @param spanKind
    *   the [[org.typelevel.otel4s.trace.SpanKind SpanKind]] of the new span
    *
    * @param attributes
    *   the [[Attributes]] associated with the new span
    *
    * @param parentLinks
    *   the list of parent links associated with the span
    */
  def shouldSample(
      parentContext: Option[SpanContext],
      traceId: ByteVector,
      name: String,
      spanKind: SpanKind,
      attributes: Attributes,
      parentLinks: List[LinkData]
  ): SamplingResult

  /** The description of the [[Sampler]]. This may be displayed on debug pages
    * or in the logs.
    */
  def description: String

  override final def toString: String = description
}

object Sampler {

  /** Always returns the [[SamplingResult.RecordAndSample]].
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwayson]]
    */
  val AlwaysOn: Sampler =
    new Const(SamplingResult.RecordAndSample, "AlwaysOnSampler")

  /** Always returns the [[SamplingResult.Drop]].
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#alwaysoff]]
    */
  val AlwaysOff: Sampler =
    new Const(SamplingResult.Drop, "AlwaysOffSampler")

  /** Returns a [[Sampler]] that always makes the same decision as the parent
    * Span to whether or not to sample.
    *
    * If there is no parent, the sampler uses the provided root [[Sampler]] to
    * determine the sampling decision.
    *
    * @param root
    *   the [[Sampler]] which is used to make the sampling decisions if the
    *   parent does not exist
    */
  def parentBased(root: Sampler): Sampler =
    parentBasedBuilder(root).build

  /** Creates a [[ParentBasedSampler.Builder]] for parent-based sampler that
    * enables configuration of the parent-based sampling strategy.
    *
    * The parent's sampling decision is used if a parent span exists, otherwise
    * this strategy uses the root sampler's decision.
    *
    * There are a several options available on the builder to control the
    * precise behavior of how the decision will be made.
    *
    * @param root
    *   the [[Sampler]] which is used to make the sampling decisions if the
    *   parent does not exist
    */
  def parentBasedBuilder(root: Sampler): ParentBasedSampler.Builder =
    ParentBasedSampler.builder(root)

  /** Creates a new ratio-based sampler.
    *
    * The ratio of sampling a trace is equal to that of the specified ratio.
    *
    * The algorithm used by the Sampler is undefined, notably it may or may not
    * use parts of the trace ID when generating a sampling decision.
    *
    * Currently, only the ratio of traces that are sampled can be relied on, not
    * how the sampled traces are determined. As such, it is recommended to only
    * use this [[Sampler]] for root spans using [[parentBased]].
    *
    * @param ratio
    *   the desired ratio of sampling. Must be >= 0 and <= 1.0.
    */
  def traceIdRatioBased(ratio: Double): Sampler =
    TraceIdRatioBasedSampler.create(ratio)

  private final class Const(
      result: SamplingResult,
      val description: String
  ) extends Sampler {
    def shouldSample(
        parentContext: Option[SpanContext],
        traceId: ByteVector,
        name: String,
        spanKind: SpanKind,
        attributes: Attributes,
        parentLinks: List[LinkData]
    ): SamplingResult =
      result
  }

}
