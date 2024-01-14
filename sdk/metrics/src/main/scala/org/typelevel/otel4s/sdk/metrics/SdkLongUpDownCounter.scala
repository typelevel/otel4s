package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.SyncInstrumentBuilder
import org.typelevel.otel4s.metrics.UpDownCounter
import org.typelevel.otel4s.sdk.context.AskContext
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.metrics.internal.Advice
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentDescriptor
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentType
import org.typelevel.otel4s.sdk.metrics.internal.InstrumentValueType
import org.typelevel.otel4s.sdk.metrics.storage.MetricStorage

private final class SdkLongUpDownCounter[F[_]: Monad: AskContext](
    descriptor: InstrumentDescriptor,
    storage: MetricStorage.Writeable[F]
) extends UpDownCounter[F, Long] {

  val backend: UpDownCounter.Backend[F, Long] =
    new UpDownCounter.LongBackend[F] {
      def meta: InstrumentMeta[F] = InstrumentMeta.enabled

      def add(value: Long, attributes: Attribute[_]*): F[Unit] =
        for {
          ctx <- Ask[F, Context].ask
          _ <- storage.recordLong(
            value,
            Attributes.fromSpecific(attributes),
            ctx
          )
        } yield ()
    }

}

private object SdkLongUpDownCounter {

  final case class Builder[F[_]: Monad: Console: AskContext](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends SyncInstrumentBuilder[F, UpDownCounter[F, Long]] {

    type Self = SyncInstrumentBuilder[F, UpDownCounter[F, Long]]

    def withUnit(unit: String): Self =
      copy(unit = Some(unit))

    def withDescription(description: String): Self =
      copy(description = Some(description))

    def create: F[UpDownCounter[F, Long]] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.UpDownCounter,
        InstrumentValueType.Long,
        Advice.empty
      )

      for {
        storage <- sharedState.registerMetricStorage(descriptor)
      } yield new SdkLongUpDownCounter[F](descriptor, storage)
    }
  }

}
