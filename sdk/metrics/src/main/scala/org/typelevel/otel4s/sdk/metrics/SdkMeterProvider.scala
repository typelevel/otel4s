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

import cats.Applicative
import cats.Monad
import cats.data.NonEmptyVector
import cats.effect.Clock
import cats.effect.Temporal
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.metrics.MeterBuilder
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.Diagnostic
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.TraceContext
import org.typelevel.otel4s.sdk.internal.ComponentRegistry
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter
import org.typelevel.otel4s.sdk.metrics.exemplar.Reservoirs
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader
import org.typelevel.otel4s.sdk.metrics.internal.MeterSharedState
import org.typelevel.otel4s.sdk.metrics.internal.exporter.RegisteredReader
import org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector
import org.typelevel.otel4s.sdk.metrics.view.RegisteredView
import org.typelevel.otel4s.sdk.metrics.view.View
import org.typelevel.otel4s.sdk.metrics.view.ViewRegistry

import scala.concurrent.duration.FiniteDuration

private final class SdkMeterProvider[F[_]: Applicative](
    componentRegistry: ComponentRegistry[F, SdkMeter[F]],
    resource: TelemetryResource,
    views: Vector[RegisteredView],
    readers: Vector[RegisteredReader[F]],
    producers: Vector[MetricProducer[F]]
) extends MeterProvider.Unsealed[F] {
  import SdkMeterProvider.DefaultMeterName

  def meter(name: String): MeterBuilder[F] =
    if (readers.isEmpty) {
      MeterBuilder.noop[F]
    } else {
      val meterName = if (name.trim.isEmpty) DefaultMeterName else name
      SdkMeterBuilder(componentRegistry, meterName)
    }

  override def toString: String =
    "SdkMeterProvider{" +
      s"resource=$resource, " +
      s"metricReaders=${readers.map(_.reader).mkString("[", ", ", "]")}, " +
      s"metricProducers=${producers.mkString("[", ", ", "]")}, " +
      s"views=${views.mkString("[", ", ", "]")}" +
      "}"

}

object SdkMeterProvider {

  private val DefaultMeterName = "unknown"

  /** Builder for [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]].
    */
  sealed trait Builder[F[_]] {

    /** Sets a [[TelemetryResource]] to be attached to all metrics created by
      * [[org.typelevel.otel4s.metrics.Meter Meter]].
      *
      * @note
      *   on multiple subsequent calls, the resource from the last call will be retained.
      *
      * @param resource
      *   the [[TelemetryResource]] to use
      */
    def withResource(resource: TelemetryResource): Builder[F]

    /** Merges the given [[TelemetryResource]] with the current one.
      *
      * @note
      *   if both resources have different non-empty `schemaUrl`, the merge will fail.
      *
      * @see
      *   [[TelemetryResource.mergeUnsafe]]
      *
      * @param resource
      *   the [[TelemetryResource]] to merge the current one with
      */
    def addResource(resource: TelemetryResource): Builder[F]

    /** Sets an [[org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter ExemplarFilter]] to be used by all metrics.
      *
      * @param filter
      *   the [[org.typelevel.otel4s.sdk.metrics.exemplar.ExemplarFilter ExemplarFilter]] to register
      */
    private[sdk] def withExemplarFilter(filter: ExemplarFilter): Builder[F]

    /** Sets a [[org.typelevel.otel4s.sdk.context.TraceContext.Lookup TraceContext.Lookup]] to be used by exemplars.
      *
      * @param lookup
      *   the [[org.typelevel.otel4s.sdk.context.TraceContext.Lookup TraceContext.Lookup]] to use
      */
    private[sdk] def withTraceContextLookup(
        lookup: TraceContext.Lookup
    ): Builder[F]

    /** Registers a [[org.typelevel.otel4s.sdk.metrics.view.View View]] for the given
      * [[org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector InstrumentSelector]].
      *
      * `View` affects aggregation and export of the instruments that match the given `selector`.
      *
      * @param selector
      *   the [[org.typelevel.otel4s.sdk.metrics.view.InstrumentSelector InstrumentSelector]] to filter instruments with
      *
      * @param view
      *   the [[org.typelevel.otel4s.sdk.metrics.view.View View]] to register
      */
    def registerView(selector: InstrumentSelector, view: View): Builder[F]

    /** Registers a [[org.typelevel.otel4s.sdk.metrics.exporter.MetricReader MetricReader]].
      *
      * @param reader
      *   the [[org.typelevel.otel4s.sdk.metrics.exporter.MetricReader MetricReader]] to register
      */
    def registerMetricReader(reader: MetricReader[F]): Builder[F]

    /** Registers a [[org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer MetricProducer]].
      *
      * @param producer
      *   the [[org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer MetricProducer]] to register
      */
    def registerMetricProducer(producer: MetricProducer[F]): Builder[F]

    /** Creates [[org.typelevel.otel4s.metrics.MeterProvider MeterProvider]] with the configuration of this builder.
      */
    def build: F[MeterProvider[F]]
  }

  /** Creates a new [[Builder]] with default configuration.
    */
  def builder[F[_]: Temporal: Random: Diagnostic: AskContext]: Builder[F] =
    BuilderImpl(
      resource = TelemetryResource.default,
      exemplarFilter = None,
      traceContextLookup = TraceContext.Lookup.noop,
      registeredViews = Vector.empty,
      metricReaders = Vector.empty,
      metricProducers = Vector.empty
    )

  private final case class BuilderImpl[
      F[_]: Temporal: Random: Diagnostic: AskContext
  ](
      resource: TelemetryResource,
      exemplarFilter: Option[ExemplarFilter],
      traceContextLookup: TraceContext.Lookup,
      registeredViews: Vector[RegisteredView],
      metricReaders: Vector[MetricReader[F]],
      metricProducers: Vector[MetricProducer[F]]
  ) extends Builder[F] {

    def withResource(resource: TelemetryResource): Builder[F] =
      copy(resource = resource)

    def addResource(resource: TelemetryResource): Builder[F] =
      copy(resource = this.resource.mergeUnsafe(resource))

    def withExemplarFilter(filter: ExemplarFilter): Builder[F] =
      copy(exemplarFilter = Some(filter))

    def withTraceContextLookup(lookup: TraceContext.Lookup): Builder[F] =
      copy(traceContextLookup = lookup)

    def registerView(selector: InstrumentSelector, view: View): Builder[F] =
      copy(registeredViews = registeredViews :+ RegisteredView(selector, view))

    def registerMetricReader(reader: MetricReader[F]): Builder[F] =
      copy(metricReaders = metricReaders :+ reader)

    def registerMetricProducer(producer: MetricProducer[F]): Builder[F] =
      copy(metricProducers = metricProducers :+ producer)

    def build: F[MeterProvider[F]] =
      if (metricReaders.isEmpty) Monad[F].pure(MeterProvider.noop)
      else create

    private def create: F[MeterProvider[F]] = {
      def createMeter(
          startTimestamp: FiniteDuration,
          scope: InstrumentationScope,
          readers: Vector[RegisteredReader[F]]
      ): F[SdkMeter[F]] = {
        val viewRegistry = ViewRegistry(registeredViews)

        val filter = exemplarFilter.getOrElse(
          ExemplarFilter.traceBased(traceContextLookup)
        )

        val reservoirs = Reservoirs(filter, traceContextLookup)

        for {
          state <- MeterSharedState.create(
            resource,
            scope,
            startTimestamp,
            reservoirs,
            viewRegistry,
            readers
          )
        } yield new SdkMeter[F](state)
      }

      def configureReader(
          registry: ComponentRegistry[F, SdkMeter[F]],
          reader: RegisteredReader[F]
      ): F[Unit] = {
        val sdkMetricProducer = new SdkMetricProducer[F](
          registry,
          reader
        )

        reader.reader.register(
          NonEmptyVector(sdkMetricProducer, metricProducers)
        )
      }

      for {
        now <- Clock[F].realTime
        readers <- metricReaders.traverse(r => RegisteredReader.create(now, r))
        registry <- ComponentRegistry.create(s => createMeter(now, s, readers))
        _ <- readers.traverse_(reader => configureReader(registry, reader))
      } yield new SdkMeterProvider(
        registry,
        resource,
        registeredViews,
        readers,
        metricProducers
      )
    }
  }

  private final class SdkMetricProducer[F[_]: Monad: Clock](
      registry: ComponentRegistry[F, SdkMeter[F]],
      reader: RegisteredReader[F]
  ) extends MetricProducer.Unsealed[F] {
    def produce: F[Vector[MetricData]] =
      for {
        meters <- registry.components
        now <- Clock[F].realTime
        result <- meters.flatTraverse(_.collectAll(reader, now))
        _ <- reader.setLastCollectTimestamp(now)
      } yield result
  }

}
