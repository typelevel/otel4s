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
package testkit
package trace

import cats.Hash
import cats.Show
import cats.syntax.hash._
import cats.syntax.show._

import scala.concurrent.duration._

// Tree-like representation of a span
final class SpanNode(
    val name: String,
    val start: FiniteDuration,
    val end: FiniteDuration,
    val attributes: List[Attribute[_]],
    val children: List[SpanNode]
) {

  override def hashCode(): Int =
    Hash[SpanNode].hash(this)

  override def toString: String =
    Show[SpanNode].show(this)

  override def equals(obj: Any): Boolean =
    obj match {
      case other: SpanNode =>
        Hash[SpanNode].eqv(this, other)
      case _ =>
        false
    }

}

object SpanNode extends SpanNodePlatform {

  def render(tree: SpanNode): String = {
    def loop(input: SpanNode, depth: Int): String = {
      val prefix = " " * depth
      val next =
        if (input.children.isEmpty) ""
        else " =>\n" + input.children.map(loop(_, depth + 2)).mkString("\n")

      val attributes =
        input.attributes.map(Show[Attribute[_]].show).mkString(" [", ", ", "]")

      s"$prefix${input.name} ${input.start.toNanos} -> ${input.end.toNanos}$attributes$next"
    }

    loop(tree, 0)
  }

  implicit def spanNodeHash: Hash[SpanNode] = {
    implicit val attributeHash: Hash[Attribute[_]] = {
      val hasher = Attribute.hashAttribute(Hash.fromUniversalHashCode[Any])

      new Hash[Attribute[_]] {
        def hash(x: Attribute[_]): Int =
          hasher.hash(x.asInstanceOf[Attribute[Any]])
        def eqv(x: Attribute[_], y: Attribute[_]): Boolean =
          hasher.eqv(
            x.asInstanceOf[Attribute[Any]],
            y.asInstanceOf[Attribute[Any]]
          )
      }
    }

    Hash.by(p => (p.name, p.start, p.end, p.attributes, p.children.map(_.hash)))
  }

  implicit val spanNodeShow: Show[SpanNode] =
    Show.show(p =>
      show"SpanNode(${p.name}, ${p.start}, ${p.end}, ${p.attributes}, ${p.children})"
    )

}
