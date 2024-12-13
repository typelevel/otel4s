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

package org.typelevel.otel4s.sdk.metrics.exemplar

import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.Context

/** Exemplar filters are used to pre-filter measurements before attempting to store them in a reservoir.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#exemplarfilter]]
  */
sealed trait ExemplarFilter {

  /** Returns whether or not a reservoir should attempt to sample a measurement.
    */
  def shouldSample[A: MeasurementValue](
      value: A,
      attributes: Attributes,
      context: Context
  ): Boolean
}

object ExemplarFilter {
  private val AlwaysOn = Const(decision = true)
  private val AlwaysOff = Const(decision = false)

  /** A filter which makes all measurements eligible for being an exemplar.
    */
  def alwaysOn: ExemplarFilter = AlwaysOn

  /** A filter which makes no measurements eligible for being an exemplar.
    */
  def alwaysOff: ExemplarFilter = AlwaysOff

  /** A filter that only accepts measurements where there is a span in a context that is being sampled.
    */
  def traceBased(lookup: TraceContextLookup): ExemplarFilter =
    TraceBased(lookup)

  private final case class Const(decision: Boolean) extends ExemplarFilter {
    def shouldSample[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): Boolean =
      decision
  }

  private final case class TraceBased(
      lookup: TraceContextLookup
  ) extends ExemplarFilter {
    def shouldSample[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): Boolean =
      lookup.get(context).exists(_.isSampled)
  }
}
