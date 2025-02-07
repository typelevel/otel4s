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

  /** Allows an implicit conversion of `A` to `Attribute`.
    *
    * @tparam A
    *   the type of the value
    */
  sealed trait Make[A] {
    type Key
    def make(a: A): Attribute[Key]
  }

  object Make {
    type Aux[A, K] = Make[A] { type Key = K }

    /** Creates a [[Make]] with a const name.
      *
      * @example
      *   {{{
      * case class UserId(id: Int)
      * object UserId {
      *   implicit val userIdFocus: AttributeKey.Focus[UserId, Long] =
      *     _.id.toLong
      *   implicit val userIdAttribute: Attribute.Make[UserId] =
      *     Attribute.Make.named("user.id")
      * }
      *
      * val userId = UserId(123)
      *
      * val attributes = Attributes(userId) // implicitly converted to Attribute[Long]
      *   }}}
      *
      * @param name
      *   the name of the attribute
      *
      * @tparam A
      *   the type of the value
      *
      * @tparam K
      *   the type of the key
      */
    def named[A, K](name: String)(implicit
        focus: AttributeKey.Focus[A, K],
        select: AttributeKey.KeySelect[K]
    ): Make.Aux[A, K] =
      new Make[A] {
        type Key = K
        def make(a: A): Attribute[Key] = Attribute(name, a)
      }
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
    * @example
    *   a projected attribute type:
    *   {{{
    * case class UserId(id: Int)
    * implicit val userIdKeySelect: AttributeKey.Focus[UserId, Long] = _.id.toLong
    * val attribute: Attribute[Long] = Attribute("key", UserId(1))
    *   }}}
    *
    * @param name
    *   the key name of an attribute
    *
    * @param value
    *   the value of an attribute
    */
  def apply[A, Key](name: String, value: A)(implicit
      focus: AttributeKey.Focus[A, Key],
      select: AttributeKey.KeySelect[Key]
  ): Attribute[Key] =
    AttributeKey.KeySelect[Key].make(name).apply(value)

  implicit def materialize[A](value: A)(implicit ev: Make[A]): Attribute[ev.Key] =
    ev.make(value)

  implicit val showAttribute: Show[Attribute[_]] = (a: Attribute[_]) => s"${show"${a.key}"}=${a.value}"

  implicit def hashAttribute[T: Hash]: Hash[Attribute[T]] =
    Hash.by(a => (a.key, a.value))

  implicit val hashAttributeExistential: Hash[Attribute[_]] = {
    implicit val hashAny: Hash[Any] = Hash.fromUniversalHashCode
    Hash.by(a => (a.key, a.value))
  }

  private final case class Impl[A](key: AttributeKey[A], value: A) extends Attribute[A]
}
