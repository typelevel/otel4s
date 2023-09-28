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

package org.typelevel.otel4s.testkit
package trace

import io.opentelemetry.sdk.trace.data.SpanData

import scala.concurrent.duration._

trait SpanNodePlatform { self: SpanNode.type =>

  def fromSpans(spans: List[SpanData]): List[SpanNode] = {
    val spansByParent = spans.groupBy { span =>
      Option.when(span.getParentSpanContext.isValid)(span.getParentSpanId)
    }
    val topNodes = spansByParent.getOrElse(None, Nil)
    val bottomToTop = sortNodesByDepth(0, topNodes, spansByParent, Nil)
    val maxDepth = bottomToTop.headOption.map(_.depth).getOrElse(0)
    buildFromBottom(maxDepth, bottomToTop, spansByParent, Map.empty)
  }

  private case class EntryWithDepth(data: SpanData, depth: Int)

  @annotation.tailrec
  private def sortNodesByDepth(
      depth: Int,
      nodesInDepth: List[SpanData],
      nodesByParent: Map[Option[String], List[SpanData]],
      acc: List[EntryWithDepth]
  ): List[EntryWithDepth] = {
    val withDepth = nodesInDepth.map(n => EntryWithDepth(n, depth))
    val calculated = withDepth ++ acc

    val children =
      nodesInDepth.flatMap(n => nodesByParent.getOrElse(Some(n.getSpanId), Nil))

    children match {
      case Nil =>
        calculated

      case _ =>
        sortNodesByDepth(depth + 1, children, nodesByParent, calculated)
    }
  }

  @annotation.tailrec
  private def buildFromBottom(
      depth: Int,
      remaining: List[EntryWithDepth],
      nodesByParent: Map[Option[String], List[SpanData]],
      processedNodesById: Map[String, SpanNode]
  ): List[SpanNode] = {
    val (nodesOnCurrentDepth, rest) = remaining.span(_.depth == depth)
    val newProcessedNodes = nodesOnCurrentDepth.map { n =>
      val nodeId = n.data.getSpanId
      val children = nodesByParent
        .getOrElse(Some(nodeId), Nil)
        .flatMap(c => processedNodesById.get(c.getSpanId))

      val node = new SpanNode(
        name = n.data.getName,
        start = n.data.getStartEpochNanos.nanos,
        end = n.data.getEndEpochNanos.nanos,
        attributes = TestkitConversion.fromJAttributes(n.data.getAttributes),
        children = children
      )
      nodeId -> node
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

}
