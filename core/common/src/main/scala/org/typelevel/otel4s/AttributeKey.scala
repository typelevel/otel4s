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

  /** The name of the attribute key. */
  def name: String

  /** The type of the attribute value. */
  def `type`: AttributeType[A]

  /** @return
    *   an [[`Attribute`]] associating this key with the given value
    */
  final def apply(value: A): Attribute[A] = Attribute(this, value)

  /** @return
    *   an [[`Attribute`]] associating this key with the given value if the value is defined
    */
  final def maybe(value: Option[A]): Option[Attribute[A]] = value.map(apply)

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
    type Out
    def make(name: String): AttributeKey[Out]
    def get(a: A): Out
  }

  object KeySelect {

    /** An `KeySelect` that has identical attribute value and attribute key types.
      *
      * @tparam A
      *   the type of the attribute value that can be used with this key, one of [[AttributeType]]
      */
    sealed trait Id[A] extends KeySelect[A] {
      type Out = A
      final def get(a: A): A = a
    }

    /** A projected instance of the `KeySelect`.
      *
      * @tparam A
      *   the type of the attribute value that can be used with this key
      *
      * @tparam Key
      *   the type of the attribute key, one of [[AttributeType]]
      */
    sealed trait Projection[A, Key] extends KeySelect[A] {
      type Out = Key
    }

    /** The lense instance allows using custom types as an attribute input.
      *
      * @example
      *   {{{
      * case class UserId(id: Int)
      * implicit val userIdKeySelect: KeySelect.Projection[UserId, Long] = KeySelect.projection(_.id.toLong)
      * val attribute = Attribute("key", UserId(1)) // the derived type is `Attribute[Long]`
      *   }}}
      *
      * @tparam A
      *   the type of the attribute value
      *
      * @tparam Key
      *   the type of the attribute key, one of [[AttributeType]]
      */
    def projection[A, Key](f: A => Key)(implicit select: KeySelect.Id[Key]): KeySelect.Projection[A, Key] =
      new Projection[A, Key] {
        def make(name: String): AttributeKey[Out] = select.make(name)
        def get(a: A): Key = f(a)
      }

    implicit val stringKey: KeySelect.Id[String] = instance(AttributeKey.string)
    implicit val booleanKey: KeySelect.Id[Boolean] = instance(AttributeKey.boolean)
    implicit val longKey: KeySelect.Id[Long] = instance(AttributeKey.long)
    implicit val doubleKey: KeySelect.Id[Double] = instance(AttributeKey.double)
    implicit val stringSeqKey: KeySelect.Id[Seq[String]] = instance(AttributeKey.stringSeq)
    implicit val booleanSeqKey: KeySelect.Id[Seq[Boolean]] = instance(AttributeKey.booleanSeq)
    implicit val longSeqKey: KeySelect.Id[Seq[Long]] = instance(AttributeKey.longSeq)
    implicit val doubleSeqKey: KeySelect.Id[Seq[Double]] = instance(AttributeKey.doubleSeq)

    private def instance[A](f: String => AttributeKey[A]): KeySelect.Id[A] =
      new KeySelect.Id[A] {
        def make(name: String): AttributeKey[A] = f(name)
      }
  }

  def apply[A](name: String)(implicit select: KeySelect[A]): AttributeKey[select.Out] =
    select.make(name)

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
