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

package org.typelevel.otel4s.sdk.metrics.exemplar

import cats.effect.Ref
import cats.effect.Temporal
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.context.TraceContext

import scala.concurrent.duration.FiniteDuration

/** A cell that stores exemplar data.
  */
private[exemplar] final class ReservoirCell[F[_]: Temporal, A] private (
    stateRef: Ref[F, Option[ReservoirCell.State[A]]],
    lookup: TraceContext.Lookup
) {

  /** Record the given `value` (measurement) to the cell.
    *
    * @param value
    *   the value to record
    *
    * @param attributes
    *   the attributes to associate with the value
    *
    * @param context
    *   the context to extract tracing information from
    */
  def record(value: A, attributes: Attributes, context: Context): F[Unit] =
    for {
      now <- Temporal[F].realTime
      ctx <- Temporal[F].pure(lookup.get(context))
      _ <- stateRef.set(Some(ReservoirCell.State(value, attributes, ctx, now)))
    } yield ()

  /** Retrieve the cell's exemplar and reset that state afterward.
    */
  def getAndReset(pointAttributes: Attributes): F[Option[Exemplar[A]]] =
    stateRef.getAndSet(None).map { state =>
      state.map { s =>
        val attrs = filtered(s.attributes, pointAttributes)
        Exemplar(attrs, s.recordTime, s.traceContext, s.value)
      }
    }

  private def filtered(
      original: Attributes,
      metricPoint: Attributes
  ): Attributes =
    if (metricPoint.isEmpty) original
    else original.filterNot(a => metricPoint.get(a.key).isDefined)
}

private[exemplar] object ReservoirCell {

  private final case class State[A](
      value: A,
      attributes: Attributes,
      traceContext: Option[TraceContext],
      recordTime: FiniteDuration
  )

  def create[F[_]: Temporal, A](
      lookup: TraceContext.Lookup
  ): F[ReservoirCell[F, A]] =
    for {
      stateRef <- Temporal[F].ref(Option.empty[State[A]])
    } yield new ReservoirCell(stateRef, lookup)

}
