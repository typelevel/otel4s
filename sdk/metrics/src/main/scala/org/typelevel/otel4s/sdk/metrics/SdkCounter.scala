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

package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.Counter
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

private object SdkCounter {

  private final class Backend[
      F[_]: Monad: Console: AskContext,
      A,
      Primitive: MeasurementValue: Numeric
  ](
      cast: A => Primitive,
      name: String,
      storage: MetricStorage.Writeable[F]
  ) extends Counter.Backend[F, A] {
    def meta: InstrumentMeta[F] = InstrumentMeta.enabled

    def add(value: A, attributes: Attribute[_]*): F[Unit] =
      record(cast(value), Attributes.fromSpecific(attributes))

    def inc(attributes: Attribute[_]*): F[Unit] =
      record(Numeric[Primitive].one, Attributes.fromSpecific(attributes))

    private def record(value: Primitive, attributes: Attributes): F[Unit] =
      if (Numeric[Primitive].lt(value, Numeric[Primitive].zero)) {
        Console[F].println(
          s"Counters can only increase. Instrument $name has tried to record a negative value."
        )
      } else {
        for {
          ctx <- Ask[F, Context].ask
          _ <- storage.record(value, attributes, ctx)
        } yield ()
      }
  }

  final case class Builder[
      F[_]: Monad: Console: AskContext,
      A: MeasurementValue
  ](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends Counter.Builder[F, A] {

    def withUnit(unit: String): Counter.Builder[F, A] =
      copy(unit = Some(unit))

    def withDescription(description: String): Counter.Builder[F, A] =
      copy(description = Some(description))

    def create: F[Counter[F, A]] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.Counter,
        InstrumentValueType.of[A],
        Advice.empty
      )

      for {
        storage <- sharedState.registerMetricStorage(descriptor)
      } yield {
        val backend: Counter.Backend[F, A] =
          MeasurementValue[A] match {
            case MeasurementValue.LongMeasurementValue(cast) =>
              new Backend[F, A, Long](cast, descriptor.name, storage)
            case MeasurementValue.DoubleMeasurementValue(cast) =>
              new Backend[F, A, Double](cast, descriptor.name, storage)
          }

        Counter.fromBackend(backend)
      }
    }
  }

}
