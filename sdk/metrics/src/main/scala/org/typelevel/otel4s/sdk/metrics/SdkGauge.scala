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
import org.typelevel.otel4s.metrics.Gauge
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.meta.InstrumentMeta
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage

import scala.collection.immutable

/** A synchronous instrument that records non-additive values.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/api/#gauge]]
  */
private object SdkGauge {

  private final class Backend[F[_]: Monad: AskContext, A, Primitive](
      cast: A => Primitive,
      storage: MetricStorage.Synchronous.Writeable[F, Primitive],
      val meta: InstrumentMeta[F]
  ) extends Gauge.Backend.Unsealed[F, A] {

    def record(
        value: A,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit] =
      for {
        ctx <- Ask[F, Context].ask
        _ <- storage.record(cast(value), attributes.to(Attributes), ctx)
      } yield ()

  }

  final case class Builder[
      F[_]: Monad: Console: AskContext,
      A: MeasurementValue
  ](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends Gauge.Builder.Unsealed[F, A] {

    def withUnit(unit: String): Gauge.Builder[F, A] =
      copy(unit = Some(unit))

    def withDescription(description: String): Gauge.Builder[F, A] =
      copy(description = Some(description))

    def create: F[Gauge[F, A]] = {
      val descriptor = InstrumentDescriptor.synchronous(
        name = CIString(name),
        description = description,
        unit = unit,
        advice = None,
        instrumentType = InstrumentType.Gauge
      )

      MeasurementValue[A] match {
        case MeasurementValue.LongMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Long](descriptor)
            .map { storage =>
              Gauge.fromBackend(
                new Backend[F, A, Long](cast, storage, sharedState.meta)
              )
            }

        case MeasurementValue.DoubleMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Double](descriptor)
            .map { storage =>
              Gauge.fromBackend(
                new Backend[F, A, Double](cast, storage, sharedState.meta)
              )
            }
      }
    }
  }

}
