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

/** The type of value that can be set with an implementation of this key is denoted by the type parameter.
  *
  * @tparam A
  *   the type of value that can be set with the key
  */
sealed trait AttributeKey[A] {
  import Attribute.From

  /** The name of the attribute key. */
  def name: String

  /** The type of the attribute value. */
  def `type`: AttributeType[A]

  /** @return
    *   an [[`Attribute`]] associating this key with the given value
    */
  final def apply(value: A): Attribute[A] =
    Attribute(this, value)

  /** Creates an [[Attribute]] with the focused value.
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
    * @return
    *   an [[`Attribute`]] associating this key with the given value
    */
  final def apply[In](value: In)(implicit from: From[In, A]): Attribute[A] =
    Attribute(this, from(value))

  /** @return
    *   an [[`Attribute`]] associating this key with the given value if the value is defined
    */
  final def maybe(value: Option[A]): Option[Attribute[A]] =
    value.map(apply)

  /** Creates an [[Attribute]] with the focused value if the given option is non-empty.
    *
    * @example
    *   {{{
    * case class UserId(id: Int)
    * implicit val userIdFrom: Attribute.From[UserId, Long] = _.id.toLong
    *
    * val userIdKey = AttributeKey[Long]("user.id")
    *
    * val attribute: Option[Attribute[Long]] = userIdKey.maybe(Some(UserId(1)))
    *   }}}
    *
    * @return
    *   an [[`Attribute`]] associating this key with the given value
    */
  final def maybe[In](value: Option[In])(implicit from: From[In, A]): Option[Attribute[A]] =
    value.map(apply[In])

  /** @return
    *   an [[`AttributeKey`]] of the same type as this key, with name transformed by `f`
    */
  final def transformName(f: String => String): AttributeKey[A] =
    new AttributeKey.Impl[A](f(name), `type`)
}

object AttributeKey {
  private class Impl[A](val name: String, val `type`: AttributeType[A]) extends AttributeKey[A] {

    override final def toString: String =
      Show[AttributeKey[A]].show(this)

    override final def hashCode(): Int =
      Hash[AttributeKey[A]].hash(this)

    override final def equals(obj: Any): Boolean =
      obj match {
        case other: AttributeKey[A @unchecked] =>
          Hash[AttributeKey[A]].eqv(this, other)
        case _ =>
          false
      }
  }

  @annotation.implicitNotFound("""
Could not find the `KeySelect` for ${A}. The `KeySelect` is defined for the following types:
String, Boolean, Long, Double, Seq[String], Seq[Boolean], Seq[Long], Seq[Double].
""")
  sealed trait KeySelect[A] {
    def make(name: String): AttributeKey[A]
  }

  object KeySelect {
    def apply[A](implicit ev: KeySelect[A]): KeySelect[A] = ev

    implicit val stringKey: KeySelect[String] = instance(AttributeKey.string)
    implicit val booleanKey: KeySelect[Boolean] = instance(AttributeKey.boolean)
    implicit val longKey: KeySelect[Long] = instance(AttributeKey.long)
    implicit val doubleKey: KeySelect[Double] = instance(AttributeKey.double)

    implicit val stringSeqKey: KeySelect[Seq[String]] =
      instance(AttributeKey.stringSeq)

    implicit val booleanSeqKey: KeySelect[Seq[Boolean]] =
      instance(AttributeKey.booleanSeq)

    implicit val longSeqKey: KeySelect[Seq[Long]] =
      instance(AttributeKey.longSeq)

    implicit val doubleSeqKey: KeySelect[Seq[Double]] =
      instance(AttributeKey.doubleSeq)

    private def instance[A](f: String => AttributeKey[A]): KeySelect[A] =
      new KeySelect[A] {
        def make(name: String): AttributeKey[A] = f(name)
      }
  }

  def apply[A: KeySelect](name: String): AttributeKey[A] =
    KeySelect[A].make(name)

  def string(name: String): AttributeKey[String] =
    new Impl(name, AttributeType.String)

  def boolean(name: String): AttributeKey[Boolean] =
    new Impl(name, AttributeType.Boolean)

  def long(name: String): AttributeKey[Long] =
    new Impl(name, AttributeType.Long)

  def double(name: String): AttributeKey[Double] =
    new Impl(name, AttributeType.Double)

  def stringSeq(name: String): AttributeKey[Seq[String]] =
    new Impl(name, AttributeType.StringSeq)

  def booleanSeq(name: String): AttributeKey[Seq[Boolean]] =
    new Impl(name, AttributeType.BooleanSeq)

  def longSeq(name: String): AttributeKey[Seq[Long]] =
    new Impl(name, AttributeType.LongSeq)

  def doubleSeq(name: String): AttributeKey[Seq[Double]] =
    new Impl(name, AttributeType.DoubleSeq)

  implicit def attributeKeyHash[A]: Hash[AttributeKey[A]] =
    Hash.by(key => (key.name, key.`type`))

  implicit def attributeKeyShow[A]: Show[AttributeKey[A]] =
    Show.show(key => show"${key.`type`}(${key.name})")

  implicit val attributeKeyExistentialHash: Hash[AttributeKey[_]] =
    Hash.by(key => (key.name, key.`type`))

}
