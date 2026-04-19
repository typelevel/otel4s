/*
 * Copyright 2024 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.trace

import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.Attributes

import scala.jdk.CollectionConverters._

class LinkSetExpectationSuite extends TraceExpectationSupport {

  testkitTest("exists contains exactly minCount maxCount none and predicate work on real links") { testkit =>
    val db = spanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331")
    val cache = spanContext("0af7651916cd43dd8448eb211c80319c", "0000000000000001")

    for {
      span <- buildSpan(
        testkit,
        links = List(
          db -> Attributes(Attribute("peer", "db")),
          cache -> Attributes(Attribute("peer", "cache"))
        )
      )
      links = span.getLinks.asScala.toList
    } yield {
      assertSuccess(
        LinkSetExpectation.exists(LinkExpectation.any.attributesSubset(Attribute("peer", "cache"))).check(links)
      )
      assertSuccess(
        LinkSetExpectation
          .contains(
            LinkExpectation.any.spanIdHex(db.spanIdHex),
            LinkExpectation.any.spanIdHex(cache.spanIdHex)
          )
          .check(links)
      )
      assertSuccess(LinkSetExpectation.minCount(1).and(LinkSetExpectation.maxCount(2)).check(links))
      assertSuccess(LinkSetExpectation.none(LinkExpectation.any.spanIdHex("ffffffffffffffff")).check(links))
      assertSuccess(
        LinkSetExpectation
          .predicate("db link present")(_.exists(_.getAttributes.get(stringKey("peer")) == "db"))
          .check(links)
      )

      val mismatch =
        assertFirstMismatch(LinkSetExpectation.exactly(LinkExpectation.any.spanIdHex(db.spanIdHex)).check(links))
      assert(mismatch.message.startsWith("unexpected link at index "))
    }
  }

  private def stringKey(name: String) = io.opentelemetry.api.common.AttributeKey.stringKey(name)
}
