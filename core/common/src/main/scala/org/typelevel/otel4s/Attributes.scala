/*
 * Copyright 2022 Typelevel
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

import cats.Hash
import cats.Monoid
import cats.Show
import cats.syntax.show._
import org.typelevel.otel4s.Attribute.KeySelect

import scala.collection.IterableOps
import scala.collection.SpecificIterableFactory
import scala.collection.immutable
import scala.collection.mutable

/** An immutable collection of [[Attribute]]s. It contains only unique keys.
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/common/#attribute-collections]]
  */
sealed trait Attributes
    extends immutable.Iterable[Attribute[_]]
    with IterableOps[Attribute[_], immutable.Iterable, Attributes] { self =>

  /** Returns an attribute for the given attribute name and type, or `None` if
    * not found.
    */
  final def get[T: KeySelect](name: String): Option[Attribute[T]] =
    get(KeySelect[T].make(name))

  /** Returns an attribute for the given attribute key, or `None` if not found.
    */
  def get[T](key: AttributeKey[T]): Option[Attribute[T]]

  /** Whether or not these `Attributes` contain a key with the given name. */
  def containsUntyped(name: String): Boolean

  /** Whether or not these `Attributes` contain a key with the given name and
    * type.
    */
  final def contains[T: KeySelect](name: String): Boolean =
    contains(KeySelect[T].make(name))

  /** Whether or not these `Attributes` contain the given key. */
  def contains(key: AttributeKey[_]): Boolean

  /** Adds an [[`Attribute`]] with the given name and value to these
    * `Attributes`, replacing any `Attribute` with the same name if one exists.
    */
  final def updated[T: KeySelect](name: String, value: T): Attributes =
    updated(Attribute(name, value))

  /** Adds an [[`Attribute`]] with the given key and value to these
    * `Attributes`, replacing any `Attribute` with the same name if one exists.
    */
  final def updated[T](key: AttributeKey[T], value: T): Attributes =
    updated(Attribute(key, value))

  /** Adds the given [[`Attribute`]] to these `Attributes`, replacing any
    * `Attribute` with the same name if one exists.
    */
  def updated(attribute: Attribute[_]): Attributes

  /** Adds the given [[`Attribute`]] to these `Attributes`, replacing any
    * `Attribute` with the same name if one exists.
    */
  final def +(attribute: Attribute[_]): Attributes =
    updated(attribute)

  /** Removes the [[`Attribute`]] with the given name, if present. */
  def removedUntyped(name: String): Attributes

  /** Removes the [[`Attribute`]] with the given name and type, if present. */
  final def removed[T: KeySelect](name: String): Attributes =
    removed(KeySelect[T].make(name))

  /** Removes the [[`Attribute`]] with the given key, if present. */
  def removed(key: AttributeKey[_]): Attributes

  /** Removes the [[`Attribute`]] with the given key, if present. */
  final def -(key: AttributeKey[_]): Attributes =
    removed(key)

  /** Invariant overload of
    * [[scala.collection.IterableOps.concat `IterableOps#concat`]] that returns
    * `Attributes` rather than `Iterable`.
    *
    * If multiple [[`Attribute`]]s in `this` and/or `that` have the same name,
    * only the final one (according to `that`'s iterator) will be retained in
    * the resulting `Attributes`.
    */
  def concat(that: IterableOnce[Attribute[_]]): Attributes =
    fromSpecific(this.view ++ that)

  /** Invariant overload of [[scala.collection.IterableOps.++ `IterableOps#++`]]
    * that returns `Attributes` rather than `Iterable`.
    *
    * If multiple [[`Attribute`]]s in `this` and/or `that` have the same name,
    * only the final one (according to `that`'s iterator) will be retained in
    * the resulting `Attributes`.
    */
  final def ++(that: IterableOnce[Attribute[_]]): Attributes =
    concat(that)

  /** Removes all attributes with any of the given key names. */
  def removedAllUntyped(names: IterableOnce[String]): Attributes =
    names.iterator.foldLeft(this)(_.removedUntyped(_))

  /** Removes all attributes with any of the given keys. */
  def removedAll(keys: IterableOnce[AttributeKey[_]]): Attributes =
    keys.iterator.foldLeft(this)(_ - _)

  /** Removes all attributes with any of the given keys. */
  final def --(keys: IterableOnce[AttributeKey[_]]): Attributes =
    removedAll(keys)

  /** @return
    *   the `Map` representation of these `Attributes` with typed
    *   [[`AttributeKey`]]s
    */
  def asMap: Map[AttributeKey[_], Attribute[_]] = new TypedMap

  /** @return
    *   the `Map` representation of these `Attributes` with untyped `String`
    *   keys
    */
  def asUntypedMap: Map[String, Attribute[_]]

  /** Equivalent to `asMap.keySet`.
    *
    * @return
    *   the typed [[`AttributeKey` keys]] of the [[`Attribute`]]s
    */
  final def keys: Set[AttributeKey[_]] = asMap.keySet

  /** Equivalent to `asUntypedMap.keySet`.
    *
    * @return
    *   the untyped `String` key names of the [[`Attribute`]]s
    */
  final def untypedKeys: Set[String] = asUntypedMap.keySet

  /** A factory for creating `Attributes`. */
  def attributesFactory: SpecificIterableFactory[Attribute[_], Attributes]

  override def empty: Attributes = attributesFactory.empty
  override protected def fromSpecific(
      coll: IterableOnce[Attribute[_]]
  ): Attributes =
    attributesFactory.fromSpecific(coll)
  override protected def newSpecificBuilder
      : mutable.Builder[Attribute[_], Attributes] =
    attributesFactory.newBuilder
  override protected[this] def className: String = "Attributes"

  override def hashCode(): Int =
    Hash[Attributes].hash(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: Attributes => Hash[Attributes].eqv(this, other)
      case _                 => false
    }

  override def toString: String =
    Show[Attributes].show(this)

  private[this] class TypedMap
      extends immutable.AbstractMap[AttributeKey[_], Attribute[_]]
      with immutable.MapOps[
        AttributeKey[_],
        Attribute[_],
        Map,
        Map[AttributeKey[_], Attribute[_]]
      ] {
    def removed(key: AttributeKey[_]): Map[AttributeKey[_], Attribute[_]] =
      self.removed(key).asMap
    def updated[V1 >: Attribute[_]](
        key: AttributeKey[_],
        value: V1
    ): Map[AttributeKey[_], V1] = {
      val b = mapFactory.newBuilder[AttributeKey[_], V1]
      b.sizeHint(this, 1)
      (b ++= this += (key -> value)).result()
    }
    def get(key: AttributeKey[_]): Option[Attribute[_]] =
      self.get(key)
    def iterator: Iterator[(AttributeKey[_], Attribute[_])] =
      self.iterator.map(a => a.key -> a)

    override def isEmpty: Boolean = self.isEmpty
    override def size: Int = self.size
    override def knownSize: Int = self.knownSize
  }
}

object Attributes extends SpecificIterableFactory[Attribute[_], Attributes] {
  private val Empty = new MapAttributes(Map.empty)

  /** Creates [[Attributes]] with the given `attributes`.
    *
    * @note
    *   if there are duplicated keys in the given `attributes`, only the last
    *   occurrence will be retained.
    *
    * @param attributes
    *   the attributes to use
    */
  override def apply(attributes: Attribute[_]*): Attributes =
    fromSpecific(attributes)

  /** Creates an empty [[Builder]] of [[Attributes]].
    */
  def newBuilder: Builder = new Builder

  /** Returns empty [[Attributes]].
    */
  def empty: Attributes = Empty

  /** Creates [[Attributes]] from the given collection.
    *
    * @note
    *   if there are duplicated keys in the given `attributes`, only the last
    *   occurrence will be retained.
    *
    * @param attributes
    *   the attributes to use
    */
  def fromSpecific(attributes: IterableOnce[Attribute[_]]): Attributes =
    attributes match {
      case a: Attributes => a
      case other         => (newBuilder ++= other).result()
    }

  implicit val showAttributes: Show[Attributes] = Show.show { attributes =>
    attributes.view
      .map(a => show"$a")
      .mkString("Attributes(", ", ", ")")
  }

  implicit val hashAttributes: Hash[Attributes] =
    Hash.by(_.asUntypedMap)

  implicit val monoidAttributes: Monoid[Attributes] =
    new Monoid[Attributes] {
      def empty: Attributes = Attributes.Empty
      def combine(x: Attributes, y: Attributes): Attributes =
        if (y.isEmpty) x
        else if (x.isEmpty) y
        else new MapAttributes(x.asUntypedMap ++ y.asUntypedMap)
    }

  /** A '''mutable''' builder of [[Attributes]].
    */
  final class Builder extends mutable.Builder[Attribute[_], Attributes] {
    private val builder = Map.newBuilder[String, Attribute[_]]

    /** Adds the attribute with the given `key` and `value` to the builder.
      *
      * @note
      *   if the given `key` is already present in the builder, the value will
      *   be overwritten with the given `value`.
      *
      * @param key
      *   the key of the attribute. Denotes the types of the `value`
      *
      * @param value
      *   the value of the attribute
      */
    def addOne[A](key: AttributeKey[A], value: A): this.type = {
      builder.addOne((key.name, Attribute(key, value)))
      this
    }

    /** Adds the attribute with the given `key` (created from `name`) and
      * `value` to the builder.
      *
      * @note
      *   if the given `key` is already present in the builder, the value will
      *   be overwritten with the given `value`.
      *
      * @param name
      *   the name of the attribute's key
      *
      * @param value
      *   the value of the attribute
      */
    def addOne[A: KeySelect](name: String, value: A): this.type = {
      val key = KeySelect[A].make(name)
      builder.addOne((name, Attribute(key, value)))
      this
    }

    /** Adds the given `attribute` to the builder.
      *
      * @note
      *   if the key of the given `attribute` is already present in the builder,
      *   the value will be overwritten with the corresponding given attribute.
      *
      * @param attribute
      *   the attribute to add
      */
    def addOne(attribute: Attribute[_]): this.type = {
      builder.addOne((attribute.key.name, attribute))
      this
    }

    /** Adds the given `attributes` to the builder.
      *
      * @note
      *   if the keys of the given `attributes` are already present in the
      *   builder, the values will be overwritten with the corresponding given
      *   attributes.
      *
      * @param attributes
      *   the attributes to add
      */
    override def addAll(attributes: IterableOnce[Attribute[_]]): this.type = {
      attributes match {
        case a: Attributes => builder.addAll(a.asUntypedMap)
        case other         => super.addAll(other)
      }
      this
    }

    override def sizeHint(size: Int): Unit =
      builder.sizeHint(size)

    def clear(): Unit =
      builder.clear()

    /** Creates [[Attributes]] with the attributes of this builder.
      */
    def result(): Attributes =
      new MapAttributes(builder.result())
  }

  private final class MapAttributes(
      val asUntypedMap: Map[String, Attribute[_]]
  ) extends Attributes {
    def get[T](key: AttributeKey[T]): Option[Attribute[T]] =
      asUntypedMap
        .get(key.name)
        .filter(_.key.`type` == key.`type`)
        .map(_.asInstanceOf[Attribute[T]])
    def containsUntyped(name: String): Boolean =
      asUntypedMap.contains(name)
    def contains(key: AttributeKey[_]): Boolean =
      asUntypedMap.get(key.name).exists(_.key.`type` == key.`type`)
    def updated(attribute: Attribute[_]): Attributes =
      new MapAttributes(asUntypedMap.updated(attribute.key.name, attribute))
    def removedUntyped(name: String): Attributes =
      new MapAttributes(asUntypedMap - name)
    def removed(key: AttributeKey[_]): Attributes =
      if (contains(key)) new MapAttributes(asUntypedMap - key.name)
      else this

    override def concat(that: IterableOnce[Attribute[_]]): Attributes =
      that match {
        case other: Attributes =>
          new MapAttributes(asUntypedMap ++ other.asUntypedMap)
        case other =>
          new MapAttributes(
            asUntypedMap ++ other.iterator.map(a => a.key.name -> a)
          )
      }
    override def removedAllUntyped(names: IterableOnce[String]): Attributes =
      new MapAttributes(asUntypedMap -- names)

    def iterator: Iterator[Attribute[_]] = asUntypedMap.valuesIterator

    def attributesFactory: Attributes.type = Attributes

    override def isEmpty: Boolean = asUntypedMap.isEmpty
    override def size: Int = asUntypedMap.size
    override def knownSize: Int = asUntypedMap.knownSize
  }

}
