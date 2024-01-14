package org.typelevel.otel4s.sdk.metrics.data

import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope

sealed trait MetricData {

  def resource: Resource

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
      resource: Resource,
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
      resource: Resource,
      instrumentationScope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: Data,
  ) extends MetricData

}
