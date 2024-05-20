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
import cats.effect.Temporal
import cats.effect.std.Random
import cats.syntax.functor._
import org.typelevel.otel4s.metrics.BucketBoundaries
import org.typelevel.otel4s.metrics.MeasurementValue

private[metrics] sealed trait Reservoirs[F[_]] {

  /** Creates a reservoir with fixed size that stores the given number of
    * exemplars.
    *
    * @param size
    *   the maximum number of exemplars to preserve
    *
    * @tparam A
    *   the type of the values to record
    */
  def fixedSize[A: MeasurementValue](size: Int): F[ExemplarReservoir[F, A]]

  /** Creates a reservoir that preserves the latest seen measurement per
    * histogram bucket.
    *
    * @param boundaries
    *   the bucket boundaries of the histogram
    *
    * @tparam A
    *   the type of the values to record
    */
  def histogramBucket[A: MeasurementValue: Numeric](
      boundaries: BucketBoundaries
  ): F[ExemplarReservoir[F, A]]

}

private[metrics] object Reservoirs {

  /** Creates [[Reservoirs]] according to the given `filter`.
    *
    * @param filter
    *   used by the exemplar reservoir to filter the offered values
    *
    * @param lookup
    *   used by the exemplar reservoir to extract tracing information from the
    *   context
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def apply[F[_]: Temporal: Random](
      filter: ExemplarFilter,
      lookup: TraceContextLookup
  ): Reservoirs[F] = {
    def isAlwaysOn =
      filter == ExemplarFilter.alwaysOn

    def isAlwaysOff =
      filter == ExemplarFilter.alwaysOff || lookup == TraceContextLookup.noop

    if (isAlwaysOn) alwaysOn[F](lookup)
    else if (isAlwaysOff) alwaysOff[F]
    else filtered(filter, lookup)
  }

  /** Creates [[Reservoirs]] that returns 'always-on' reservoirs.
    *
    * @param lookup
    *   used by the exemplar reservoir to extract tracing information from the
    *   context
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def alwaysOn[F[_]: Temporal: Random](
      lookup: TraceContextLookup
  ): Reservoirs[F] =
    new AlwaysOn[F](lookup)

  /** Creates [[Reservoirs]] that returns no-op reservoirs.
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def alwaysOff[F[_]: Applicative]: Reservoirs[F] =
    new AlwaysOff[F]

  /** Creates [[Reservoirs]] that returns filtered reservoirs.
    *
    * @param lookup
    *   used by the exemplar reservoir to extract tracing information from the
    *   context
    *
    * @tparam F
    *   the higher-kinded type of a polymorphic effect
    */
  def filtered[F[_]: Temporal: Random](
      filter: ExemplarFilter,
      lookup: TraceContextLookup
  ): Reservoirs[F] =
    new Filtered[F](filter, lookup)

  private final class AlwaysOn[F[_]: Temporal: Random](
      lookup: TraceContextLookup
  ) extends Reservoirs[F] {
    def fixedSize[A: MeasurementValue](
        size: Int
    ): F[ExemplarReservoir[F, A]] =
      ExemplarReservoir.fixedSize[F, A](size, lookup)

    def histogramBucket[A: MeasurementValue: Numeric](
        boundaries: BucketBoundaries
    ): F[ExemplarReservoir[F, A]] =
      ExemplarReservoir.histogramBucket[F, A](boundaries, lookup)
  }

  private final class AlwaysOff[F[_]: Applicative] extends Reservoirs[F] {
    def fixedSize[A: MeasurementValue](size: Int): F[ExemplarReservoir[F, A]] =
      Applicative[F].pure(ExemplarReservoir.noop[F, A])

    def histogramBucket[A: MeasurementValue: Numeric](
        boundaries: BucketBoundaries
    ): F[ExemplarReservoir[F, A]] =
      Applicative[F].pure(ExemplarReservoir.noop[F, A])
  }

  private final class Filtered[F[_]: Temporal: Random](
      filter: ExemplarFilter,
      lookup: TraceContextLookup
  ) extends Reservoirs[F] {
    def fixedSize[A: MeasurementValue](
        size: Int
    ): F[ExemplarReservoir[F, A]] =
      ExemplarReservoir
        .fixedSize[F, A](size, lookup)
        .map(r => ExemplarReservoir.filtered(filter, r))

    def histogramBucket[A: MeasurementValue: Numeric](
        boundaries: BucketBoundaries
    ): F[ExemplarReservoir[F, A]] =
      ExemplarReservoir
        .histogramBucket[F, A](boundaries, lookup)
        .map(r => ExemplarReservoir.filtered(filter, r))
  }

}
