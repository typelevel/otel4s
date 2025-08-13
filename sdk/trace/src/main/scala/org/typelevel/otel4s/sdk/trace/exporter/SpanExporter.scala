/*
 * Copyright 2023 Typelevel
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

package org.typelevel.otel4s.sdk.trace
package exporter

import cats.Applicative
import cats.Foldable
import cats.MonadThrow
import cats.Monoid
import cats.Parallel
import cats.data.NonEmptyList
import cats.syntax.all._
import org.typelevel.otel4s.sdk.trace.data.SpanData

/** An interface that allows different tracing services to export recorded data for sampled spans in their own format.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-exporter]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
sealed trait SpanExporter[F[_]] {

  /** The name of the exporter.
    *
    * It will be used in an exception to distinguish individual failures in the multi-error scenario.
    *
    * @see
    *   [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter.ExporterFailure SpanExporter.ExporterFailure]]
    *
    * @see
    *   [[org.typelevel.otel4s.sdk.trace.exporter.SpanExporter.CompositeExporterFailure SpanExporter.CompositeExporterFailure]]
    */
  def name: String

  /** Called to export sampled [[org.typelevel.otel4s.sdk.trace.data.SpanData SpanData]].
    *
    * @note
    *   the export operations can be performed simultaneously depending on the type of span processor being used.
    *   However, the batch span processor will ensure that only one export can occur at a time.
    *
    * @param spans
    *   the sampled spans to be exported
    */
  def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit]

  /** Exports the collection of sampled [[org.typelevel.otel4s.sdk.trace.data.SpanData SpanData]] that have not yet been
    * exported.
    *
    * @note
    *   the export operations can be performed simultaneously depending on the type of span processor being used.
    *   However, the batch span processor will ensure that only one export can occur at a time.
    */
  def flush: F[Unit]

  override def toString: String =
    name
}

object SpanExporter {
  private[sdk] trait Unsealed[F[_]] extends SpanExporter[F]

  /** Creates a [[SpanExporter]] which delegates all exports to the exporters.
    */
  def of[F[_]: MonadThrow: Parallel](
      exporters: SpanExporter[F]*
  ): SpanExporter[F] =
    if (exporters.sizeIs == 1) exporters.head
    else exporters.combineAll

  /** Creates a no-op implementation of the [[SpanExporter]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: SpanExporter[F] =
    new Noop

  implicit def spanExporterMonoid[F[_]: MonadThrow: Parallel]: Monoid[SpanExporter[F]] =
    new Monoid[SpanExporter[F]] {
      val empty: SpanExporter[F] =
        noop[F]

      def combine(x: SpanExporter[F], y: SpanExporter[F]): SpanExporter[F] =
        (x, y) match {
          case (that, _: Noop[F]) =>
            that
          case (_: Noop[F], other) =>
            other
          case (that: Multi[F], other: Multi[F]) =>
            Multi(that.exporters.concatNel(other.exporters))
          case (that: Multi[F], other) =>
            Multi(that.exporters :+ other)
          case (that, other: Multi[F]) =>
            Multi(that :: other.exporters)
          case (that, other) =>
            Multi(NonEmptyList.of(that, other))
        }
    }

  /** An error occurred when invoking an exporter.
    *
    * @param exporter
    *   the name of an exporter that failed. See [[SpanExporter.name]]
    *
    * @param failure
    *   the error occurred
    */
  final case class ExporterFailure(exporter: String, failure: Throwable)
      extends Exception(
        s"The exporter [$exporter] has failed due to ${failure.getMessage}",
        failure
      )

  /** A composite failure, when '''at least 2''' exporters have failed.
    *
    * @param first
    *   the first occurred error
    *
    * @param rest
    *   the rest of errors
    */
  final case class CompositeExporterFailure(
      first: ExporterFailure,
      rest: NonEmptyList[ExporterFailure]
  ) extends Exception(
        s"Multiple exporters [${rest.prepend(first).map(_.exporter).mkString_(", ")}] have failed",
        first
      )

  private final class Noop[F[_]: Applicative] extends SpanExporter[F] {
    val name: String = "SpanExporter.Noop"

    def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit] =
      Applicative[F].unit

    def flush: F[Unit] =
      Applicative[F].unit
  }

  private final case class Multi[F[_]: MonadThrow: Parallel](
      exporters: NonEmptyList[SpanExporter[F]]
  ) extends SpanExporter[F] {
    val name: String =
      s"SpanExporter.Multi(${exporters.map(_.toString).mkString_(", ")})"

    def exportSpans[G[_]: Foldable](spans: G[SpanData]): F[Unit] =
      exporters
        .parTraverse(e => e.exportSpans(spans).attempt.tupleLeft(e.toString))
        .flatMap(attempts => handleAttempts(attempts))

    def flush: F[Unit] =
      exporters
        .parTraverse(e => e.flush.attempt.tupleLeft(e.toString))
        .flatMap(attempts => handleAttempts(attempts))

    private def handleAttempts(
        results: NonEmptyList[(String, Either[Throwable, Unit])]
    ): F[Unit] = {
      val failures = results.collect { case (exporter, Left(failure)) =>
        ExporterFailure(exporter, failure)
      }

      failures match {
        case Nil =>
          MonadThrow[F].unit

        case head :: Nil =>
          MonadThrow[F].raiseError(head)

        case head :: tail =>
          MonadThrow[F].raiseError(
            CompositeExporterFailure(head, NonEmptyList.fromListUnsafe(tail))
          )
      }
    }
  }

}
