package org.typelevel.otel4s.semconv

/** The metric specification.
  */
trait MetricSpec {

  /** The name of the metric.
    */
  def name: String

  /** The description of the metric.
    */
  def description: String

  /** The measurement unit of the metric.
    */
  def unit: String

  /** The stability of the metric.
    */
  def stability: Stability

  /** The stability of the attribute.
    */
  def attributeSpecs: List[AttributeSpec[_]]

}
