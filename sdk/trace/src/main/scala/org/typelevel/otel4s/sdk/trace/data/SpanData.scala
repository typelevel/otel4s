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
package sdk
package trace
package data

import cats.Hash
import cats.Show
import cats.syntax.foldable._
import cats.syntax.show._
import org.typelevel.otel4s.sdk.common.InstrumentationScope
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

import scala.concurrent.duration.FiniteDuration

/** Immutable representation of all data collected by the
  * [[org.typelevel.otel4s.trace.Span Span]].
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/api/#span]]
  */
sealed trait SpanData {

  /** The name of the span.
    */
  def name: String

  /** The span context associated with this span.
    */
  def spanContext: SpanContext

  /** The parent span context, if any.
    */
  def parentSpanContext: Option[SpanContext]

  /** The kind of the span.
    */
  def kind: SpanKind

  /** The start timestamp of the span.
    */
  def startTimestamp: FiniteDuration

  /** The end time of the span.
    */
  def endTimestamp: Option[FiniteDuration]

  /** The status data of the span.
    */
  def status: StatusData

  /** The attributes associated with the span.
    */
  def attributes: Attributes

  /** The events associated with the span.
    */
  def events: Vector[EventData]

  /** The links associated with the span.
    */
  def links: Vector[LinkData]

  /** The instrumentation scope associated with the span.
    */
  def instrumentationScope: InstrumentationScope

  /** The resource associated with the span.
    */
  def resource: Resource

  /** Whether the span has ended.
   */
  final def hasEnded: Boolean = endTimestamp.isDefined

  final def droppedAttributesCount: Int = totalAttributeCount - attributes.size
  final def droppedEventsCount: Int = totalRecordedEvents - events.size
  final def droppedLinksCount: Int = totalRecordedLinks - links.size

  override final def hashCode(): Int =
    Hash[SpanData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: SpanData => Hash[SpanData].eqv(this, other)
      case _               => false
    }

  override final def toString: String =
    Show[SpanData].show(this)
}

object SpanData {

  /** Creates [[SpanData]] with the given arguments.
    *
    * @param name
    *   the name of the span
    *
    * @param spanContext
    *   the span context associated with the span
    *
    * @param parentSpanContext
    *   an optional parent span context. Use `None` if there is no parent span
    *   context
    *
    * @param kind
    *   the kind of the span
    *
    * @param startTimestamp
    *   the start timestamp of the span
    *
    * @param endTimestamp
    *   the end timestamp of the span. Use `None` is the span hasn't ended yet
    *
    * @param status
    *   the status data of the span
    *
    * @param attributes
    *   the attributes associated with the span
    *
    * @param events
    *   the events associated with the span
    *
    * @param links
    *   the links associated with the span
    *
    * @param instrumentationScope
    *   the instrumentation scope associated with the span
    *
    * @param resource
    *   the resource associated with the span
    */
  def apply(
      name: String,
      spanContext: SpanContext,
      parentSpanContext: Option[SpanContext],
      kind: SpanKind,
      startTimestamp: FiniteDuration,
      endTimestamp: Option[FiniteDuration],
      status: StatusData,
      attributes: Attributes,
      events: Vector[EventData],
      links: Vector[LinkData],
      instrumentationScope: InstrumentationScope,
      resource: Resource
  ): SpanData =
    Impl(
      name = name,
      spanContext = spanContext,
      parentSpanContext = parentSpanContext,
      kind = kind,
      startTimestamp = startTimestamp,
      endTimestamp = endTimestamp,
      status = status,
      attributes = attributes,
      events = events,
      links = links,
      instrumentationScope = instrumentationScope,
      resource = resource
    )

  implicit val spanDataHash: Hash[SpanData] =
    Hash.by { data =>
      (
        data.name,
        data.spanContext,
        data.parentSpanContext,
        data.kind,
        data.startTimestamp,
        data.endTimestamp,
        data.status,
        data.attributes,
        data.events,
        data.links,
        data.instrumentationScope,
        data.resource
      )
    }

  implicit val spanDataShow: Show[SpanData] =
    Show.show { data =>
      val endTimestamp =
        data.endTimestamp.foldMap(ts => show"endTimestamp=$ts, ")

      show"SpanData{" +
        show"name=${data.name}, " +
        show"spanContext=${data.spanContext}, " +
        show"parentSpanContext=${data.parentSpanContext}, " +
        show"kind=${data.kind}, " +
        show"startTimestamp=${data.startTimestamp}, " +
        endTimestamp +
        show"hasEnded=${data.hasEnded}, " +
        show"status=${data.status}, " +
        show"attributes=${data.attributes}, " +
        show"events=${data.events}, " +
        show"links=${data.links}, " +
        show"instrumentationScope=${data.instrumentationScope}, " +
        show"resource=${data.resource}}"
    }

  private final case class Impl(
      name: String,
      spanContext: SpanContext,
      parentSpanContext: Option[SpanContext],
      kind: SpanKind,
      startTimestamp: FiniteDuration,
      endTimestamp: Option[FiniteDuration],
      status: StatusData,
      attributes: Attributes,
      events: Vector[EventData],
      links: Vector[LinkData],
      instrumentationScope: InstrumentationScope,
      resource: Resource
  ) extends SpanData

}
