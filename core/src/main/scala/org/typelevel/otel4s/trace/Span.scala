/*
 * Copyright 2022 Typelevel
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
package trace

import scala.concurrent.duration.FiniteDuration

/** The API to trace an operation.
  *
  * There are two types of span: [[Span.Manual]] and [[Span.Auto]].
  *
  * ==[[Span.Manual]]==
  * The manual span requires an ''explicit'' termination. Manual span can be
  * used when it's necessary to end a span outside of the resource scope (i.e.
  * async callback). Make sure the span is ended properly.
  *
  * Leaked span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val leaked: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     this.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  *
  * Properly ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     this.setStatus(Status.Ok, "all good") >> span.end
  *   }
  * }}}
  *
  * ==[[Span.Auto]]==
  * Unlike [[Span.Manual]] the auto span has a fully managed lifecycle. That
  * means the span is started upon resource allocation and ended upon
  * finalization. Abnormal terminations (error, cancelation) are recorded as
  * well.
  *
  * Automatically ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createAuto.use { span =>
  *     this.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  */
trait Span[F[_]] {

  /** Returns the [[SpanContext]] associated with this span.
    */
  def context: SpanContext

  /** Sets attributes to the span. If the span previously contained a mapping
    * for any of the keys, the old values are replaced by the specified values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  def setAttributes(attributes: Attribute[_]*): F[Unit]

  /** Adds an event to the span with the given attributes. The timestamp of the
    * event will be the current time.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(name: String, attributes: Attribute[_]*): F[Unit]

  /** Adds an event to the span with the given attributes and timestamp.
    *
    * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using
    * `Clock[F].monotonic` may lead to an incorrect data.
    *
    * @param name
    *   the name of the event
    *
    * @param timestamp
    *   the explicit event timestamp since epoch
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(
      name: String,
      timestamp: FiniteDuration,
      attributes: Attribute[_]*
  ): F[Unit]

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are
    * free to ignore previous calls.
    *
    * @param status
    *   the [[Status]] to set
    */
  def setStatus(status: Status): F[Unit]

  /** Sets the status to the span.
    *
    * Only the value of the last call will be recorded, and implementations are
    * free to ignore previous calls.
    *
    * @param status
    *   the [[Status]] to set
    *
    * @param description
    *   the description of the [[Status]]
    */
  def setStatus(status: Status, description: String): F[Unit]

  /** Records information about the `Throwable` to the span.
    *
    * @param exception
    *   the `Throwable` to record
    *
    * @param attributes
    *   the set of attributes to associate with the value
    */
  def recordException(
      exception: Throwable,
      attributes: Attribute[_]*
  ): F[Unit]

  /** Indicates whether this span is active or not. Returns `false` when the
    * span is ended.
    */
  def isRecording: Boolean

}

object Span {

  /** Automatically started and ended.
    */
  trait Auto[F[_]] extends Span[F]

  /** Must be ended explicitly by calling `end`.
    */
  trait Manual[F[_]] extends Span[F] {

    /** Marks the end of [[Span]] execution.
      *
      * Only the timing of the first end call for a given span will be recorded,
      * and implementations are free to ignore all further calls.
      */
    def end: F[Unit]

    /** Marks the end of [[Span]] execution with the specified timestamp.
      *
      * Only the timing of the first end call for a given span will be recorded,
      * and implementations are free to ignore all further calls.
      *
      * @param timestamp
      *   the explicit timestamp from the epoch
      */
    def end(timestamp: FiniteDuration): F[Unit]
  }

  /** Automatically started and ended. Carries a value of a wrapped resource.
    */
  trait Res[F[_], A] extends Auto[F] {
    def value: A
  }

}
