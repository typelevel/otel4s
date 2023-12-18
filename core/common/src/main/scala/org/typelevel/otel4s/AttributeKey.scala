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

/** The type of value that can be set with an implementation of this key is
  * denoted by the type parameter.
  *
  * @tparam A
  *   the type of value that can be set with the key
  */
sealed trait AttributeKey[A] {
  def name: String
  def `type`: AttributeType[A]

  /** @return
    *   an [[`Attribute`]] associating this key with the given value
    */
  final def apply(value: A): Attribute[A] = Attribute(this, value)
}

object AttributeKey {
  private class Impl[A](val name: String, val `type`: AttributeType[A])
      extends AttributeKey[A] {

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

  def string(name: String): AttributeKey[String] =
    new Impl(name, AttributeType.String)

  def boolean(name: String): AttributeKey[Boolean] =
    new Impl(name, AttributeType.Boolean)

  def long(name: String): AttributeKey[Long] =
    new Impl(name, AttributeType.Long)

  def double(name: String): AttributeKey[Double] =
    new Impl(name, AttributeType.Double)

  def stringList(name: String): AttributeKey[List[String]] =
    new Impl(name, AttributeType.StringList)

  def booleanList(name: String): AttributeKey[List[Boolean]] =
    new Impl(name, AttributeType.BooleanList)

  def longList(name: String): AttributeKey[List[Long]] =
    new Impl(name, AttributeType.LongList)

  def doubleList(name: String): AttributeKey[List[Double]] =
    new Impl(name, AttributeType.DoubleList)

  implicit def attributeKeyHash[A]: Hash[AttributeKey[A]] =
    Hash.by(key => (key.name, key.`type`))

  implicit def attributeKeyShow[A]: Show[AttributeKey[A]] =
    Show.show(key => show"${key.`type`}(${key.name})")

  implicit val attributeKeyExistentialHash: Hash[AttributeKey[_]] =
    Hash.by(key => (key.name, key.`type`))

}
