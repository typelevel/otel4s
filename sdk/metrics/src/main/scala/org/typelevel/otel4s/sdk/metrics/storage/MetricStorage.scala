package org.typelevel.otel4s.sdk.metrics.storage

import cats.{Applicative, Monad}
import cats.effect.Ref
import cats.effect.kernel.Concurrent
import cats.syntax.foldable._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.Resource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.{
  ExemplarFilter,
  RegisteredReader,
  RegisteredView
}
import org.typelevel.otel4s.sdk.metrics.data.{
  AggregationTemporality,
  ExemplarData,
  MetricData,
  PointData
}
import org.typelevel.otel4s.sdk.metrics.internal.Aggregator.AggregatorHandle
import org.typelevel.otel4s.sdk.metrics.internal.{
  Aggregator,
  AttributesProcessor,
  InstrumentDescriptor,
  Measurement,
  MetricDescriptor
}

import scala.concurrent.duration.FiniteDuration

trait MetricStorage[F[_]] {
  def metricDescriptor: MetricDescriptor
  def collect(
      resource: Resource,
      scope: InstrumentationScope,
      startTimestamp: FiniteDuration,
      collectTimestamp: FiniteDuration
  ): F[MetricData]
}

object MetricStorage {

  trait Writeable[F[_]] {
    def recordLong(
        value: Long,
        attributes: Attributes,
        context: Context
    ): F[Unit]

    def recordDouble(
        value: Double,
        attributes: Attributes,
        context: Context
    ): F[Unit]
  }

  object Writeable {
    def of[F[_]: Applicative](storages: Writeable[F]*): Writeable[F] =
      new Writeable[F] {
        def recordLong(
            value: Long,
            attributes: Attributes,
            context: Context
        ): F[Unit] =
          storages.traverse_(_.recordLong(value, attributes, context))

        def recordDouble(
            value: Double,
            attributes: Attributes,
            context: Context
        ): F[Unit] =
          storages.traverse_(_.recordDouble(value, attributes, context))
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
    val aggregator: Aggregator.Aux[F] =
      view.aggregation.createAggregator(instrumentDescriptor, exemplarFilter)
    // todo if aggregator == drop

    Ref
      .of(Map.empty[Attributes, AggregatorHandle[F, PointData, ExemplarData]])
      .map { handlers =>
        new DefaultSynchronous(
          reader,
          descriptor,
          aggregator,
          registeredView.viewAttributesProcessor,
          registeredView.cardinalityLimit - 1,
          handlers
        )
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
      aggregator: Aggregator.Aux[F],
      attributesProcessor: AttributesProcessor,
      maxCardinality: Int,
      handlers: Ref[F, Map[Attributes, AggregatorHandle[F, PointData, ExemplarData]]]
  ) extends Synchronous[F] {

    private val aggregationTemporality =
      reader.reader.aggregationTemporality(
        metricDescriptor.sourceInstrument.instrumentType
      )

    def recordLong(
        value: Long,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      for {
        handle <- getHandle(attributes, context)
        result <- handle.recordLong(value, attributes, context)
      } yield result

    def recordDouble(
        value: Double,
        attributes: Attributes,
        context: Context
    ): F[Unit] =
      for {
        handle <- getHandle(attributes, context)
        result <- handle.recordDouble(value, attributes, context)
      } yield result

    def collect(
        resource: Resource,
        scope: InstrumentationScope,
        startTimestamp: FiniteDuration,
        collectTimestamp: FiniteDuration
    ): F[MetricData] = {
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
          points.flatten.asInstanceOf[Vector[PointData]],
          aggregationTemporality
        )
      } yield data
    }

    private def getHandle(attributes: Attributes, context: Context): F[AggregatorHandle[F, PointData, ExemplarData]] =
      handlers.modify { map =>
        val attrs = attributesProcessor.process(attributes, context)
        map.get(attrs) match {
          case Some(handle) =>
            (map, handle)

          case None =>
            // todo: check cardinality

            val newHandle = aggregator.createHandle.asInstanceOf[AggregatorHandle[F, PointData, ExemplarData]]
            (map.updated(attrs, newHandle), newHandle)
        }
      }

  }

  /*private final class AggregatorHolder[F[_], A <: PointData, E <: ExemplarData](
      handles: Ref[F, Map[Attributes, AggregatorHandle[F, A, E]]]
  ) {
    val activeRecordingThreads =
  }*/
}
