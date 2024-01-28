package org.typelevel.otel4s.sdk.metrics

import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

sealed abstract class Aggregation(
    supportedInstruments: Set[InstrumentType]
) {
  def compatibleWith(tpe: InstrumentType): Boolean =
    supportedInstruments.contains(tpe)
}

object Aggregation {

  def drop: Aggregation = Drop

  def default: Aggregation = Default

  def sum: Aggregation = Sum

  def lastValue: Aggregation = LastValue

  private[metrics] sealed trait HasAggregator

  private[metrics] case object Drop extends Aggregation(Compatability.Default)

  private[metrics] case object Default
      extends Aggregation(Compatability.Default)
      with HasAggregator

  private[metrics] case object Sum
      extends Aggregation(Compatability.Sum)
      with HasAggregator

  private[metrics] case object LastValue
      extends Aggregation(Compatability.LastValue)
      with HasAggregator

  private[metrics] final case class ExplicitBucketHistogram(
      boundaries: BucketBoundaries
  ) extends Aggregation(Compatability.ExplicitBucketHistogram)
      with HasAggregator

  private[metrics] final case class Base2ExponentialHistogram(
      maxBuckets: Int,
      maxScale: Int
  ) extends Aggregation(Compatability.Base2ExponentialHistogram)
      with HasAggregator

  private object Compatability {
    val Drop: Set[InstrumentType] =
      InstrumentType.values

    val Default: Set[InstrumentType] =
      InstrumentType.values

    val Sum: Set[InstrumentType] = Set(
      InstrumentType.Counter,
      InstrumentType.UpDownCounter,
      InstrumentType.ObservableGauge,
      InstrumentType.ObservableUpDownCounter,
      InstrumentType.Histogram
    )

    val LastValue: Set[InstrumentType] =
      Set(InstrumentType.ObservableGauge)

    val ExplicitBucketHistogram: Set[InstrumentType] =
      Set(InstrumentType.Counter, InstrumentType.Histogram)

    val Base2ExponentialHistogram: Set[InstrumentType] =
      Set(InstrumentType.Counter, InstrumentType.Histogram)
  }

}
