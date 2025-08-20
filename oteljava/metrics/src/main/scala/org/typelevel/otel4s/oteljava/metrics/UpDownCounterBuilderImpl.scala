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
import org.typelevel.otel4s.oteljava.AttributeConverters._
import org.typelevel.otel4s.oteljava.context.AskContext

import scala.collection.immutable

private[oteljava] case class UpDownCounterBuilderImpl[F[_], A](
    factory: UpDownCounterBuilderImpl.Factory[F, A],
    name: String,
    unit: Option[String] = None,
    description: Option[String] = None
) extends UpDownCounter.Builder.Unsealed[F, A] {

  def withUnit(unit: String): UpDownCounter.Builder[F, A] =
    copy(unit = Option(unit))

  def withDescription(description: String): UpDownCounter.Builder[F, A] =
    copy(description = Option(description))

  def create: F[UpDownCounter[F, A]] =
    factory.create(name, unit, description)
}

private[oteljava] object UpDownCounterBuilderImpl {

  def apply[F[_]: Sync: AskContext, A: MeasurementValue](
      jMeter: JMeter,
      name: String,
      meta: InstrumentMeta.Dynamic[F]
  ): UpDownCounter.Builder[F, A] =
    MeasurementValue[A] match {
      case MeasurementValue.LongMeasurementValue(cast) =>
        UpDownCounterBuilderImpl(longFactory(jMeter, cast, meta), name)

      case MeasurementValue.DoubleMeasurementValue(cast) =>
        UpDownCounterBuilderImpl(doubleFactory(jMeter, cast, meta), name)
    }

  private[oteljava] trait Factory[F[_], A] {
    def create(
        name: String,
        unit: Option[String],
        description: Option[String]
    ): F[UpDownCounter[F, A]]
  }

  private def longFactory[F[_]: Sync: AskContext, A](
      jMeter: JMeter,
      cast: A => Long,
      instrumentMeta: InstrumentMeta.Dynamic[F]
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

          val backend = new UpDownCounter.Backend.Unsealed[F, A] {
            val meta: InstrumentMeta.Dynamic[F] = instrumentMeta

            def add(
                value: A,
                attributes: immutable.Iterable[Attribute[_]]
            ): F[Unit] =
              record(cast(value), attributes)

            def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
              record(1L, attributes)

            def dec(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
              record(-1L, attributes)

            private def record(
                value: Long,
                attributes: immutable.Iterable[Attribute[_]]
            ): F[Unit] =
              ContextUtils.delayWithContext { () =>
                counter.add(value, attributes.toJavaAttributes)
              }
          }

          UpDownCounter.fromBackend(backend)
        }
    }

  private def doubleFactory[F[_]: Sync: AskContext, A](
      jMeter: JMeter,
      cast: A => Double,
      instrumentMeta: InstrumentMeta.Dynamic[F]
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

          val backend = new UpDownCounter.Backend.Unsealed[F, A] {
            val meta: InstrumentMeta.Dynamic[F] = instrumentMeta

            def add(
                value: A,
                attributes: immutable.Iterable[Attribute[_]]
            ): F[Unit] =
              record(cast(value), attributes)

            def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
              record(1.0, attributes)

            def dec(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
              record(-1.0, attributes)

            private def record(
                value: Double,
                attributes: immutable.Iterable[Attribute[_]]
            ): F[Unit] =
              ContextUtils.delayWithContext { () =>
                counter.add(value, attributes.toJavaAttributes)
              }
          }

          UpDownCounter.fromBackend(backend)
        }
    }

}
