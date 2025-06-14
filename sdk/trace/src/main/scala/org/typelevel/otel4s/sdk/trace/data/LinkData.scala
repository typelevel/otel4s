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
import cats.syntax.show._
import org.typelevel.otel4s.sdk.data.LimitedData
import org.typelevel.otel4s.trace.SpanContext

/** Data representation of a link.
  *
  * Link can be also used to reference spans from the same trace.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/trace/api/#link]]
  */
sealed trait LinkData {

  /** The [[org.typelevel.otel4s.trace.SpanContext SpanContext]] of the span this link refers to.
    */
  def spanContext: SpanContext

  /** The [[Attributes]] associated with this link.
    */
  def attributes: LimitedData[Attribute[_], Attributes]

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

  /** Creates a [[LinkData]] with the given `context`.
    *
    * @param context
    *   the context of the span the link refers to
    */
  def apply(
      context: SpanContext,
      attributes: LimitedData[Attribute[_], Attributes]
  ): LinkData =
    Impl(context, attributes)

  implicit val linkDataHash: Hash[LinkData] =
    Hash.by(data => (data.spanContext, data.attributes))

  implicit val linkDataShow: Show[LinkData] =
    Show.show { data =>
      show"LinkData{spanContext=${data.spanContext}, attributes=${data.attributes.elements}}"
    }

  private final case class Impl(
      spanContext: SpanContext,
      attributes: LimitedData[Attribute[_], Attributes]
  ) extends LinkData

}
