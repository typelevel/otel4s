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

package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.sdk.metrics.view.View

/** Describes a metric that will be output.
  */
private[metrics] sealed trait MetricDescriptor {

  /** The name of the descriptor.
    *
    * Either the [[View.name]] or [[InstrumentDescriptor.name]].
    */
  def name: String

  /** The description of the descriptor.
    *
    * Either the [[View.description]] or [[InstrumentDescriptor.description]].
    */
  def description: Option[String]

  /** The instrument used by this metric.
    */
  def sourceInstrument: InstrumentDescriptor

}

private[metrics] object MetricDescriptor {

  /** Creates a [[MetricDescriptor]] using the given `view` and
    * `instrumentDescriptor`.
    *
    * The `name` and `description` from the `view` are prioritized.
    *
    * @param view
    *   the view associated with the instrument
    *
    * @param instrumentDescriptor
    *   the descriptor of the instrument
    */
  def apply(
      view: Option[View],
      instrumentDescriptor: InstrumentDescriptor
  ): MetricDescriptor =
    Impl(
      view.flatMap(_.name).getOrElse(instrumentDescriptor.name.toString),
      view.flatMap(_.description).orElse(instrumentDescriptor.description),
      instrumentDescriptor
    )

  private final case class Impl(
      name: String,
      description: Option[String],
      sourceInstrument: InstrumentDescriptor
  ) extends MetricDescriptor

}
