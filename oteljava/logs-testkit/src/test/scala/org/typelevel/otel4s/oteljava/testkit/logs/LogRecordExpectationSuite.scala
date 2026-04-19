/*
 * Copyright 2025 Typelevel
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

package org.typelevel.otel4s.oteljava.testkit.logs

import org.typelevel.otel4s.AnyValue
import org.typelevel.otel4s.Attribute
import org.typelevel.otel4s.logs.Severity
import org.typelevel.otel4s.oteljava.testkit.InstrumentationScopeExpectation
import org.typelevel.otel4s.oteljava.testkit.TelemetryResourceExpectation

import scala.concurrent.duration._

class LogRecordExpectationSuite extends LogExpectationSupport {

  testkitTest("matches body severity trace correlation and nested expectations", Attribute("service.name", "svc")) {
    testkit =>
      for {
        record <- buildLog(
          testkit,
          loggerName = "service",
          loggerVersion = Some("1.0.0"),
          loggerSchemaUrl = Some("https://schema.example/v1"),
          body = Some(AnyValue.string("request failed")),
          severity = Some(Severity.error),
          severityText = Some("ERROR"),
          attributes = List(Attribute("http.route", "/users")),
          context = Some(traceContext("0af7651916cd43dd8448eb211c80319c", "b7ad6b7169203331")),
          timestamp = Some(1.second),
          observedTimestamp = Some(1500.millis)
        )
      } yield {
        assertSuccess(
          LogRecordExpectation
            .message("request failed")
            .severity(Severity.error)
            .severityText("ERROR")
            .traceId("0af7651916cd43dd8448eb211c80319c")
            .spanId("b7ad6b7169203331")
            .attributesSubset(Attribute("http.route", "/users"))
            .scope(
              InstrumentationScopeExpectation
                .name("service")
                .version("1.0.0")
                .schemaUrl("https://schema.example/v1")
            )
            .resource(TelemetryResourceExpectation.any.attributesSubset(Attribute("service.name", "svc")))
            .timestampWhere(_ == 1.second.toNanos)
            .observedTimestampWhere(_ == 1500.millis.toNanos)
            .check(record)
        )
      }
  }

  testkitTest("matches raw body values") { testkit =>
    for {
      record <- buildLog(
        testkit,
        body = Some(AnyValue.map(Map("status" -> AnyValue.string("failed"), "count" -> AnyValue.long(2L))))
      )
    } yield {
      assertSuccess(
        LogRecordExpectation.any
          .body(AnyValue.map(Map("status" -> AnyValue.string("failed"), "count" -> AnyValue.long(2L))))
          .check(record)
      )
    }
  }

  testkitTest("reports nested and timestamp mismatches") { testkit =>
    for {
      record <- buildLog(
        testkit,
        loggerName = "service",
        body = Some(AnyValue.string("ok")),
        attributes = List(Attribute("http.route", "/users")),
        timestamp = Some(1.second),
        observedTimestamp = Some(2.seconds)
      )
    } yield {
      val result = LogRecordExpectation
        .message("failed")
        .severity(Severity.error)
        .attributesSubset(Attribute("error.type", "timeout"))
        .scope(InstrumentationScopeExpectation.name("other"))
        .resource(TelemetryResourceExpectation.any.attributesSubset(Attribute("service.name", "svc")))
        .timestampWhere("exact timestamp")(_ == 3.seconds.toNanos)
        .observedTimestampWhere("exact observed timestamp")(_ == 4.seconds.toNanos)
        .check(record)

      val mismatches = result.left.toOption.get
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.BodyMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.SeverityMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.AttributesMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.ScopeMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.ResourceMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.TimestampMismatch]))
      assert(mismatches.exists(_.isInstanceOf[LogRecordExpectation.Mismatch.ObservedTimestampMismatch]))
    }
  }

  testkitTest("supports untraced expectation") { testkit =>
    for {
      record <- buildLog(
        testkit,
        body = Some(AnyValue.string("hello"))
      )
    } yield {
      assertSuccess(
        LogRecordExpectation.any.message("hello").untraced.check(record)
      )
    }
  }
}
