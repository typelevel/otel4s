package org.typelevel.otel4s.sdk.metrics.internal

import cats.Monad
import cats.data.NonEmptyList
import cats.effect.kernel.MonadCancelThrow
import cats.effect.syntax.monadCancel._
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.metrics.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.data.MetricData

import scala.concurrent.duration.FiniteDuration

private[metrics] final class CallbackRegistration[F[_]: MonadCancelThrow](
    measurements: NonEmptyList[SdkObservableMeasurement[F, _]],
    callback: F[Unit]
) {

  private val descriptors: NonEmptyList[InstrumentDescriptor] =
    measurements.map(_.descriptor)

  private val hasStorages: Boolean =
    measurements.exists(_.storages.nonEmpty)

  def invokeCallback(
      reader: RegisteredReader[F],
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[Unit] =
    measurements
      .traverse_(_.setActiveReader(reader, startTimestamp, collectTimestamp))
      .flatMap(_ => callback)
      .guarantee(measurements.traverse_(_.unsetActiveReader))
      .whenA(hasStorages)

}

private[metrics] object CallbackRegistration {
  def create[F[_]: MonadCancelThrow](
      measurements: NonEmptyList[SdkObservableMeasurement[F, Any]],
      callback: F[Unit]
  ): CallbackRegistration[F] =
    new CallbackRegistration[F](measurements, callback)
}
