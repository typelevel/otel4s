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

import cats.Applicative
import cats.effect.Concurrent
import cats.effect.std.Random
import cats.syntax.flatMap._
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.sdk.metrics.internal.utils.Adder

/** Selects which [[ReservoirCell]] within the [[ExemplarReservoir]] should receive the measurements.
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  *
  * @tparam A
  *   the type of the values to record
  */
private[exemplar] trait CellSelector[F[_], A] {

  /** Returns the [[ReservoirCell]] that should record the given value.
    */
  def select(
      cells: Vector[ReservoirCell[F, A]],
      value: A,
  ): F[Option[ReservoirCell[F, A]]]

  /** Resets the internal state.
    */
  def reset: F[Unit]
}

private[exemplar] object CellSelector {

  def histogramBucket[F[_]: Applicative, A: Numeric](
      boundaries: BucketBoundaries
  ): CellSelector[F, A] =
    new CellSelector[F, A] {
      def select(
          cells: Vector[ReservoirCell[F, A]],
          value: A
      ): F[Option[ReservoirCell[F, A]]] =
        Applicative[F].pure(cells.lift(bucketIndex(Numeric[A].toDouble(value))))

      def reset: F[Unit] = Applicative[F].unit

      private def bucketIndex(value: Double): Int = {
        val idx = boundaries.boundaries.indexWhere(b => value <= b)
        if (idx == -1) boundaries.length else idx
      }
    }

  def random[F[_]: Concurrent: Random, A]: F[CellSelector[F, A]] =
    for {
      adder <- Adder.create[F, Int]
    } yield new CellSelector[F, A] {
      def select(
          cells: Vector[ReservoirCell[F, A]],
          value: A
      ): F[Option[ReservoirCell[F, A]]] =
        for {
          sum <- adder.sum(reset = false)
          count = sum + 1
          idx <- Random[F].nextIntBounded(if (count > 0) count else 1)
          _ <- adder.add(1)
        } yield cells.lift(idx)

      def reset: F[Unit] = adder.reset
    }

}
