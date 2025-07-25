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
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.BucketBoundaries

/** A point in the metric data model.
  *
  * A point represents the aggregation of measurements recorded with a particular set of [[Attributes]] over some time
  * interval.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#metric-points]]
  */
sealed trait PointData {

  /** A [[TimeWindow]] for which the point data was calculated.
    */
  def timeWindow: TimeWindow

  /** An [[Attributes]] associated with the point data.
    */
  def attributes: Attributes

  override final def hashCode(): Int =
    Hash[PointData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: PointData => Hash[PointData].eqv(this, other)
      case _                => false
    }

  override final def toString: String =
    Show[PointData].show(this)
}

object PointData {

  /** The number point represents a single value.
    *
    * Can hold either Long or Double values.
    *
    * Used by Sum and Gauge metrics.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#gauge]]
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#sums]]
    */
  sealed trait NumberPoint extends PointData {
    type Exemplar <: ExemplarData
    type Value

    /** The [[ExemplarData]] associated with the point data.
      */
    def exemplars: Vector[Exemplar]

    /** The measurement value.
      */
    def value: Value
  }

  sealed trait LongNumber extends NumberPoint {
    type Exemplar = ExemplarData.LongExemplar
    type Value = Long
  }

  sealed trait DoubleNumber extends NumberPoint {
    type Exemplar = ExemplarData.DoubleExemplar
    type Value = Double
  }

  /** A population of recorded measurements. A histogram bundles a set of events into divided populations with an
    * overall event count and aggregate sum for all events.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#histogram]]
    */
  sealed trait Histogram extends PointData {

    /** The [[ExemplarData]] associated with the histogram data.
      */
    def exemplars: Vector[ExemplarData.DoubleExemplar]

    /** The [[Histogram.Stats]] of the current measurement. `None` means the histogram is empty.
      */
    def stats: Option[Histogram.Stats]

    /** The boundaries of this histogram.
      */
    def boundaries: BucketBoundaries

    /** The numbers of observations that fell within each bucket.
      */
    def counts: Vector[Long]
  }

  object Histogram {

    /** The aggregated stats of the histogram */
    sealed trait Stats {

      /** A sum of all values in the histogram. */
      def sum: Double

      /** The min of all values in the histogram. */
      def min: Double

      /** The max of all values in the histogram. */
      def max: Double

      /** The total population of points in the histogram. */
      def count: Long

      override final def hashCode(): Int =
        Hash[Stats].hash(this)

      override final def equals(obj: Any): Boolean =
        obj match {
          case other: Stats => Hash[Stats].eqv(this, other)
          case _            => false
        }

      override final def toString: String =
        Show[Stats].show(this)
    }

    object Stats {

      /** Creates [[Stats]] with the given values.
        */
      def apply(sum: Double, min: Double, max: Double, count: Long): Stats =
        Impl(sum, min, max, count)

      implicit val statsHash: Hash[Stats] =
        Hash.by(s => (s.sum, s.min, s.max, s.count))

      implicit val statsShow: Show[Stats] =
        Show.show { s =>
          s"Stats{sum=${s.sum}, min=${s.min}, max=${s.max}, count=${s.count}}"
        }

      private final case class Impl(
          sum: Double,
          min: Double,
          max: Double,
          count: Long
      ) extends Stats

    }

  }

  /** Creates a [[LongNumber]] with the given values.
    */
  def longNumber(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.LongExemplar],
      value: Long
  ): LongNumber =
    LongNumberImpl(timeWindow, attributes, exemplars, value)

  /** Creates a [[DoubleNumber]] with the given values.
    */
  def doubleNumber(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      value: Double
  ): DoubleNumber =
    DoubleNumberImpl(timeWindow, attributes, exemplars, value)

  /** Creates a [[Histogram]] with the given values.
    */
  def histogram(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      stats: Option[Histogram.Stats],
      boundaries: BucketBoundaries,
      counts: Vector[Long]
  ): Histogram =
    HistogramImpl(timeWindow, attributes, exemplars, stats, boundaries, counts)

  implicit val pointDataHash: Hash[PointData] = {
    val numberHash: Hash[NumberPoint] = {
      // a value can be either Long or Double. The universal hashcode is safe
      implicit val valueHash: Hash[NumberPoint#Value] =
        Hash.fromUniversalHashCode

      Hash.by { d =>
        (
          d.timeWindow,
          d.attributes,
          d.exemplars: Vector[ExemplarData],
          d.value: NumberPoint#Value
        )
      }
    }

    val histogramHash: Hash[Histogram] =
      Hash.by { h =>
        (
          h.timeWindow,
          h.attributes,
          h.exemplars: Vector[ExemplarData],
          h.stats,
          h.boundaries,
          h.counts
        )
      }

    new Hash[PointData] {
      def hash(x: PointData): Int =
        x match {
          case point: NumberPoint   => numberHash.hash(point)
          case histogram: Histogram => histogramHash.hash(histogram)
        }

      def eqv(x: PointData, y: PointData): Boolean =
        (x, y) match {
          case (left: NumberPoint, right: NumberPoint) =>
            numberHash.eqv(left, right)
          case (left: Histogram, right: Histogram) =>
            histogramHash.eqv(left, right)
          case _ =>
            false
        }
    }
  }

  implicit val pointDataShow: Show[PointData] =
    Show.show {
      case data: NumberPoint =>
        val prefix = data match {
          case _: LongNumber   => "LongNumber"
          case _: DoubleNumber => "DoubleNumber"
        }

        s"PointData.$prefix{" +
          s"timeWindow=${data.timeWindow}, " +
          s"attributes=${data.attributes}, " +
          s"exemplars=${data.exemplars}, " +
          s"value=${data.value}}"

      case data: Histogram =>
        "PointData.Histogram{" +
          s"timeWindow=${data.timeWindow}, " +
          s"attributes=${data.attributes}, " +
          s"exemplars=${data.exemplars}, " +
          s"stats=${data.stats}, " +
          s"boundaries=${data.boundaries}, " +
          s"counts=${data.counts}}"
    }

  private final case class LongNumberImpl(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.LongExemplar],
      value: Long
  ) extends LongNumber

  private final case class DoubleNumberImpl(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      value: Double
  ) extends DoubleNumber

  private final case class HistogramImpl(
      timeWindow: TimeWindow,
      attributes: Attributes,
      exemplars: Vector[ExemplarData.DoubleExemplar],
      stats: Option[Histogram.Stats],
      boundaries: BucketBoundaries,
      counts: Vector[Long]
  ) extends Histogram

}
