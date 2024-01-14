package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.Clock
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.metrics.Histogram
import org.typelevel.otel4s.metrics.Histogram.Meta
import org.typelevel.otel4s.metrics.SyncInstrumentBuilder
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

private final class SdkDoubleHistogram[F[_]: Monad: Clock: Console: AskContext](
    descriptor: InstrumentDescriptor,
    storage: MetricStorage.Writeable[F]
) extends Histogram[F, Double] {

  val backend: Histogram.Backend[F, Double] =
    new Histogram.DoubleBackend[F] {
      def meta: Meta[F] = Meta.enabled

      def record(value: Double, attributes: Attribute[_]*): F[Unit] =
        if (value < 0) {
          Console[F].println(
            s"Histograms can only record non-negative values. Instrument ${descriptor.name} has tried to record a negative value."
          )
        } else {
          for {
            ctx <- Ask[F, Context].ask
            _ <- storage.recordDouble(
              value,
              Attributes.fromSpecific(attributes),
              ctx
            )
          } yield ()
        }

    }

}

private object SdkDoubleHistogram {

  // todo: setExplicitBucketBoundaries
  final case class Builder[F[_]: Monad: Clock: Console: AskContext](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends SyncInstrumentBuilder[F, Histogram[F, Double]] {

    type Self = SyncInstrumentBuilder[F, Histogram[F, Double]]

    def withUnit(unit: String): Self =
      copy(unit = Some(unit))

    def withDescription(description: String): Self =
      copy(description = Some(description))

    def create: F[Histogram[F, Double]] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.Histogram,
        InstrumentValueType.Double,
        Advice.empty
      )

      for {
        storage <- sharedState.registerMetricStorage(descriptor)
      } yield new SdkDoubleHistogram[F](descriptor, storage)
    }
  }

}
