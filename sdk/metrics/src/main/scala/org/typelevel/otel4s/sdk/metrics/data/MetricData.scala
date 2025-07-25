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

package org.typelevel.otel4s.sdk.metrics.data

import cats.Hash
import cats.Show
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope

/** Representation of the aggregated measurements of an instrument.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#timeseries-model]]
  */
sealed trait MetricData {

  /** The name of the metric.
    *
    * The name is typically the instrument name, but may be optionally overridden by a view.
    */
  def name: String

  /** The description of the metric.
    *
    * The metric name is typically the instrument description, but may be optionally overridden by a view.
    */
  def description: Option[String]

  /** The unit of the metric.
    */
  def unit: Option[String]

  /** The datapoints (measurements) of the metric.
    */
  def data: MetricPoints

  /** The instrumentation scope associated with the measurements.
    */
  def instrumentationScope: InstrumentationScope

  /** The resource associated with the measurements.
    */
  def resource: TelemetryResource

  override final def hashCode(): Int =
    Hash[MetricData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: MetricData => Hash[MetricData].eqv(this, other)
      case _                 => false
    }

  override final def toString: String =
    Show[MetricData].show(this)
}

object MetricData {

  /** Creates a new [[MetricData]] with the given values.
    */
  def apply(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: MetricPoints
  ): MetricData =
    Impl(
      resource = resource,
      instrumentationScope = scope,
      name = name,
      description = description,
      unit = unit,
      data = data
    )

  implicit val metricDataHash: Hash[MetricData] =
    Hash.by { data =>
      (
        data.name,
        data.description,
        data.unit,
        data.data,
        data.instrumentationScope,
        data.resource
      )
    }

  implicit val metricDataShow: Show[MetricData] =
    Show.show { data =>
      val description = data.description.foldMap(d => s"description=$d, ")
      val unit = data.unit.foldMap(d => s"unit=$d, ")
      "MetricData{" +
        s"name=${data.name}, " +
        description +
        unit +
        s"data=${data.data}, " +
        s"instrumentationScope=${data.instrumentationScope}, " +
        s"resource=${data.resource}}"
    }

  private final case class Impl(
      resource: TelemetryResource,
      instrumentationScope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: MetricPoints,
  ) extends MetricData

}
