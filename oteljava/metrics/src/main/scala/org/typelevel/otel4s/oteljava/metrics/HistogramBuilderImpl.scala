/*
 * Copyright 2022 Typelevel
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

package org.typelevel.otel4s
package oteljava
package metrics

import cats.effect.Sync
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.metrics._

import scala.jdk.CollectionConverters._

private[oteljava] case class HistogramBuilderImpl[F[_], A](
    factory: HistogramBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None,
    boundaries: Option[BucketBoundaries] = None
) extends Histogram.Builder[F, A] {

  def withUnit(unit: String): Histogram.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(description: String): Histogram.Builder[F, A] =
    copy(description = Option(description))

  def withExplicitBucketBoundaries(
      boundaries: BucketBoundaries
  ): Histogram.Builder[F, A] =
    copy(boundaries = Some(boundaries))

  def create: F[Histogram[F, A]] =
    factory.create(name, unit, description, boundaries)

}

object HistogramBuilderImpl {

  def apply[F[_]: Sync, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): Histogram.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue =>
        HistogramBuilderImpl(longFactory(jMeter), name)

      case MeasurementValue.DoubleMeasurementValue =>
        HistogramBuilderImpl(doubleFactory(jMeter), name)
    }

  private[oteljava] trait Factory[F[_], A] {
    def create(
        name: String,
        unit: Option[String],
        description: Option[String],
        boundaries: Option[BucketBoundaries]
    ): F[Histogram[F, A]]
  }

  private def longFactory[F[_]: Sync](jMeter: JMeter): Factory[F, Long] =
    (name, unit, description, boundaries) =>
      Sync[F].delay {
        val builder = jMeter.histogramBuilder(name)
        unit.foreach(builder.setUnit)
        description.foreach(builder.setDescription)
        boundaries.foreach(b =>
          builder.setExplicitBucketBoundariesAdvice(
            b.boundaries.map(Double.box).asJava
          )
        )
        val histogram = builder.ofLongs().build

        val backend = new Histogram.LongBackend[F] {
          val meta: Histogram.Meta[F] = Histogram.Meta.enabled

          def record(value: Long, attributes: Attribute[_]*): F[Unit] =
            Sync[F].delay(
              histogram.record(value, Conversions.toJAttributes(attributes))
            )
        }

        Histogram.fromBackend(backend)
      }

  private def doubleFactory[F[_]: Sync](jMeter: JMeter): Factory[F, Double] =
    (name, unit, description, boundaries) =>
      Sync[F].delay {
        val builder = jMeter.histogramBuilder(name)
        unit.foreach(builder.setUnit)
        description.foreach(builder.setDescription)
        boundaries.foreach(b =>
          builder.setExplicitBucketBoundariesAdvice(
            b.boundaries.map(Double.box).asJava
          )
        )
        val histogram = builder.build

        val backend = new Histogram.DoubleBackend[F] {
          val meta: Histogram.Meta[F] = Histogram.Meta.enabled

          def record(value: Double, attributes: Attribute[_]*): F[Unit] =
            Sync[F].delay(
              histogram.record(value, Conversions.toJAttributes(attributes))
            )
        }

        Histogram.fromBackend(backend)
      }

}
