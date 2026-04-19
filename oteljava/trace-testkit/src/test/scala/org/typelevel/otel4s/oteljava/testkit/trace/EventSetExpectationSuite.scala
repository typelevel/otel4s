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

import scala.concurrent.duration._
import scala.jdk.CollectionConverters._

class EventSetExpectationSuite extends TraceExpectationSupport {

  testkitTest("exists contains exactly minCount maxCount none and predicate work on real events") { testkit =>
    for {
      span <- buildSpan(
        testkit,
        addEvents = span =>
          span.addEvent("message", 1.second, List(Attribute("kind", "a"))) >>
            span.addEvent("message", 2.seconds, List(Attribute("kind", "b"))) >>
            span.addEvent("exception", 3.seconds, List(Attribute("type", "timeout")))
      )
      events = span.getEvents.asScala.toList
    } yield {
      assertSuccess(EventSetExpectation.exists(EventExpectation.name("exception")).check(events))
      assertSuccess(
        EventSetExpectation
          .contains(
            EventExpectation.name("message").attributesSubset(Attribute("kind", "a")),
            EventExpectation.name("message").attributesSubset(Attribute("kind", "b"))
          )
          .check(events)
      )
      assertSuccess(EventSetExpectation.minCount(2).and(EventSetExpectation.maxCount(3)).check(events))
      assertSuccess(EventSetExpectation.none(EventExpectation.name("missing")).check(events))
      assertSuccess(
        EventSetExpectation.predicate("exception present")(_.exists(_.getName == "exception")).check(events)
      )

      val mismatch = assertFirstMismatch(EventSetExpectation.exactly(EventExpectation.name("message")).check(events))
      assert(mismatch.message.startsWith("unexpected event at index "))
    }
  }
}
