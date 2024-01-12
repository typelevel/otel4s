package org.typelevel.otel4s.sdk.metrics.exporter

import cats.effect.kernel.{Concurrent, Fiber, Ref, Resource, Spawn, Temporal}
import cats.effect.std.{AtomicCell, Console}
import cats.effect.syntax.spawn._
import cats.effect.syntax.temporal._
import cats.syntax.applicative._
import cats.syntax.applicativeError._
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

import scala.concurrent.duration.FiniteDuration

class PeriodicMetricReader[F[_]: Temporal: Console](
                                                     stateRef: AtomicCell[F, PeriodicMetricReader.State[F]],
                                                     exporter: MetricExporter[F],
                                                     interval: FiniteDuration
) extends MetricReader[F] {
  import PeriodicMetricReader._

  def defaultAggregationSelector: DefaultAggregationSelector =
    new DefaultAggregationSelector {
      def get(instrumentType: InstrumentType): Aggregation =
        Aggregation.default
    }

  def aggregationTemporality(
      instrumentType: InstrumentType
  ): AggregationTemporality =
    exporter.aggregationTemporality(instrumentType)

  def register(registration: CollectionRegistration[F]): F[Unit] =
    stateRef.evalUpdate {
      case State.Idle() =>
        process(registration).start.map(fiber => State.Running(fiber))

      case state: State.Running[F] =>
        Spawn[F].pure(state)
    }

  private def process(registration: CollectionRegistration[F]): F[Unit] = {
    val doExport =
      for {
        data <- registration.collectAllMetrics
        _ <- exporter.exportMetrics(data).whenA(data.nonEmpty)
      } yield ()

    val step = doExport.handleErrorWith { e =>
      Console[F].error(
        s"PeriodicMetricExporter: the export has failed: ${e.getMessage}\n${e.getStackTrace.mkString("\n")}\n"
      )
    }

    step.delayBy(interval).foreverM
  }

  def forceFlush: F[Unit] = ???
}

object PeriodicMetricReader {

  private sealed trait State[F[_]]
  private object State {
    final case class Idle[F[_]]() extends State[F]

    final case class Running[F[_]](
        process: Fiber[F, Throwable, Unit]
    ) extends State[F]
  }

  def create[F[_]: Temporal: Console](
      exporter: MetricExporter[F],
      interval: FiniteDuration
  ): Resource[F, PeriodicMetricReader[F]] =
    for {
      ref <- Resource.eval(AtomicCell[F].of[State[F]](State.Idle()))
      _ <- Resource.make(Concurrent[F].unit)(_ =>
        ref.get.flatMap {
          case State.Idle()           => Concurrent[F].unit
          case State.Running(process) => process.cancel
        }
      )
    } yield new PeriodicMetricReader(ref, exporter, interval)

}
