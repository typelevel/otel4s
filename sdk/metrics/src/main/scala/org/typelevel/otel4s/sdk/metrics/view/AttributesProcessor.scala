/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.sdk.metrics.view

import cats.Monoid
import cats.Show
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes
import org.typelevel.otel4s.sdk.context.Context

/** The AttributesProcessor can customize which attributes are to be reported as
  * metric dimensions and add additional dimensions from the context.
  */
private[metrics] sealed trait AttributesProcessor {

  /** Processes a set of attributes, returning the desired set.
    *
    * @param attributes
    *   the attributes associated with an incoming measurement
    *
    * @param context
    *   the context associated with the measurement
    */
  def process(attributes: Attributes, context: Context): Attributes

  override final def toString: String =
    Show[AttributesProcessor].show(this)
}

private[metrics] object AttributesProcessor {

  /** Returns a no-op [[AttributesProcessor]] that returns original attributes.
    */
  def noop: AttributesProcessor = Noop

  /** Creates an [[AttributesProcessor]] that retains attributes with the
    * matching names.
    *
    * @param retain
    *   the attribute keys to retain
    */
  def retain(retain: Set[String]): AttributesProcessor =
    new Retain(retain)

  /** Creates an [[AttributesProcessor]] that retains attributes that match the
    * predicate.
    *
    * @param predicate
    *   the predicate to match
    */
  def attributePredicate(
      predicate: Attribute[_] => Boolean
  ): AttributesProcessor =
    new AttributePredicate(predicate)

  implicit val attributesProcessorShow: Show[AttributesProcessor] =
    Show.show {
      case p: Retain =>
        s"AttributesProcessor.Retain(retain = ${p.retain.mkString("{", ", ", "}")})"

      case _: AttributePredicate =>
        "AttributesProcessor.AttributePredicate(<f>)"

      case c: Combined =>
        s"AttributesProcessor.Combined(left=${c.left}, right=${c.right})"

      case Noop =>
        "AttributesProcessor.Noop"
    }

  implicit val attributesProcessorMonoid: Monoid[AttributesProcessor] =
    new Monoid[AttributesProcessor] {
      def empty: AttributesProcessor = AttributesProcessor.noop

      def combine(
          x: AttributesProcessor,
          y: AttributesProcessor
      ): AttributesProcessor =
        (x, y) match {
          case (Noop, right) => right
          case (left, Noop)  => left
          case (left, right) => new Combined(left, right)
        }
    }

  private final class Retain(
      val retain: Set[String]
  ) extends AttributesProcessor {
    def process(attributes: Attributes, context: Context): Attributes =
      attributes.filter(a => retain.contains(a.key.name))
  }

  private final class AttributePredicate(
      predicate: Attribute[_] => Boolean
  ) extends AttributesProcessor {
    def process(attributes: Attributes, context: Context): Attributes =
      attributes.filter(predicate)
  }

  private final class Combined(
      val left: AttributesProcessor,
      val right: AttributesProcessor
  ) extends AttributesProcessor {
    def process(attributes: Attributes, context: Context): Attributes =
      left.process(right.process(attributes, context), context)
  }

  private object Noop extends AttributesProcessor {
    def process(attributes: Attributes, context: Context): Attributes =
      attributes
  }

}
