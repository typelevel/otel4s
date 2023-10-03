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
  * @param key
  *   the key of the attribute. Denotes the types of the `value`
  * @param value
  *   the value of the attribute
  * @tparam A
  *   the type of the attribute's value. One of [[AttributeType]]
  */
final case class Attribute[A](key: AttributeKey[A], value: A)

object Attribute {

  @annotation.implicitNotFound("""
Could not find the `KeySelect` for ${A}. The `KeySelect` is defined for the following types:
String, Boolean, Long, Double, List[String], List[Boolean], List[Long], List[Double].
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

    implicit val stringListKey: KeySelect[List[String]] =
      instance(AttributeKey.stringList)

    implicit val booleanListKey: KeySelect[List[Boolean]] =
      instance(AttributeKey.booleanList)

    implicit val longListKey: KeySelect[List[Long]] =
      instance(AttributeKey.longList)

    implicit val doubleListKey: KeySelect[List[Double]] =
      instance(AttributeKey.doubleList)

    private def instance[A](f: String => AttributeKey[A]): KeySelect[A] =
      new KeySelect[A] {
        def make(name: String): AttributeKey[A] = f(name)
      }
  }

  def apply[A: KeySelect](name: String, value: A): Attribute[A] =
    Attribute(KeySelect[A].make(name), value)

  implicit val showAttribute: Show[Attribute[_]] = (a: Attribute[_]) =>
    s"${show"${a.key}"}=${a.value}"

  implicit def hashAttribute[T: Hash]: Hash[Attribute[T]] =
    Hash.by(a => (a.key, a.value))

  implicit val hashAnyAttribute: Hash[Attribute[_]] = {
    implicit val hashAny: Hash[Any] = Hash.fromUniversalHashCode
    Hash.by(a => (a.key, a.value))
  }
}
