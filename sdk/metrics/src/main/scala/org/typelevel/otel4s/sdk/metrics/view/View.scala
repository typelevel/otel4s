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

import cats.Show
import cats.syntax.foldable._
import cats.syntax.show._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.sdk.metrics.Aggregation

/** A view configures how measurements are aggregated and exported as metrics.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/metrics/sdk/#view]]
  */
sealed trait View {

  /** The custom name of the resulting metric.
    *
    * `None` means the name of the matched instrument should be used.
    */
  def name: Option[String]

  /** The custom description of the resulting metric.
    *
    * `None` means the description of the matched instrument should be used.
    */
  def description: Option[String]

  /** The [[Aggregation]] of the resulting metric.
    *
    * `None` means the default aggregation should be used.
    */
  def aggregation: Option[Aggregation]

  /** The [[AttributesProcessor]] associated with this view.
    *
    * `None` means the attributes will not be customized.
    */
  private[metrics] def attributesProcessor: Option[AttributesProcessor]

  /** The cardinality limit of this view - the maximum number of series for a
    * metric.
    *
    * `None` means the MetricReader's default cardinality limit should be used.
    */
  private[metrics] def cardinalityLimit: Option[Int]

  override final def toString: String =
    Show[View].show(this)
}

object View {

  /** Builder of a [[View]].
    */
  sealed trait Builder {

    /** Sets the custom name of the resulting metric.
      *
      * @param name
      *   the name to use
      */
    def withName(name: String): Builder

    /** Sets the custom description of the resulting metric.
      *
      * @param description
      *   the description to use
      */
    def withDescription(description: String): Builder

    /** Sets the aggregation to use with the resulting metric.
      *
      * @param aggregation
      *   the aggregation to use
      */
    def withAggregation(aggregation: Aggregation): Builder

    /** Sets the cardinality limit - the maximum number of series for a metric.
      *
      * @param limit
      *   the limit to use
      */
    def withCardinalityLimit(limit: Int): Builder

    /** Adds an attribute filter which retains keys included in the `retain`.
      *
      * @param retain
      *   the key to retain
      */
    def addAttributeFilter(retain: Set[String]): Builder

    /** Adds an attribute filter which retains attributes that satisfy the
      * `filter`.
      *
      * @param filter
      *   the filter to use
      */
    def addAttributeFilter(filter: Attribute[_] => Boolean): Builder

    /** Creates a [[View]] using the configuration of this builder.
      */
    def build: View
  }

  /** Returns an empty [[Builder]] of a [[View]].
    */
  def builder: Builder =
    BuilderImpl()

  implicit val viewShow: Show[View] = Show.show { view =>
    def prop[A: Show](focus: View => Option[A], name: String) =
      focus(view).map(value => show"$name=$value")

    Seq(
      prop(_.name, "name"),
      prop(_.description, "description"),
      prop(_.aggregation, "aggregation"),
      prop(_.attributesProcessor, "attributesProcessor"),
      prop(_.cardinalityLimit, "cardinalityLimit")
    ).collect { case Some(k) => k }.mkString("View{", ", ", "}")
  }

  private final case class Impl(
      name: Option[String],
      description: Option[String],
      aggregation: Option[Aggregation],
      attributesProcessor: Option[AttributesProcessor],
      cardinalityLimit: Option[Int]
  ) extends View

  private final case class BuilderImpl(
      name: Option[String] = None,
      description: Option[String] = None,
      aggregation: Option[Aggregation] = None,
      cardinalityLimit: Option[Int] = None,
      attributesProcessors: Vector[AttributesProcessor] = Vector.empty
  ) extends Builder {

    def withName(name: String): Builder =
      copy(name = keepNonEmpty(name))

    def withDescription(description: String): Builder =
      copy(description = keepNonEmpty(description))

    def withAggregation(aggregation: Aggregation): Builder =
      copy(aggregation = Some(aggregation))

    def addAttributeFilter(retain: Set[String]): Builder =
      copy(attributesProcessors =
        attributesProcessors :+ AttributesProcessor.retain(retain)
      )

    def addAttributeFilter(filter: Attribute[_] => Boolean): Builder =
      copy(attributesProcessors =
        attributesProcessors :+ AttributesProcessor.attributePredicate(filter)
      )

    def withCardinalityLimit(limit: Int): Builder = {
      require(limit > 0, s"cardinality limit [$limit] must be positive")
      copy(cardinalityLimit = Some(limit))
    }

    def build: View =
      Impl(
        name,
        description,
        aggregation,
        attributesProcessors.combineAllOption,
        cardinalityLimit
      )

    private def keepNonEmpty(value: String): Option[String] = {
      val trimmed = value.trim
      Option.when(trimmed.nonEmpty)(trimmed)
    }
  }

}
