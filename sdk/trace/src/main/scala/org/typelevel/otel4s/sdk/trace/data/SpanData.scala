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

package org.typelevel.otel4s.sdk
package trace
package data

import cats.Hash
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

sealed trait SpanData {
  def name: String
  def kind: SpanKind
  def spanContext: SpanContext
  def parentSpanContext: Option[SpanContext]
  def status: StatusData
  def startEpochNanos: Long
  def attributes: Attributes
  def events: List[EventData]
  def links: List[LinkData]
  def endEpochNanos: Long
  def hasEnded: Boolean
  def totalRecordedEvents: Int
  def totalRecordedLinks: Int
  def totalAttributeCount: Int
  def instrumentationScopeInfo: InstrumentationScopeInfo
  def resource: Resource

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

  private final case class SpanDataImpl(
      name: String,
      kind: SpanKind,
      spanContext: SpanContext,
      parentSpanContext: Option[SpanContext],
      status: StatusData,
      startEpochNanos: Long,
      attributes: Attributes,
      events: List[EventData],
      links: List[LinkData],
      endEpochNanos: Long,
      hasEnded: Boolean,
      totalRecordedEvents: Int,
      totalRecordedLinks: Int,
      totalAttributeCount: Int,
      instrumentationScopeInfo: InstrumentationScopeInfo,
      resource: Resource
  ) extends SpanData

  def create(
      name: String,
      kind: SpanKind,
      spanContext: SpanContext,
      parentSpanContext: Option[SpanContext],
      status: StatusData,
      startEpochNanos: Long,
      attributes: Attributes,
      events: List[EventData],
      links: List[LinkData],
      endEpochNanos: Long,
      hasEnded: Boolean,
      totalRecordedEvents: Int,
      totalRecordedLinks: Int,
      totalAttributeCount: Int,
      instrumentationScopeInfo: InstrumentationScopeInfo,
      resource: Resource
  ): SpanData =
    SpanDataImpl(
      name,
      kind,
      spanContext,
      parentSpanContext,
      status,
      startEpochNanos,
      attributes,
      events,
      links,
      endEpochNanos,
      hasEnded,
      totalRecordedEvents,
      totalRecordedLinks,
      totalAttributeCount,
      instrumentationScopeInfo,
      resource
    )

  implicit val spanDataHash: Hash[SpanData] =
    Hash.by { data =>
      (
        data.name,
        data.kind,
        data.spanContext,
        data.parentSpanContext,
        data.status,
        data.startEpochNanos,
        data.attributes,
        data.events,
        data.links,
        data.endEpochNanos,
        data.hasEnded,
        data.totalRecordedEvents,
        data.totalRecordedLinks,
        data.totalAttributeCount,
        data.instrumentationScopeInfo,
        data.resource
      )
    }

  implicit val spanDataShow: Show[SpanData] =
    Show.show { data =>
      show"SpanData{" +
        show"name=${data.name}, " +
        show"kind=${data.kind}, " +
        show"spanContext=${data.spanContext}, " +
        show"parentSpanContext=${data.parentSpanContext}, " +
        show"status=${data.status}, " +
        show"startEpochNanos=${data.startEpochNanos}, " +
        show"attributes=${data.attributes}, " +
        show"events=${data.events}, " +
        show"links=${data.links}, " +
        show"endEpochNanos=${data.endEpochNanos}, " +
        show"hasEnded=${data.hasEnded}, " +
        show"totalRecordedEvents=${data.totalRecordedEvents}, " +
        show"totalRecordedLinks=${data.totalRecordedLinks}, " +
        show"totalAttributeCount=${data.totalAttributeCount}, " +
        show"instrumentationScopeInfo=${data.instrumentationScopeInfo}, " +
        show"resource=${data.resource}}"
    }

}
