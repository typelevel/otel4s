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

import cats.Applicative
import cats.syntax.foldable._
import org.typelevel.otel4s.sdk.trace.data.SpanData
import org.typelevel.otel4s.trace.SpanContext

/** The interface that [[SdkTracer]] uses to allow hooks for when a span is
  * started or ended.
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
    *   the optional parent
    *   [[org.typelevel.otel4s.trace.SpanContext SpanContext]]
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
}

object SpanProcessor {

  /** Creates a [[SpanProcessor]] which delegates all processing to the
    * processors in order.
    */
  def composite[F[_]: Applicative](
      processors: List[SpanProcessor[F]]
  ): SpanProcessor[F] =
    processors match {
      case Nil         => new Noop
      case head :: Nil => head
      case _           => new Multi[F](processors)
    }

  private final class Noop[F[_]: Applicative] extends SpanProcessor[F] {
    def isStartRequired: Boolean = false

    def isEndRequired: Boolean = false

    def onStart(parentCtx: Option[SpanContext], span: SpanRef[F]): F[Unit] =
      Applicative[F].unit

    def onEnd(span: SpanData): F[Unit] =
      Applicative[F].unit
  }

  private final class Multi[F[_]: Applicative](
      processors: List[SpanProcessor[F]]
  ) extends SpanProcessor[F] {
    private val startOnly: List[SpanProcessor[F]] =
      processors.filter(_.isStartRequired)

    private val endOnly: List[SpanProcessor[F]] =
      processors.filter(_.isEndRequired)

    def isStartRequired: Boolean = startOnly.nonEmpty

    def isEndRequired: Boolean = endOnly.nonEmpty

    def onStart(parentCtx: Option[SpanContext], span: SpanRef[F]): F[Unit] =
      startOnly.traverse_(_.onStart(parentCtx, span))

    def onEnd(span: SpanData): F[Unit] =
      endOnly.traverse_(_.onEnd(span))
  }
}
