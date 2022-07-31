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
  * There are three types of span: [[Span.Manual]], [[Span.Auto]], [[Span.Res]].
  *
  * ==[[Span.Manual]]==
  * The manual span requires to be ended '''explicitly'''. Manual span can be
  * used when it's necessary to end a span outside of the resource scope (i.e.
  * async callback). Make sure the span is ended properly.
  *
  * Leaked span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val leaked: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     span.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  *
  * Properly ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createManual.use { span =>
  *     span.setStatus(Status.Ok, "all good") >> span.end
  *   }
  * }}}
  *
  * ==[[Span.Auto]]==
  * Unlike [[Span.Manual]] the auto span has a fully managed lifecycle. That
  * means the span is started upon resource allocation and ended upon
  * finalization.
  *
  * Automatically ended span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createAuto.use { span =>
  *     span.setStatus(Status.Ok, "all good")
  *   }
  * }}}
  *
  * ==[[Span.Res]]==
  * The behaviour and lifecycle management are identical to [[Span.Auto]]. The
  * allocation and release stages of a supplied resource are traced by separate
  * spans. Carries a value of a wrapped resource.
  *
  * The structure of the inner spans:
  * {{{
  * > span-name
  *   > acquire
  *   > use
  *   > release
  * }}}
  *
  * Resource span:
  * {{{
  * val tracer: Tracer[F] = ???
  * val resource: Resource[F, String] = Resource.eval(Sync[F].delay("string"))
  * val ok: F[Unit] =
  *   tracer.spanBuilder("manual-span").createRes(resource).use { case span @ Span.Res(value) =>
  *     span.setStatus(Status.Ok, s"all good. resource value: $${value}")
  *   }
  * }}}
  */
trait Span[F[_]] {

  /** Returns the [[SpanContext]] associated with this span.
    *
    * Returns `None` if the span is invalid or no-op.
    */
  def context: Option[SpanContext]

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

}

object Span {

  /** The manual span requires to be ended '''explicitly''' by calling `end`.
    * Manual span can be used when it's necessary to end a span outside of the
    * resource scope (i.e. async callback).
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
      * '''Note''': the timestamp should be based on `Clock[F].realTime`. Using
      * `Clock[F].monotonic` may lead to a missing span.
      *
      * @param timestamp
      *   the explicit timestamp from the epoch
      */
    def end(timestamp: FiniteDuration): F[Unit]
  }

  /** Unlike [[Span.Manual]] the auto span has a fully managed lifecycle. That
    * means the span is started upon resource allocation and ended upon
    * finalization.
    */
  trait Auto[F[_]] extends Span[F]

  /** The behaviour and lifecycle management are identical to [[Span.Auto]]. The
    * allocation and release stages of a supplied resource are traced by
    * separate spans. Carries a value of a wrapped resource.
    *
    * The structure of the inner spans:
    * {{{
    * > span-name
    *   > acquire
    *   > use
    *   > release
    * }}}
    */
  trait Res[F[_], A] extends Auto[F] {
    def value: A
  }

  object Res {
    def unapply[F[_], A](span: Span.Res[F, A]): Option[A] =
      Some(span.value)
  }

}
