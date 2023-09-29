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

import org.typelevel.otel4s.trace.SpanContext

sealed trait LinkData {
  def spanContext: SpanContext
  def attributes: Attributes
  def totalAttributeCount: Int
}

object LinkData {

  private final case class LinkDataImpl(
      spanContext: SpanContext,
      attributes: Attributes,
      totalAttributeCount: Int
  ) extends LinkData

  def create(context: SpanContext): LinkData =
    LinkDataImpl(context, Attributes.Empty, 0)

  def create(context: SpanContext, attributes: Attributes): LinkData =
    LinkDataImpl(context, attributes, attributes.size)

  def create(
      context: SpanContext,
      attributes: Attributes,
      totalAttributeCount: Int
  ): LinkData =
    LinkDataImpl(context, attributes, totalAttributeCount)

}
