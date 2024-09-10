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

package org.typelevel.otel4s.sdk.trace.data

import cats.Hash
import cats.Semigroup
import cats.syntax.semigroup._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.AttributeType
import org.typelevel.otel4s.Attributes

import scala.collection.immutable

/** A generic collection container with fixed bounds.
  *
  * @tparam A
  *   the type of container elements
  *
  * @tparam S
  *   the type of underlying container collection
  */
sealed trait LimitedData[A, S <: immutable.Iterable[A]] {

  /** The limit for container elements.
    */
  def sizeLimit: Int

  /** The number of dropped elements.
    */
  def dropped: Int

  /** The container elements.
    */
  def elements: S

  /** Appends an element to the container if the number of container elements has not reached the limit, otherwise drops
    * the element.
    *
    * @param a
    *   the element to append
    */
  def append(a: A): LimitedData[A, S]

  /** Appends all given elements to the container up to the limit and drops the rest elements.
    *
    * @param as
    *   the collection of elements to append
    */
  def appendAll(as: S): LimitedData[A, S]

  /** Prepends all given elements to the container up to the limit and drops all elements that exceeds the limit (even
    * if they already existed before).
    *
    * @param as
    *   the collection of elements to prepend
    */
  def prependAll(as: S): LimitedData[A, S]

  /** Maps the container elements by applying the passed function.
    *
    * @param f
    *   the function to apply
    */
  def map(f: S => S): LimitedData[A, S]
}

object LimitedData {

  implicit def limitedDataHash[A, S <: immutable.Iterable[A]: Hash]: Hash[LimitedData[A, S]] =
    Hash.by(events => (events.sizeLimit, events.dropped, events.elements))

  /** Created [[LimitedData]] with the collection of [[Attribute]] inside.
    *
    * @param sizeLimit
    *   The limit for container elements
    *
    * @param valueLengthLimit
    *   The limit for attribute's value
    */
  def attributes(
      sizeLimit: Int,
      valueLengthLimit: Int
  ): LimitedData[Attribute[_], Attributes] = {
    def limitValueLength(a: Attribute[_]) =
      a.key.`type` match {
        case AttributeType.String =>
          Attribute(
            a.key.name,
            a.value.asInstanceOf[String].take(valueLengthLimit)
          )
        case AttributeType.BooleanSeq =>
          Attribute(
            a.key.name,
            a.value.asInstanceOf[Seq[Boolean]].take(valueLengthLimit)
          )
        case AttributeType.DoubleSeq =>
          Attribute(
            a.key.name,
            a.value.asInstanceOf[Seq[Double]].take(valueLengthLimit)
          )
        case AttributeType.StringSeq =>
          Attribute(
            a.key.name,
            a.value.asInstanceOf[Seq[String]].take(valueLengthLimit)
          )
        case AttributeType.LongSeq =>
          Attribute(
            a.key.name,
            a.value.asInstanceOf[Seq[Long]].take(valueLengthLimit)
          )
        case _ =>
          a
      }

    Impl[Attribute[_], Attributes](
      sizeLimit,
      Attributes.empty,
      _ splitAt _,
      Attributes(_),
      _.map(limitValueLength).to(Attributes)
    )
  }

  /** Created [[LimitedData]] with the vector of [[LinkData]] inside.
    *
    * @param sizeLimit
    *   The limit for container elements
    */
  def links(sizeLimit: Int): LimitedData[LinkData, Vector[LinkData]] =
    Impl[LinkData, Vector[LinkData]](
      sizeLimit,
      Vector.empty,
      _ splitAt _,
      Vector(_)
    )

  /** Created [[LimitedData]] with the vector of [[EventData]] inside.
    *
    * @param sizeLimit
    *   The limit for container elements
    */
  def events(sizeLimit: Int): LimitedData[EventData, Vector[EventData]] =
    Impl[EventData, Vector[EventData]](
      sizeLimit,
      Vector.empty,
      _ splitAt _,
      Vector(_)
    )

  private final case class Impl[A, S <: immutable.Iterable[A]: Semigroup](
      sizeLimit: Int,
      elements: S,
      splitAt: (S, Int) => (S, S),
      pure: A => S,
      refine: S => S = identity[S],
      dropped: Int = 0
  ) extends LimitedData[A, S] {
    def append(a: A): LimitedData[A, S] = {
      if (elements.size < sizeLimit) {
        copy(elements = elements |+| refine(pure(a)))
      } else {
        copy(dropped = dropped + 1)
      }
    }

    def appendAll(as: S): LimitedData[A, S] =
      if (elements.size + as.size <= sizeLimit) {
        copy(elements = elements |+| refine(as))
      } else {
        val (toAdd, toDrop) = splitAt(as, sizeLimit - elements.size)
        copy(
          elements = elements |+| refine(toAdd),
          dropped = dropped + toDrop.size
        )
      }

    def prependAll(as: S): LimitedData[A, S] = {
      val allElements = as |+| elements
      val (toKeep, toDrop) = splitAt(allElements, sizeLimit)
      copy(elements = refine(toKeep), dropped = dropped + toDrop.size)
    }

    def map(f: S => S): LimitedData[A, S] =
      copy(elements = f(elements))
  }
}
