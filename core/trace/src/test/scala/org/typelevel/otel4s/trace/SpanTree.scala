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

package org.typelevel.otel4s.trace

import cats.Functor
import cats.Hash
import cats.Show
import cats.syntax.functor._
import cats.syntax.hash._
import cats.syntax.show._

/** A tree representation of a span.
  */
sealed trait SpanTree[A] {

  /** The current span.
    */
  def current: A

  /** Children of the current span.
    */
  def children: List[SpanTree[A]]

}

object SpanTree {

  trait SpanLike[A] {
    def spanIdHex(a: A): String
    def parentSpanIdHex(a: A): Option[String]
  }

  object SpanLike {
    def apply[A](implicit ev: SpanLike[A]): SpanLike[A] = ev

    def make[A](
        getSpanIdHex: A => String,
        getParentSpanIdHex: A => Option[String]
    ): SpanLike[A] =
      new SpanLike[A] {
        def spanIdHex(a: A): String = getSpanIdHex(a)
        def parentSpanIdHex(a: A): Option[String] = getParentSpanIdHex(a)
      }
  }

  def apply[A](span: A): SpanTree[A] =
    Impl(span, Nil)

  def apply[A](span: A, children: Iterable[SpanTree[A]]): SpanTree[A] =
    Impl(span, children.toList)

  /** Transforms the given spans into the tree-like structure.
    */
  def of[A: SpanLike](spans: Iterable[A]): List[SpanTree[A]] = {
    val byParent = spans.toList.groupBy(s => SpanLike[A].parentSpanIdHex(s))
    val topNodes = byParent.getOrElse(None, Nil)
    val bottomToTop = sortNodesByDepth(0, topNodes, byParent, Nil)
    val maxDepth = bottomToTop.headOption.map(_.depth).getOrElse(0)
    buildFromBottom(maxDepth, bottomToTop, byParent, Map.empty)
  }

  implicit def spanTreeHash[S: Hash]: Hash[SpanTree[S]] =
    Hash.by(p => (p.current, p.children.map(_.hash)))

  implicit def spanTreeShow[S: Show]: Show[SpanTree[S]] =
    Show.show(p => show"SpanTree{span=${p.current}, children=${p.children}}")

  implicit val spanTreeFunctor: Functor[SpanTree] =
    new Functor[SpanTree] {
      def map[A, B](fa: SpanTree[A])(f: A => B): SpanTree[B] =
        SpanTree(f(fa.current), fa.children.map(_.fmap(f)))
    }

  private case class EntryWithDepth[A](data: A, depth: Int)

  @annotation.tailrec
  private def sortNodesByDepth[A: SpanLike](
      depth: Int,
      nodesInDepth: List[A],
      nodesByParent: Map[Option[String], List[A]],
      acc: List[EntryWithDepth[A]]
  ): List[EntryWithDepth[A]] = {
    val withDepth = nodesInDepth.map(n => EntryWithDepth(n, depth))
    val calculated = withDepth ++ acc

    val children = nodesInDepth.flatMap { n =>
      nodesByParent.getOrElse(Some(SpanLike[A].spanIdHex(n)), Nil)
    }

    children match {
      case Nil =>
        calculated

      case _ =>
        sortNodesByDepth(depth + 1, children, nodesByParent, calculated)
    }
  }

  @annotation.tailrec
  private def buildFromBottom[A: SpanLike](
      depth: Int,
      remaining: List[EntryWithDepth[A]],
      nodesByParent: Map[Option[String], List[A]],
      processedNodesById: Map[String, SpanTree[A]]
  ): List[SpanTree[A]] = {
    val (nodesOnCurrentDepth, rest) = remaining.span(_.depth == depth)
    val newProcessedNodes = nodesOnCurrentDepth.map { n =>
      val nodeId = SpanLike[A].spanIdHex(n.data)
      val children = nodesByParent
        .getOrElse(Some(nodeId), Nil)
        .flatMap(c => processedNodesById.get(SpanLike[A].spanIdHex(c)))
      val leaf = SpanTree(n.data, children)
      nodeId -> leaf
    }.toMap

    if (depth > 0) {
      buildFromBottom(
        depth - 1,
        rest,
        nodesByParent,
        processedNodesById ++ newProcessedNodes
      )
    } else {
      // top nodes
      newProcessedNodes.values.toList
    }
  }

  private final case class Impl[A](
      current: A,
      children: List[SpanTree[A]]
  ) extends SpanTree[A]

}
