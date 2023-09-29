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

import org.typelevel.otel4s.sdk.common.InstrumentationScopeInfo
import org.typelevel.otel4s.trace.SpanContext
import org.typelevel.otel4s.trace.SpanKind

trait SpanData {
  def name: String
  def kind: SpanKind
  def spanContext: SpanContext
  // def parentSpanContext: SpanContext
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
}
