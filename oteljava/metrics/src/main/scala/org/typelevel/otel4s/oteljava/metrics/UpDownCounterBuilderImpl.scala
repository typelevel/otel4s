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

import cats.effect.kernel.Sync
import io.opentelemetry.api.metrics.{Meter => JMeter}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics._

private[oteljava] case class UpDownCounterBuilderImpl[F[_], A](
    factory: UpDownCounterBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
) extends UpDownCounter.Builder[F, A] {

  def withUnit(unit: String): UpDownCounter.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(description: String): UpDownCounter.Builder[F, A] =
    copy(description = Option(description))

  def of[B: MeasurementValue]: UpDownCounter.Builder[F, B] =
    copy(factory = factory.switchType[B])

  def create: F[UpDownCounter[F, A]] =
    factory.create(name, unit, description)
}

private[oteljava] object UpDownCounterBuilderImpl {

  def apply[F[_]: Sync, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): UpDownCounter.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        UpDownCounterBuilderImpl(longFactory(jMeter, cast), name)

      case MeasurementValue.DoubleMeasurementValue(cast) =>
        UpDownCounterBuilderImpl(doubleFactory(jMeter, cast), name)
    }

  private[oteljava] trait Factory[F[_], A] {
    def create(
        name: String,
        unit: Option[String],
        description: Option[String]
    ): F[UpDownCounter[F, A]]

    def switchType[B: MeasurementValue]: Factory[F, B]
  }

  private def longFactory[F[_]: Sync, A](
      jMeter: JMeter,
      cast: A => Long
  ): Factory[F, A] =
    new Factory[F, A] {
      def create(
          name: String,
          unit: Option[String],
          description: Option[String]
      ): F[UpDownCounter[F, A]] =
        Sync[F].delay {
          val builder = jMeter.upDownCounterBuilder(name)
          unit.foreach(builder.setUnit)
          description.foreach(builder.setDescription)
          val counter = builder.build()

          val backend = new UpDownCounter.Backend[F, A] {
            val meta: InstrumentMeta[F] = InstrumentMeta.enabled

            def add(value: A, attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(cast(value), Conversions.toJAttributes(attributes))
              )

            def inc(attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(1L, Conversions.toJAttributes(attributes))
              )

            def dec(attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(-1L, Conversions.toJAttributes(attributes))
              )
          }

          UpDownCounter.fromBackend(backend)
        }

      def switchType[B: MeasurementValue]: Factory[F, B] =
        MeasurementValue[B] match {
          case MeasurementValue.LongMeasurementValue(cast) =>
            longFactory(jMeter, cast)
          case MeasurementValue.DoubleMeasurementValue(cast) =>
            doubleFactory(jMeter, cast)
        }
    }

  private def doubleFactory[F[_]: Sync, A](
      jMeter: JMeter,
      cast: A => Double
  ): Factory[F, A] =
    new Factory[F, A] {
      def create(
          name: String,
          unit: Option[String],
          description: Option[String]
      ): F[UpDownCounter[F, A]] =
        Sync[F].delay {
          val builder = jMeter.upDownCounterBuilder(name)
          unit.foreach(builder.setUnit)
          description.foreach(builder.setDescription)
          val counter = builder.ofDoubles().build()

          val backend = new UpDownCounter.Backend[F, A] {
            val meta: InstrumentMeta[F] = InstrumentMeta.enabled

            def add(value: A, attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(cast(value), Conversions.toJAttributes(attributes))
              )

            def inc(attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(1.0, Conversions.toJAttributes(attributes))
              )

            def dec(attributes: Attribute[_]*): F[Unit] =
              Sync[F].delay(
                counter.add(-1.0, Conversions.toJAttributes(attributes))
              )
          }

          UpDownCounter.fromBackend(backend)
        }

      def switchType[B: MeasurementValue]: Factory[F, B] =
        MeasurementValue[B] match {
          case MeasurementValue.LongMeasurementValue(cast) =>
            longFactory(jMeter, cast)
          case MeasurementValue.DoubleMeasurementValue(cast) =>
            doubleFactory(jMeter, cast)
        }
    }

}
