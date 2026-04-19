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

class LinkExpectationSuite extends TraceExpectationSupport {

  testkitTest("matches trace id span id sampled and attributes") { testkit =>
    val linked = spanContext(
      traceIdHex = "0af7651916cd43dd8448eb211c80319c",
      spanIdHex = "b7ad6b7169203331",
      sampled = true
    )

    for {
      span <- buildSpan(
        testkit,
        links = List(linked -> Attributes(Attribute("peer", "db")))
      )
      link = span.getLinks.asScala.toList.head
    } yield {
      assertSuccess(
        LinkExpectation.any
          .traceIdHex(linked.traceIdHex)
          .spanIdHex(linked.spanIdHex)
          .sampled
          .attributesSubset(Attribute("peer", "db"))
          .check(link)
      )
      assert(LinkExpectation.any.notSampled.check(link).isLeft)
    }
  }
}
