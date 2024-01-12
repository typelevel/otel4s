package org.typelevel.otel4s.sdk.metrics.data

import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope

sealed trait MetricData {

  def resource: Resource

  def instrumentationScope: InstrumentationScope

  def name: String

  def description: Option[String]

  def unit: Option[String]

  def tpe: MetricDataType

  def data: Data

  final def isEmpty: Boolean = data.points.isEmpty
  final def nonEmpty: Boolean = !isEmpty
}

object MetricData {

  def longSum(
                 resource: Resource,
                 scope: InstrumentationScope,
                 name: String,
                 description: Option[String],
                 unit: Option[String],
                 data: Data.LongSum
               ): MetricData =
    MetricDataImpl(
      resource = resource,
      instrumentationScope = scope,
      name = name,
      description = description,
      unit = unit,
      tpe = MetricDataType.LongSum,
      data = data
    )

  def doubleSum(
      resource: Resource,
      scope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: Data.DoubleSum
  ): MetricData =
    MetricDataImpl(
      resource = resource,
      instrumentationScope = scope,
      name = name,
      description = description,
      unit = unit,
      tpe = MetricDataType.DoubleSum,
      data = data
    )

  def doubleGauge(
      resource: Resource,
      scope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      data: Data.DoubleGauge
  ): MetricData =
    MetricDataImpl(
      resource = resource,
      instrumentationScope = scope,
      name = name,
      description = description,
      unit = unit,
      tpe = MetricDataType.DoubleGauge,
      data = data
    )

  private final case class MetricDataImpl(
      resource: Resource,
      instrumentationScope: InstrumentationScope,
      name: String,
      description: Option[String],
      unit: Option[String],
      tpe: MetricDataType,
      data: Data,
  ) extends MetricData

}
