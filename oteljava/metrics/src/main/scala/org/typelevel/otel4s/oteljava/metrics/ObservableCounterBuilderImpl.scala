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

import cats.effect.kernel.Async
import cats.effect.kernel.Resource
import cats.effect.std.Dispatcher
import cats.syntax.all._
import io.opentelemetry.api.metrics.{Meter => JMeter}
import io.opentelemetry.api.metrics.{
  ObservableMeasurement => JObservableMeasurement
}
import io.opentelemetry.api.metrics.LongCounterBuilder
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement
import io.opentelemetry.api.metrics.ObservableLongMeasurement
import org.typelevel.otel4s.metrics._

private[oteljava] case class ObservableCounterBuilderImpl[F[_], A](
    factory: ObservableCounterBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
) extends ObservableCounter.Builder[F, A] {

  def withUnit(unit: String): ObservableCounter.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(
      description: String
  ): ObservableCounter.Builder[F, A] =
    copy(description = Option(description))

  def createWithCallback(
      cb: ObservableMeasurement[F, A] => F[Unit]
  ): Resource[F, ObservableCounter] =
    factory.createWithCallback(name, unit, description, cb)

  def create(
      measurements: F[Iterable[Measurement[A]]]
  ): Resource[F, ObservableCounter] =
    factory.create(name, unit, description, measurements)

  def createObserver: F[ObservableMeasurement[F, A]] =
    factory.createObserver(name, unit, description)
}

private[oteljava] object ObservableCounterBuilderImpl {

  def apply[F[_]: Async, A: MeasurementValue](
      jMeter: JMeter,
      name: String
  ): ObservableCounter.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        ObservableCounterBuilderImpl(longFactory(jMeter, cast), name)

      case MeasurementValue.DoubleMeasurementValue(cast) =>
        ObservableCounterBuilderImpl(doubleFactory(jMeter, cast), name)
    }

  private[oteljava] sealed abstract class Factory[F[_]: Async, A](
      jMeter: JMeter
  ) {
    type JMeasurement <: JObservableMeasurement

    final def create(
        name: String,
        unit: Option[String],
        description: Option[String],
        measurements: F[Iterable[Measurement[A]]]
    ): Resource[F, ObservableCounter] =
      createInternal(name, unit, description) { om =>
        measurements.flatMap { ms =>
          Async[F].delay(
            ms.foreach(m => doRecord(om, m.value, m.attributes))
          )
        }
      }

    final def createObserver(
        name: String,
        unit: Option[String],
        description: Option[String]
    ): F[ObservableMeasurement[F, A]] =
      Async[F].delay {
        val b = jMeter.counterBuilder(name)
        unit.foreach(b.setUnit)
        description.foreach(b.setDescription)
        buildObservable(b)
      }

    final def createWithCallback(
        name: String,
        unit: Option[String],
        description: Option[String],
        cb: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableCounter] =
      createInternal(name, unit, description) { om =>
        cb(
          new ObservableMeasurement[F, A] {
            def record(value: A, attributes: Attributes): F[Unit] =
              Async[F].delay(
                doRecord(om, value, attributes)
              )
          }
        )
      }

    protected def create(
        builder: LongCounterBuilder,
        dispatcher: Dispatcher[F],
        cb: JMeasurement => F[Unit]
    ): AutoCloseable

    protected def buildObservable(
        builder: LongCounterBuilder
    ): ObservableMeasurementImpl[F, A]

    protected def doRecord(
        measurement: JMeasurement,
        value: A,
        attributes: Attributes
    ): Unit

    private final def createInternal(
        name: String,
        unit: Option[String],
        description: Option[String]
    )(cb: JMeasurement => F[Unit]): Resource[F, ObservableCounter] =
      Dispatcher.sequential.flatMap { dispatcher =>
        Resource
          .fromAutoCloseable(Async[F].delay {
            val b = jMeter.counterBuilder(name)
            unit.foreach(b.setUnit)
            description.foreach(b.setDescription)
            create(b, dispatcher, cb)
          })
          .as(new ObservableCounter {})
      }

  }

  private def longFactory[F[_]: Async, A](
      jMeter: JMeter,
      cast: A => Long
  ): Factory[F, A] =
    new Factory[F, A](jMeter) {
      type JMeasurement = ObservableLongMeasurement

      protected def create(
          builder: LongCounterBuilder,
          dispatcher: Dispatcher[F],
          cb: ObservableLongMeasurement => F[Unit]
      ): AutoCloseable =
        builder.buildWithCallback(om => dispatcher.unsafeRunSync(cb(om)))

      protected def buildObservable(
          builder: LongCounterBuilder
      ): ObservableMeasurementImpl[F, A] =
        new ObservableMeasurementImpl.LongObservableMeasurement[F, A](
          cast,
          builder.buildObserver()
        )

      protected def doRecord(
          om: ObservableLongMeasurement,
          value: A,
          attributes: Attributes
      ): Unit =
        om.record(cast(value), Conversions.toJAttributes(attributes))
    }

  private def doubleFactory[F[_]: Async, A](
      jMeter: JMeter,
      cast: A => Double
  ): Factory[F, A] =
    new Factory[F, A](jMeter) {
      type JMeasurement = ObservableDoubleMeasurement

      protected def create(
          builder: LongCounterBuilder,
          dispatcher: Dispatcher[F],
          cb: ObservableDoubleMeasurement => F[Unit]
      ): AutoCloseable =
        builder
          .ofDoubles()
          .buildWithCallback(om => dispatcher.unsafeRunSync(cb(om)))

      protected def buildObservable(
          builder: LongCounterBuilder
      ): ObservableMeasurementImpl[F, A] =
        new ObservableMeasurementImpl.DoubleObservableMeasurement[F, A](
          cast,
          builder.ofDoubles().buildObserver()
        )

      protected def doRecord(
          om: ObservableDoubleMeasurement,
          value: A,
          attributes: Attributes
      ): Unit =
        om.record(cast(value), Conversions.toJAttributes(attributes))
    }

}
