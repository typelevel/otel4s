package org.typelevel.otel4s.sdk.metrics

import cats.data.NonEmptyList
import cats.effect.Async
import cats.effect.Clock
import cats.effect.MonadCancelThrow
import cats.effect.Resource
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.Measurement
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.metrics.ObservableGauge
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.CallbackRegistration
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType

private object SdkObservableGauge {

  final case class Builder[
      F[_]: MonadCancelThrow: Clock: Console: AskContext,
      A: MeasurementValue
  ](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends ObservableGauge.Builder[F, A] {

    def withUnit(unit: String): ObservableGauge.Builder[F, A] =
      copy(unit = Some(unit))

    def withDescription(description: String): ObservableGauge.Builder[F, A] =
      copy(description = Some(description))

    def createWithCallback(
        cb: ObservableMeasurement[F, A] => F[Unit]
    ): Resource[F, ObservableGauge] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.ObservableGauge,
        InstrumentValueType.of[A],
        Advice.empty
      )

      Resource
        .eval(sharedState.registerObservableMeasurement(descriptor))
        .flatMap { observable =>
          val runnable = cb(observable)
          val cr =
            new CallbackRegistration[F](NonEmptyList.one(observable), runnable)

          Resource
            .make(sharedState.registerCallback(cr))(_ =>
              sharedState.removeCallback(cr)
            )
            .as(new ObservableGauge {})
        }
    }

    def create(
        measurements: F[Iterable[Measurement[A]]]
    ): Resource[F, ObservableGauge] =
      createWithCallback { cb =>
        for {
          m <- measurements
          _ <- m.toVector.traverse_(m => cb.record(m.value, m.attributes))
        } yield ()
      }
  }

}
