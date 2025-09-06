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

package org.typelevel.otel4s.sdk.logs.processor

import cats.Applicative
import cats.MonadThrow
import cats.Monoid
import cats.Parallel
import cats.data.NonEmptyList
import cats.syntax.all._
import org.typelevel.otel4s.sdk.context.Context
import org.typelevel.otel4s.sdk.logs.LogRecordRef

/** The interface that log record builder uses to emit the log record.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/sdk/#logrecordprocessor]]
  *
  * @tparam F
  *   the higher-kinded type of polymorphic effect
  */
sealed trait LogRecordProcessor[F[_]] {

  /** The name of the processor.
    *
    * It will be used in an exception to distinguish individual failures in the multi-error scenario.
    *
    * @see
    *   [[LogRecordProcessor.ProcessorFailure]]
    *
    * @see
    *   [[LogRecordProcessor.CompositeProcessorFailure]]
    */
  def name: String

  /** Called when a log record is emitted.
    *
    * @note
    *   the handler is called synchronously on the execution thread, should not throw or block the execution thread.
    */
  def onEmit(context: Context, logRecord: LogRecordRef[F]): F[Unit]

  /** Processes all pending log records (if any).
    */
  def forceFlush: F[Unit]

  override def toString: String =
    name
}

object LogRecordProcessor {
  private[sdk] trait Unsealed[F[_]] extends LogRecordProcessor[F]

  /** Creates a [[LogRecordProcessor]] which delegates all processing to the processors in order.
    */
  def of[F[_]: MonadThrow: Parallel](processors: LogRecordProcessor[F]*): LogRecordProcessor[F] =
    if (processors.sizeIs == 1) processors.head
    else processors.combineAll

  /** Creates a no-op implementation of the [[LogRecordProcessor]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: LogRecordProcessor[F] =
    new Noop

  implicit def logRecordProcessorMonoid[F[_]: MonadThrow: Parallel]: Monoid[LogRecordProcessor[F]] =
    new Monoid[LogRecordProcessor[F]] {
      val empty: LogRecordProcessor[F] =
        noop[F]

      def combine(x: LogRecordProcessor[F], y: LogRecordProcessor[F]): LogRecordProcessor[F] =
        (x, y) match {
          case (that, _: Noop[F]) =>
            that
          case (_: Noop[F], other) =>
            other
          case (that: Multi[F], other: Multi[F]) =>
            Multi(that.processors.concatNel(other.processors))
          case (that: Multi[F], other) =>
            Multi(that.processors :+ other)
          case (that, other: Multi[F]) =>
            Multi(that :: other.processors)
          case (that, other) =>
            Multi(NonEmptyList.of(that, other))
        }
    }

  /** An error occurred when invoking a processor.
    *
    * @param processor
    *   the name of a processor that failed. See [[LogRecordProcessor.name]]
    *
    * @param failure
    *   the error occurred
    */
  final case class ProcessorFailure(processor: String, failure: Throwable)
      extends Exception(
        s"The processor [$processor] has failed due to ${failure.getMessage}",
        failure
      )

  /** A composite failure, when '''at least 2''' processors have failed.
    *
    * @param first
    *   the first occurred error
    *
    * @param rest
    *   the rest of errors
    */
  final case class CompositeProcessorFailure(
      first: ProcessorFailure,
      rest: NonEmptyList[ProcessorFailure]
  ) extends Exception(
        s"Multiple processors [${rest.prepend(first).map(_.processor).mkString_(", ")}] have failed",
        first
      )

  private final class Noop[F[_]: Applicative] extends LogRecordProcessor[F] {
    private val unit = Applicative[F].unit
    val name: String = "LogRecordProcessor.Noop"
    def onEmit(context: Context, logRecord: LogRecordRef[F]): F[Unit] = unit
    def forceFlush: F[Unit] = unit
  }

  private final case class Multi[F[_]: MonadThrow: Parallel](
      processors: NonEmptyList[LogRecordProcessor[F]]
  ) extends LogRecordProcessor[F] {
    val name: String =
      s"LogRecordProcessor.Multi(${processors.map(_.name).mkString_(", ")})"

    def onEmit(context: Context, logRecord: LogRecordRef[F]): F[Unit] =
      processors
        .parTraverse(p => p.onEmit(context, logRecord).attempt.tupleLeft(p.name))
        .flatMap(attempts => handleAttempts(attempts))

    def forceFlush: F[Unit] =
      processors
        .parTraverse(p => p.forceFlush.attempt.tupleLeft(p.name))
        .flatMap(attempts => handleAttempts(attempts))

    private def handleAttempts(results: NonEmptyList[(String, Either[Throwable, Unit])]): F[Unit] = {
      val failures = results.collect { case (processor, Left(failure)) =>
        ProcessorFailure(processor, failure)
      }

      failures match {
        case Nil =>
          MonadThrow[F].unit

        case head :: Nil =>
          MonadThrow[F].raiseError(head)

        case head :: tail =>
          MonadThrow[F].raiseError(
            CompositeProcessorFailure(head, NonEmptyList.fromListUnsafe(tail))
          )
      }
    }
  }

}
