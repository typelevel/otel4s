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

package org.typelevel.otel4s
package sdk.trace
package processor

import cats.Applicative
import cats.MonadThrow
import cats.Monoid
import cats.Parallel
import cats.data.NonEmptyList
import cats.syntax.all._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

/** The interface that tracer uses to invoke hooks when a span is started or ended.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-processor]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait SpanProcessor[F[_]] {

  /** The name of the processor.
    *
    * It will be used in an exception to distinguish individual failures in the multi-error scenario.
    *
    * @see
    *   [[SpanProcessor.ProcessorFailure]]
    *
    * @see
    *   [[SpanProcessor.CompositeProcessorFailure]]
    */
  def name: String

  /** Called when a span is started, if the `span.isRecording` returns true.
    *
    * The handler is called synchronously on the execution thread, should not throw or block the execution thread.
    *
    * Use [[SpanProcessor.OnStart.noop]] to define a noop operation if start events aren't required by the processor.
    */
  def onStart: SpanProcessor.OnStart[F]

  /** Called when a span is ended, if the `span.isRecording` returns true.
    *
    * The handler is called synchronously on the execution thread, should not throw or block the execution thread.
    *
    * Use [[SpanProcessor.OnStart.noop]] to define a noop operation if end events aren't required by the processor.
    */
  def onEnd: SpanProcessor.OnEnd[F]

  /** Processes all pending spans (if any).
    */
  def forceFlush: F[Unit]

  override def toString: String =
    name
}

object SpanProcessor {

  /** Evaluated when a span is started.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#onstart]]
    */
  trait OnStart[F[_]] {

    /** Called when a span is started, if the `span.isRecording` returns true.
      *
      * This method is called synchronously on the execution thread, should not throw or block the execution thread.
      *
      * @param parentContext
      *   the optional parent [[trace.SpanContext SpanContext]]
      *
      * @param span
      *   the started span
      */
    def apply(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit]
  }

  object OnStart {

    def noop[F[_]: Applicative]: OnStart[F] =
      new Noop

    private[SpanProcessor] final class Noop[F[_]: Applicative] extends OnStart[F] {
      private val unit: F[Unit] = Applicative[F].unit
      def apply(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit] = unit
    }
  }

  /** Evaluated when a span is ended.
    *
    * @see
    *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#onendspan]]
    */
  trait OnEnd[F[_]] {

    /** Called when a span is ended, if the `span.isRecording` returns true.
      *
      * This method is called synchronously on the execution thread, should not throw or block the execution thread.
      *
      * @param span
      *   the ended span
      */
    def apply(span: SpanData): F[Unit]
  }

  object OnEnd {

    def noop[F[_]: Applicative]: OnEnd[F] =
      new Noop

    private[SpanProcessor] final class Noop[F[_]: Applicative] extends OnEnd[F] {
      private val unit: F[Unit] = Applicative[F].unit
      def apply(span: SpanData): F[Unit] = unit
    }
  }

  /** Creates a [[SpanProcessor]] which delegates all processing to the processors in order.
    */
  def of[F[_]: MonadThrow: Parallel](processors: SpanProcessor[F]*): SpanProcessor[F] =
    if (processors.sizeIs == 1) processors.head
    else processors.combineAll

  /** Creates a no-op implementation of the [[SpanProcessor]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: SpanProcessor[F] =
    new Noop

  implicit def spanProcessorMonoid[F[_]: MonadThrow: Parallel]: Monoid[SpanProcessor[F]] =
    new Monoid[SpanProcessor[F]] {
      val empty: SpanProcessor[F] =
        noop[F]

      def combine(x: SpanProcessor[F], y: SpanProcessor[F]): SpanProcessor[F] =
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
    *   the name of a processor that failed. See [[SpanProcessor.name]]
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

  private final class Noop[F[_]: Applicative] extends SpanProcessor[F] {
    val name: String = "SpanProcessor.Noop"
    val onStart: OnStart[F] = OnStart.noop
    val onEnd: OnEnd[F] = OnEnd.noop
    val forceFlush: F[Unit] = Applicative[F].unit
  }

  private final case class Multi[F[_]: MonadThrow: Parallel](
      processors: NonEmptyList[SpanProcessor[F]]
  ) extends SpanProcessor[F] {
    val name: String =
      s"SpanProcessor.Multi(${processors.map(_.name).mkString_(", ")})"

    /** We use 'traverse' instead of 'parTraverse' due to:
      *
      * If multiple SpanProcessors are registered, their OnStart callbacks are invoked in the order they have been
      * registered.
      *
      * Source: https://opentelemetry.io/docs/specs/otel/trace/sdk/#onstart
      */
    val onStart: OnStart[F] = {
      val start = processors.filterNot(_.onStart.isInstanceOf[OnStart.Noop[F]])

      if (start.nonEmpty) { (parentContext, span) =>
        start
          .traverse(p => p.onStart(parentContext, span).attempt.tupleLeft(p.name))
          .flatMap(attempts => handleAttempts(attempts))
      } else {
        OnStart.noop
      }
    }

    val onEnd: OnEnd[F] = {
      val end = processors.filterNot(_.onEnd.isInstanceOf[OnEnd.Noop[F]])

      if (end.nonEmpty) { span =>
        end
          .parTraverse(p => p.onEnd(span).attempt.tupleLeft(p.name))
          .flatMap(attempts => handleAttempts(attempts))
      } else {
        OnEnd.noop
      }
    }

    def forceFlush: F[Unit] =
      processors
        .parTraverse(p => p.forceFlush.attempt.tupleLeft(p.name))
        .flatMap(attempts => handleAttempts(attempts.toList))

    private def handleAttempts(results: List[(String, Either[Throwable, Unit])]): F[Unit] = {
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
