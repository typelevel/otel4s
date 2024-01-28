package org.typelevel.otel4s.sdk.metrics.storage

import cats.Applicative
import cats.Monad
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeasurementValue
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.Aggregation
import org.typelevel.otel4s.sdk.metrics.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.RegisteredView
import org.typelevel.otel4s.sdk.metrics.data.AggregationTemporality
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.data.PointData
import org.typelevel.otel4s.sdk.metrics.internal.AttributesProcessor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.Measurement
import org.typelevel.otel4s.sdk.metrics.internal.MetricDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.aggregation.Aggregator

import scala.concurrent.duration.FiniteDuration

trait MetricStorage[F[_]] {
  def metricDescriptor: MetricDescriptor
  def collect(
      resource: TelemetryResource,
      scope: InstrumentationScope,
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[Option[MetricData]]
}

object MetricStorage {

  trait Writeable[F[_]] {
    def record[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): F[Unit]
  }

  object Writeable {
    def of[F[_]: Applicative](storages: Writeable[F]*): Writeable[F] =
      new Writeable[F] {
        def record[A: MeasurementValue](
            value: A,
            attributes: Attributes,
            context: Context
        ): F[Unit] =
          storages.traverse_(_.record(value, attributes, context))
      }
  }

  trait Synchronous[F[_]] extends MetricStorage[F] with Writeable[F]

  trait Asynchronous[F[_]] extends MetricStorage[F] {
    def record(measurement: Measurement): F[Unit] = ???

    def reader: RegisteredReader[F]
  }

  def synchronous[F[_]: Concurrent](
      reader: RegisteredReader[F],
      registeredView: RegisteredView,
      instrumentDescriptor: InstrumentDescriptor,
      exemplarFilter: ExemplarFilter
  ): F[Synchronous[F]] = {
    val view = registeredView.view
    val descriptor = MetricDescriptor(view, instrumentDescriptor)

    view.aggregation match {
      case Aggregation.Drop =>
        Concurrent[F].pure {
          new Synchronous[F] {
            def record[A: MeasurementValue](
                value: A,
                attributes: Attributes,
                context: Context
            ): F[Unit] =
              Concurrent[F].unit

            def metricDescriptor: MetricDescriptor =
              descriptor // todo should be noop

            def collect(
                resource: TelemetryResource,
                scope: InstrumentationScope,
                startTimestamp: FiniteDuration,
                collectTimestamp: FiniteDuration
            ): F[Option[MetricData]] =
              Concurrent[F].pure(None)
          }
        }

      case aggregation: Aggregation.HasAggregator =>
        val aggregator: Aggregator[F] =
          Aggregator.create(
            aggregation,
            instrumentDescriptor,
            exemplarFilter
          )

        AtomicCell[F]
          .of(Map.empty[Attributes, Aggregator.Handle[F, PointData]])
          .map { handlers =>
            new DefaultSynchronous(
              reader,
              descriptor,
              aggregator.asInstanceOf[Aggregator.Aux[F, PointData]],
              registeredView.viewAttributesProcessor,
              registeredView.cardinalityLimit - 1,
              handlers
            )
          }
    }
  }

  def asynchronous[F[_]](
      reader: RegisteredReader[F],
      registeredView: RegisteredView,
      descriptor: InstrumentDescriptor
  ): F[Asynchronous[F]] = ???

  private final class DefaultSynchronous[F[_]: Monad](
      reader: RegisteredReader[F],
      val metricDescriptor: MetricDescriptor,
      aggregator: Aggregator.Aux[F, PointData],
      attributesProcessor: AttributesProcessor,
      maxCardinality: Int,
      handlers: AtomicCell[F, Map[Attributes, Aggregator.Handle[F, PointData]]]
  ) extends Synchronous[F] {

    private val aggregationTemporality =
      reader.reader.aggregationTemporality(
        metricDescriptor.sourceInstrument.instrumentType
      )

    def record[A: MeasurementValue](
        value: A,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      for {
        handle <- getHandle(attributes, context)
        _ <- handle.record(value, attributes, context)
      } yield ()

    def collect(
        resource: TelemetryResource,
        scope: InstrumentationScope,
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration
    ): F[Option[MetricData]] = {
      val isDelta = aggregationTemporality == AggregationTemporality.Delta
      val reset = isDelta
      val getStart =
        if (isDelta) reader.getLastCollectTimestamp
        else Monad[F].pure(startTimestamp)

      val getHandlers =
        if (reset) handlers.getAndSet(Map.empty) else handlers.get

      for {
        start <- getStart
        handlers <- getHandlers
        points <- handlers.toVector.traverse { case (attributes, handle) =>
          handle.aggregate(start, collectTimestamp, attributes, reset)
        }
        data <- aggregator.toMetricData(
          resource,
          scope,
          metricDescriptor,
          points.flatten,
          aggregationTemporality
        )
      } yield Some(data)
    }

    private def getHandle(
        attributes: Attributes,
        context: Context
    ): F[Aggregator.Handle[F, PointData]] =
      handlers.evalModify { map =>
        val attrs = attributesProcessor.process(attributes, context)
        map.get(attrs) match {
          case Some(handle) =>
            Monad[F].pure((map, handle))

          case None =>
            // todo: check cardinality
            for {
              handle <- aggregator.createHandle
            } yield (map.updated(attrs, handle), handle)
        }
      }

  }

  /*private final class AggregatorHolder[F[_], A <: PointData, E <: ExemplarData](
      handles: Ref[F, Map[Attributes, AggregatorHandle[F, A, E]]]
  ) {
    val activeRecordingThreads =
  }*/
}
