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

import cats.Monoid
import cats.Show
import cats.implicits.showInterpolator
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attribute.KeySelect
import org.typelevel.otel4s.AttributeKey

/** An immutable collection of [[Attribute]]s.
  */
final class Attributes private (
    private val m: Map[AttributeKey[_], Attribute[_]]
) {
  def get[T: KeySelect](name: String): Option[Attribute[T]] = {
    val key = KeySelect[T].make(name)
    m.get(key).map(_.asInstanceOf[Attribute[T]])
  }
  def get[T](key: AttributeKey[T]): Option[Attribute[T]] =
    m.get(key).map(_.asInstanceOf[Attribute[T]])

  def isEmpty: Boolean = m.isEmpty
  def size: Int = m.size
  def contains(key: AttributeKey[_]): Boolean = m.contains(key)
  def foldLeft[B](z: B)(f: (B, Attribute[_]) => B): B =
    m.foldLeft(z)((b, v) => f(b, v._2))
  def forall(p: Attribute[_] => Boolean): Boolean =
    m.forall(v => p(v._2))
  def toMap: Map[AttributeKey[_], Attribute[_]] = m
  def toList: List[Attribute[_]] = m.values.toList

  def foreach(f: Attribute[_] => Unit): Unit =
    m.foreach(v => f(v._2))

  /** Returns a new [[Attributes]] instance with the given [[Attributes]] added.
    * If the key already exists, the value will be overwritten.
    * @return
    *   a new [[Attributes]] instance
    */
  def ++(those: Attributes): Attributes =
    if (those.isEmpty) this
    else if (this.isEmpty) those
    else new Attributes(m ++ those.m)
}

object Attributes {

  val Empty = new Attributes(Map.empty)

  def apply(attributes: Attribute[_]*): Attributes = {
    val m = attributes.foldLeft(Map.empty[AttributeKey[_], Attribute[_]]) {
      case (m, a) =>
        m.updated(a.key, a)
    }
    new Attributes(m)
  }

  implicit val attributesShow: Show[Attributes] = Show.show { attributes =>
    attributes.toList
      .map(a => show"$a")
      .mkString("Attributes(", ", ", ")")
  }

  implicit val attributesMonoid: Monoid[Attributes] =
    new Monoid[Attributes] {
      def empty: Attributes = Attributes.Empty
      def combine(x: Attributes, y: Attributes): Attributes = x ++ y
    }
}
