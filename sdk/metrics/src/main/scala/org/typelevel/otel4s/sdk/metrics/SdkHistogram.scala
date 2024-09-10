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
import cats.effect.Clock
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.ci.CIString
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.storage.MetricStorage

import scala.collection.immutable
import scala.concurrent.duration.TimeUnit

/** A synchronous instrument that can be used to report arbitrary values that are likely to be statistically meaningful.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/api/#histogram]]
  */
private object SdkHistogram {

  private final class Backend[
      F[_]: Monad: Clock: Console: AskContext,
      A,
      Primitive: Numeric
  ](
      cast: A => Primitive,
      castDuration: Double => Primitive,
      name: String,
      storage: MetricStorage.Synchronous.Writeable[F, Primitive]
  ) extends Histogram.Backend[F, A] {
    val meta: InstrumentMeta[F] = InstrumentMeta.enabled

    def record(
        value: A,
        attributes: immutable.Iterable[Attribute[_]]
    ): F[Unit] =
      doRecord(cast(value), Attributes.fromSpecific(attributes))

    def recordDuration(
        timeUnit: TimeUnit,
        attributes: immutable.Iterable[Attribute[_]]
    ): Resource[F, Unit] =
      Resource
        .makeCase(Clock[F].monotonic) { case (start, ec) =>
          for {
            end <- Clock[F].monotonic
            _ <- doRecord(
              castDuration((end - start).toUnit(timeUnit)),
              (attributes ++ Histogram.causeAttributes(ec)).to(Attributes)
            )
          } yield ()
        }
        .void

    private def doRecord(value: Primitive, attributes: Attributes): F[Unit] =
      if (Numeric[Primitive].lt(value, Numeric[Primitive].zero)) {
        Console[F].errorln(
          s"SdkHistogram: histograms can only record non-negative values. Instrument [$name] has tried to record a negative value [$value]."
        )
      } else {
        for {
          ctx <- Ask[F, Context].ask
          _ <- storage.record(value, attributes, ctx)
        } yield ()
      }
  }

  final case class Builder[
      F[_]: Monad: Clock: Console: AskContext,
      A: MeasurementValue
  ](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None,
      boundaries: Option[BucketBoundaries] = None
  ) extends Histogram.Builder[F, A] {

    def withUnit(unit: String): Histogram.Builder[F, A] =
      copy(unit = Some(unit))

    def withDescription(description: String): Histogram.Builder[F, A] =
      copy(description = Some(description))

    def withExplicitBucketBoundaries(
        boundaries: BucketBoundaries
    ): Histogram.Builder[F, A] =
      copy(boundaries = Some(boundaries))

    def create: F[Histogram[F, A]] = {
      val descriptor = InstrumentDescriptor.synchronous(
        name = CIString(name),
        description = description,
        unit = unit,
        advice = boundaries.map(b => Advice(Some(b))),
        instrumentType = InstrumentType.Histogram
      )

      MeasurementValue[A] match {
        case MeasurementValue.LongMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Long](descriptor)
            .map { storage =>
              Histogram.fromBackend(
                new Backend[F, A, Long](cast, _.toLong, name, storage)
              )
            }

        case MeasurementValue.DoubleMeasurementValue(cast) =>
          sharedState
            .registerMetricStorage[Double](descriptor)
            .map { storage =>
              Histogram.fromBackend(
                new Backend[F, A, Double](cast, identity, name, storage)
              )
            }
      }
    }
  }

}
