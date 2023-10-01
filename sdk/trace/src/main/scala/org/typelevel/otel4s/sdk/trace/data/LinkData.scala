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

import cats.Show
import cats.Hash
import cats.syntax.show._
import org.typelevel.otel4s.trace.SpanContext

sealed trait LinkData {
  def spanContext: SpanContext
  def attributes: Attributes
  def totalAttributeCount: Int

  override final def hashCode(): Int =
    Hash[LinkData].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: LinkData => Hash[LinkData].eqv(this, other)
      case _               => false
    }

  override final def toString: String =
    Show[LinkData].show(this)
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

  implicit val linkDataHash: Hash[LinkData] =
    Hash.by { data =>
      (data.spanContext, data.attributes, data.totalAttributeCount)
    }

  implicit val linkDataShow: Show[LinkData] =
    Show.show { data =>
      show"LinkData{spanContext=${data.spanContext}, attributes=${data.attributes}, totalAttributeCount=${data.totalAttributeCount}}"
    }

}
