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
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation
import org.typelevel.otel4s.oteljava.testkit.TelemetryResourceExpectation
import org.typelevel.otel4s.trace.SpanKind
import org.typelevel.otel4s.trace.StatusCode

import scala.concurrent.duration._

class SpanExpectationSuite extends TraceExpectationSupport {

  testkitTest("matches common span fields and nested expectations", Attribute("service.name", "svc")) { testkit =>
    val parent = spanContext("0af7651916cd43dd8448eb211c80319c", "0000000000000001", sampled = true)
    val linked = spanContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331")

    for {
      span <- buildSpan(
        testkit,
        tracerName = "service",
        tracerVersion = Some("1.0.0"),
        spanName = "GET /users",
        kind = SpanKind.Server,
        parent = Some(parent),
        links = List(linked -> Attributes(Attribute("peer", "db"))),
        status = Some(StatusCode.Ok -> None),
        addEvents = span =>
          span.addAttributes(List(Attribute("http.method", "GET"))) >>
            span.addEvent("exception", List(Attribute("type", "timeout")))
      )
    } yield {
      assertSuccess(
        SpanExpectation
          .server("GET /users")
          .status(StatusExpectation.ok)
          .parentSpanContext(SpanContextExpectation.any.spanIdHex(parent.spanIdHex))
          .attributesSubset(Attribute("http.method", "GET"))
          .containsEvents(EventExpectation.name("exception"))
          .containsLinks(LinkExpectation.any.spanIdHex(linked.spanIdHex))
          .scopeName("service")
          .scope(InstrumentationScopeExpectation.name("service").version("1.0.0"))
          .resource(TelemetryResourceExpectation.any.attributesSubset(Attribute("service.name", "svc")))
          .startTimestamp(span.getStartEpochNanos.nanos)
          .endTimestamp(span.getEndEpochNanos.nanos)
          .hasEnded
          .check(span)
      )
    }
  }

  testkitTest("reports nested mismatches") { testkit =>
    for {
      span <- buildSpan(testkit, spanName = "GET /users", kind = SpanKind.Server)
    } yield {
      val result =
        SpanExpectation
          .client("POST /users")
          .status(StatusExpectation.error.description("timeout"))
          .check(span)

      val mismatches = result.left.toOption.get
      assert(mismatches.exists(_.isInstanceOf[SpanExpectation.Mismatch.NameMismatch]))
      assert(mismatches.exists(_.isInstanceOf[SpanExpectation.Mismatch.KindMismatch]))
      assert(mismatches.exists(_.isInstanceOf[SpanExpectation.Mismatch.StatusMismatch]))
    }
  }
}
