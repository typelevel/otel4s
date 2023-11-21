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

import cats.Hash
import cats.Monoid
import cats.Show
import cats.implicits._
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attribute.KeySelect
import org.typelevel.otel4s.AttributeKey

import scala.collection.IterableOps
import scala.collection.SpecificIterableFactory
import scala.collection.immutable
import scala.collection.mutable

/** An immutable collection of [[Attribute]]s.
  */
final class Attributes private (
    private val m: Map[AttributeKey[_], Attribute[_]]
) extends immutable.Iterable[Attribute[_]]
    with IterableOps[Attribute[_], immutable.Iterable, Attributes] {
  def get[T: KeySelect](name: String): Option[Attribute[T]] = {
    val key = KeySelect[T].make(name)
    m.get(key).map(_.asInstanceOf[Attribute[T]])
  }
  def get[T](key: AttributeKey[T]): Option[Attribute[T]] =
    m.get(key).map(_.asInstanceOf[Attribute[T]])
  def contains(key: AttributeKey[_]): Boolean = m.contains(key)

  override def isEmpty: Boolean = m.isEmpty
  override def size: Int = m.size
  override def knownSize: Int = m.knownSize
  def iterator: Iterator[Attribute[_]] = m.valuesIterator

  override def empty: Attributes = Attributes.empty
  override protected def fromSpecific(
      coll: IterableOnce[Attribute[_]]
  ): Attributes =
    Attributes.fromSpecific(coll)
  override protected def newSpecificBuilder
      : mutable.Builder[Attribute[_], Attributes] =
    Attributes.newBuilder

  def toMap: Map[AttributeKey[_], Attribute[_]] = m

  override def hashCode(): Int =
    Hash[Attributes].hash(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Attributes => Hash[Attributes].eqv(this, other)
      case _                 => false
    }

  override def toString: String =
    Show[Attributes].show(this)
}

object Attributes extends SpecificIterableFactory[Attribute[_], Attributes] {
  private[this] val _empty = new Attributes(Map.empty)

  def empty: Attributes = _empty

  def fromSpecific(it: IterableOnce[Attribute[_]]): Attributes =
    it match {
      case attributes: Attributes => attributes
      case other                  => (newBuilder ++= other).result()
    }

  // more precise return type to expose additional `addOne(key, value)`
  def newBuilder: AttributesBuilder = new AttributesBuilder

  final class AttributesBuilder
      extends mutable.Builder[Attribute[_], Attributes] {
    private[this] val mapBuilder = Map.newBuilder[AttributeKey[_], Attribute[_]]

    def addOne[A](key: AttributeKey[A], value: A): this.type = {
      mapBuilder.addOne(key -> Attribute(key, value))
      this
    }

    def clear(): Unit = mapBuilder.clear()
    def result(): Attributes = new Attributes(mapBuilder.result())
    def addOne(elem: Attribute[_]): this.type = {
      mapBuilder.addOne(elem.key -> elem)
      this
    }
    override def addAll(xs: IterableOnce[Attribute[_]]): this.type = {
      xs match {
        case attributes: Attributes => mapBuilder.addAll(attributes.m)
        case other                  => super.addAll(other)
      }
      this
    }
    override def sizeHint(size: Int): Unit = mapBuilder.sizeHint(size)
  }

  implicit val showAttributes: Show[Attributes] = Show.show { attributes =>
    attributes.toList
      .map(a => show"$a")
      .mkString("Attributes(", ", ", ")")
  }

  implicit val hashAttributes: Hash[Attributes] =
    Hash.by(_.m)

  implicit val monoidAttributes: Monoid[Attributes] =
    new Monoid[Attributes] {
      def empty: Attributes = Attributes.empty
      def combine(x: Attributes, y: Attributes): Attributes = {
        if (y.isEmpty) x
        else if (x.isEmpty) y
        else new Attributes(x.m ++ y.m)
      }
    }
}
