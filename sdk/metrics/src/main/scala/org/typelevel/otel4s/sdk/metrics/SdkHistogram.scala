package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.Clock
import cats.effect.kernel.Resource
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.{BucketBoundaries, Histogram, MeasurementValue}
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

import scala.concurrent.duration.{FiniteDuration, TimeUnit}

private object SdkHistogram {

  private final class Backend[
    F[_]: Monad: Clock: Console: AskContext,
    A,
    Primitive: MeasurementValue: Numeric
  ](
     cast: A => Primitive,
     castDuration: Double => Primitive,
     name: String,
     storage: MetricStorage.Writeable[F]
   ) extends Histogram.Backend[F, A] {
    def meta: Histogram.Meta[F] = Histogram.Meta.enabled

    def record(value: A, attributes: Attribute[_]*): F[Unit] =
      doRecord(cast(value), Attributes.fromSpecific(attributes))

    def recordDuration(timeUnit: TimeUnit, attributes: Attribute[_]*): Resource[F, Unit] =
      Resource
        .makeCase(Clock[F].monotonic) { case (start, ec) =>
          for {
            end <- Clock[F].monotonic
            _ <- doRecord(
              castDuration((end - start).toUnit(timeUnit)),
              Attributes.fromSpecific(attributes ++ Histogram.causeAttributes(ec))
            )
          } yield ()
        }
        .void

    private def doRecord(value: Primitive, attributes: Attributes): F[Unit] =
      if (Numeric[Primitive].lt(value, Numeric[Primitive].zero)) {
        Console[F].println(
          s"Histograms can only record non-negative values. Instrument $name has tried to record a negative value."
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
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.Histogram,
        InstrumentValueType.of[A],
        Advice(boundaries)
      )

      for {
        storage <- sharedState.registerMetricStorage(descriptor)
      } yield {
        val backend: Histogram.Backend[F, A] =
          MeasurementValue[A] match {
            case MeasurementValue.LongMeasurementValue(cast)   =>
              new Backend[F, A, Long](cast, _.toLong, name, storage)
            case MeasurementValue.DoubleMeasurementValue(cast) =>
              new Backend[F, A, Double](cast, identity, name, storage)
          }

        Histogram.fromBackend(backend)
      }
    }
  }

}
