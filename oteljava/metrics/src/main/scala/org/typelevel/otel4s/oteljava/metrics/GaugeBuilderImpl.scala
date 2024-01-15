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

package org.typelevel.otel4s.oteljava.metrics

import cats.effect.Async
import cats.effect.Resource
import cats.effect.std.Dispatcher
import cats.syntax.flatMap._
import cats.syntax.functor._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.metrics._
import org.typelevel.otel4s.oteljava.Conversions

private[oteljava] case class GaugeBuilderImpl[F[_], A](
    factory: GaugeBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
) extends Gauge.Builder[F, A] {

  def withUnit(unit: String): Gauge.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(description: String): Gauge.Builder[F, A] =
    copy(description = Option(description))

  def createWithCallback(
      cb: ObservableMeasurement[F, A] => F[Unit]
  ): Resource[F, Unit] =
    factory.createWithCallback(name, unit, description, cb)

  def createWithSupplier(
      measurements: F[List[Measurement[A]]]
  ): Resource[F, Unit] =
    factory.createWithSupplier(name, unit, description, measurements)

}

private[oteljava] object GaugeBuilderImpl {

  def apply[F[_]: Async, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): Gauge.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue =>
        GaugeBuilderImpl(longFactory(jMeter), name)

      case MeasurementValue.DoubleMeasurementValue =>
        GaugeBuilderImpl(doubleFactory(jMeter), name)
    }

  private[oteljava] trait Factory[F[_], A] {
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
      def createWithCallback(
          name: String,
          unit: Option[String],
          description: Option[String],
          cb: ObservableMeasurement[F, Long] => F[Unit]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { odm =>
          cb(new ObservableLongImpl(odm))
        }

      def createWithSupplier(
          name: String,
          unit: Option[String],
          description: Option[String],
          measurements: F[List[Measurement[Long]]]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { odm =>
          measurements.flatMap(ms =>
            Async[F].delay(
              ms.foreach(m =>
                odm.record(m.value, Conversions.toJAttributes(m.attributes))
              )
            )
          )
        }

      private def createInternal(
          name: String,
          unit: Option[String],
          description: Option[String]
      )(cb: ObservableLongMeasurement => F[Unit]): Resource[F, Unit] =
        Dispatcher.sequential.flatMap { dispatcher =>
          Resource
            .fromAutoCloseable(Async[F].delay {
              val b = jMeter.gaugeBuilder(name)
              unit.foreach(b.setUnit)
              description.foreach(b.setDescription)
              b.ofLongs().buildWithCallback { odm =>
                dispatcher.unsafeRunSync(cb(odm))
              }
            })
            .void
        }
    }

  private def doubleFactory[F[_]: Async](jMeter: JMeter): Factory[F, Double] =
    new Factory[F, Double] {
      def createWithCallback(
          name: String,
          unit: Option[String],
          description: Option[String],
          cb: ObservableMeasurement[F, Double] => F[Unit]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { odm =>
          cb(new ObservableDoubleImpl(odm))
        }

      def createWithSupplier(
          name: String,
          unit: Option[String],
          description: Option[String],
          measurements: F[List[Measurement[Double]]]
      ): Resource[F, Unit] =
        createInternal(name, unit, description) { odm =>
          measurements.flatMap(ms =>
            Async[F].delay(
              ms.foreach(m =>
                odm.record(m.value, Conversions.toJAttributes(m.attributes))
              )
            )
          )
        }

      private def createInternal(
          name: String,
          unit: Option[String],
          description: Option[String]
      )(cb: ObservableDoubleMeasurement => F[Unit]): Resource[F, Unit] =
        Dispatcher.sequential.flatMap { dispatcher =>
          Resource
            .fromAutoCloseable(Async[F].delay {
              val b = jMeter.gaugeBuilder(name)
              unit.foreach(b.setUnit)
              description.foreach(b.setDescription)
              b.buildWithCallback { odm =>
                dispatcher.unsafeRunSync(cb(odm))
              }
            })
            .void
        }
    }

}
