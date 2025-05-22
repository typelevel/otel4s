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
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.Counter
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage

import scala.collection.immutable

/** A synchronous instrument that supports non-negative increments.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/api/#counter]]
  */
private object SdkCounter {

  private final class Backend[
      F[_]: Monad: Console: AskContext,
      A,
      Primitive: Numeric
  ](
      cast: A => Primitive,
      name: String,
      storage: MetricStorage.Synchronous.Writeable[F, Primitive],
      instrumentMeta: InstrumentMeta.Dynamic[F]
  ) extends Counter.Backend[F, A] {
    def meta: InstrumentMeta.Dynamic[F] = instrumentMeta

    def add(value: A, attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
      record(cast(value), attributes)

    def inc(attributes: immutable.Iterable[Attribute[_]]): F[Unit] =
      record(Numeric[Primitive].one, attributes)

    private def record(
        value: Primitive,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit] =
      if (Numeric[Primitive].lt(value, Numeric[Primitive].zero)) {
        Console[F].errorln(
          s"SdkCounter: counters can only increase. Instrument [$name] has tried to record a negative value [$value]."
        )
      } else {
        for {
          ctx <- Ask[F, Context].ask
          _ <- storage.record(value, attributes.to(Attributes), ctx)
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
      val descriptor = InstrumentDescriptor.synchronous(
        name = CIString(name),
        description = description,
        unit = unit,
        advice = None,
        instrumentType = InstrumentType.Counter
      )

      MeasurementValue[A] match {
        case MeasurementValue.LongMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Long](descriptor)
            .map { storage =>
              Counter.fromBackend(
                new Backend[F, A, Long](cast, name, storage, sharedState.meta)
              )
            }

        case MeasurementValue.DoubleMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Double](descriptor)
            .map { storage =>
              Counter.fromBackend(
                new Backend[F, A, Double](cast, name, storage, sharedState.meta)
              )
            }
      }
    }
  }

}
