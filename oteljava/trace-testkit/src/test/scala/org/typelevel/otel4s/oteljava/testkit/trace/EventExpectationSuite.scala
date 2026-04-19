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

class EventExpectationSuite extends TraceExpectationSupport {

  testkitTest("event expectation matches name timestamp attributes and predicates") { testkit =>
    for {
      span <- buildSpan(
        testkit,
        addEvents = _.addEvent(
          "db.query",
          5.seconds,
          List(Attribute("db.system", "postgresql"), Attribute("db.operation", "SELECT"))
        )
      )
      event = span.getEvents.get(0)
    } yield {
      val expectation =
        EventExpectation
          .name("db.query")
          .timestamp(5.seconds)
          .attributesSubset(Attribute("db.system", "postgresql"))
          .where("must be a SELECT")(_.getAttributes.get(stringKey("db.operation")) == "SELECT")
          .clue("database event")

      assertSuccess(expectation.check(event))
      assert(expectation.matches(event))
      assertEquals(expectation.clue, Some("database event"))
    }
  }

  testkitTest("any leaves event fields unconstrained") { testkit =>
    for {
      span <- buildSpan(
        testkit,
        addEvents = _.addEvent("message", 1.second, List(Attribute("kind", "ok")))
      )
    } yield {
      val event = span.getEvents.get(0)
      assertSuccess(EventExpectation.any.check(event))
      assert(EventExpectation.any.matches(event))
    }
  }

  private def stringKey(name: String) = io.opentelemetry.api.common.AttributeKey.stringKey(name)
}
