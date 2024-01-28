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

import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope

sealed trait MetricData {

  def resource: TelemetryResource

  def instrumentationScope: InstrumentationScope

  def name: String

  def description: Option[String]

  def unit: Option[String]

  def data: Data

  final def tpe: MetricDataType = data.tpe
  final def isEmpty: Boolean = data.points.isEmpty
  final def nonEmpty: Boolean = !isEmpty
}

object MetricData {

  def apply(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: Data
  ): MetricData =
    MetricDataImpl(
      resource = resource,
      instrumentationScope = scope,
      name = name,
      description = description,
      unit = unit,
      data = data
    )

  private final case class MetricDataImpl(
      resource: TelemetryResource,
      instrumentationScope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: Data,
  ) extends MetricData

}
