package org.typelevel.otel4s.sdk.metrics

import cats.Applicative
import cats.Functor
import cats.Monad
import cats.effect.Clock
import cats.effect.Concurrent
import cats.effect.Ref
import cats.effect.Temporal
import cats.effect.std.Console
import cats.syntax.flatMap._
import cats.syntax.foldable._
import cats.syntax.functor._
import cats.syntax.traverse._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.MeterBuilder
import org.typelevel.otel4s.metrics.MeterProvider
import org.typelevel.otel4s.sdk.TelemetryResource
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.internal.ComponentRegistry
import org.typelevel.otel4s.sdk.metrics.data.MetricData
import org.typelevel.otel4s.sdk.metrics.exporter.CollectionRegistration
import org.typelevel.otel4s.sdk.metrics.exporter.DefaultAggregationSelector
import org.typelevel.otel4s.sdk.metrics.exporter.MetricProducer
import org.typelevel.otel4s.sdk.metrics.exporter.MetricReader
import org.typelevel.otel4s.sdk.metrics.internal.AttributesProcessor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType

import scala.concurrent.duration.Duration
import scala.concurrent.duration.FiniteDuration

private final class SdkMeterProvider[F[_]: Functor](
    componentRegistry: ComponentRegistry[F, SdkMeter[F]],
    views: Vector[RegisteredView],
    readers: Vector[RegisteredReader[F]],
    producers: Vector[MetricProducer[F]]
) extends MeterProvider[F] {

  def meter(name: String): MeterBuilder[F] =
    SdkMeterBuilder(componentRegistry, name)
}

trait RegisteredView {
  def selector: InstrumentSelector
  def view: View
  def viewAttributesProcessor: AttributesProcessor
  def cardinalityLimit: Int
}

object RegisteredView {
  def apply(selector: InstrumentSelector, view: View): RegisteredView = ???

  def apply(
      selector: InstrumentSelector,
      view: View,
      attributesProcessor: AttributesProcessor,
      cardinalityLimit: Int
  ): RegisteredView =
    Impl(selector, view, attributesProcessor, cardinalityLimit)

  private final case class Impl(
      selector: InstrumentSelector,
      view: View,
      viewAttributesProcessor: AttributesProcessor,
      cardinalityLimit: Int
  ) extends RegisteredView

}

trait RegisteredReader[F[_]] {
  def reader: MetricReader[F]
  def viewRegistry: ViewRegistry
  def getLastCollectTimestamp: F[FiniteDuration]
  def setLastCollectTimestamp(timestamp: FiniteDuration): F[Unit]
}

object RegisteredReader {
  def create[F[_]: Concurrent](
      reader: MetricReader[F],
      viewRegistry: ViewRegistry
  ): F[RegisteredReader[F]] =
    Concurrent[F].ref(Duration.Zero).map { ref =>
      new Impl(ref, reader, viewRegistry)
    }

  private final class Impl[F[_]](
      lastCollectTimestamp: Ref[F, FiniteDuration],
      val reader: MetricReader[F],
      val viewRegistry: ViewRegistry
  ) extends RegisteredReader[F] {

    def getLastCollectTimestamp: F[FiniteDuration] =
      lastCollectTimestamp.get

    def setLastCollectTimestamp(timestamp: FiniteDuration): F[Unit] =
      lastCollectTimestamp.set(timestamp)
  }
}

trait ViewRegistry {
  def findViews(
      descriptor: InstrumentDescriptor,
      scope: InstrumentationScope
  ): Vector[RegisteredView]
}

object ViewRegistry {
  def apply(
      defaultAggregationSelector: DefaultAggregationSelector,
      cardinalityLimitSelector: CardinalityLimitSelector,
      registeredViews: Vector[RegisteredView]
  ): ViewRegistry = {
    val defaultViews = InstrumentType.values.map { tpe =>
      tpe -> RegisteredView(
        InstrumentSelector.builder.withName("*").build,
        View.builder.withAggregation(defaultAggregationSelector.get(tpe)).build,
        AttributesProcessor.noop,
        cardinalityLimitSelector.limit(tpe)
      )
    }

    new Impl(defaultViews.toMap, registeredViews)
  }

  private final class Impl(
      defaultViews: Map[InstrumentType, RegisteredView],
      registeredViews: Vector[RegisteredView]
  ) extends ViewRegistry {
    def findViews(
        descriptor: InstrumentDescriptor,
        scope: InstrumentationScope
    ): Vector[RegisteredView] = {
      val result = registeredViews.filter { entry =>
        matchesSelector(entry.selector, descriptor, scope) &&
        entry.view.aggregation.compatibleWith(descriptor.instrumentType)
      }

      if (result.nonEmpty) {
        result
      } else {
        val defaultView = defaultViews(descriptor.instrumentType)
        // val aggregation = defaultView.view.aggregation

        // todo  if (!aggregation.compatibleWith(descriptor))
        // todo: applyAdviceToDefaultAttribute
        // descriptor.advice.h
        Vector(defaultView)
      }
    }

    private def matchesSelector(
        selector: InstrumentSelector,
        descriptor: InstrumentDescriptor,
        scope: InstrumentationScope
    ): Boolean = ???
  }
}

trait ExemplarFilter {
  def shouldSample(value: Long, attributes: Attributes, context: Context): Boolean
  def shouldSample(value: Double, attributes: Attributes, context: Context): Boolean

}

object ExemplarFilter {
  def traceBased: ExemplarFilter = new ExemplarFilter {
    def shouldSample(value: Long, attributes: Attributes, context: Context): Boolean =
      true

    def shouldSample(value: Double, attributes: Attributes, context: Context): Boolean =
      true
  }
}
sealed trait InstrumentSelector {
  def instrumentType: Option[InstrumentType]
  def instrumentName: Option[String]
  def instrumentUnit: Option[String]
  def meterName: Option[String]
  def meterVersion: Option[String]
  def meterSchemaUrl: Option[String]
}

object InstrumentSelector {

  sealed trait Builder {
    def withType(tpe: InstrumentType): Builder
    def withName(name: String): Builder
    def withUnit(unit: String): Builder
    def withMeterName(name: String): Builder
    def withMeterVersion(version: String): Builder
    def withMeterSchemaUrl(schemaUrl: String): Builder
    def build: InstrumentSelector
  }

  def builder: Builder =
    BuilderImpl()

  private final case class BuilderImpl(
      instrumentType: Option[InstrumentType] = None,
      instrumentName: Option[String] = None,
      instrumentUnit: Option[String] = None,
      meterName: Option[String] = None,
      meterVersion: Option[String] = None,
      meterSchemaUrl: Option[String] = None
  ) extends Builder
      with InstrumentSelector {

    def withType(tpe: InstrumentType): Builder =
      copy(instrumentType = Some(tpe))

    def withName(name: String): Builder =
      copy(instrumentName = Some(name))

    def withUnit(unit: String): Builder =
      copy(instrumentUnit = Some(unit))

    def withMeterName(name: String): Builder =
      copy(meterName = Some(name))

    def withMeterVersion(version: String): Builder =
      copy(meterVersion = Some(version))

    def withMeterSchemaUrl(schemaUrl: String): Builder =
      copy(meterSchemaUrl = Some(schemaUrl))

    def build: InstrumentSelector = {
      require(
        instrumentType.isDefined || instrumentName.isDefined ||
          instrumentUnit.isDefined || meterName.isDefined ||
          meterVersion.isDefined || meterSchemaUrl.isDefined,
        "at least one criteria must be defined"
      )

      this
    }
  }

}

sealed trait View {
  def name: Option[String]
  def description: Option[String]
  def aggregation: Aggregation
  def attributesProcessor: AttributesProcessor
  def cardinalityLimit: Int
}

object View {

  sealed trait Builder {
    def withName(name: String): Builder
    def withDescription(description: String): Builder
    def withAggregation(aggregation: Aggregation): Builder
    def withAttributeFilter(retain: Set[String]): Builder
    def withAttributeFilter(filter: String => Boolean): Builder
    def withCardinalityLimit(limit: Int): Builder
    def addAttributesProcessor(processor: AttributesProcessor): Builder
    def build: View
  }

  def builder: Builder =
    BuilderImpl()

  private final case class ViewImpl(
      name: Option[String],
      description: Option[String],
      aggregation: Aggregation,
      attributesProcessor: AttributesProcessor,
      cardinalityLimit: Int
  ) extends View

  private final case class BuilderImpl(
      name: Option[String] = None,
      description: Option[String] = None,
      aggregation: Option[Aggregation] = None,
      cardinalityLimit: Option[Int] = None,
      attributesProcessors: List[AttributesProcessor] = Nil
  ) extends Builder {

    def withName(name: String): Builder = copy(name = Some(name))

    def withDescription(description: String): Builder =
      copy(description = Some(description))

    def withAggregation(aggregation: Aggregation): Builder =
      copy(aggregation = Some(aggregation))

    def withAttributeFilter(retain: Set[String]): Builder =
      copy(attributesProcessors =
        List(AttributesProcessor.filterByKeyName(retain.contains))
      )

    def withAttributeFilter(filter: String => Boolean): Builder =
      copy(attributesProcessors =
        List(AttributesProcessor.filterByKeyName(filter))
      )

    def withCardinalityLimit(limit: Int): Builder =
      copy(cardinalityLimit = Some(limit))

    def addAttributesProcessor(processor: AttributesProcessor): Builder =
      copy(attributesProcessors = attributesProcessors :+ processor)

    def build: View = {
      val attributesProcessor =
        new AttributesProcessor {
          def process(incoming: Attributes, context: Context): Attributes =
            attributesProcessors.foldLeft(incoming) { (attrs, processor) =>
              processor.process(attrs, context)
            }
        }

      ViewImpl(
        name,
        description,
        aggregation.getOrElse(Aggregation.default),
        attributesProcessor,
        cardinalityLimit.getOrElse(2000)
      )
    }
  }

}

trait CardinalityLimitSelector {
  def limit(instrumentType: InstrumentType): Int
}

object CardinalityLimitSelector {
  private object Default extends CardinalityLimitSelector {
    def limit(instrumentType: InstrumentType): Int = 2000
  }

  def default: CardinalityLimitSelector = Default

}

object SdkMeterProvider {

  private[metrics] final case class Config(
      resource: TelemetryResource,
      exemplarFilter: ExemplarFilter
  )

  sealed trait Builder[F[_]] {

    /** Sets a [[TelemetryResource]] to be attached to all spans created by
      * [[org.typelevel.otel4s.trace.Tracer Tracer]].
      *
      * @note
      *   on multiple subsequent calls, the resource from the last call will be
      *   retained.
      *
      * @param resource
      *   the [[TelemetryResource]] to use
      */
    def withResource(resource: TelemetryResource): Builder[F]

    /** Merges the given [[TelemetryResource]] with the current one.
      *
      * @note
      *   if both resources have different non-empty `schemaUrl`, the merge will
      *   fail.
      *
      * @see
      *   [[TelemetryResource.mergeUnsafe]]
      *
      * @param resource
      *   the [[TelemetryResource]] to merge the current one with
      */
    def addResource(resource: TelemetryResource): Builder[F]

    def withExemplarFilter(filter: ExemplarFilter): Builder[F]

    def registerView(selector: InstrumentSelector, view: View): Builder[F]
    def registerMetricReader(reader: MetricReader[F]): Builder[F]
    def registerMetricReader(
        reader: MetricReader[F],
        selector: CardinalityLimitSelector
    ): Builder[F]
    def registerMetricProducer(producer: MetricProducer[F]): Builder[F]

    def build: F[MeterProvider[F]]
  }

  def builder[F[_]: Temporal: Console: AskContext]: Builder[F] =
    BuilderImpl(
      resource = TelemetryResource.default,
      exemplarFilter = ExemplarFilter.traceBased,
      registeredViews = Vector.empty,
      metricReaders = Map.empty,
      metricProducers = Vector.empty
    )

  private final case class BuilderImpl[F[_]: Temporal: Console: AskContext](
      resource: TelemetryResource,
      exemplarFilter: ExemplarFilter,
      registeredViews: Vector[RegisteredView],
      metricReaders: Map[MetricReader[F], CardinalityLimitSelector],
      metricProducers: Vector[MetricProducer[F]]
  ) extends Builder[F] {

    def withResource(resource: TelemetryResource): Builder[F] =
      copy(resource = resource)

    def addResource(resource: TelemetryResource): Builder[F] =
      copy(resource = this.resource.mergeUnsafe(resource))

    def withExemplarFilter(filter: ExemplarFilter): Builder[F] =
      copy(exemplarFilter = filter)

    def registerView(selector: InstrumentSelector, view: View): Builder[F] =
      copy(registeredViews = registeredViews :+ RegisteredView(selector, view))

    def registerMetricReader(reader: MetricReader[F]): Builder[F] =
      copy(metricReaders =
        metricReaders.updated(reader, CardinalityLimitSelector.default)
      )

    def registerMetricReader(
        reader: MetricReader[F],
        selector: CardinalityLimitSelector
    ): Builder[F] =
      copy(metricReaders = metricReaders.updated(reader, selector))

    def registerMetricProducer(producer: MetricProducer[F]): Builder[F] =
      copy(metricProducers = metricProducers :+ producer)

    def build: F[MeterProvider[F]] = {
      if (metricReaders.isEmpty) {
        Monad[F].pure(MeterProvider.noop)
      } else
        Clock[F].realTime.flatMap { now =>
          metricReaders.toVector
            .traverse { case (reader, limit) =>
              val registry = ViewRegistry(
                reader.defaultAggregationSelector,
                limit,
                registeredViews
              )

              RegisteredReader.create(reader, registry)
            }
            .flatMap { readers =>
              val config = Config(resource, exemplarFilter)

              ComponentRegistry
                .create { scope =>
                  for {
                    state <- MeterSharedState.create(
                      resource,
                      scope,
                      now,
                      exemplarFilter,
                      readers
                    )
                  } yield new SdkMeter[F](state)
                }
                .flatMap { registry =>
                  readers
                    .traverse_ { reader =>
                      val producers =
                        metricProducers :+ new LeasedMetricProducer[F](
                          registry,
                          reader
                        )

                      for {
                        _ <- reader.reader.register(
                          new SdkCollectionRegistration[F](producers, resource)
                        )
                        _ <- reader.setLastCollectTimestamp(now)
                      } yield ()
                    }
                    .map { _ =>
                      new SdkMeterProvider(
                        registry,
                        registeredViews,
                        readers,
                        metricProducers
                      )
                    }
                }

            }

        }
    }
  }

  private final class LeasedMetricProducer[F[_]: Monad: Clock](
      registry: ComponentRegistry[F, SdkMeter[F]],
      reader: RegisteredReader[F]
  ) extends MetricProducer[F] {
    def produce(resource: TelemetryResource): F[Vector[MetricData]] =
      registry.components.flatMap { meters =>
        Clock[F].realTime.flatMap { now =>
          for {
            result <- meters.flatTraverse(_.collectAll(reader, now))
            _ <- reader.setLastCollectTimestamp(now)
          } yield result
        }
      }
  }

  private final class SdkCollectionRegistration[F[_]: Applicative](
      producers: Vector[MetricProducer[F]],
      resource: TelemetryResource
  ) extends CollectionRegistration[F] {
    def collectAllMetrics: F[Vector[MetricData]] =
      producers.flatTraverse(producer => producer.produce(resource))
  }

}
