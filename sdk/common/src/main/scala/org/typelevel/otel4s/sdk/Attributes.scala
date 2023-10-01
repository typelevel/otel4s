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

import cats.Applicative
import cats.Monad
import cats.Monoid
import cats.Show
import cats.implicits._
import cats.kernel.Hash
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
  def foldLeft[F[_]: Monad, B](z: B)(f: (B, Attribute[_]) => F[B]): F[B] =
    m.foldLeft(Monad[F].pure(z)) { (acc, v) =>
      acc.flatMap { b =>
        f(b, v._2)
      }
    }
  def forall[F[_]: Monad](p: Attribute[_] => F[Boolean]): F[Boolean] =
    foldLeft(true)((b, a) => {
      if (b) p(a).map(b && _)
      else Monad[F].pure(false)
    })
  def foreach[F[_]: Applicative](f: Attribute[_] => F[Unit]): F[Unit] =
    m.foldLeft(Applicative[F].unit) { (acc, v) =>
      acc *> f(v._2)
    }

  def toMap: Map[AttributeKey[_], Attribute[_]] = m
  def toList: List[Attribute[_]] = m.values.toList

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

object Attributes {

  val Empty = new Attributes(Map.empty)

  def apply(attributes: Attribute[_]*): Attributes = {
    val builder = Map.newBuilder[AttributeKey[_], Attribute[_]]
    attributes.foreach { a =>
      builder += (a.key -> a)
    }
    new Attributes(builder.result())
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
      def empty: Attributes = Attributes.Empty
      def combine(x: Attributes, y: Attributes): Attributes = {
        if (y.isEmpty) x
        else if (x.isEmpty) y
        else new Attributes(x.m ++ y.m)
      }
    }
}
