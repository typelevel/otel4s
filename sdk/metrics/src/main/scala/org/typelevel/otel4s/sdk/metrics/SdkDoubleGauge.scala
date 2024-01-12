package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.MonadCancelThrow
import cats.effect.kernel.{Clock, Resource}
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.foldable._
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.Histogram.Meta
import org.typelevel.otel4s.metrics.{
  Histogram,
  Measurement,
  ObservableGauge,
  ObservableInstrumentBuilder,
  ObservableMeasurement,
  SyncInstrumentBuilder
}
import org.typelevel.otel4s.sdk.context.{AskContext, Context}
import org.typelevel.otel4s.sdk.metrics.internal.{
  Advice,
  CallbackRegistration,
  InstrumentDescriptor,
  InstrumentType,
  InstrumentValueType
}
import org.typelevel.otel4s.{Attribute, Attributes}

object SdkDoubleGauge {

  final case class Builder[F[_]: MonadCancelThrow: Clock: Console: AskContext](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends ObservableInstrumentBuilder[F, Double, ObservableGauge] {

    type Self = ObservableInstrumentBuilder[F, Double, ObservableGauge]

    def withUnit(unit: String): Self =
      copy(unit = Some(unit))

    def withDescription(description: String): Self =
      copy(description = Some(description))

    def createWithCallback(
        cb: ObservableMeasurement[F, Double] => F[Unit]
    ): Resource[F, ObservableGauge] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.ObservableGauge,
        InstrumentValueType.Double,
        Advice.empty
      )

      Resource
        .eval(sharedState.registerDoubleObservableMeasurement(descriptor))
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
        measurements: F[List[Measurement[Double]]]
    ): Resource[F, ObservableGauge] =
      createWithCallback { cb =>
        for {
          m <- measurements
          _ <- m.traverse_(m => cb.record(m.value, m.attributes: _*))
        } yield ()
      }
  }

}
