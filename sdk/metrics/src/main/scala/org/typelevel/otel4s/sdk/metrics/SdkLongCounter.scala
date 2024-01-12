package org.typelevel.otel4s.sdk.metrics

import cats.Monad
import cats.effect.std.Console
import cats.mtl.Ask
import cats.syntax.functor._
import cats.syntax.flatMap._
import org.typelevel.otel4s.{Attribute, Attributes}
import org.typelevel.otel4s.meta.InstrumentMeta
import org.typelevel.otel4s.metrics.{Counter, SyncInstrumentBuilder}
import org.typelevel.otel4s.sdk.context.{AskContext, Context}
import org.typelevel.otel4s.sdk.metrics.internal.{Advice, InstrumentDescriptor, InstrumentType, InstrumentValueType}
import org.typelevel.otel4s.sdk.metrics.storage.{MetricStorage}

private final class SdkLongCounter[F[_]: Monad: Console: AskContext](
    descriptor: InstrumentDescriptor,
    storage: MetricStorage.Writeable[F]
) extends Counter[F, Long] {

  val backend: Counter.Backend[F, Long] =
    new Counter.LongBackend[F] {
      def meta: InstrumentMeta[F] = InstrumentMeta.enabled

      def add(value: Long, attributes: Attribute[_]*): F[Unit] =
        if (value < 0) {
          Console[F].println(
            s"Counters can only increase. Instrument ${descriptor.name} has tried to record a negative value."
          )
        } else {
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

}

private object SdkLongCounter {

  final case class Builder[F[_]: Monad: Console: AskContext](
      name: String,
      sharedState: MeterSharedState[F],
      unit: Option[String] = None,
      description: Option[String] = None
  ) extends SyncInstrumentBuilder[F, Counter[F, Long]] {

    type Self = SyncInstrumentBuilder[F, Counter[F, Long]]

    def withUnit(unit: String): Self =
      copy(unit = Some(unit))

    def withDescription(description: String): Self =
      copy(description = Some(description))

    def create: F[Counter[F, Long]] = {
      val descriptor = InstrumentDescriptor(
        name,
        unit,
        description,
        InstrumentType.Counter,
        InstrumentValueType.Long,
        Advice.empty
      )

      for {
        storage <- sharedState.registerMetricStorage(descriptor)
      } yield new SdkLongCounter[F](descriptor, storage)
    }
  }

}
