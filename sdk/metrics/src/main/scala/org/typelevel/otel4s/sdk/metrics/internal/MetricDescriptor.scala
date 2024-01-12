package org.typelevel.otel4s.sdk.metrics.internal

import org.typelevel.otel4s.sdk.metrics.View

trait MetricDescriptor {

  def name: String
  def description: Option[String]
  def view: View
  def sourceInstrument: InstrumentDescriptor

}

object MetricDescriptor {
  def apply(
      view: View,
      instrumentDescriptor: InstrumentDescriptor
  ): MetricDescriptor =
    Impl(
      view.name.getOrElse(instrumentDescriptor.name),
      view.description.orElse(instrumentDescriptor.description),
      view,
      instrumentDescriptor
    )

  private final case class Impl(
      name: String,
      description: Option[String],
      view: View,
      sourceInstrument: InstrumentDescriptor
  ) extends MetricDescriptor

}
