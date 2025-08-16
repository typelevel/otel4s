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

package org.typelevel.otel4s.logs

import cats.Applicative
import cats.Monad
import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.KindTransformer
import org.typelevel.otel4s.meta.InstrumentMeta

import java.time.Instant
import scala.collection.immutable
import scala.concurrent.duration.FiniteDuration

/** Provides a way to build and emit a log record.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/api/#emit-a-logrecord]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/logs/data-model/]]
  */
sealed trait LogRecordBuilder[F[_], Ctx] {

  /** The instrument's metadata. Indicates whether instrumentation is enabled.
    */
  def meta: InstrumentMeta.Dynamic[F]

  /** Sets the time when the event occurred measured by the origin clock, i.e. the time at the source.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Ctx]

  /** Sets the time when the event occurred measured by the origin clock, i.e. the time at the source.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withTimestamp(timestamp: Instant): LogRecordBuilder[F, Ctx]

  /** Sets the time when the event was observed by the collection system.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withObservedTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Ctx]

  /** Sets the time when the event was observed by the collection system.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withObservedTimestamp(timestamp: Instant): LogRecordBuilder[F, Ctx]

  /** Sets the context to associate with the log record.
    *
    * The context will be used to extract tracing information, such as trace id and span id.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withContext(context: Ctx): LogRecordBuilder[F, Ctx]

  /** Sets the [[Severity]] level.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withSeverity(severity: Severity): LogRecordBuilder[F, Ctx]

  /** Sets the severity text (also known as log level) to the builder.
    *
    * This is the original string representation of the severity as it is known at the source.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withSeverityText(severityText: String): LogRecordBuilder[F, Ctx]

  /** Sets the given body to the builder.
    *
    * Can be, for example, a human-readable string message (including multi-line) describing the event in a free form,
    * or it can be a structured data composed of arrays and maps of other values.
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained.
    *
    * @example
    *   {{{
    *   val builder: LogRecordBuilder[F] = ???
    *   builder.withBody(AnyValue.string("the log message"))
    *   }}}
    */
  def withBody(body: AnyValue): LogRecordBuilder[F, Ctx]

  /** Sets the event name, which identifies the class or type of the event.
    *
    * This name should uniquely identify the event structure (both attributes and body).
    *
    * @note
    *   on multiple subsequent calls, the value from the last call will be retained
    */
  def withEventName(eventName: String): LogRecordBuilder[F, Ctx]

  /** Adds the given attribute to the builder.
    *
    * @note
    *   if the builder previously contained a mapping for the key, the old value is replaced by the specified value
    */
  def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[F, Ctx]

  /** Adds attributes to the builder.
    *
    * @note
    *   if the builder previously contained a mapping for any of the keys, the old values are replaced by the specified
    *   values
    */
  def addAttributes(attributes: Attribute[_]*): LogRecordBuilder[F, Ctx]

  /** Adds attributes to the builder.
    *
    * @note
    *   if the builder previously contained a mapping for any of the keys, the old values are replaced by the specified
    *   values
    */
  def addAttributes(attributes: immutable.Iterable[Attribute[_]]): LogRecordBuilder[F, Ctx]

  /** Creates a log record and emits it to the processing pipeline.
    */
  def emit: F[Unit]

  /** Modify the context `F` using an implicit [[KindTransformer]] from `F` to `G`.
    */
  def mapK[G[_]](implicit G: Monad[G], kt: KindTransformer[F, G]): LogRecordBuilder[G, Ctx] =
    new LogRecordBuilder.MappedK(this)

}

object LogRecordBuilder {

  def noop[F[_]: Applicative, Ctx]: LogRecordBuilder[F, Ctx] =
    new LogRecordBuilder[F, Ctx] {
      val meta: InstrumentMeta.Dynamic[F] = InstrumentMeta.Dynamic.disabled
      def withTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Ctx] = this
      def withTimestamp(timestamp: Instant): LogRecordBuilder[F, Ctx] = this
      def withObservedTimestamp(timestamp: FiniteDuration): LogRecordBuilder[F, Ctx] = this
      def withObservedTimestamp(timestamp: Instant): LogRecordBuilder[F, Ctx] = this
      def withContext(context: Ctx): LogRecordBuilder[F, Ctx] = this
      def withSeverity(severity: Severity): LogRecordBuilder[F, Ctx] = this
      def withSeverityText(severityText: String): LogRecordBuilder[F, Ctx] = this
      def withBody(body: AnyValue): LogRecordBuilder[F, Ctx] = this
      def withEventName(eventName: String): LogRecordBuilder[F, Ctx] = this
      def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[F, Ctx] = this
      def addAttributes(attributes: Attribute[_]*): LogRecordBuilder[F, Ctx] = this
      def addAttributes(attributes: immutable.Iterable[Attribute[_]]): LogRecordBuilder[F, Ctx] = this
      def emit: F[Unit] = Applicative[F].unit
    }

  private final class MappedK[F[_], G[_]: Monad, Ctx](
      builder: LogRecordBuilder[F, Ctx]
  )(implicit kt: KindTransformer[F, G])
      extends LogRecordBuilder[G, Ctx] {

    val meta: InstrumentMeta.Dynamic[G] = builder.meta.mapK[G]

    def withTimestamp(timestamp: FiniteDuration): LogRecordBuilder[G, Ctx] =
      builder.withTimestamp(timestamp).mapK

    def withTimestamp(timestamp: Instant): LogRecordBuilder[G, Ctx] =
      builder.withTimestamp(timestamp).mapK

    def withObservedTimestamp(timestamp: FiniteDuration): LogRecordBuilder[G, Ctx] =
      builder.withObservedTimestamp(timestamp).mapK

    def withObservedTimestamp(timestamp: Instant): LogRecordBuilder[G, Ctx] =
      builder.withObservedTimestamp(timestamp).mapK

    def withContext(context: Ctx): LogRecordBuilder[G, Ctx] =
      builder.withContext(context).mapK

    def withSeverity(severity: Severity): LogRecordBuilder[G, Ctx] =
      builder.withSeverity(severity).mapK

    def withSeverityText(severityText: String): LogRecordBuilder[G, Ctx] =
      builder.withSeverityText(severityText).mapK

    def withBody(body: AnyValue): LogRecordBuilder[G, Ctx] =
      builder.withBody(body).mapK

    def withEventName(eventName: String): LogRecordBuilder[G, Ctx] =
      builder.withEventName(eventName).mapK

    def addAttribute[A](attribute: Attribute[A]): LogRecordBuilder[G, Ctx] =
      builder.addAttribute(attribute).mapK

    def addAttributes(attributes: Attribute[_]*): LogRecordBuilder[G, Ctx] =
      builder.addAttributes(attributes).mapK

    def addAttributes(attributes: immutable.Iterable[Attribute[_]]): LogRecordBuilder[G, Ctx] =
      builder.addAttributes(attributes).mapK

    def emit: G[Unit] =
      kt.liftK(builder.emit)
  }

}
