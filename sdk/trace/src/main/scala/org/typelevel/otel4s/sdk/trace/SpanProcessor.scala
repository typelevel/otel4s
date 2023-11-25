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

import cats.Applicative
import cats.Monoid
import cats.Parallel
import cats.data.NonEmptyList
import cats.syntax.foldable._
import cats.syntax.parallel._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

/** The interface that tracer uses to invoke hooks when a span is started or
  * ended.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/sdk/#span-processor]]
  *
  * @tparam F
  *   the higher-kinded type of a polymorphic effect
  */
trait SpanProcessor[F[_]] {

  /** Called when a span is started, if the `span.isRecording` returns true.
    *
    * This method is called synchronously on the execution thread, should not
    * throw or block the execution thread.
    *
    * @param parentContext
    *   the optional parent [[trace.SpanContext SpanContext]]
    *
    * @param span
    *   the started span
    */
  def onStart(parentContext: Option[SpanContext], span: SpanRef[F]): F[Unit]

  /** Whether the [[SpanProcessor]] requires start events.
    *
    * If true, the [[onStart]] will be called upon the start of a span.
    */
  def isStartRequired: Boolean

  /** Called when a span is ended, if the `span.isRecording` returns true.
    *
    * This method is called synchronously on the execution thread, should not
    * throw or block the execution thread.
    *
    * @param span
    *   the ended span
    */
  def onEnd(span: SpanData): F[Unit]

  /** Whether the [[SpanProcessor]] requires end events.
    *
    * If true, the [[onEnd]] will be called upon the end of a span.
    */
  def isEndRequired: Boolean

  /** Processes all pending spans (if any).
    */
  def forceFlush: F[Unit]
}

object SpanProcessor {

  /** Creates a [[SpanProcessor]] which delegates all processing to the
    * processors in order.
    */
  def of[F[_]: Applicative: Parallel](
      processors: SpanProcessor[F]*
  ): SpanProcessor[F] =
    if (processors.sizeIs == 1) processors.head
    else processors.combineAll

  /** Creates a no-op implementation of the [[SpanProcessor]].
    *
    * All export operations are no-op.
    */
  def noop[F[_]: Applicative]: SpanProcessor[F] =
    new Noop

  implicit def spanProcessorMonoid[F[_]: Applicative: Parallel]
      : Monoid[SpanProcessor[F]] =
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

  private final class Noop[F[_]: Applicative] extends SpanProcessor[F] {
    def isStartRequired: Boolean = false
    def isEndRequired: Boolean = false

    def onStart(parentCtx: Option[SpanContext], span: SpanRef[F]): F[Unit] =
      Applicative[F].unit

    def onEnd(span: SpanData): F[Unit] =
      Applicative[F].unit

    def forceFlush: F[Unit] =
      Applicative[F].unit

    override def toString: String = "SpanProcessor.Noop"
  }

  private final case class Multi[F[_]: Parallel](
      processors: NonEmptyList[SpanProcessor[F]]
  ) extends SpanProcessor[F] {
    private val startOnly: List[SpanProcessor[F]] =
      processors.filter(_.isStartRequired)

    private val endOnly: List[SpanProcessor[F]] =
      processors.filter(_.isEndRequired)

    def isStartRequired: Boolean = startOnly.nonEmpty
    def isEndRequired: Boolean = endOnly.nonEmpty

    def onStart(parentCtx: Option[SpanContext], span: SpanRef[F]): F[Unit] =
      startOnly.parTraverse_(_.onStart(parentCtx, span))

    def onEnd(span: SpanData): F[Unit] =
      endOnly.parTraverse_(_.onEnd(span))

    def forceFlush: F[Unit] =
      processors.parTraverse_(_.forceFlush)

    override def toString: String =
      s"SpanProcessor.Multi(${processors.map(_.toString).mkString_(", ")})"
  }
}
