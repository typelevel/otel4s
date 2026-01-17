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

import cats.Applicative
import cats.Semigroup
import cats.data.NonEmptyList
import cats.effect.kernel.Resource
import cats.syntax.applicative._
import cats.syntax.foldable._
import cats.syntax.semigroup._

import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

/** A deferred update applied to a span when it is finalized.
  *
  * Finalizers are executed only when the span is enabled and are meant to capture changes at finalization time.
  */
sealed trait SpanFinalizer

object SpanFinalizer {

  /** Selects a [[SpanFinalizer]] based on an outcome.
    */
  type Strategy = PartialFunction[Resource.ExitCase, SpanFinalizer]

  object Strategy {

    /** A strategy that never produces a finalizer. */
    def empty: Strategy = PartialFunction.empty

    /** Records errors on abnormal termination and marks the span as errored. */
    def reportAbnormal: Strategy = {
      case Resource.ExitCase.Errored(e) =>
        recordException(e) |+| setStatus(StatusCode.Error)

      case Resource.ExitCase.Canceled =>
        setStatus(StatusCode.Error, "canceled")
    }
  }

  private final class RecordException(
      val throwable: Throwable
  ) extends SpanFinalizer

  private final class SetStatus(
      val status: StatusCode,
      val description: Option[String]
  ) extends SpanFinalizer

  private final class AddAttributes(
      val attributes: Attributes
  ) extends SpanFinalizer

  private final class UpdateName(
      val name: String
  ) extends SpanFinalizer

  private final class AddLink(
      val spanContext: SpanContext,
      val attributes: Attributes
  ) extends SpanFinalizer

  private final class AddEvent(
      val name: String,
      val timestamp: Option[FiniteDuration],
      val attributes: Attributes
  ) extends SpanFinalizer

  private final class Multiple(
      val finalizers: NonEmptyList[SpanFinalizer]
  ) extends SpanFinalizer

  /** Records information about the `Throwable` to the span.
    *
    * No additional attributes are recorded.
    *
    * @param throwable
    *   the exception to record
    */
  def recordException(throwable: Throwable): SpanFinalizer =
    new RecordException(throwable)

  /** Sets the status of the span.
    *
    * @param status
    *   the [[StatusCode]] to set
    */
  def setStatus(status: StatusCode): SpanFinalizer =
    new SetStatus(status, None)

  /** Sets the status of the span.
    *
    * @param status
    *   the [[StatusCode]] to set
    *
    * @param description
    *   the description of the [[StatusCode]]
    */
  def setStatus(status: StatusCode, description: String): SpanFinalizer =
    new SetStatus(status, Some(description))

  /** Adds an attribute to the span.
    *
    * If the span previously contained a mapping for the key, the old value is replaced by the specified value.
    *
    * @param attribute
    *   the attribute to add to the span
    */
  def addAttribute[A](attribute: Attribute[A]): SpanFinalizer =
    new AddAttributes(Attributes(attribute))

  /** Adds attributes to the span.
    *
    * If the span previously contained a mapping for any of the keys, the old values are replaced by the specified
    * values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  def addAttributes(attributes: Attribute[_]*): SpanFinalizer =
    addAttributes(attributes)

  /** Adds attributes to the span.
    *
    * If the span previously contained a mapping for any of the keys, the old values are replaced by the specified
    * values.
    *
    * @param attributes
    *   the set of attributes to add to the span
    */
  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): SpanFinalizer =
    new AddAttributes(attributes.to(Attributes))

  /** Updates the name of the [[Span]].
    *
    * @note
    *   this overrides the name provided via the [[SpanBuilder]].
    *
    * @note
    *   sampling behavior based on the span name is implementation-specific.
    *
    * @param name
    *   the new name of the span
    */
  def updateName(name: String): SpanFinalizer =
    new UpdateName(name)

  /** Adds an event to the span with the given attributes.
    *
    * The event timestamp is captured when the finalizer is executed.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(name: String, attributes: Attribute[_]*): SpanFinalizer =
    new AddEvent(name, None, attributes.to(Attributes))

  /** Adds an event to the span with the given attributes.
    *
    * The event timestamp is captured when the finalizer is executed.
    *
    * @param name
    *   the name of the event
    *
    * @param attributes
    *   the set of attributes to associate with the event
    */
  def addEvent(
      name: String,
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanFinalizer =
    new AddEvent(name, None, attributes.to(Attributes))

  /** Adds an event to the span with the given attributes and an explicit timestamp.
    *
    * @note
    *   the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to incorrect data.
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
  ): SpanFinalizer =
    new AddEvent(name, Some(timestamp), attributes.to(Attributes))

  /** Adds an event to the span with the given attributes and an explicit timestamp.
    *
    * @note
    *   the timestamp should be based on `Clock[F].realTime`. Using `Clock[F].monotonic` may lead to incorrect data.
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
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanFinalizer =
    new AddEvent(name, Some(timestamp), attributes.to(Attributes))

  /** Adds a link to the span.
    *
    * Links connect spans across traces. A common use case is batching, where a single handler processes requests from
    * different traces or from the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associate with the link
    */
  def addLink(
      spanContext: SpanContext,
      attributes: Attribute[_]*
  ): SpanFinalizer =
    new AddLink(spanContext, attributes.to(Attributes))

  /** Adds a link to the span.
    *
    * Links connect spans across traces. A common use case is batching, where a single handler processes requests from
    * different traces or from the same trace.
    *
    * @param spanContext
    *   the context of the linked span
    *
    * @param attributes
    *   the set of attributes to associate with the link
    */
  def addLink(
      spanContext: SpanContext,
      attributes: immutable.Iterable[Attribute[_]]
  ): SpanFinalizer =
    new AddLink(spanContext, attributes.to(Attributes))

  /** Combines multiple finalizers into a single finalizer.
    *
    * Finalizers are executed in the order they are provided.
    */
  def multiple(head: SpanFinalizer, tail: SpanFinalizer*): SpanFinalizer =
    new Multiple(NonEmptyList.of(head, tail: _*))

  implicit val spanFinalizerSemigroup: Semigroup[SpanFinalizer] = {
    case (left: Multiple, right: Multiple) =>
      new Multiple(left.finalizers.concatNel(right.finalizers))

    case (left: Multiple, right: SpanFinalizer) =>
      new Multiple(left.finalizers.append(right))

    case (left: SpanFinalizer, right: Multiple) =>
      new Multiple(right.finalizers.prepend(left))

    case (left, right) =>
      new Multiple(NonEmptyList.of(left, right))
  }

  private[otel4s] def run[F[_]: Applicative](
      backend: Span.Backend[F],
      finalizer: SpanFinalizer
  ): F[Unit] = {

    def loop(input: SpanFinalizer): F[Unit] =
      input match {
        case r: RecordException =>
          backend.recordException(r.throwable, Attributes.empty)

        case s: SetStatus =>
          s.description match {
            case Some(desc) => backend.setStatus(s.status, desc)
            case None       => backend.setStatus(s.status)
          }

        case s: AddAttributes =>
          backend.addAttributes(s.attributes)

        case link: AddLink =>
          backend.addLink(link.spanContext, link.attributes)

        case event: AddEvent =>
          event.timestamp match {
            case Some(timestamp) => backend.addEvent(event.name, timestamp, event.attributes)
            case None            => backend.addEvent(event.name, event.attributes)
          }

        case updateName: UpdateName =>
          backend.updateName(updateName.name)

        case m: Multiple =>
          m.finalizers.traverse_(strategy => loop(strategy))
      }

    loop(finalizer).whenA(backend.meta.isEnabled)
  }

}
