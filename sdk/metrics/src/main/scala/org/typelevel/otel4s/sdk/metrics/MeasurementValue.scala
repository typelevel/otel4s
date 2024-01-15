package org.typelevel.otel4s.sdk.metrics

sealed trait MeasurementValue[A] {

}

object MeasurementValue {

  def apply[A](implicit ev: MeasurementValue[A]): MeasurementValue[A] = ev

  implicit val longMeasurementValue: MeasurementValue[Long] = ???
  implicit val doubleMeasurementValue: MeasurementValue[Double] = ???

}
