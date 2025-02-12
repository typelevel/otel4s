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
import cats.Show
import cats.syntax.show._

/** Represents the key-value attribute.
  *
  * @tparam A
  *   the type of the attribute's value. One of [[AttributeType]]
  *
  * @see
  *   [[https://opentelemetry.io/docs/specs/otel/common/#attribute]]
  */
sealed trait Attribute[A] {

  /** The key of the attribute. Denotes the types of the `value`.
    */
  def key: AttributeKey[A]

  /** The value of the attribute.
    */
  def value: A

  override final def hashCode(): Int =
    Hash[Attribute[_]].hash(this)

  override final def equals(obj: Any): Boolean =
    obj match {
      case other: Attribute[_] =>
        Hash[Attribute[_]].eqv(this, other)
      case _ =>
        false
    }

  override final def toString: String =
    Show[Attribute[_]].show(this)

}

object Attribute {

  /** Allows creating an attribute value from an arbitrary type:
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    * implicit val userIdFrom: Attribute.From[UserId, Long] = _.id.toLong
    *
    * val userIdKey = AttributeKey[Long]("user.id")
    *
    * val attribute: Attribute[Long] = userIdKey(UserId(1))
    * val attribute: Attribute[Long] = Attribute.from(userIdKey, UserId(1))
    * val attribute: Attribute[Long] = Attribute.from("user.id", UserId(1))
    *   }}}
    *
    * @tparam Value
    *   the type of the value
    *
    * @tparam Key
    *   the type of the key, one of [[AttributeType]]
    */
  @annotation.implicitNotFound("Could not find the `Attribute.From` for value ${Value} and key ${Key}.")
  trait From[Value, Key] {
    def apply(in: Value): Key
  }

  object From {
    implicit def id[A]: From[A, A] = a => a
  }

  /** Creates an attribute with the given key and value.
    *
    * @example
    *   {{{
    * val stringAttribute: Attribute[String] = Attribute(AttributeKey[String]("key"), "string")
    * val longAttribute: Attribute[Long] = Attribute(AttributeKey[Long]("key"), 1L)
    * val boolSeqAttribute: Attribute[Seq[Boolean]] = Attribute(AttributeKey[Seq[Boolean]]("key"), Seq(false))
    *   }}}
    */
  def apply[A](key: AttributeKey[A], value: A): Attribute[A] =
    Impl(key, value)

  /** Creates an attribute with the given name and value. The type is derived automatically from the value type.
    *
    * @example
    *   {{{
    * val stringAttribute: Attribute[String] = Attribute("key", "string")
    * val longAttribute: Attribute[Long] = Attribute("key", 1L)
    * val boolSeqAttribute: Attribute[Seq[Boolean]] = Attribute("key", Seq(false))
    *   }}}
    *
    * @param name
    *   the key name of an attribute
    *
    * @param value
    *   the value of an attribute
    */
  def apply[A: AttributeKey.KeySelect](name: String, value: A): Attribute[A] =
    Impl(AttributeKey.KeySelect[A].make(name), value)

  /** Creates an attribute with the given name and value.
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    * implicit val userIdKeyFrom: Attribute.From[UserId, Long] = _.id.toLong
    * val userIdKey = AttributeKey[Long]("user.id")
    * val attribute: Attribute[Long] = Attribute.from(userIdKey, UserId(1))
    *   }}}
    *
    * @param key
    *   the key of an attribute
    *
    * @param value
    *   the value of an attribute
    */
  def from[A, Key](key: AttributeKey[Key], value: A)(implicit from: From[A, Key]): Attribute[Key] =
    Impl(key, from(value))

  /** Creates an attribute with the given name and value.
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    * implicit val userIdKeyFrom: Attribute.From[UserId, Long] = _.id.toLong
    * val attribute: Attribute[Long] = Attribute.from("user.id", UserId(1))
    *   }}}
    *
    * @param name
    *   the key name of an attribute
    *
    * @param value
    *   the value of an attribute
    */
  def from[A, Key](name: String, value: A)(implicit
      from: From[A, Key],
      select: AttributeKey.KeySelect[Key]
  ): Attribute[Key] =
    Impl(select.make(name), from(value))

  implicit val showAttribute: Show[Attribute[_]] = (a: Attribute[_]) => s"${show"${a.key}"}=${a.value}"

  implicit def hashAttribute[T: Hash]: Hash[Attribute[T]] =
    Hash.by(a => (a.key, a.value))

  implicit val hashAttributeExistential: Hash[Attribute[_]] = {
    implicit val hashAny: Hash[Any] = Hash.fromUniversalHashCode
    Hash.by(a => (a.key, a.value))
  }

  private final case class Impl[A](key: AttributeKey[A], value: A) extends Attribute[A]
}
