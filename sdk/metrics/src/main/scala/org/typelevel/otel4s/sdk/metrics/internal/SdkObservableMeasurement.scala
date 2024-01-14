package org.typelevel.otel4s.sdk.metrics.internal

import cats.Monad
import cats.effect.Concurrent
import cats.effect.Ref
import cats.syntax.applicative._
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.ObservableMeasurement
import org.typelevel.otel4s.sdk.metrics.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

import scala.concurrent.duration.FiniteDuration

private[metrics] abstract class SdkObservableMeasurement[F[_]: Monad, A](
    stateRef: Ref[F, SdkObservableMeasurement.State[F]],
    /*scope: InstrumentationScope,*/
    val descriptor: InstrumentDescriptor,
    val storages: Vector[MetricStorage.Asynchronous[F]]
) extends ObservableMeasurement[F, A] {
  import SdkObservableMeasurement._

  def setActiveReader(
      reader: RegisteredReader[F],
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[Unit] =
    stateRef.set(State.WithReader(reader, startTimestamp, collectTimestamp))

  def unsetActiveReader: F[Unit] =
    stateRef.set(State.Empty())

  def record(value: A, attributes: Attribute[_]*): F[Unit] = {
    stateRef.get.flatMap {
      case State.Empty() =>
        Monad[F].unit // todo: log warning

      case State.WithReader(reader, start, collect) =>
        val measurement = create(
          value,
          start,
          collect,
          Attributes.fromSpecific(attributes)
        )

        storages.traverse_ { storage =>
          storage.record(measurement).whenA(storage.reader == reader)
        }
    }
  }

  protected def create(
      value: A,
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration,
      attributes: Attributes
  ): Measurement

}

object SdkObservableMeasurement {

  sealed trait State[F[_]]
  object State {
    final case class Empty[F[_]]() extends State[F]

    final case class WithReader[F[_]](
        reader: RegisteredReader[F],
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration
    ) extends State[F]
  }

  def ofLong[F[_]: Concurrent](
      storages: Vector[MetricStorage.Asynchronous[F]],
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, Long]] =
    Ref.of[F, State[F]](State.Empty()).map { state =>
      new SdkObservableMeasurement[F, Long](state, descriptor, storages) {
        protected def create(
            value: Long,
            startTimestamp: FiniteDuration,
            collectTimestamp: FiniteDuration,
            attributes: Attributes
        ): Measurement =
          Measurement.LongMeasurement(
            startTimestamp,
            collectTimestamp,
            attributes,
            value
          )
      }
    }

  def ofDouble[F[_]: Concurrent](
      storages: Vector[MetricStorage.Asynchronous[F]],
      descriptor: InstrumentDescriptor
  ): F[SdkObservableMeasurement[F, Double]] = {
    Ref.of[F, State[F]](State.Empty()).map { state =>
      new SdkObservableMeasurement[F, Double](state, descriptor, storages) {
        protected def create(
            value: Double,
            startTimestamp: FiniteDuration,
            collectTimestamp: FiniteDuration,
            attributes: Attributes
        ): Measurement =
          Measurement.DoubleMeasurement(
            startTimestamp,
            collectTimestamp,
            attributes,
            value
          )
      }
    }
  }

}
