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

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import io.opentelemetry.api.metrics.ObservableLongMeasurement
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

  def create: F[UpDownCounter[F, A]] =
    factory.create(name, unit, description)

  def createWithCallback(
      cb: ObservableMeasurement[F, A] => F[Unit]
  ): Resource[F, Unit] =
    factory.createWithCallback(name, unit, description, cb)

  def createWithSupplier(
      measurements: F[List[Measurement[A]]]
  ): Resource[F, Unit] =
    factory.createWithSupplier(name, unit, description, measurements)
}

private[oteljava] object UpDownCounterBuilderImpl {

  def apply[F[_]: Async, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): UpDownCounter.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue =>
        UpDownCounterBuilderImpl(longFactory(jMeter), name)

      case MeasurementValue.DoubleMeasurementValue =>
        UpDownCounterBuilderImpl(doubleFactory(jMeter), name)
    }

  private[oteljava] trait Factory[F[_], A] {
    def create(
        name: String,
        unit: Option[String],
        description: Option[String]
    ): F[UpDownCounter[F, A]]

    def createWithCallback(
        name: String,
        unit: Option[String],
        description: Option[String],
        cb: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, Unit]

    def createWithSupplier(
        name: String,
        unit: Option[String],
        description: Option[String],
        measurements: F[List[Measurement[A]]]
    ): Resource[F, Unit]
  }

  private def longFactory[F[_]: Async](jMeter: JMeter): Factory[F, Long] =
    new Factory[F, Long] {
      def create(
          name: String,
          unit: Option[String],
          description: Option[String]
      ): F[UpDownCounter[F, Long]] =
        Async[F].delay {
          val builder = jMeter.upDownCounterBuilder(name)
          unit.foreach(builder.setUnit)
          description.foreach(builder.setDescription)
          val counter = builder.build()

          val backend = new UpDownCounter.LongBackend[F] {
            val meta: InstrumentMeta[F] = InstrumentMeta.enabled

            def add(value: Long, attributes: Attribute[_]*): F[Unit] =
              Async[F].delay(
                counter.add(value, Conversions.toJAttributes(attributes))
              )
          }

          UpDownCounter.fromBackend(backend)
        }

      def createWithCallback(
          name: String,
          unit: Option[String],
          description: Option[String],
          cb: ObservableMeasurement[F, Long] => F[Unit]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { olm =>
          cb(new ObservableLongImpl(olm))
        }

      def createWithSupplier(
          name: String,
          unit: Option[String],
          description: Option[String],
          measurements: F[List[Measurement[Long]]]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { olm =>
          measurements.flatMap(ms =>
            Async[F].delay(
              ms.foreach(m =>
                olm.record(m.value, Conversions.toJAttributes(m.attributes))
              )
            )
          )
        }

      private def createInternal(
          name: String,
          unit: Option[String],
          description: Option[String],
      )(cb: ObservableLongMeasurement => F[Unit]): Resource[F, Unit] =
        Dispatcher.sequential.flatMap { dispatcher =>
          Resource
            .fromAutoCloseable(Async[F].delay {
              val b = jMeter.upDownCounterBuilder(name)
              unit.foreach(b.setUnit)
              description.foreach(b.setDescription)
              b.buildWithCallback { olm =>
                dispatcher.unsafeRunSync(cb(olm))
              }
            })
            .void
        }
    }

  private def doubleFactory[F[_]: Async](jMeter: JMeter): Factory[F, Double] =
    new Factory[F, Double] {
      def create(
          name: String,
          unit: Option[String],
          description: Option[String]
      ): F[UpDownCounter[F, Double]] =
        Async[F].delay {
          val builder = jMeter.upDownCounterBuilder(name)
          unit.foreach(builder.setUnit)
          description.foreach(builder.setDescription)
          val counter = builder.ofDoubles().build()

          val backend = new UpDownCounter.DoubleBackend[F] {
            val meta: InstrumentMeta[F] = InstrumentMeta.enabled

            def add(value: Double, attributes: Attribute[_]*): F[Unit] =
              Async[F].delay(
                counter.add(value, Conversions.toJAttributes(attributes))
              )
          }

          UpDownCounter.fromBackend(backend)
        }

      def createWithCallback(
          name: String,
          unit: Option[String],
          description: Option[String],
          cb: ObservableMeasurement[F, Double] => F[Unit]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { olm =>
          cb(new ObservableDoubleImpl(olm))
        }

      def createWithSupplier(
          name: String,
          unit: Option[String],
          description: Option[String],
          measurements: F[List[Measurement[Double]]]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { olm =>
          measurements.flatMap(ms =>
            Async[F].delay(
              ms.foreach(m =>
                olm.record(m.value, Conversions.toJAttributes(m.attributes))
              )
            )
          )
        }

      private def createInternal(
          name: String,
          unit: Option[String],
          description: Option[String],
      )(cb: ObservableDoubleMeasurement => F[Unit]): Resource[F, Unit] =
        Dispatcher.sequential.flatMap { dispatcher =>
          Resource
            .fromAutoCloseable(Async[F].delay {
              val b = jMeter.upDownCounterBuilder(name)
              unit.foreach(b.setUnit)
              description.foreach(b.setDescription)
              b.ofDoubles().buildWithCallback { olm =>
                dispatcher.unsafeRunSync(cb(olm))
              }
            })
            .void
        }
    }

}
