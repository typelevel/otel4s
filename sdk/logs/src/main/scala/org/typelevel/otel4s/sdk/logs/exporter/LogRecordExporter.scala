/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.sdk.logs.exporter

import cats.Applicative
import cats.Foldable
import cats.MonadThrow
import cats.Monoid
import cats.Parallel
import cats.data.NonEmptyList
import cats.syntax.all._
import org.typelevel.otel4s.sdk.logs.data.LogRecordData

/** An interface for exporting `LogRecordData`.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordexporter]]
  *
  * @tparam F
  *   the higher-kinded type of polymorphic effect
  */
sealed trait LogRecordExporter[F[_]] {

  /** The name of the exporter.
    *
    * It will be used in an exception to distinguish individual failures in the multi-error scenario.
    *
    * @see
    *   [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter.ExporterFailure LogRecordExporter.ExporterFailure]]
    *
    * @see
    *   [[org.typelevel.otel4s.sdk.logs.exporter.LogRecordExporter.CompositeExporterFailure LogRecordExporter.CompositeExporterFailure]]
    */
  def name: String

  /** Called to export sampled [[org.typelevel.otel4s.sdk.logs.data.LogRecordData LogRecordData]].
    *
    * @note
    *   the export operations can be performed simultaneously depending on the type of log record processor being used.
    *   However, the batch log record processor will ensure that only one export can occur at a time.
    *
    * @param logs
    *   the logs to be exported
    */
  def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): F[Unit]

  /** Exports the collection of sampled [[org.typelevel.otel4s.sdk.logs.data.LogRecordData LogRecordData]] that have not
    * yet been exported.
    *
    * @note
    *   the export operations can be performed simultaneously depending on the type of log record processor being used.
    *   However, the batch log record processor will ensure that only one export can occur at a time.
    */
  def flush: F[Unit]

  override def toString: String =
    name

}

object LogRecordExporter {
  private[sdk] trait Unsealed[F[_]] extends LogRecordExporter[F]

  /** Creates a [[LogRecordExporter]] which delegates all exports to the exporters.
    */
  def of[F[_]: MonadThrow: Parallel](
      exporters: LogRecordExporter[F]*
  ): LogRecordExporter[F] =
    if (exporters.sizeIs == 1) exporters.head
    else exporters.combineAll

  /** Creates a no-op implementation of the [[LogRecordExporter]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: LogRecordExporter[F] =
    new Noop

  implicit def LogRecordExporterMonoid[F[_]: MonadThrow: Parallel]: Monoid[LogRecordExporter[F]] =
    new Monoid[LogRecordExporter[F]] {
      val empty: LogRecordExporter[F] =
        noop[F]

      def combine(x: LogRecordExporter[F], y: LogRecordExporter[F]): LogRecordExporter[F] =
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
    *   the name of an exporter that failed. See [[LogRecordExporter.name]]
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

  private final class Noop[F[_]: Applicative] extends LogRecordExporter[F] {
    val name: String = "LogRecordExporter.Noop"

    def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): F[Unit] =
      Applicative[F].unit

    def flush: F[Unit] =
      Applicative[F].unit
  }

  private final case class Multi[F[_]: MonadThrow: Parallel](
      exporters: NonEmptyList[LogRecordExporter[F]]
  ) extends LogRecordExporter[F] {
    val name: String =
      s"LogRecordExporter.Multi(${exporters.map(_.toString).mkString_(", ")})"

    def exportLogRecords[G[_]: Foldable](logs: G[LogRecordData]): F[Unit] =
      exporters
        .parTraverse(e => e.exportLogRecords(logs).attempt.tupleLeft(e.toString))
        .flatMap(attempts => handleAttempts(attempts))

    def flush: F[Unit] =
      exporters
        .parTraverse(e => e.flush.attempt.tupleLeft(e.toString))
        .flatMap(attempts => handleAttempts(attempts))

    private def handleAttempts(results: NonEmptyList[(String, Either[Throwable, Unit])]): F[Unit] = {
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
