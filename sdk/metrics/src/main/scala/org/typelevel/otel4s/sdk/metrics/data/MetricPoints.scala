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
import cats.data.NonEmptyVector
import cats.syntax.foldable._

/** A collection of metric data points.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#metric-points]]
  */
sealed trait MetricPoints {

  /** The collection of the metric [[PointData]]s.
    */
  def points: NonEmptyVector[PointData]

  override final lazy val hashCode: Int =
    Hash[MetricPoints].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: MetricPoints => Hash[MetricPoints].eqv(this, other)
      case _                   => false
    }

  override final def toString: String =
    Show[MetricPoints].show(this)
}

object MetricPoints {

  /** Sum represents the type of a numeric double scalar metric that is calculated as a sum of all reported measurements
    * over a time interval.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#sums]]
    */
  sealed trait Sum extends MetricPoints {
    type Point <: PointData.NumberPoint

    def points: NonEmptyVector[Point]

    /** Whether the points are monotonic. If true, it means the data points are nominally increasing.
      */
    def monotonic: Boolean

    /** The aggregation temporality of this aggregation.
      */
    def aggregationTemporality: AggregationTemporality
  }

  /** Gauge represents a sampled value at a given type.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#gauge]]
    */
  sealed trait Gauge extends MetricPoints {
    type Point <: PointData.NumberPoint

    def points: NonEmptyVector[Point]
  }

  /** Histogram represents the type of a metric that is calculated by aggregating as a histogram of all reported double
    * measurements over a time interval.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/metrics/data-model/#histogram]]
    */
  sealed trait Histogram extends MetricPoints {
    def points: NonEmptyVector[PointData.Histogram]

    /** The aggregation temporality of this aggregation.
      */
    def aggregationTemporality: AggregationTemporality
  }

  /** Creates a [[Sum]] with the given values.
    */
  def sum[A <: PointData.NumberPoint](
      points: NonEmptyVector[A],
      monotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ): Sum =
    SumImpl(points, monotonic, aggregationTemporality)

  /** Creates a [[Gauge]] with the given values.
    */
  def gauge[A <: PointData.NumberPoint](
      points: NonEmptyVector[A]
  ): Gauge =
    GaugeImpl(points)

  /** Creates a [[Histogram]] with the given values.
    */
  def histogram(
      points: NonEmptyVector[PointData.Histogram],
      aggregationTemporality: AggregationTemporality
  ): Histogram =
    HistogramImpl(points, aggregationTemporality)

  implicit val metricPointsHash: Hash[MetricPoints] = {
    val sumHash: Hash[Sum] =
      Hash.by { s =>
        (
          s.points.toVector: Vector[PointData],
          s.monotonic,
          s.aggregationTemporality
        )
      }

    val gaugeHash: Hash[Gauge] =
      Hash.by(_.points.toVector: Vector[PointData])

    val histogramHash: Hash[Histogram] =
      Hash.by(h => (h.points.toVector: Vector[PointData], h.aggregationTemporality))

    new Hash[MetricPoints] {
      def hash(x: MetricPoints): Int =
        x match {
          case sum: Sum             => sumHash.hash(sum)
          case gauge: Gauge         => gaugeHash.hash(gauge)
          case histogram: Histogram => histogramHash.hash(histogram)
        }

      def eqv(x: MetricPoints, y: MetricPoints): Boolean =
        (x, y) match {
          case (left: Sum, right: Sum) =>
            sumHash.eqv(left, right)
          case (left: Gauge, right: Gauge) =>
            gaugeHash.eqv(left, right)
          case (left: Histogram, right: Histogram) =>
            histogramHash.eqv(left, right)
          case _ =>
            false
        }
    }
  }

  implicit val metricPointsShow: Show[MetricPoints] = {
    Show.show {
      case sum: Sum =>
        "MetricPoints.Sum{" +
          s"points=${(sum.points: NonEmptyVector[PointData]).mkString_("{", ",", "}")}, " +
          s"monotonic=${sum.monotonic}, " +
          s"aggregationTemporality=${sum.aggregationTemporality}}"

      case gauge: Gauge =>
        "MetricPoints.Gauge{" +
          s"points=${(gauge.points: NonEmptyVector[PointData]).mkString_("{", ",", "}")}}"

      case h: Histogram =>
        "MetricPoints.Histogram{" +
          s"points=${(h.points: NonEmptyVector[PointData]).mkString_("{", ",", "}")}, " +
          s"aggregationTemporality=${h.aggregationTemporality}}"
    }
  }

  private final case class SumImpl[A <: PointData.NumberPoint](
      points: NonEmptyVector[A],
      monotonic: Boolean,
      aggregationTemporality: AggregationTemporality
  ) extends Sum { type Point = A }

  private final case class GaugeImpl[A <: PointData.NumberPoint](
      points: NonEmptyVector[A]
  ) extends Gauge { type Point = A }

  private final case class HistogramImpl(
      points: NonEmptyVector[PointData.Histogram],
      aggregationTemporality: AggregationTemporality
  ) extends Histogram

}
