package org.typelevel.otel4s.sdk.trace
package processor

import cats.Applicative
import cats.effect.Concurrent
import cats.effect.std.AtomicCell
import cats.syntax.functor._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

class SpanStorage[F[_]: Applicative](
    storage: AtomicCell[F, Map[SpanContext, SpanRef[F]]]
) extends SpanProcessor[F] {
  val name: String = "SpanStorage"

  def isStartRequired: Boolean = true
  def isEndRequired: Boolean = true

  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] =
    storage.update(_.updated(span.context, span))

  def onEnd(span: SpanData): F[Unit] =
    storage.update(_.removed(span.spanContext))

  def get(context: SpanContext): F[Option[SpanRef[F]]] =
    storage.get.map(_.get(context))

  def forceFlush: F[Unit] =
    Applicative[F].unit
}

object SpanStorage {
  def create[F[_]: Concurrent]: F[SpanStorage[F]] =
    for {
      storage <- AtomicCell[F].of(Map.empty[SpanContext, SpanRef[F]])
    } yield new SpanStorage[F](storage)
}
