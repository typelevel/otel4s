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
import org.typelevel.scalaccompat.annotation.threadUnsafe3

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

  @threadUnsafe3
  override final lazy val hashCode: Int =
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
  trait From[-Value, Key] {

    /** Transforms the given value into one that can be used in an `Attribute`. */
    def apply(in: Value): Key
  }

  object From extends LowPriorityFromImplicits {
    implicit def id[A]: From[A, A] = a => a

    implicit val longFromInt: From[Int, Long] = _.toLong
    implicit val longFromShort: From[Short, Long] = _.toLong
    implicit val longFromByte: From[Byte, Long] = _.toLong
    implicit val seqLongFromSeqInt: From[Seq[Int], Seq[Long]] = _.map(_.toLong)
    implicit val seqLongFromSeqShort: From[Seq[Short], Seq[Long]] = _.map(_.toLong)
    implicit val seqLongFromSeqByte: From[Seq[Byte], Seq[Long]] = _.map(_.toLong)

    /** Derives a `From` instance for a `Seq` of given type from a `From` instance for that type. */
    def deriveForSeq[Value, Key](implicit
        from: From[Value, Key]
    ): From[Seq[Value], Seq[Key]] = _.map(from(_))
  }

  sealed abstract class LowPriorityFromImplicits {
    implicit def seqTypeFromType[Value, Key](implicit
        from: From[Value, Key]
    ): From[Seq[Value], Seq[Key]] = From.deriveForSeq
  }

  /** Allows creating an [[Attribute]] from an arbitrary type `A`.
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    *
    * implicit val userIdAttributeFrom: Attribute.From[UserId, Long] =
    *   _.id.toLong
    *
    * implicit val userIdAttributeMake: Attribute.Make[UserId, Long] =
    *   Attribute.Make.const("user.id")
    *
    * val attribute: Attribute[Long] = Attribute.from(UserId(1))
    *   }}}
    *
    * @tparam A
    *   the type of the value
    *
    * @tparam Key
    *   type of the attribute key
    */
  trait Make[A, Key] {
    def make(a: A): Attribute[Key]
  }

  object Make {

    /** Creates a [[Make]] instance with a const name.
      *
      * @example
      *   {{{
      * case class UserId(id: Int)
      * object UserId {
      *   val UserIdKey: AttributeKey[Long] = AttributeKey("user.id")
      *   implicit val userIdAttributeFrom: Attribute.From[UserId, Long] =
      *     _.id.toLong
      *   implicit val userIdAttributeMake: Attribute.Make[UserId, Long] =
      *     Attribute.Make.const(UserIdKey)
      * }
      *
      * val userId = UserId(123)
      *
      * val attribute: Attribute[Long] = Attribute.from(userId)
      *   }}}
      *
      * @param key
      *   the key of the attribute
      *
      * @tparam A
      *   the type of the value
      *
      * @tparam Key
      *   the type of the key
      */
    def const[A, Key](key: AttributeKey[Key])(implicit from: Attribute.From[A, Key]): Make[A, Key] =
      (value: A) => Attribute.from(key, value)

    /** Creates a [[Make]] instance with a const name.
      *
      * @example
      *   {{{
      * case class UserId(id: Int)
      * object UserId {
      *   implicit val userIdAttributeFrom: Attribute.From[UserId, Long] =
      *     _.id.toLong
      *   implicit val userIdAttributeMake: Attribute.Make[UserId, Long] =
      *     Attribute.Make.const("user.id")
      * }
      *
      * val userId = UserId(123)
      *
      * val attribute: Attribute[Long] = Attribute.from(userId)
      *   }}}
      *
      * @param name
      *   the name of the attribute
      *
      * @tparam A
      *   the type of the value
      *
      * @tparam Key
      *   the type of the key
      */
    def const[A, Key](name: String)(implicit
        from: Attribute.From[A, Key],
        select: AttributeKey.KeySelect[Key]
    ): Make[A, Key] =
      (a: A) => Attribute.from(name, a)
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

  /** Creates an [[Attribute]] from the given value using an implicit [[Make]] instance.
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    *
    * implicit val userIdAttributeFrom: Attribute.From[UserId, Long] =
    *   _.id.toLong
    *
    * implicit val userIdAttributeMake: Attribute.Make[UserId, Long] =
    *   Attribute.Make.const("user.id")
    *
    * val attribute: Attribute[Long] = Attribute.from(UserId(1))
    *   }}}
    *
    * @param value
    *   the value of an attribute
    */
  def from[A, Key](value: A)(implicit make: Make[A, Key]): Attribute[Key] =
    make.make(value)

  implicit val showAttribute: Show[Attribute[_]] = (a: Attribute[_]) => s"${show"${a.key}"}=${a.value}"

  implicit def hashAttribute[T: Hash]: Hash[Attribute[T]] =
    Hash.by(a => (a.key, a.value))

  implicit val hashAttributeExistential: Hash[Attribute[_]] = {
    implicit val hashAny: Hash[Any] = Hash.fromUniversalHashCode
    Hash.by(a => (a.key, a.value))
  }

  private final case class Impl[A](key: AttributeKey[A], value: A) extends Attribute[A]
}
